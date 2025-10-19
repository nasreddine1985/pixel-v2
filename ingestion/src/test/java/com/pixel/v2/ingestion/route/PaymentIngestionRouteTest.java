package com.pixel.v2.ingestion.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the PaymentIngestionRouteBuilder
 * 
 * This test class covers the main payment ingestion orchestration flow
 * and its various stages including receipt, enrichment, validation,
 * idempotence checking, and publishing to Kafka.
 */
@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("test")
@UseAdviceWith
class PaymentIngestionRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    private static final String SAMPLE_PAYMENT_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
            <FIToFICstmrCdtTrf>
                <GrpHdr>
                    <MsgId>MSG123456789</MsgId>
                    <CreDtTm>2025-10-19T14:30:00</CreDtTm>
                    <NbOfTxs>1</NbOfTxs>
                </GrpHdr>
                <CdtTrfTxInf>
                    <PmtId>
                        <InstrId>INSTR123</InstrId>
                        <EndToEndId>E2E123</EndToEndId>
                        <TxId>TXN123</TxId>
                    </PmtId>
                    <IntrBkSttlmAmt Ccy="EUR">1000.00</IntrBkSttlmAmt>
                </CdtTrfTxInf>
            </FIToFICstmrCdtTrf>
        </Document>
        """;

        @BeforeEach
    void setup() throws Exception {
        // Set system properties for kamelets
        System.setProperty("refloader.serviceUrl", "http://localhost:8080");
        
        // Set up route advice to replace external endpoints with mocks
        AdviceWith.adviceWith(camelContext, "payment-ingestion-orchestrator", 
            route -> {
                route.replaceFromWith("direct:payment-ingestion");
                route.mockEndpoints("direct:reference-enrichment");
                route.mockEndpoints("direct:validation");
                route.mockEndpoints("direct:idempotence-check");
                route.mockEndpoints("direct:kafka-publisher");
                route.mockEndpoints("direct:rejection-handler");
            });

        AdviceWith.adviceWith(camelContext, "reference-enrichment", 
            route -> route.mockEndpoints("kamelet:k-ref-loader"));

        AdviceWith.adviceWith(camelContext, "validation-step", 
            route -> route.mockEndpoints("kamelet:k-ingest-validation"));

        AdviceWith.adviceWith(camelContext, "idempotence-check", 
            route -> route.mockEndpoints("kamelet:k-idempotence"));

        AdviceWith.adviceWith(camelContext, "kafka-publisher", 
            route -> route.mockEndpoints("kafka:*"));

        AdviceWith.adviceWith(camelContext, "rejection-handler", 
            route -> route.mockEndpoints("kafka:*"));

        AdviceWith.adviceWith(camelContext, "error-handler", 
            route -> route.mockEndpoints("kafka:*"));

        // Start the context so routes are created and we can see what endpoints exist
        camelContext.start();

        // Find and mock any HTTP endpoints that got created
        camelContext.getEndpoints().forEach(endpoint -> {
            if (endpoint.getEndpointUri().startsWith("http:") || endpoint.getEndpointUri().startsWith("https:")) {
                try {
                    camelContext.getEndpoint("mock:" + endpoint.getEndpointUri().replace(":", "-").replace("/", "-"));
                } catch (Exception e) {
                    // Ignore if mock creation fails
                }
            }
        });

        // Set up default mock responses for kamelets
        MockEndpoint refLoaderMock = camelContext.getEndpoint("mock:kamelet:k-ref-loader", MockEndpoint.class);
        refLoaderMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CmdMapping", "TEST_MAPPING");
            exchange.getIn().setHeader("Rail", "TEST_RAIL");
            exchange.getIn().setHeader("Mode", "TEST_MODE");
        });

        MockEndpoint validationMock = camelContext.getEndpoint("mock:kamelet:k-ingest-validation", MockEndpoint.class);
        validationMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("IsValid", true);
            exchange.getIn().setHeader("ValidationResult", "PASSED");
        });

        MockEndpoint idempotenceMock = camelContext.getEndpoint("mock:kamelet:k-idempotence", MockEndpoint.class);
        idempotenceMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("IsDuplicate", false);
            exchange.getIn().setHeader("IdempotenceChecked", true);
            exchange.getIn().setHeader("CanProcess", true);
        });

        // Mock HTTP endpoints that might be called by kamelets
        MockEndpoint httpMock = camelContext.getEndpoint("mock:http:localhost", MockEndpoint.class);
        httpMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setBody("{\"cmdMapping\":\"TEST_MAPPING\",\"rail\":\"TEST_RAIL\",\"mode\":\"TEST_MODE\"}");
        });

        // Enable global HTTP mocking
        camelContext.setStreamCaching(true);
    }

    @Test
    void testPaymentIngestionOrchestrator() throws Exception {
        // Setup mocks
        MockEndpoint referenceEnrichmentMock = camelContext.getEndpoint("mock:direct:reference-enrichment", MockEndpoint.class);
        MockEndpoint validationMock = camelContext.getEndpoint("mock:direct:validation", MockEndpoint.class);
        MockEndpoint idempotenceMock = camelContext.getEndpoint("mock:direct:idempotence-check", MockEndpoint.class);
        MockEndpoint kafkaPublisherMock = camelContext.getEndpoint("mock:direct:kafka-publisher", MockEndpoint.class);

        // Setup kamelet mocks
        MockEndpoint refLoaderMock = camelContext.getEndpoint("mock:kamelet:k-ref-loader", MockEndpoint.class);
        MockEndpoint validationKameletMock = camelContext.getEndpoint("mock:kamelet:k-ingest-validation", MockEndpoint.class);
        MockEndpoint idempotenceKameletMock = camelContext.getEndpoint("mock:kamelet:k-idempotence", MockEndpoint.class);

        referenceEnrichmentMock.expectedMessageCount(1);
        validationMock.expectedMessageCount(1);
        idempotenceMock.expectedMessageCount(1);
        kafkaPublisherMock.expectedMessageCount(1);

        refLoaderMock.expectedMessageCount(1);
        validationKameletMock.expectedMessageCount(1);
        idempotenceKameletMock.expectedMessageCount(1);

        // Configure kamelet responses to proceed successfully
        refLoaderMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("CmdMapping", "TEST_MAPPING");
            exchange.getIn().setHeader("Rail", "TEST_RAIL");
            exchange.getIn().setHeader("Mode", "TEST_MODE");
        });

        validationKameletMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsValid", true);
            exchange.getIn().setHeader("ValidationResult", "PASSED");
        });

        idempotenceKameletMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsDuplicate", false);
            exchange.getIn().setHeader("CanProcess", true);
            exchange.getIn().setHeader("IdempotenceChecked", true);
        });

        // Send test message
        Exchange result = producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(SAMPLE_PAYMENT_XML);
            exchange.getIn().setHeader("ReceiptChannel", "TEST_API");
            exchange.getIn().setHeader("MessageId", "MSG123456789");
        });

        // Verify all steps were called
        referenceEnrichmentMock.assertIsSatisfied();
        validationMock.assertIsSatisfied();
        idempotenceMock.assertIsSatisfied();
        kafkaPublisherMock.assertIsSatisfied();

        // Verify kamelet interactions
        refLoaderMock.assertIsSatisfied();
        validationKameletMock.assertIsSatisfied();
        idempotenceKameletMock.assertIsSatisfied();

        // Verify headers were set correctly
        assertNotNull(result.getIn().getHeader("IngestionStartTime"));
        assertEquals("INGESTION_START", result.getIn().getHeader("ProcessingStage"));
    }

    @Test
    void testReferenceEnrichment() throws Exception {
        MockEndpoint kameletMock = camelContext.getEndpoint("mock:kamelet:k-ref-loader", MockEndpoint.class);
        kameletMock.expectedMessageCount(1);

        // Configure kamelet to add reference data
        kameletMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("CmdMapping", "TEST_MAPPING");
            exchange.getIn().setHeader("Rail", "TEST_RAIL");
            exchange.getIn().setHeader("Mode", "TEST_MODE");
        });

        // Send message to reference enrichment
        Exchange result = producerTemplate.send("direct:reference-enrichment", exchange -> {
            exchange.getIn().setBody(SAMPLE_PAYMENT_XML);
            exchange.getIn().setHeader("MessageId", "MSG123456789");
        });

        kameletMock.assertIsSatisfied();

        // Verify reference data was added
        assertEquals("TEST_MAPPING", result.getIn().getHeader("CmdMapping"));
        assertEquals("TEST_RAIL", result.getIn().getHeader("Rail"));
        assertEquals("TEST_MODE", result.getIn().getHeader("Mode"));
        assertEquals("REFERENCE_ENRICHMENT", result.getIn().getHeader("ProcessingStage"));
    }

    @Test
    void testValidationStep() throws Exception {
        MockEndpoint kameletMock = camelContext.getEndpoint("mock:kamelet:k-ingest-validation", MockEndpoint.class);
        kameletMock.expectedMessageCount(1);

        // Configure validation to pass
        kameletMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsValid", true);
            exchange.getIn().setHeader("ValidationResult", "PASSED");
        });

        // Send message to validation
        Exchange result = producerTemplate.send("direct:validation", exchange -> {
            exchange.getIn().setBody(SAMPLE_PAYMENT_XML);
            exchange.getIn().setHeader("MessageId", "MSG123456789");
        });

        kameletMock.assertIsSatisfied();

        // Verify validation passed
        assertEquals(true, result.getIn().getHeader("IsValid"));
        assertEquals("VALIDATION", result.getIn().getHeader("ProcessingStage"));
    }

    @Test
    void testIdempotenceCheck() throws Exception {
        MockEndpoint kameletMock = camelContext.getEndpoint("mock:kamelet:k-idempotence", MockEndpoint.class);
        kameletMock.expectedMessageCount(1);

        // Configure idempotence check (not duplicate)
        kameletMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsDuplicate", false);
            exchange.getIn().setHeader("IdempotenceChecked", true);
        });

        // Send message to idempotence check
        Exchange result = producerTemplate.send("direct:idempotence-check", exchange -> {
            exchange.getIn().setBody(SAMPLE_PAYMENT_XML);
            exchange.getIn().setHeader("MessageId", "MSG123456789");
        });

        kameletMock.assertIsSatisfied();

        // Verify idempotence check passed
        assertEquals(false, result.getIn().getHeader("IsDuplicate"));
        assertEquals("IDEMPOTENCE", result.getIn().getHeader("ProcessingStage"));
    }

    @Test
    void testKafkaPublisher() throws Exception {
        MockEndpoint kafkaMock = camelContext.getEndpoint("mock:kafka:payments-processed", MockEndpoint.class);
        kafkaMock.expectedMessageCount(1);

        // Send message to Kafka publisher
        Exchange result = producerTemplate.send("direct:kafka-publisher", exchange -> {
            exchange.getIn().setBody(SAMPLE_PAYMENT_XML);
            exchange.getIn().setHeader("ExpectedMessageType", "pacs.008");
            exchange.getIn().setHeader("ReceiptChannel", "REST_API");
            exchange.getIn().setHeader("PrimaryIdentifier", "MSG123456789");
            exchange.getIn().setHeader("IsValid", true);
            exchange.getIn().setHeader("IdempotenceChecked", true);
        });

        kafkaMock.assertIsSatisfied();

        // Verify Kafka topic was set correctly
        assertEquals("test-payments-pacs008", result.getIn().getHeader("KafkaTopic"));
        assertEquals("MSG123456789", result.getIn().getHeader("kafka.KEY"));
        assertEquals("KAFKA_PUBLISH", result.getIn().getHeader("ProcessingStage"));
    }
}