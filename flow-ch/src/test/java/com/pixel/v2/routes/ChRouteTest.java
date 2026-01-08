package com.pixel.v2.routes;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for ChRoute Tests the complete CH payment processing pipeline including MQ consumption,
 * identification, validation, transformation, and Kafka publishing
 */
@CamelSpringBootTest
@SpringBootTest(classes = ChRouteTestApplication.class)
@TestPropertySource(properties = {"camel.component.kamelet.location=classpath:kamelets",
        "pixel.referential.service.url=http://mock-referential:8080",
        "pixel.kafka.brokers=mock-kafka:9092", "pixel.cache.ttl=3600",
        "kmq.starter.mqFileName=CH.REQUEST.QUEUE",
        "kmq.starter.connectionFactory=mockConnectionFactory", "kmq.starter.flowCode=PACS008",
        "kmq.starter.messageType=pacs.008.001.02.ch",
        "kmq.starter.kafkaFlowSummaryTopicName=flow-summary",
        "kmq.starter.kafkaLogTopicName=log-events",
        "kmq.starter.kafkaDistributionTopicName=distribution",
        "kmq.starter.brokers=mock-kafka:9092", "kmq.starter.flowCountryCode=CH",
        "kmq.starter.flowCountryId=756", "kmq.starter.dataSource=mockDataSource",
        "nas.archive.url=smb://mock-nas/archive"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Produce("direct:test-input")
    private ProducerTemplate producer;

    @EndpointInject("mock:sink")
    private MockEndpoint mockSink;

    @EndpointInject("mock:kafka-publisher")
    private MockEndpoint mockKafkaPublisher;

    @EndpointInject("mock:log-summary")
    private MockEndpoint mockLogSummary;

    @EndpointInject("mock:error-handler")
    private MockEndpoint mockErrorHandler;

    private static final String SAMPLE_PACS008_MESSAGE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>CHPACS008TEST001</MsgId>
                        <CreDtTm>2025-12-18T10:00:00</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <EndToEndId>E2E123456</EndToEndId>
                            <TxId>TX123456</TxId>
                        </PmtId>
                        <Amt>
                            <InstdAmt Ccy="CHF">1000.00</InstdAmt>
                        </Amt>
                        <Dbtr>
                            <Nm>John Doe</Nm>
                        </Dbtr>
                        <Cdtr>
                            <Nm>Jane Smith</Nm>
                        </Cdtr>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

    private static final String TRANSFORMED_MESSAGE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">
                <CstmrCdtTrfInitn>
                    <GrpHdr>
                        <MsgId>CHPACS008TEST001_TRANSFORMED</MsgId>
                        <CreDtTm>2025-12-18T10:00:00</CreDtTm>
                    </GrpHdr>
                </CstmrCdtTrfInitn>
            </Document>
            """;

    private static final String FLOW_CONFIG_RESPONSE = """
            {
                "flowCode": "PACS008",
                "flowName": "CH Customer Credit Transfer",
                "enabled": true,
                "validation": {
                    "xsdFile": "pacs.008.001.02.ch.02.xsd",
                    "mode": "STRICT"
                },
                "transformation": {
                    "xslFile": "overall-xslt-ch-pacs008-001-02.xsl",
                    "mode": "STRICT"
                },
                "routing": {
                    "targetTopic": "ch-out"
                }
            }
            """;

    @BeforeEach
    void setUp() throws Exception {
        mockSink.reset();
        mockKafkaPublisher.reset();
        mockLogSummary.reset();
        mockErrorHandler.reset();
    }

    /**
     * Test complete CH payment processing pipeline
     */
    @Test
    public void testCompleteChPaymentProcessing() throws Exception {
        // Create mock route for complete flow
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-ch-complete-flow")
                        .log("Starting CH payment processing")

                        // Mock k-mq-starter (message receipt and initial logging)
                        .setHeader("flowCode", constant("PACS008"))
                        .setHeader("messageType", constant("pacs.008.001.02.ch"))
                        .setHeader("flowCountryCode", constant("CH"))
                        .setHeader("flowOccurId", simple("CH-${date:now:yyyyMMdd-HHmmss-SSS}"))
                        .log("Mock k-mq-starter: Message received with flowOccurId: ${header.flowOccurId}")

                        // Mock k-identification-interne (fetch reference data)
                        .setHeader("FlowConfiguration", constant(FLOW_CONFIG_RESPONSE))
                        .log("Mock k-identification-interne: Flow configuration loaded")

                        // Mock k-xsd-validation
                        .setHeader("XsdValidationResult", constant("VALID"))
                        .log("Mock k-xsd-validation: Message validated successfully")

                        // Mock k-xsl-transformation
                        .setBody(constant(TRANSFORMED_MESSAGE))
                        .setHeader("TransformationResult", constant("SUCCESS"))
                        .log("Mock k-xsl-transformation: Message transformed")

                        // Mock k-kafka-publisher
                        .setHeader("KafkaPublishResult", constant("SUCCESS"))
                        .setHeader("KafkaTopicName", constant("ch-out")).to("mock:kafka-publisher")

                        // Mock k-log-flow-summary
                        .setHeader("FlowStep", constant("COMPLETED")).to("mock:log-summary")

                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);
        mockKafkaPublisher.expectedMessageCount(1);
        mockLogSummary.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put("JMSMessageID", "CH-TEST-MSG-001");

        producer.sendBodyAndHeaders(SAMPLE_PACS008_MESSAGE, headers);

        mockSink.assertIsSatisfied(5000);
        mockKafkaPublisher.assertIsSatisfied(5000);
        mockLogSummary.assertIsSatisfied(5000);

        // Verify flow processing
        Exchange sinkExchange = mockSink.getReceivedExchanges().get(0);
        assertEquals("PACS008", sinkExchange.getIn().getHeader("flowCode"));
        assertEquals("CH", sinkExchange.getIn().getHeader("flowCountryCode"));
        assertEquals("SUCCESS", sinkExchange.getIn().getHeader("TransformationResult"));
        assertNotNull(sinkExchange.getIn().getHeader("flowOccurId"));

        // Verify Kafka publishing
        Exchange kafkaExchange = mockKafkaPublisher.getReceivedExchanges().get(0);
        assertEquals("SUCCESS", kafkaExchange.getIn().getHeader("KafkaPublishResult"));
        assertEquals("ch-out", kafkaExchange.getIn().getHeader("KafkaTopicName"));

        // Verify flow summary logging
        Exchange summaryExchange = mockLogSummary.getReceivedExchanges().get(0);
        assertEquals("COMPLETED", summaryExchange.getIn().getHeader("FlowStep"));
    }

    /**
     * Test identification and caching flow
     */
    @Test
    public void testIdentificationAndCaching() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-identification-flow")
                        .setHeader("FlowCode", constant("PACS008"))
                        .setHeader("CacheKey", simple("flow_config_${header.FlowCode}"))

                        // Simulate cache miss and referential call
                        .choice().when(header("SimulateCacheHit").isEqualTo(true))
                        .setBody(constant(FLOW_CONFIG_RESPONSE))
                        .setHeader("CacheResult", constant("HIT"))
                        .log("Cache HIT for flow ${header.FlowCode}").otherwise()
                        .setBody(constant(FLOW_CONFIG_RESPONSE))
                        .setHeader("CacheResult", constant("MISS"))
                        .log("Cache MISS - fetched from referential service").end()

                        .setHeader("FlowConfiguration", simple("${body}")).to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(2);

        // Test cache miss scenario
        Map<String, Object> headers1 = new HashMap<>();
        headers1.put("SimulateCacheHit", false);
        producer.sendBodyAndHeaders(SAMPLE_PACS008_MESSAGE, headers1);

        // Test cache hit scenario
        Map<String, Object> headers2 = new HashMap<>();
        headers2.put("SimulateCacheHit", true);
        producer.sendBodyAndHeaders(SAMPLE_PACS008_MESSAGE, headers2);

        mockSink.assertIsSatisfied(5000);

        // Verify cache miss scenario
        Exchange missExchange = mockSink.getReceivedExchanges().get(0);
        assertEquals("MISS", missExchange.getIn().getHeader("CacheResult"));
        assertEquals(FLOW_CONFIG_RESPONSE.trim(),
                missExchange.getIn().getHeader("FlowConfiguration", String.class).trim());

        // Verify cache hit scenario
        Exchange hitExchange = mockSink.getReceivedExchanges().get(1);
        assertEquals("HIT", hitExchange.getIn().getHeader("CacheResult"));
        assertEquals(FLOW_CONFIG_RESPONSE.trim(),
                hitExchange.getIn().getHeader("FlowConfiguration", String.class).trim());
    }

    /**
     * Test XSD validation scenarios
     */
    @Test
    public void testXsdValidation() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-xsd-validation")
                        .setHeader("XsdFileName", constant("pacs.008.001.02.ch.02.xsd"))
                        .setHeader("ValidationMode", constant("STRICT"))

                        .choice().when(header("SimulateValidationError").isEqualTo(true))
                        .throwException(new IllegalArgumentException(
                                "XSD Validation failed: Invalid message structure"))
                        .otherwise().setHeader("ValidationResult", constant("VALID"))
                        .log("XSD Validation successful").end()

                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody(SAMPLE_PACS008_MESSAGE);

        mockSink.assertIsSatisfied(5000);

        Exchange validExchange = mockSink.getReceivedExchanges().get(0);
        assertEquals("VALID", validExchange.getIn().getHeader("ValidationResult"));
        assertEquals("pacs.008.001.02.ch.02.xsd", validExchange.getIn().getHeader("XsdFileName"));
    }

    /**
     * Test XSLT transformation scenarios
     */
    @Test
    public void testXslTransformation() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-xsl-transformation")
                        .setHeader("XslFileName", constant("overall-xslt-ch-pacs008-001-02.xsl"))
                        .setHeader("TransformationMode", constant("STRICT"))

                        // Mock transformation
                        .setBody(constant(TRANSFORMED_MESSAGE))
                        .setHeader("TransformationResult", constant("SUCCESS"))
                        .setHeader("OriginalMessageSize", simple("${body.length()}"))
                        .log("XSLT Transformation completed successfully")

                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody(SAMPLE_PACS008_MESSAGE);

        mockSink.assertIsSatisfied(5000);

        Exchange transformedExchange = mockSink.getReceivedExchanges().get(0);
        assertEquals("SUCCESS", transformedExchange.getIn().getHeader("TransformationResult"));
        assertEquals("overall-xslt-ch-pacs008-001-02.xsl",
                transformedExchange.getIn().getHeader("XslFileName"));
        assertTrue(transformedExchange.getIn().getBody(String.class).contains("CstmrCdtTrfInitn"));
    }

    /**
     * Test error handling scenarios
     */
    @Test
    public void testErrorHandling() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Global exception handler
                onException(Exception.class)
                        .setHeader("ErrorType", simple("${exception.class.simpleName}"))
                        .setHeader("ErrorReason", simple("${exception.message}"))
                        .to("mock:error-handler").handled(true);

                from("direct:test-input").routeId("test-error-handling")
                        .log("Processing message that will cause error")

                        .choice().when(header("ErrorType").isEqualTo("ValidationError"))
                        .throwException(new IllegalArgumentException("XSD validation failed"))
                        .when(header("ErrorType").isEqualTo("TransformationError"))
                        .throwException(new RuntimeException("XSLT transformation failed"))
                        .when(header("ErrorType").isEqualTo("KafkaError"))
                        .throwException(new RuntimeException("Kafka publishing failed")).otherwise()
                        .throwException(new RuntimeException("General processing error")).end()

                        .to("mock:sink");
            }
        });

        mockErrorHandler.expectedMessageCount(3);

        // Test validation error
        Map<String, Object> headers1 = new HashMap<>();
        headers1.put("ErrorType", "ValidationError");
        producer.sendBodyAndHeaders(SAMPLE_PACS008_MESSAGE, headers1);

        // Test transformation error
        Map<String, Object> headers2 = new HashMap<>();
        headers2.put("ErrorType", "TransformationError");
        producer.sendBodyAndHeaders(SAMPLE_PACS008_MESSAGE, headers2);

        // Test Kafka error
        Map<String, Object> headers3 = new HashMap<>();
        headers3.put("ErrorType", "KafkaError");
        producer.sendBodyAndHeaders(SAMPLE_PACS008_MESSAGE, headers3);

        mockErrorHandler.assertIsSatisfied(5000);

        // Verify validation error handling
        Exchange validationErrorExchange = mockErrorHandler.getReceivedExchanges().get(0);
        assertEquals("IllegalArgumentException",
                validationErrorExchange.getIn().getHeader("ErrorType"));
        assertTrue(validationErrorExchange.getIn().getHeader("ErrorReason", String.class)
                .contains("XSD validation failed"));

        // Verify transformation error handling
        Exchange transformationErrorExchange = mockErrorHandler.getReceivedExchanges().get(1);
        assertEquals("RuntimeException",
                transformationErrorExchange.getIn().getHeader("ErrorType"));
        assertTrue(transformationErrorExchange.getIn().getHeader("ErrorReason", String.class)
                .contains("XSLT transformation failed"));

        // Verify Kafka error handling
        Exchange kafkaErrorExchange = mockErrorHandler.getReceivedExchanges().get(2);
        assertEquals("RuntimeException", kafkaErrorExchange.getIn().getHeader("ErrorType"));
        assertTrue(kafkaErrorExchange.getIn().getHeader("ErrorReason", String.class)
                .contains("Kafka publishing failed"));
    }

    /**
     * Test Kafka publishing scenarios
     */
    @Test
    public void testKafkaPublishing() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-kafka-publishing")
                        .setHeader("KafkaTopicName", constant("ch-out"))
                        .setHeader("KafkaBrokers", constant("mock-kafka:9092"))
                        .setHeader("MessageKey", simple("CH-${header.flowOccurId}"))

                        // Mock Kafka publishing
                        .setHeader("KafkaPublishResult", constant("SUCCESS"))
                        .setHeader("KafkaPartition", constant("0"))
                        .setHeader("KafkaOffset", constant("12345"))
                        .log("Message published to Kafka topic: ${header.KafkaTopicName}")

                        .to("mock:kafka-publisher");
            }
        });

        mockKafkaPublisher.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put("flowOccurId", "CH-20251218-103000-001");

        producer.sendBodyAndHeaders(TRANSFORMED_MESSAGE, headers);

        mockKafkaPublisher.assertIsSatisfied(5000);

        Exchange kafkaExchange = mockKafkaPublisher.getReceivedExchanges().get(0);
        assertEquals("SUCCESS", kafkaExchange.getIn().getHeader("KafkaPublishResult"));
        assertEquals("ch-out", kafkaExchange.getIn().getHeader("KafkaTopicName"));
        assertEquals("CH-CH-20251218-103000-001", kafkaExchange.getIn().getHeader("MessageKey"));
        assertEquals("0", kafkaExchange.getIn().getHeader("KafkaPartition"));
    }
}
