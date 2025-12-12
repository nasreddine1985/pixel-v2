package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * CH Payment Processing Route - With Redis caching implementation
 */
@Component
public class ChProcessingRoute extends RouteBuilder {

        // Route endpoint constants
        private static final String FETCH_REFERENCE_DATA_ENDPOINT =
                        "direct:fetchReferenceDataFromRedis";

        @Override
        public void configure() throws Exception {

                // Global exception handler
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
                                                flowCountryId={{kmq.starter.flowCountryId}}&\
                                                dataSource={{kmq.starter.dataSource}}""";

                from(kameletMqStarterEndpoint).routeId("ch-processing-flow").log(
                                "K-MQ-Starter kamelet initiated - message will be processed and sent to sink")
                                .end();

                // Sink endpoint to receive messages from k-mq-starter kamelet
                from("{{kmq.starter.sinkEndpoint}}").routeId("ch-main-processing")
                                .setHeader("MessageType", constant("PACS008"))
                                .setHeader("ProcessingTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .to("direct:process-ch-message");

                // CH processing logic - With Redis caching
                from("direct:process-ch-message").routeId("ch-message-processing")
                                .setHeader("RouteName", constant("CH-Processing"))
                                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                                .setHeader("flowCode", constant("ICHSIC")) // Set flow code for
                                                                           // Redis caching
                                .log("Processing CH message: ${body.substring(0, 100)}...")

                                // Step 1: Fetch reference data from Redis cache
                                .to(FETCH_REFERENCE_DATA_ENDPOINT)

                                // Step 2: XSD Validation using k-xsd-validation
                                .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.02.ch.02.xsd&validationMode=STRICT")

                                // Step 3: XSLT Transformation using k-xsl-transformation
                                .to("kamelet:k-xsl-transformation?xslFileName=overall-xslt-ch-pacs008-001-02.xsl&transformationMode=STRICT")

                                // Log completion
                                .log("CH message processing completed successfully")
                                .wireTap("kamelet:k-log-flow-summary?step=COMPLETED&kafkaTopicName=${header.kafkaFlowSummaryTopicName}&brokers=${header.brokers}");
        }
}
