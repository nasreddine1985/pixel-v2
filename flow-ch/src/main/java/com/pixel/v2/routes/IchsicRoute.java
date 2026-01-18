package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * IchsicRoute - CH Payment Processing Route (ICHSIC Flow)
 * 
 * This route handles the complete CH payment processing pipeline: 1. Receives payment messages from
 * MQ 2. Fetches reference data using k-identification-interne kamelet with Spring internal caching
 * 3. Performs XSD validation using k-xsd-validation kamelet 4. Applies XSLT transformation using
 * k-xsl-transformation kamelet 5. Publishes transformed message to ch-out Kafka topic 6. Logs flow
 * summary for monitoring and auditing
 */
@Component
public class IchsicRoute extends RouteBuilder {

        // Kamelet endpoint for identification and Spring internal caching
        private static final String K_IDENTIFICATION_ENDPOINT =
                        "kamelet:k-identification?flowCode={{pixel.flow.code}}&referentialServiceUrl={{pixel.referential.service.url}}&kafkaBrokers={{pixel.kafka.brokers}}&cacheTtl={{pixel.cache.ttl}}";

        // Kamelet endpoint for duplicate check
        private static final String K_DUPLICATE_CHECK_ENDPOINT =
                        "kamelet:k-duplicate-check?dataSource={{pixel.datasource.name}}&disableCheckDB={{pixel.duplicate.check.disable:false}}&disableCheckMaxFileSize={{pixel.duplicate.check.max.file.size.disable:false}}&maxRetryCount={{pixel.duplicate.check.max.retry:3}}&retrySleepPeriod={{pixel.duplicate.check.retry.sleep:1000}}";

        // Kamelet endpoint for XSD validation
        private static final String K_XSD_VALIDATION_ENDPOINT =
                        "kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.ch.02.xsd&validationMode=STRICT";

        // Kamelet endpoint for XSL transformation
        private static final String K_XSL_PACS008_001_08TO_CDM_TRANSFORMATION_ENDPOINT =
                        "kamelet:k-xsl-transformation?xslFileName=overall-xslt-ch-pacs008-001-08-CH.xml&transformationMode=STRICT";
        // Kamelet endpoint for XSL transformation
        private static final String K_XSL_CDM_TO_PACS008_001_02_TRANSFORMATION_ENDPOINT =
                        "kamelet:k-xsl-transformation?xslFileName=overall-xslt-ch-CDM-2pacs008-001-02DOM.xml&transformationMode=STRICT";


        // Kamelet endpoint for dynamic publisher
        private static final String K_DYNAMIC_PUBLISHER_ENDPOINT =
                        "kamelet:k-dynamic-publisher?header-name=RefFlowData";
        // Kamelet endpoint for dynamic publisher
        private static final String K_LOG_FLOW_SUMMARY_PUBLISHER_ENDPOINT =
                        "kamelet:k-log-flow-summary?step=COMPLETED&kafkaTopicName=${header.kafkaFlowSummaryTopicName}&brokers={{pixel.kafka.brokers}}";

        // Kamelet endpoint for MQ starter
        private static final String K_MQ_STARTER_ENDPOINT = """
                        kamelet:k-mq-starter?mqFileName={{kmq.starter.mqFileName}}&\
                        connectionFactory={{kmq.starter.connectionFactory}}&\
                        flowCode={{kmq.starter.flowCode}}&\
                        brokers={{pixel.kafka.brokers}}&\
                        dataSource={{pixel.datasource.name}}&\
                        nasArchiveUrl={{pixel.nas.archive.url}}""";

        @Override
        public void configure() throws Exception {

                // Step 1: Receive Payment Message
                from(K_MQ_STARTER_ENDPOINT)

                                // Step 2: Fetch reference data using k-identification-interne
                                // kamelet
                                .to(K_IDENTIFICATION_ENDPOINT)

                                // // Step 3: Duplicate check processing
                                .to(K_DUPLICATE_CHECK_ENDPOINT)

                                // // Step 4: XSD Validation using
                                // // k-xsd-validation
                                .to(K_XSD_VALIDATION_ENDPOINT)

                                // // Step 5: XSLT Transformation using k-xsl-transformation
                                .to(K_XSL_PACS008_001_08TO_CDM_TRANSFORMATION_ENDPOINT)

                                // // Step 6: XSLT Transformation using k-xsl-transformation
                                .to(K_XSL_CDM_TO_PACS008_001_02_TRANSFORMATION_ENDPOINT)

                                // // Step 7: Dynamic route to destination using k-dynamic-publisher
                                .to(K_DYNAMIC_PUBLISHER_ENDPOINT)

                                // Step 8: Complete processing - dynamic step based on failure count
                                .wireTap(K_LOG_FLOW_SUMMARY_PUBLISHER_ENDPOINT);

        }
}
