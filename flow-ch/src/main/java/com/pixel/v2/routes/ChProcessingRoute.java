package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * CH Payment Processing Route - Uses k-identification kamelet for Redis caching
 */
@Component
public class ChProcessingRoute extends RouteBuilder {

        // Kamelet endpoint for identification and Redis caching
        private static final String K_IDENTIFICATION_ENDPOINT =
                        "kamelet:k-identification?flowCode=${header.flowCode}&referentielServiceUrl={{pixel.referentiel.service.url}}&kafkaBrokers={{pixel.kafka.brokers}}&cacheTtl={{pixel.cache.ttl}}";

        // Kamelet endpoint for XSD validation
        private static final String K_XSD_VALIDATION_ENDPOINT =
                        "kamelet:k-xsd-validation?xsdFileName=pacs.008.001.02.ch.02.xsd&validationMode=STRICT";

        // Kamelet endpoint for XSL transformation
        private static final String K_XSL_TRANSFORMATION_ENDPOINT =
                        "kamelet:k-xsl-transformation?xslFileName=overall-xslt-ch-pacs008-001-02.xsl&transformationMode=STRICT";

        // Kamelet endpoint for flow summary logging
        private static final String K_LOG_FLOW_SUMMARY_ENDPOINT =
                        "kamelet:k-log-flow-summary?step=COMPLETED&kafkaTopicName=${header.kafkaFlowSummaryTopicName}&brokers=${header.brokers}";

        // Kamelet endpoint for MQ starter
        private static final String K_MQ_STARTER_ENDPOINT = """
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

        @Override
        public void configure() throws Exception {

                // Global exception handler
                onException(Exception.class)
                                .setHeader("ErrorType", simple("${exception.class.simpleName}"))
                                .setHeader("ErrorReason", simple("${exception.message}"))
                                .to("direct:error-handling").handled(true);

                // Error handler configuration
                errorHandler(defaultErrorHandler().maximumRedeliveries(0));

                from(K_MQ_STARTER_ENDPOINT).routeId("ch-processing-flow").to(
                                "log:ch-processing?level=DEBUG&showBody=false&showHeaders=false");

                // Sink endpoint to receive messages from k-mq-starter kamelet
                from("{{kmq.starter.sinkEndpoint}}").routeId("ch-main-processing")
                                .setHeader("ProcessingTimestamp",
                                                simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                                .to("direct:process-ch-message");

                // CH processing logic - With Redis caching
                from("direct:process-ch-message").routeId("ch-message-processing")
                                .setHeader("RouteName", constant("CH-Processing"))
                                .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
                                .setHeader("flowCode", simple("{{kmq.starter.flowCode}}"))
                                .setHeader("MessageType", constant("{{kmq.starter.payementType}}"))

                                // Step 1: Fetch reference data using k-identification kamelet
                                .to(K_IDENTIFICATION_ENDPOINT)

                                // Step 2: XSD Validation using k-xsd-validation
                                .to(K_XSD_VALIDATION_ENDPOINT)

                                // Step 3: XSLT Transformation using k-xsl-transformation
                                .to(K_XSL_TRANSFORMATION_ENDPOINT)

                                // Complete processing
                                .wireTap(K_LOG_FLOW_SUMMARY_ENDPOINT);
        }
}
