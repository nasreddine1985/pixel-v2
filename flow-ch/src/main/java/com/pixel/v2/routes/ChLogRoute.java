package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * CH Log Route - Consumes messages from Kafka flow-summary topic and persists them to database
 */
@Component
public class ChLogRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Main route: Consume from flow-summary topic and persist to database
        from("kamelet:k-kafka-starter?bootstrapServers={{ch.log.kafka.brokers}}&topic={{kmq.starter.kafkaFlowSummaryTopicName}}&groupId={{ch.log.kafka.groupId}}&offsetReset={{ch.log.kafka.offsetReset}}&sinkEndpoint=direct:process-flow-summary-log")
                .routeId("ch-log-processing-flow")
                .log("K-Kafka Starter kamelet initiated - flow summary message will be processed and persisted");

        // Processing route: Handle flow summary message and persist to database
        from("direct:process-flow-summary-log").routeId("ch-log-main-processing")
                // .log("Received flow summary message from k-kafka-starter: ${body}")
                .setHeader("MessageType", constant("FLOW_SUMMARY"))
                .setHeader("ProcessingTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                .to("direct:persist-flow-summary-message");

        // Persistence route: Log and persist flow summary data to database
        from("direct:persist-flow-summary-message").routeId("ch-log-message-persistence").log(
                "Persisting flow summary message with Kafka metadata - Topic: ${header.kafkaTopic}, Offset: ${header.kafkaOffset}")
                .setHeader("RouteName", constant("CH-Log-Processing"))
                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                // Log the message details
                // .log("📊 CH-LOG: Flow Summary Data Received - Message Body Length:
                // ${body.length()}")
                // .log("📊 CH-LOG: Kafka Metadata - Topic: ${header.kafkaTopic}, Partition:
                // ${header.kafkaPartition}, Offset: ${header.kafkaOffset}")
                // .log("📊 CH-LOG: Message Content: ${body}")
                // Use the new k-db-flow-summary kamelet for JPA persistence
                .to("kamelet:k-db-flow-summary")
        // .log("✅ CH-LOG: Flow summary message persisted successfully to database via
        // k-db-flow-summary kamelet")
        ;


    }
}
