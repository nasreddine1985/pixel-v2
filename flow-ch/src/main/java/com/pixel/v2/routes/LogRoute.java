package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Log Route - Consumes messages from Kafka flow-summary topic and persists them to database
 */
@Component
public class LogRoute extends RouteBuilder {

        @Override
        public void configure() throws Exception {

                // Main route: Consume from flow-summary topic and persist to database
                from("kamelet:k-kafka-starter?bootstrapServers={{ch.log.kafka.brokers}}&topic={{kmq.starter.kafkaFlowSummaryTopicName}}&groupId={{ch.log.kafka.groupId}}&offsetReset={{ch.log.kafka.offsetReset}}&sinkEndpoint=direct:process-flow-summary-log")
                                .routeId("log-processing-flow")
                                .log("K-Kafka Starter kamelet initiated - flow summary message will be processed and persisted");

                // Log Events route: Consume from log-events topic and persist to database
                from("kamelet:k-kafka-starter?bootstrapServers={{ch.log.kafka.brokers}}&topic={{kmq.starter.kafkaLogTopicName}}&groupId={{ch.log.kafka.groupId}}&offsetReset={{ch.log.kafka.offsetReset}}&sinkEndpoint=direct:process-log-events")
                                .routeId("log-events-processing-flow")
                                .log("K-Kafka Starter kamelet initiated - log events message will be processed and persisted");

                // Processing route: Handle flow summary message and persist to database
                from("direct:process-flow-summary-log").routeId("log-main-processing")
                                .setHeader("MessageType", constant("FLOW_SUMMARY"))
                                .setHeader("ProcessingTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .to("direct:persist-flow-summary-message");

                // Persistence route: Log and persist flow summary data to database
                from("direct:persist-flow-summary-message").routeId("log-message-persistence")
                                .setHeader("RouteName", constant("CH-Log-Processing"))
                                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                                .to("kamelet:k-db-flow-summary")
                ;

                from("direct:process-log-events").routeId("log-events-processing")
                                .setHeader("MessageType", constant("LOG_EVENTS"))
                                .setHeader("ProcessingTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .to("direct:persist-log-events-message");

                // Persistence route: Log and persist log events data to database
                from("direct:persist-log-events-message").routeId("ch-log-events-persistence").log(
                                "Persisting log events message with Kafka metadata - Topic: ${header.kafkaTopic}, Offset: ${header.kafkaOffset}")
                                .setHeader("RouteName", constant("CH-Log-Events-Processing"))
                                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                                .to("kamelet:k-db-log-events")
                ;

        }
}
