package com.pixel.v2.ingestion.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Payment Ingestion Route Builder (Java DSL)
 * 
 * Defines the main ingestion flow orchestrating multiple kamelets: 1. Receipt from various channels
 * (MQ, API, File) 2. Database persistence (initial message save) 3. Reference data enrichment 4.
 * Enriched data persistence (save enriched data) 5. Validation 6. Idempotence checking 7.
 * Publishing to Kafka
 * 
 * Note: This Java-based RouteBuilder is used when YAML routes are disabled. Set
 * camel.routes.yaml.enabled=false to use this implementation. Set camel.routes.yaml.enabled=true to
 * use YAML-based routes instead.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(name = "camel.routes.yaml.enabled", havingValue = "false",
        matchIfMissing = true)
public class PaymentIngestionRouteBuilder extends RouteBuilder {

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @Override
    public void configure() throws Exception {

        // Global error handler
        onException(Exception.class).handled(true)
                .log("Error in payment ingestion: ${exception.message}")
                .setHeader("ErrorOccurred", constant(true))
                .setHeader("ErrorMessage", simple("${exception.message}"))
                .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
                .to("direct:error-handler");

        // Main ingestion orchestrator route
        from("direct:payment-ingestion").routeId("payment-ingestion-orchestrator")
                .log("Starting payment ingestion orchestration for message: ${body}")
                .setHeader("IngestionStartTime", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
                .setHeader("ProcessingStage", constant("INGESTION_START"))

                // Step 1: Save message to database first
                .to("direct:database-persistence")

                // Step 2: Enrich with reference data
                .to("direct:reference-enrichment")

                // Step 3: Save enriched data to database
                .to("direct:enriched-data-persistence")

                // Step 4: Apply validation
                .to("direct:validation")

                // Step 5: Apply idempotence checking
                .to("direct:idempotence-check")

                // Step 6: Route based on source channel (only if validation and idempotence passed)
                .choice().when(header("CanProcess").isEqualTo(true))
                .log("Routing message based on receipt channel: ${header.ReceiptChannel}").choice()
                .when(header("ReceiptChannel").isEqualTo("CFT"))
                .log("CFT message - routing to Kafka").to("direct:kafka-publisher").otherwise()
                .log("HTTP/MQ message - routing to processing module")
                .to("direct:processing-publisher").endChoice().otherwise()
                .to("direct:rejection-handler").end();

        // MQ Series receipt route using k-mq-message-receiver kamelet
        if (activeProfiles == null || !activeProfiles.contains("test")) {
            from("kamelet:k-mq-message-receiver?queueName={{ingestion.mq.input.queue:PAYMENT_INPUT}}&host={{ingestion.mq.host:localhost}}&port={{ingestion.mq.port:1414}}&queueManager={{ingestion.mq.queue.manager:QM1}}&channel={{ingestion.mq.channel:DEV.ADMIN.SVRCONN}}&username={{ingestion.mq.username:admin}}&password={{ingestion.mq.password:admin}}")
                    .routeId("mq-receipt-route")
                    .log("Message received from k-mq-message-receiver kamelet")
                    .setHeader("ReceiptChannel", constant("MQ")).to("direct:payment-ingestion");
        }

        // API receipt route using k-http-message-receiver kamelet
        from("kamelet:k-http-message-receiver?port={{server.port:8080}}&contextPath={{server.servlet.context-path:/ingestion}}/api/v1/payments")
                .routeId("api-receipt-route")
                .log("Message received from k-http-message-receiver kamelet")
                .setHeader("ReceiptChannel", constant("HTTP")).to("direct:payment-ingestion");

        // File (CFT) receipt route using k-cft-data-receiver kamelet
        from("kamelet:k-cft-data-receiver?directoryPath={{ingestion.file.input.directory:/tmp/payments-in}}&filePattern={{ingestion.file.pattern:.*\\.xml}}&processedDirectory={{ingestion.file.processed.directory:/tmp/payments-processed}}&errorDirectory={{ingestion.file.error.directory:/tmp/payments-error}}&delay={{ingestion.file.delay:5000}}")
                .routeId("cft-receipt").log("Message received from k-cft-data-receiver kamelet")
                .setHeader("ReceiptChannel", constant("CFT")).to("direct:payment-ingestion");

        // Database persistence step (first step after message receipt)
        from("direct:database-persistence").routeId("database-persistence")
                .log("Persisting message to database via k-db-tx")
                .setHeader("ProcessingStage", constant("DATABASE_PERSISTENCE"))
                .to("kamelet:k-db-tx").choice()
                .when(header("persistenceStatus").isEqualTo("SUCCESS"))
                .log("Message successfully persisted to database with ID: ${header.persistedMessageId}")
                .setHeader("MessagePersisted", constant(true)).otherwise()
                .log("Database persistence failed: ${header.persistenceError}")
                .setHeader("MessagePersisted", constant(false))
                .setHeader("RejectionReason", constant("DATABASE_PERSISTENCE_FAILED"))
                .to("direct:error-handler").stop().end();

        // Reference data enrichment step
        from("direct:reference-enrichment").routeId("reference-enrichment")
                .log("Enriching message with reference data")
                .setHeader("ProcessingStage", constant("REFERENCE_ENRICHMENT"))
                .to("kamelet:k-referentiel-data-loader").log("Reference data enrichment completed");

        // Enriched data persistence step (after reference enrichment)
        from("direct:enriched-data-persistence").routeId("enriched-data-persistence")
                .log("Persisting enriched message data to database")
                .setHeader("ProcessingStage", constant("ENRICHED_DATA_PERSISTENCE"))
                .setHeader("PersistenceType", constant("ENRICHED")).to("kamelet:k-db-tx").choice()
                .when(header("persistenceStatus").isEqualTo("SUCCESS"))
                .log("Enriched message data successfully persisted to database with ID: ${header.persistedMessageId}")
                .setHeader("EnrichedDataPersisted", constant(true)).otherwise()
                .log("Enriched data persistence failed: ${header.persistenceError}")
                .setHeader("EnrichedDataPersisted", constant(false))
                .setHeader("RejectionReason", constant("ENRICHED_DATA_PERSISTENCE_FAILED"))
                .to("direct:error-handler").stop().end();

        // Validation step
        from("direct:validation").routeId("validation-step").log("Starting message validation")
                .setHeader("ProcessingStage", constant("VALIDATION"))
                .to("kamelet:k-ingestion-technical-validation").choice()
                .when(header("IsValid").isEqualTo(true)).log("Message validation passed")
                .otherwise().log("Message validation failed: ${header.ValidationResult}")
                .setHeader("RejectionReason", constant("VALIDATION_FAILED")).end();

        // Idempotence check step
        from("direct:idempotence-check").routeId("idempotence-check")
                .log("Starting idempotence check")
                .setHeader("ProcessingStage", constant("IDEMPOTENCE"))

                // Use k-payment-idempotence-helper kamelet
                .to("kamelet:k-payment-idempotence-helper?messageId=${header.MessageId}&checkMode=PROCESS")

                .choice().when(header("IsDuplicate").isEqualTo(true))
                .log("Duplicate message detected, checking action").choice()
                .when(header("ShouldReject").isEqualTo(true))
                .setHeader("RejectionReason", constant("DUPLICATE_MESSAGE"))
                .setHeader("CanProcess", constant(false))
                .when(header("ShouldIgnore").isEqualTo(true))
                .log("Ignoring duplicate message as per configuration")
                .setHeader("CanProcess", constant(false)).otherwise()
                .log("Processing duplicate with warning").endChoice().otherwise()
                .log("Message idempotence check passed").endChoice();

        // Kafka publisher step
        from("direct:kafka-publisher").routeId("kafka-publisher").log("Publishing message to Kafka")
                .setHeader("ProcessingStage", constant("KAFKA_PUBLISH"))
                .setHeader("PublishTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))

                // Determine Kafka topic based on message type or channel
                .choice().when(header("ExpectedMessageType").isEqualTo("pacs.008"))
                .setHeader("KafkaTopic",
                        simple("{{ingestion.kafka.topic.pacs008:payments-pacs008}}"))
                .when(header("ExpectedMessageType").isEqualTo("pacs.009"))
                .setHeader("KafkaTopic",
                        simple("{{ingestion.kafka.topic.pacs009:payments-pacs009}}"))
                .when(header("ExpectedMessageType").isEqualTo("pain.001"))
                .setHeader("KafkaTopic",
                        simple("{{ingestion.kafka.topic.pain001:payments-pain001}}"))
                .when(header("ExpectedMessageType").isEqualTo("camt.053"))
                .setHeader("KafkaTopic",
                        simple("{{ingestion.kafka.topic.camt053:payments-camt053}}"))
                .otherwise()
                .setHeader("KafkaTopic",
                        simple("{{ingestion.kafka.topic.default:payments-processed}}"))
                .end()

                // Set Kafka message key (use primary identifier if available)
                .setHeader("kafka.KEY", simple("${header.PrimaryIdentifier:${exchangeId}}"))

                // Add processing metadata to the message
                .process(exchange -> {
                    String originalBody = exchange.getIn().getBody(String.class);
                    String enrichedMessage = String.format("""
                            {
                                "metadata": {
                                    "receiptChannel": "%s",
                                    "receiptTimestamp": "%s",
                                    "ingestionStartTime": "%s",
                                    "publishTimestamp": "%s",
                                    "primaryIdentifier": "%s",
                                    "messageType": "%s",
                                    "validationPassed": %s,
                                    "duplicateCheck": %s
                                },
                                "payload": %s
                            }
                            """, exchange.getIn().getHeader("ReceiptChannel", "UNKNOWN"),
                            exchange.getIn().getHeader("ReceiptTimestamp", ""),
                            exchange.getIn().getHeader("IngestionStartTime", ""),
                            exchange.getIn().getHeader("PublishTimestamp", ""),
                            exchange.getIn().getHeader("PrimaryIdentifier", ""),
                            exchange.getIn().getHeader("ExpectedMessageType", "UNKNOWN"),
                            exchange.getIn().getHeader("IsValid", false),
                            exchange.getIn().getHeader("IdempotenceChecked", false),
                            originalBody != null ? originalBody : "null");


                    exchange.getIn().setBody(enrichedMessage);
                })

                .toD("kafka:${header.KafkaTopic}?brokers={{ingestion.kafka.brokers:localhost:9092}}")
                .log("Message successfully published to Kafka topic: ${header.KafkaTopic}");

        // Processing publisher step - for HTTP/MQ messages - Direct call to business module
        from("direct:processing-publisher").routeId("business-direct-publisher")
                .log("Publishing message directly to business module")
                .setHeader("ProcessingStage", constant("BUSINESS_MODULE_DIRECT"))
                .setHeader("PublishTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
                .setHeader("Content-Type", constant("application/json"))

                // Route directly to business module based on message type
                .choice().when(header("ExpectedMessageType").isEqualTo("pacs.008"))
                .log("Routing PACS.008 message directly to business module")
                .to("http://localhost:8081/business/api/direct/pacs-008-transform?bridgeEndpoint=true&httpMethod=POST")
                .when(header("ExpectedMessageType").isEqualTo("pacs.009"))
                .log("Routing PACS.009 message directly to business module")
                .to("http://localhost:8081/business/api/direct/pacs-009-transform?bridgeEndpoint=true&httpMethod=POST")
                .when(header("ExpectedMessageType").isEqualTo("pain.001"))
                .log("Routing PAIN.001 message directly to business module")
                .to("http://localhost:8081/business/api/direct/pain-001-transform?bridgeEndpoint=true&httpMethod=POST")
                .when(header("ExpectedMessageType").isEqualTo("camt.053"))
                .log("Routing CAMT.053 message directly to business module")
                .to("http://localhost:8081/business/api/direct/camt-053-transform?bridgeEndpoint=true&httpMethod=POST")
                .otherwise()
                .log("Unknown message type: ${header.ExpectedMessageType}, routing to business module general processor")
                .to("http://localhost:8081/business/api/direct/kafka-message-processing?bridgeEndpoint=true&httpMethod=POST")
                .end().log("Message successfully sent directly to business module");

        // Rejection handler
        from("direct:rejection-handler").routeId("rejection-handler")
                .log("Processing rejected message: ${header.RejectionReason}")
                .setHeader("ProcessingStage", constant("REJECTION"))
                .setHeader("RejectionTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))

                // Create rejection message
                .process(exchange -> {
                    String rejectionMessage = String.format("""
                            {
                                "rejectionInfo": {
                                    "reason": "%s",
                                    "timestamp": "%s",
                                    "receiptChannel": "%s",
                                    "originalMessageId": "%s",
                                    "errorDetails": "%s"
                                },
                                "originalMessage": %s
                            }
                            """, exchange.getIn().getHeader("RejectionReason", "UNKNOWN"),
                            exchange.getIn().getHeader("RejectionTimestamp", ""),
                            exchange.getIn().getHeader("ReceiptChannel", "UNKNOWN"),
                            exchange.getIn().getHeader("PrimaryIdentifier", ""),
                            exchange.getIn().getHeader("ValidationResult", ""),
                            exchange.getIn().getBody(String.class));
                    exchange.getIn().setBody(rejectionMessage);
                })

                .to("kafka:{{ingestion.kafka.topic.rejected:payments-rejected}}?brokers={{ingestion.kafka.brokers:localhost:9092}}")
                .log("Rejected message sent to dead letter topic");

        // Error handler
        from("direct:error-handler").routeId("error-handler").log("Processing system error")
                .setHeader("ProcessingStage", constant("ERROR"))

                // Create error message
                .process(exchange -> {
                    String errorMessage = String.format("""
                            {
                                "errorInfo": {
                                    "message": "%s",
                                    "timestamp": "%s",
                                    "receiptChannel": "%s",
                                    "processingStage": "%s"
                                },
                                "originalMessage": %s
                            }
                            """, exchange.getIn().getHeader("ErrorMessage", "Unknown error"),
                            exchange.getIn().getHeader("ErrorTimestamp", ""),
                            exchange.getIn().getHeader("ReceiptChannel", "UNKNOWN"),
                            exchange.getIn().getHeader("ProcessingStage", "UNKNOWN"),
                            exchange.getIn().getBody(String.class));
                    exchange.getIn().setBody(errorMessage);
                })

                .to("kafka:{{ingestion.kafka.topic.errors:payments-errors}}?brokers={{ingestion.kafka.brokers:localhost:9092}}")
                .log("Error message sent to error topic");

        // Health check endpoint
        from("platform-http:/health").routeId("health-check")
                .setBody(constant("{\"status\":\"UP\",\"service\":\"payment-ingestion\"}"))
                .setHeader("Content-Type", constant("application/json"));

        // Metrics endpoint
        from("platform-http:/metrics").routeId("metrics-endpoint").setBody(constant(
                "{\"ingestionRoutes\":{\"active\":true,\"channels\":[\"MQ_SERIES\",\"REST_API\",\"FILE_CFT\"]}}"))
                .setHeader("Content-Type", constant("application/json"));
    }
}
