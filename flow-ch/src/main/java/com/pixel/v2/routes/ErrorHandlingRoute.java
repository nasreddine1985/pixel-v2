package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Error Handling Route
 * 
 * Handles errors and exceptions from payment processing routes
 */
@Component
public class ErrorHandlingRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Error handling route
        from("direct:error-handling").routeId("error-handler")
                .log("Error processing message: ${exception.message}")
                .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                .setHeader("ErrorReason", simple("${exception.message}"))
                .setHeader("ShortErrorReason",
                        simple("${exception.class.simpleName}: ${exception.message}"))
                .process(exchange -> {
                    // Truncate ShortErrorReason to fit varchar(128) constraint
                    String shortReason =
                            exchange.getIn().getHeader("ShortErrorReason", String.class);
                    if (shortReason != null && shortReason.length() > 120) {
                        exchange.getIn().setHeader("ShortErrorReason",
                                shortReason.substring(0, 120) + "...");
                    }
                })
                // Log error event to Kafka with explicit headers
                .wireTap(
                        "kamelet:k-log-events?level=ERROR&logMessageTxt=[ERROR] ${header.MessageType}: ${header.ShortErrorReason}&kafkaTopicName=${header.kafkaLogTopicName}&brokers=${header.brokers}")
                .log("Error headers: ${headers}").setHeader("step", constant("ERROR")).wireTap(
                        "kamelet:k-log-flow-summary?kafkaTopicName=${header.kafkaFlowSummaryTopicName}&brokers=${header.brokers}");
    }
}
