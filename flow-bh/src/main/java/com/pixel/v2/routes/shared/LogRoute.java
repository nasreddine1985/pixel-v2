package com.pixel.v2.routes.shared;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Log Route for BH WPS flows
 * Consumes messages from Kafka flow-summary topic and persists them to database
 */
@Component
public class LogRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Main route: Consume from flow-summary topic and persist to database
        from("kamelet:k-kafka-log-starter?bootstrapServers={{pixel.kafka.brokers}}&topic={{pixel.kafka.flow-summary.topic-name}}&groupId={{pixel.kafka.groupId}}")
                .routeId("bh-log-processing-flow")
                .to("direct:process-flow-summary-log");

        // Log Events route: Consume from log-events topic and persist to database
        from("kamelet:k-kafka-log-starter?bootstrapServers={{pixel.kafka.brokers}}&topic={{pixel.kafka.log.topic-name}}&groupId={{pixel.kafka.groupId}}")
                .routeId("bh-log-events-processing-flow")
                .to("direct:process-log-events");

        // Log Error Events route: Consume from error-log-events topic and persist to database
        from("kamelet:k-kafka-log-starter?bootstrapServers={{pixel.kafka.brokers}}&topic={{pixel.kafka.error.log.topic-name}}&groupId={{pixel.kafka.groupId}}")
                .routeId("bh-log-events-error-flow")
                .to("direct:error-log-events");

        // Processing route: Handle flow summary message and persist to database
        from("direct:process-flow-summary-log")
                .routeId("bh-log-main-processing")
                .setHeader("MessageType", constant("FLOW_SUMMARY"))
                .setHeader("ProcessingTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                .to("direct:persist-flow-summary-message");

        // Persistence route: Log and persist flow summary data to database
        from("direct:persist-flow-summary-message")
                .routeId("bh-log-message-persistence")
                .setHeader("RouteName", constant("BH-Log-Processing"))
                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                .to("kamelet:k-db-flow-summary");

        from("direct:process-log-events")
                .routeId("bh-log-events-processing")
                .setHeader("MessageType", constant("LOG_EVENTS"))
                .setHeader("ProcessingTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                .to("direct:persist-log-events-message");

        from("direct:error-log-events")
                .routeId("bh-log-events-error-processing")
                .setHeader("MessageType", constant("LOG_EVENTS"))
                .setHeader("ProcessingTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                .to("direct:persist-error-log-events-message");

        // Persistence route: Log and persist log events data to database
        from("direct:persist-log-events-message")
                .routeId("bh-log-events-persistence")
                .setHeader("RouteName", constant("BH-Log-Events-Processing"))
                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                .to("kamelet:k-db-log-events");

        from("direct:persist-error-log-events-message")
                .routeId("bh-log-events-error-persistence")
                .setHeader("RouteName", constant("BH-Log-Events-Error-Processing"))
                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                .to("kamelet:k-db-error-log-events");
    }
}
