package com.pixel.v2.ingestion.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete Payment Ingestion flow
 * 
 * Tests the end-to-end integration from message receipt through database persistence,
 * validation, idempotence checking, and Kafka publishing.
 */
@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("test")
@UseAdviceWith
class PaymentIngestionIntegrationTest {

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

    private static final String INVALID_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <InvalidDocument>
            <InvalidContent>This is not a valid payment message</InvalidContent>
        </InvalidDocument>
        """;

    @BeforeEach
    void setup() throws Exception {
        // Mock all external dependencies
        mockDatabasePersistence();
        mockReferenceEnrichment();
        mockValidation();
        mockIdempotence();
        mockKafkaEndpoints();
        
        camelContext.start();
    }

    private void mockDatabasePersistence() throws Exception {
        AdviceWith.adviceWith(camelContext, "database-persistence", route -> {
            route.mockEndpoints("kamelet:k-database-transaction");
        });
    }

    private void mockReferenceEnrichment() throws Exception {
        AdviceWith.adviceWith(camelContext, "reference-enrichment", route -> {
            route.mockEndpoints("kamelet:k-referentiel-data-loader");
        });
    }

    private void mockValidation() throws Exception {
        AdviceWith.adviceWith(camelContext, "validation-step", route -> {
            route.mockEndpoints("kamelet:k-ingestion-technical-validation");
        });
    }

    private void mockIdempotence() throws Exception {
        AdviceWith.adviceWith(camelContext, "idempotence-check", route -> {
            route.mockEndpoints("kamelet:k-payment-idempotence-helper");
        });
    }

    private void mockKafkaEndpoints() throws Exception {
        AdviceWith.adviceWith(camelContext, "kafka-publisher", route -> {
            route.mockEndpoints("kafka:*");
        });
        
        AdviceWith.adviceWith(camelContext, "rejection-handler", route -> {
            route.mockEndpoints("kafka:*");
        });
        
        AdviceWith.adviceWith(camelContext, "error-handler", route -> {
            route.mockEndpoints("kafka:*");
        });
    }

    @Test
    @DisplayName("Complete successful payment processing flow")
    void testSuccessfulPaymentFlow() throws Exception {
        // Setup database persistence mock
        MockEndpoint databaseMock = camelContext.getEndpoint("mock:kamelet:k-database-transaction", MockEndpoint.class);
        databaseMock.expectedMessageCount(1);
        databaseMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("persistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("persistedMessageId", "DB12345");
            exchange.getIn().setHeader("MessagePersisted", true);
        });

        // Setup reference enrichment mock
        MockEndpoint referenceMock = camelContext.getEndpoint("mock:kamelet:k-referentiel-data-loader", MockEndpoint.class);
        referenceMock.expectedMessageCount(1);
        referenceMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("CmdMapping", "SEPA_CREDIT_TRANSFER");
            exchange.getIn().setHeader("Rail", "SEPA");
            exchange.getIn().setHeader("Mode", "INSTANT");
            exchange.getIn().setHeader("ExpectedMessageType", "pacs.008");
        });

        // Setup validation mock (success)
        MockEndpoint validationMock = camelContext.getEndpoint("mock:kamelet:k-ingestion-technical-validation", MockEndpoint.class);
        validationMock.expectedMessageCount(1);
        validationMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsValid", true);
            exchange.getIn().setHeader("ValidationResult", "PASSED");
            exchange.getIn().setHeader("PrimaryIdentifier", "MSG123456789");
        });

        // Setup idempotence mock (not duplicate)
        MockEndpoint idempotenceMock = camelContext.getEndpoint("mock:kamelet:k-payment-idempotence-helper", MockEndpoint.class);
        idempotenceMock.expectedMessageCount(1);
        idempotenceMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsDuplicate", false);
            exchange.getIn().setHeader("IdempotenceChecked", true);
            exchange.getIn().setHeader("CanProcess", true);
        });

        // Setup Kafka mock
        MockEndpoint kafkaMock = camelContext.getEndpoint("mock:kafka:test-payments-pacs008", MockEndpoint.class);
        kafkaMock.expectedMessageCount(1);

        // Send message through the complete flow
        Exchange result = producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
            exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.123");
            exchange.getIn().setHeader("receiptChannel", "REST_API");
        });

        // Verify all mocks were called
        databaseMock.assertIsSatisfied();
        referenceMock.assertIsSatisfied();
        validationMock.assertIsSatisfied();
        idempotenceMock.assertIsSatisfied();
        kafkaMock.assertIsSatisfied();

        // Verify final message state
        assertNotNull(result.getIn().getHeader("IngestionStartTime"));
        assertEquals("SUCCESS", result.getIn().getHeader("persistenceStatus"));
        assertEquals("DB12345", result.getIn().getHeader("persistedMessageId"));
        assertEquals("SEPA_CREDIT_TRANSFER", result.getIn().getHeader("CmdMapping"));
        assertEquals(true, result.getIn().getHeader("IsValid"));
        assertEquals(false, result.getIn().getHeader("IsDuplicate"));
    }

    @Test
    @DisplayName("Database persistence failure handling")
    void testDatabasePersistenceFailure() throws Exception {
        // Setup database persistence mock to fail
        MockEndpoint databaseMock = camelContext.getEndpoint("mock:kamelet:k-database-transaction", MockEndpoint.class);
        databaseMock.expectedMessageCount(1);
        databaseMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("persistenceStatus", "FAILED");
            exchange.getIn().setHeader("persistenceError", "Database connection timeout");
            exchange.getIn().setHeader("MessagePersisted", false);
        });

        // Setup error handler mock
        MockEndpoint errorMock = camelContext.getEndpoint("mock:kafka:test-payments-errors", MockEndpoint.class);
        errorMock.expectedMessageCount(1);

        // Other services should not be called due to early failure
        MockEndpoint referenceMock = camelContext.getEndpoint("mock:kamelet:k-referentiel-data-loader", MockEndpoint.class);
        referenceMock.expectedMessageCount(0);

        // Send message
        producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
            exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.123");
        });

        // Verify error handling
        databaseMock.assertIsSatisfied();
        errorMock.assertIsSatisfied();
        referenceMock.assertIsSatisfied(); // Should be 0
    }

    @Test
    @DisplayName("Validation failure routing to rejection handler")
    void testValidationFailure() throws Exception {
        // Setup successful database persistence
        MockEndpoint databaseMock = camelContext.getEndpoint("mock:kamelet:k-database-transaction", MockEndpoint.class);
        databaseMock.expectedMessageCount(1);
        databaseMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("persistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("persistedMessageId", "DB12345");
            exchange.getIn().setHeader("MessagePersisted", true);
        });

        // Setup reference enrichment
        MockEndpoint referenceMock = camelContext.getEndpoint("mock:kamelet:k-referentiel-data-loader", MockEndpoint.class);
        referenceMock.expectedMessageCount(1);
        referenceMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("CmdMapping", "UNKNOWN");
            exchange.getIn().setHeader("Rail", "UNKNOWN");
        });

        // Setup validation mock to fail
        MockEndpoint validationMock = camelContext.getEndpoint("mock:kamelet:k-ingestion-technical-validation", MockEndpoint.class);
        validationMock.expectedMessageCount(1);
        validationMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsValid", false);
            exchange.getIn().setHeader("ValidationResult", "Invalid XML structure");
            exchange.getIn().setHeader("RejectionReason", "VALIDATION_FAILED");
        });

        // Setup idempotence check (should still be called)
        MockEndpoint idempotenceMock = camelContext.getEndpoint("mock:kamelet:k-payment-idempotence-helper", MockEndpoint.class);
        idempotenceMock.expectedMessageCount(1);
        idempotenceMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsDuplicate", false);
            exchange.getIn().setHeader("CanProcess", false); // Should be false due to validation failure
        });

        // Setup rejection handler mock
        MockEndpoint rejectionMock = camelContext.getEndpoint("mock:kafka:test-payments-rejected", MockEndpoint.class);
        rejectionMock.expectedMessageCount(1);

        // Kafka publisher should not be called
        MockEndpoint kafkaMock = camelContext.getEndpoint("mock:kafka:test-payments-pacs008", MockEndpoint.class);
        kafkaMock.expectedMessageCount(0);

        // Send invalid message
        producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(INVALID_XML);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
        });

        // Verify routing to rejection handler
        databaseMock.assertIsSatisfied();
        referenceMock.assertIsSatisfied();
        validationMock.assertIsSatisfied();
        idempotenceMock.assertIsSatisfied();
        rejectionMock.assertIsSatisfied();
        kafkaMock.assertIsSatisfied(); // Should be 0
    }

    @Test
    @DisplayName("Duplicate message handling")
    void testDuplicateMessageHandling() throws Exception {
        // Setup successful database persistence
        MockEndpoint databaseMock = camelContext.getEndpoint("mock:kamelet:k-database-transaction", MockEndpoint.class);
        databaseMock.expectedMessageCount(1);
        databaseMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("persistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("persistedMessageId", "DB12345");
            exchange.getIn().setHeader("MessagePersisted", true);
        });

        // Setup reference enrichment
        MockEndpoint referenceMock = camelContext.getEndpoint("mock:kamelet:k-referentiel-data-loader", MockEndpoint.class);
        referenceMock.expectedMessageCount(1);
        referenceMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("CmdMapping", "SEPA_CREDIT_TRANSFER");
            exchange.getIn().setHeader("ExpectedMessageType", "pacs.008");
        });

        // Setup successful validation
        MockEndpoint validationMock = camelContext.getEndpoint("mock:kamelet:k-ingestion-technical-validation", MockEndpoint.class);
        validationMock.expectedMessageCount(1);
        validationMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsValid", true);
            exchange.getIn().setHeader("ValidationResult", "PASSED");
        });

        // Setup idempotence mock to detect duplicate
        MockEndpoint idempotenceMock = camelContext.getEndpoint("mock:kamelet:k-payment-idempotence-helper", MockEndpoint.class);
        idempotenceMock.expectedMessageCount(1);
        idempotenceMock.whenExchangeReceived(1, exchange -> {
            exchange.getIn().setHeader("IsDuplicate", true);
            exchange.getIn().setHeader("ShouldReject", true);
            exchange.getIn().setHeader("RejectionReason", "DUPLICATE_MESSAGE");
            exchange.getIn().setHeader("CanProcess", false);
        });

        // Setup rejection handler mock
        MockEndpoint rejectionMock = camelContext.getEndpoint("mock:kafka:test-payments-rejected", MockEndpoint.class);
        rejectionMock.expectedMessageCount(1);

        // Kafka publisher should not be called
        MockEndpoint kafkaMock = camelContext.getEndpoint("mock:kafka:test-payments-pacs008", MockEndpoint.class);
        kafkaMock.expectedMessageCount(0);

        // Send duplicate message
        producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
            exchange.getIn().setHeader("MessageId", "MSG123456789"); // Duplicate ID
        });

        // Verify duplicate handling
        databaseMock.assertIsSatisfied();
        referenceMock.assertIsSatisfied();
        validationMock.assertIsSatisfied();
        idempotenceMock.assertIsSatisfied();
        rejectionMock.assertIsSatisfied();
        kafkaMock.assertIsSatisfied(); // Should be 0
    }

    @Test
    @DisplayName("Message type routing to correct Kafka topic")
    void testMessageTypeRouting() throws Exception {
        // Setup successful flow
        setupSuccessfulFlow();

        // Test pacs.008 routing
        MockEndpoint pacs008Mock = camelContext.getEndpoint("mock:kafka:test-payments-pacs008", MockEndpoint.class);
        pacs008Mock.expectedMessageCount(1);

        MockEndpoint pan001Mock = camelContext.getEndpoint("mock:kafka:test-payments-pan001", MockEndpoint.class);
        pan001Mock.expectedMessageCount(0);

        // Send pacs.008 message
        producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(VALID_PACS008_XML);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
        });

        pacs008Mock.assertIsSatisfied();
        pan001Mock.assertIsSatisfied(); // Should be 0
    }

    private void setupSuccessfulFlow() throws Exception {
        // Helper method to setup all mocks for successful flow
        MockEndpoint databaseMock = camelContext.getEndpoint("mock:kamelet:k-database-transaction", MockEndpoint.class);
        databaseMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("persistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("persistedMessageId", "DB12345");
            exchange.getIn().setHeader("MessagePersisted", true);
        });

        MockEndpoint referenceMock = camelContext.getEndpoint("mock:kamelet:k-referentiel-data-loader", MockEndpoint.class);
        referenceMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CmdMapping", "SEPA_CREDIT_TRANSFER");
            exchange.getIn().setHeader("ExpectedMessageType", "pacs.008");
        });

        MockEndpoint validationMock = camelContext.getEndpoint("mock:kamelet:k-ingestion-technical-validation", MockEndpoint.class);
        validationMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("IsValid", true);
            exchange.getIn().setHeader("ValidationResult", "PASSED");
        });

        MockEndpoint idempotenceMock = camelContext.getEndpoint("mock:kamelet:k-payment-idempotence-helper", MockEndpoint.class);
        idempotenceMock.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("IsDuplicate", false);
            exchange.getIn().setHeader("CanProcess", true);
        });
    }
}