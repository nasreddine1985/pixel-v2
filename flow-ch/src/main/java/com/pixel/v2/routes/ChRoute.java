package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * ChRoute - CH Payment Processing Route
 * 
 * This route handles the complete CH payment processing pipeline: 1. Receives payment messages from
 * MQ 2. Fetches reference data using k-identification-interne kamelet with Spring internal caching
 * 3. Performs XSD validation using k-xsd-validation kamelet 4. Applies XSLT transformation using
 * k-xsl-transformation kamelet 5. Publishes transformed message to ch-out Kafka topic 6. Logs flow
 * summary for monitoring and auditing
 */
@Component
public class ChRoute extends RouteBuilder {

        // Kamelet endpoint for identification and Spring internal caching
        private static final String K_IDENTIFICATION_ENDPOINT =
                        "kamelet:k-identification?flowCode=${header.flowCode}&referentielServiceUrl={{pixel.referentiel.service.url}}&kafkaBrokers={{pixel.kafka.brokers}}&cacheTtl={{pixel.cache.ttl}}";

        // Kamelet endpoint for duplicate check
        private static final String K_DUPLICATE_CHECK_ENDPOINT =
                        "kamelet:k-duplicate-check?dataSource={{kmq.starter.dataSource}}&disableCheckDB={{pixel.duplicate.check.disable:false}}&disableCheckMaxFileSize={{pixel.duplicate.check.max.file.size.disable:false}}&maxRetryCount={{pixel.duplicate.check.max.retry:3}}&retrySleepPeriod={{pixel.duplicate.check.retry.sleep:1000}}";

        // Kamelet endpoint for XSD validation
        private static final String K_XSD_VALIDATION_ENDPOINT =
                        "kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.ch.02.xsd&validationMode=STRICT";

        // Kamelet endpoint for XSL transformation
        private static final String K_XSL_PACS008_001_08TO_CDM_TRANSFORMATION_ENDPOINT =
                        "kamelet:k-xsl-transformation?xslFileName=overall-xslt-ch-pacs008-001-08-CH.xml&transformationMode=STRICT";
        // Kamelet endpoint for XSL transformation
        private static final String K_XSL_CDM_TO_PACS008_001_02_TRANSFORMATION_ENDPOINT =
                        "kamelet:k-xsl-transformation?xslFileName=overall-xslt-ch-CDM-2pacs008-001-02DOM.xml&transformationMode=STRICT";

        // Kamelet endpoint for Kafka publisher
        private static final String K_KAFKA_PUBLISHER_ENDPOINT =
                        "kamelet:k-kafka-publisher?kafkaTopicName=ch-out&brokers={{pixel.kafka.brokers}}";

        // Kamelet endpoint for dynamic publisher
        private static final String K_DYNAMIC_PUBLISHER_ENDPOINT =
                        "kamelet:k-dynamic-publisher?header-name=RefFlowData";

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
                        flowCountryCode={{kmq.starter.flowCountryCode}}&\
                        flowCountryId={{kmq.starter.flowCountryId}}&\
                        dataSource={{kmq.starter.dataSource}}&\
                        nasArchiveUrl={{nas.archive.url}}""";

        @Override
        public void configure() throws Exception {

                // Global exception handler
                onException(Exception.class)
                                .setHeader("ErrorType", simple("${exception.class.simpleName}"))
                                .setHeader("ErrorReason", simple("${exception.message}"))
                                .to("direct:error-handling").handled(true);

                // Error handler configuration
                errorHandler(defaultErrorHandler().maximumRedeliveries(0));

                // Step 1: Receive Payement Message
                from(K_MQ_STARTER_ENDPOINT)

                                // Step 2: Fetch reference data using k-identification-interne
                                // kamelet
                                .to(K_IDENTIFICATION_ENDPOINT)

                                // Step 3: Duplicate check processing
                                .to(K_DUPLICATE_CHECK_ENDPOINT)

                                // Step 4: XSD Validation using
                                // k-xsd-validation
                                .to(K_XSD_VALIDATION_ENDPOINT)

                                // Step 5: XSLT Transformation using k-xsl-transformation
                                .to(K_XSL_PACS008_001_08TO_CDM_TRANSFORMATION_ENDPOINT)

                                // Step 6: XSLT Transformation using k-xsl-transformation
                                .to(K_XSL_CDM_TO_PACS008_001_02_TRANSFORMATION_ENDPOINT)

                                // Step 7: Dynamic route to destination using k-dynamic-publisher
                                .to(K_DYNAMIC_PUBLISHER_ENDPOINT)

                                // Step 8: Publish transformed message to ch-out topic
                                // .to(K_KAFKA_PUBLISHER_ENDPOINT)

                                // Step 9: Complete processing - dynamic step based on failure count
                                .wireTap("direct:log-flow-summary");

                // Dynamic flow summary logging route
                from("direct:log-flow-summary").choice()
                                .when(simple("${exchangeProperty.failureCount} == 0"))
                                .setHeader("stepValue", constant("COMPLETED")).otherwise()
                                .setHeader("stepValue", constant("FAILURE")).end()
                                .toD("kamelet:k-log-flow-summary?step=${header.stepValue}&kafkaTopicName=${header.kafkaFlowSummaryTopicName}&brokers={{camel.component.kafka.brokers}}");
        }
}
