package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * PACS008 Payment Processing Route
 * 
 * Main route for processing PACS.008 payment messages using k-kafka-starter kamelet
 * 
 * NOTE: This Java RouteBuilder uses k-mq-starter kamelet for PACS008 processing
 */
@Component
public class ChProcessingRoute extends RouteBuilder {

        @Override
        public void configure() throws Exception {

                // Error handler configuration
                // errorHandler(defaultErrorHandler()
                // .maximumRedeliveries(3)
                // .redeliveryDelay(5000)
                // .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN));

                // Main PACS008 processing route using k-mq-starter kamelet with sink functionality
                String kameletMqStarterEndpoint = """
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

                from(kameletMqStarterEndpoint).routeId("pacs008-processing-flow").log(
                                "K-MQ-Starter kamelet initiated - message will be processed and sent to sink")
                                .end();

                // Sink endpoint to receive messages from k-mq-starter kamelet
                from("{{kmq.starter.sinkEndpoint}}").routeId("pacs008-main-processing")
                                .log("Received PACS008 message from k-mq-starter sink: ${body}")
                                .setHeader("MessageType", constant("PACS008"))
                                .setHeader("ProcessingTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .to("direct:process-pacs008-message");

                // PACS008 processing logic
                from("direct:process-pacs008-message").routeId("pacs008-message-processing")
                                .log("Processing PACS008 message with ID: ${header.JMSMessageID}")

                                // Enrich message with metadata
                                .setHeader("RouteName", constant("PACS008-Processing"))
                                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                                .log("Message Body: ${body}").log("Message Headers: ${headers}")
                                .end();



                // Error handling route
                from("direct:error-handling").routeId("pacs008-error-handler")
                                .log("Error processing message: ${exception.message}")
                                .setHeader("ErrorTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .setHeader("ErrorReason", simple("${exception.message}"))

                                // Send to error queue
                                .to("jms:queue:pacs008.error.queue")
                                .log("Message sent to error queue for manual review");
        }
}
