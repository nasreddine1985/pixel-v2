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
                from("direct:error-handling").routeId("error-handler").log(
                                "Error processing message. Headers: ErrorType=${header.ErrorType}, ErrorReason=${header.ErrorReason}")
                                .process(exchange -> {
                                        String errorReason = exchange.getIn()
                                                        .getHeader("ErrorReason", String.class);

                                        if (errorReason != null && errorReason.length() > 128) {
                                                errorReason = errorReason.substring(0, 125) + "...";
                                        }
                                        exchange.getIn().setHeader("ShortErrorReason", errorReason);

                                        
                                })
                                // Log error event to Kafka with sanitized headers
                                .toD("kamelet:k-log-events?flowId=${header.FlowOccurId}&flowCode=${header.FlowCode}&kafkaTopicName=${header.KafkaLogTopicName}&brokers=${header.Brokers}&logMessageTxt=ERROR_${header.SanitizedErrorReason}&level=ERROR&processingTimestamp=${header.ProcessingTimestamp}&contextId=ERROR")
                                .wireTap("kamelet:k-log-flow-summary?step=ERROR&kafkaTopicName=${header.KafkaFlowSummaryTopicName}&brokers=${header.Brokers}");
        }
}
