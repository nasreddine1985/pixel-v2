package com.pixel.v2.flow.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * PACS.008 Message Processing Routes using Apache Camel Java DSL Flow: k-mq-message-receiver
 * kamelet -> Apache Camel aggregate -> batch persistence processor
 */
@Component
public class Pacs008RouteBuilder extends RouteBuilder {

        @Override
        public void configure() throws Exception {

                // Global Exception Handler - ensures JMS transaction rollback on any failure
                onException(Exception.class).log(
                                "[PACS008-EXCEPTION] Global exception handler triggered: ${exception.message}")
                                .log("[PACS008-JMS] ⚠️  Global exception - JMS transaction will ROLLBACK (no acknowledgment)")
                                .to("direct:pacs008-error-handler").handled(false) // Ensure
                                                                                   // transaction
                                                                                   // rollback
                                .end();

                // Route 1: Message Consumption and Batch Aggregation using Apache Camel Aggregator
                from("kamelet:k-mq-message-receiver" + "?destination={{flow.pacs008.queue.name}}"
                                + "&brokerUrl={{mq.broker-url}}" + "&user={{mq.user}}"
                                + "&password={{mq.password}}"
                                + "&acknowledgmentModeName=CLIENT_ACKNOWLEDGE" + "&transacted=true"
                                + "&concurrentConsumers={{flow.pacs008.concurrent-consumers}}"
                                + "&maxConcurrentConsumers={{flow.pacs008.max-concurrent-consumers}}")
                                                .routeId("pacs008-message-consumer")
                                                .log("[PACS008-CONSUMER] Message received from queue: messageId=${header.JMSMessageID}, size=${body.length()}")
                                                .log("[PACS008-JMS] CLIENT_ACKNOWLEDGE mode - message will be acknowledged on transaction commit")

                                                // Set processing metadata
                                                .setHeader("ProcessingRoute", constant("PACS008"))
                                                .setHeader("MessageType",
                                                                constant("pacs.008.001.08"))
                                                .setHeader("ProcessingTimestamp", simple(
                                                                "${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                                .setHeader("MessageSource",
                                                                constant("ARTEMIS_QUEUE"))

                                                // Apache Camel native aggregation for batching
                                                // messages
                                                .aggregate(constant("batch"))
                                                .aggregationStrategy(
                                                                "messageBatchAggregationStrategy")
                                                .completionSize("{{flow.pacs008.batch.completion-size}}")
                                                .completionTimeout(
                                                                "{{flow.pacs008.batch.completion-timeout}}")
                                                .log("[PACS008-BATCH] Batch aggregated: ${header.CamelAggregatedSize} messages")

                                                // Route aggregated batch to persistence with
                                                // transaction
                                                .to("direct:pacs008-persistence");

                // Route 2: Batch Persistence using batch processor with transaction support
                from("direct:pacs008-persistence").routeId("pacs008-persistence").log(
                                "[PACS008-PERSIST] Starting batch persistence for ${header.CamelAggregatedSize} messages")

                                // Prepare headers for batch persistence
                                .setHeader("batchSize", simple("${header.CamelAggregatedSize}"))
                                .setHeader("messageType", constant("pacs.008.001.08"))
                                .setHeader("processingRoute", constant("PACS008"))
                                .setHeader("messageSource", constant("ARTEMIS_QUEUE"))

                                // Route the entire collection to the batch persistence processor
                                .to("bean:pacs008BatchPersistenceProcessor?method=process")
                                .log("[PACS008-PERSIST] Batch persistence completed: status=${header.persistenceStatus}, count=${header.persistedCount}")

                                // Only acknowledge JMS messages if persistence was successful
                                .choice().when(header("persistenceStatus").isEqualTo("SUCCESS"))
                                .log("[PACS008-PERSIST] Database persistence successful - JMS messages will be acknowledged")
                                .log("[PACS008-JMS] Transaction will commit - ${header.persistedCount} messages will be ACKNOWLEDGED to MQ")

                                // Call referentiel service to load flow configuration
                                .to("direct:pacs008-referentiel-loader")

                                .log("[PACS008-JMS] ✅ Route completion - JMS acknowledgment will occur automatically on transaction commit")
                                .otherwise()
                                .log("[PACS008-PERSIST] Database persistence failed - throwing exception to prevent JMS acknowledgment")
                                .log("[PACS008-JMS] Transaction will ROLLBACK - messages will remain in queue for reprocessing")
                                .throwException(RuntimeException.class,
                                                "Database persistence failed")
                                .end();

                // Route 3: Referentiel Configuration Loader
                from("direct:pacs008-referentiel-loader").routeId("pacs008-referentiel-loader").log(
                                "[PACS008-REFERENTIEL] Loading configuration from referentiel service for flowId: {{flow.pacs008.referentiel.flowId}}")

                                // Store original body (collection of messages) in a header to
                                // preserve it
                                .setHeader("OriginalMessageCollection", simple("${body}"))

                                // Call k-referentiel-data-loader kamelet to get flow configuration
                                .to("kamelet:k-referentiel-data-loader"
                                                + "?serviceUrl={{referentiel.service.url}}"
                                                + "&configEndpoint=/api/config"
                                                + "&flowId={{flow.pacs008.referentiel.flowId}}")

                                .log("[PACS008-REFERENTIEL] Configuration loaded successfully")
                                .log("[PACS008-REFERENTIEL] CmdMapping: ${header.CmdMapping}, Rail: ${header.Rail}, Mode: ${header.Mode}")
                                .log("[PACS008-REFERENTIEL] XsltFileToCdm: ${header.XsltFileToCdm}, KafkaTopic: ${header.KafkaTopic}")
                                .log("[PACS008-REFERENTIEL] NeedSplit: ${header.NeedSplit}, ChunkSize: ${header.ChunkSize}")

                                // Restore original message collection body
                                .setBody(simple("${header.OriginalMessageCollection}"))
                                .removeHeader("OriginalMessageCollection")

                                .log("[PACS008-REFERENTIEL] Configuration headers added, original message collection preserved (${body.size()} messages)");

                // Route 4: Error Handler
                from("direct:pacs008-error-handler").routeId("pacs008-error-handler").log(
                                "[PACS008-ERROR] Processing error for message: ${header.JMSMessageID}, error: ${exception.message}")
                                .log("[PACS008-JMS] ❌ Error occurred - JMS transaction will ROLLBACK (message remains in queue)")

                                // Set error metadata
                                .setHeader("ErrorTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .setHeader("ErrorRoute", constant("PACS008"))
                                .setHeader("ErrorMessage", simple("${exception.message}"))
                                .setHeader("entityType", constant("ERROR"))

                                // Route to k-db-tx for error persistence
                                .to("kamelet:k-db-tx" + "?entityType=ERROR"
                                                + "&persistenceOperation=CREATE"
                                                + "&enableAuditTrail=false")

                                .log("[PACS008-ERROR] Error logged and persisted: ${header.ErrorTimestamp}")
                                .log("[PACS008-JMS] Error handling complete - original transaction will still rollback");
        }
}
