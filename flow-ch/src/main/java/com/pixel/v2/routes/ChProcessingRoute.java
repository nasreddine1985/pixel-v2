package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * CH Payment Processing Route
 * 
 * Main route for processing CH payment messages using k-kafka-starter kamelet
 * 
 * NOTE: This Java RouteBuilder uses k-mq-starter kamelet for CH processing
 */
@Component
public class ChProcessingRoute extends RouteBuilder {

        @Override
        public void configure() throws Exception {

                // Global exception handler for all errors including XSD validation
                onException(Exception.class).log(
                                "Global exception handler - Exception caught in CH processing: ${exception.message}")
                                .setHeader("ErrorType", simple("${exception.class.simpleName}"))
                                .setHeader("ErrorReason", simple("${exception.message}"))
                                .to("direct:error-handling").handled(true);


                // Error handler configuration
                errorHandler(defaultErrorHandler().maximumRedeliveries(0));

                // Main PACS008 processing route using k-mq-starter kamelet with sink functionality
                String kameletMqStarterEndpoint =
                                """
                                                kamelet:k-mq-starter?mqFileName={{kmq.starter.mqFileName}}&\
                                                connectionFactory={{kmq.starter.connectionFactory}}&\
                                                flowCode={{kmq.starter.flowCode}}&\
                                                messageType={{kmq.starter.messageType}}&\
                                                kafkaFlowSummaryTopicName={{kmq.starter.kafkaFlowSummaryTopicName}}&\
                                                kafkaLogTopicName={{kmq.starter.kafkaLogTopicName}}&\
                                                kafkaDistributionTopicName={{kmq.starter.kafkaDistributionTopicName}}&\
                                                brokers={{kmq.starter.brokers}}&\
                                                sinkEndpoint={{kmq.starter.sinkEndpoint}}&\
                                                flowCountryCode={{kmq.starter.flowCountryCode}}&\
                                                flowCountryId={{kmq.starter.flowCountryId}}""";

                from(kameletMqStarterEndpoint).routeId("ch-processing-flow").log(
                                "K-MQ-Starter kamelet initiated - message will be processed and sent to sink")
                                .end();

                // Sink endpoint to receive messages from k-mq-starter kamelet
                from("{{kmq.starter.sinkEndpoint}}").routeId("ch-main-processing")
                                .setHeader("MessageType", constant("PACS008"))
                                .setHeader("ProcessingTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .to("direct:process-ch-message");

                // CH processing logic
                from("direct:process-ch-message").routeId("ch-message-processing")
                                // Enrich message with metadata
                                .setHeader("RouteName", constant("CH-Processing"))
                                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))

                                // Step 1: XSD Validation using k-xsd-validation kamelet with
                                // explicit error handling
                                .doTry()
                                .to("kamelet:k-xsd-validation-custom?xsdFileName=pacs.008.001.08.xsd&validationMode=STRICT")
                                .doCatch(Exception.class).to("direct:error-handling").end().end();

                // Test route to verify exception handling works
                from("direct:test-exception").routeId("test-exception")
                                .log("Testing exception handling")
                                .throwException(new RuntimeException(
                                                "Test exception for error handling verification"))
                                .end();

                // Error handling route
                from("direct:error-handling").routeId("ch-error-handler")
                                .log("Error processing message: ${exception.message}")
                                .setHeader("ErrorTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .setHeader("ErrorReason", simple("${exception.message}"))
                                .setHeader("ShortErrorReason", simple(
                                                "${exception.class.simpleName}: ${exception.message}"))
                                .process(exchange -> {
                                        // Truncate ShortErrorReason to fit varchar(128) constraint
                                        String shortReason = exchange.getIn().getHeader(
                                                        "ShortErrorReason", String.class);
                                        if (shortReason != null && shortReason.length() > 120) {
                                                exchange.getIn().setHeader("ShortErrorReason",
                                                                shortReason.substring(0, 120)
                                                                                + "...");
                                        }
                                })
                                // Log error event to Kafka with explicit headers
                                .wireTap("kamelet:k-log-events?level=ERROR&logMessageTxt=[CH-ERROR] ${header.MessageType}: ${header.ShortErrorReason}&kafkaTopicName=${header.kafkaLogTopicName}&brokers=${header.brokers}")
                                .log("Error headers: ${headers}")
                                .setHeader("step", constant("ERROR"))
                                .wireTap("kamelet:k-log-flow-summary?kafkaTopicName=${header.kafkaFlowSummaryTopicName}&brokers=${header.brokers}");
        }
}
