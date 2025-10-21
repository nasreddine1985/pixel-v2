package com.pixel.v2.ingestion.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified Integration tests for the Payment Ingestion flow using the test routes
 * 
 * These tests work with the actual test route configuration that uses direct endpoints
 * instead of trying to mock kamelet endpoints that don't exist in the test setup.
 */
@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("test")
class SimplePaymentIngestionTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    private static final String VALID_PACS008_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
            <FIToFICstmrCdtTrf>
                <GrpHdr>
                    <MsgId>MSG123456789</MsgId>
                    <CreDtTm>2025-10-21T14:30:00</CreDtTm>
                    <NbOfTxs>1</NbOfTxs>
                    <IntrBkSttlmDt>2025-10-21</IntrBkSttlmDt>
                </GrpHdr>
                <CdtTrfTxInf>
                    <PmtId>
                        <InstrId>INSTR123456</InstrId>
                        <EndToEndId>E2E123456</EndToEndId>
                        <TxId>TXN123456</TxId>
                    </PmtId>
                    <IntrBkSttlmAmt Ccy="EUR">1000.00</IntrBkSttlmAmt>
                    <Dbtr>
                        <Nm>Debtor Name</Nm>
                        <PstlAdr>
                            <Ctry>FR</Ctry>
                        </PstlAdr>
                    </Dbtr>
                    <Cdtr>
                        <Nm>Creditor Name</Nm>
                        <PstlAdr>
                            <Ctry>DE</Ctry>
                        </PstlAdr>
                    </Cdtr>
                </CdtTrfTxInf>
            </FIToFICstmrCdtTrf>
        </Document>
        """;

    @Test
    @DisplayName("Complete successful payment processing flow")
    void testSuccessfulPaymentFlow() throws Exception {
        // Send message through the complete flow
        Exchange result = producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
            exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.123");
            exchange.getIn().setHeader("receiptChannel", "REST_API");
        });

        // Verify final message state - the test routes set these headers
        assertNotNull(result.getIn().getHeader("IngestionStartTime"));
        assertEquals("SUCCESS", result.getIn().getHeader("persistenceStatus"));
        assertEquals("TEST_ENRICHED_ID_67890", result.getIn().getHeader("persistedMessageId")); // Final ID is from enriched data persistence
        assertEquals("true", result.getIn().getHeader("IsValid"));
        assertEquals("false", result.getIn().getHeader("IsDuplicate"));
        assertEquals("KAFKA_PUBLISH", result.getIn().getHeader("ProcessingStage"));
        assertEquals("true", result.getIn().getHeader("MessagePersisted"));
        assertEquals("true", result.getIn().getHeader("EnrichedDataPersisted"));
        assertEquals("true", result.getIn().getHeader("CanProcess"));
    }

    @Test
    @DisplayName("Database persistence step")
    void testDatabasePersistence() throws Exception {
        Exchange result = producerTemplate.send("direct:database-persistence", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
        });

        assertEquals("DATABASE_PERSISTENCE", result.getIn().getHeader("ProcessingStage"));
        assertEquals("SUCCESS", result.getIn().getHeader("persistenceStatus"));
        assertEquals("TEST_ID_12345", result.getIn().getHeader("persistedMessageId"));
        assertEquals("true", result.getIn().getHeader("MessagePersisted"));
    }

    @Test
    @DisplayName("Reference enrichment step")
    void testReferenceEnrichment() throws Exception {
        Exchange result = producerTemplate.send("direct:reference-enrichment", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
        });

        assertEquals("REFERENCE_ENRICHMENT", result.getIn().getHeader("ProcessingStage"));
    }

    @Test
    @DisplayName("Enriched data persistence step")
    void testEnrichedDataPersistence() throws Exception {
        Exchange result = producerTemplate.send("direct:enriched-data-persistence", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
        });

        assertEquals("ENRICHED_DATA_PERSISTENCE", result.getIn().getHeader("ProcessingStage"));
        assertEquals("ENRICHED", result.getIn().getHeader("PersistenceType"));
        assertEquals("SUCCESS", result.getIn().getHeader("persistenceStatus"));
        assertEquals("TEST_ENRICHED_ID_67890", result.getIn().getHeader("persistedMessageId"));
        assertEquals("true", result.getIn().getHeader("EnrichedDataPersisted"));
    }

    @Test
    @DisplayName("Validation step")
    void testValidation() throws Exception {
        Exchange result = producerTemplate.send("direct:validation", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
        });

        assertEquals("VALIDATION", result.getIn().getHeader("ProcessingStage"));
        assertEquals("true", result.getIn().getHeader("IsValid"));
    }

    @Test
    @DisplayName("Idempotence check step")
    void testIdempotenceCheck() throws Exception {
        Exchange result = producerTemplate.send("direct:idempotence-check", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
        });

        assertEquals("IDEMPOTENCE", result.getIn().getHeader("ProcessingStage"));
        assertEquals("false", result.getIn().getHeader("IsDuplicate"));
        assertEquals("true", result.getIn().getHeader("CanProcess"));
    }

    @Test
    @DisplayName("Kafka publisher step")
    void testKafkaPublisher() throws Exception {
        Exchange result = producerTemplate.send("direct:kafka-publisher", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
        });

        assertEquals("KAFKA_PUBLISH", result.getIn().getHeader("ProcessingStage"));
        assertEquals("test-payments-processed", result.getIn().getHeader("KafkaTopic"));
        assertNotNull(result.getIn().getHeader("PublishTimestamp"));
    }

    @Test
    @DisplayName("Error handler step")
    void testErrorHandler() throws Exception {
        Exchange result = producerTemplate.send("direct:error-handler", exchange -> {
            exchange.getIn().setBody("Invalid message");
            exchange.getIn().setHeader("ErrorType", "VALIDATION_ERROR");
        });

        assertEquals("ERROR", result.getIn().getHeader("ProcessingStage"));
    }

    @Test
    @DisplayName("Rejection handler step")
    void testRejectionHandler() throws Exception {
        Exchange result = producerTemplate.send("direct:rejection-handler", exchange -> {
            exchange.getIn().setBody("Rejected message");
            exchange.getIn().setHeader("RejectionReason", "DUPLICATE_MESSAGE");
        });

        assertEquals("REJECTION", result.getIn().getHeader("ProcessingStage"));
        assertNotNull(result.getIn().getHeader("RejectionTimestamp"));
    }
}