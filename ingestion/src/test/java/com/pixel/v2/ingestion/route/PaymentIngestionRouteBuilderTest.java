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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentIngestionRouteBuilder
 * 
 * Tests individual route components in isolation to verify their behavior.
 */
@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("test")
@UseAdviceWith
class PaymentIngestionRouteBuilderTest {

    private static final String MOCK_DATABASE_ENDPOINT = "mock:kamelet:k-database-transaction";
    private static final String MOCK_REFERENCE_ENDPOINT = "mock:kamelet:k-referentiel-data-loader";
    private static final String MOCK_VALIDATION_ENDPOINT = "mock:kamelet:k-ingestion-technical-validation";
    private static final String MOCK_IDEMPOTENCE_ENDPOINT = "mock:kamelet:k-payment-idempotence-helper";
    
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAILED_STATUS = "FAILED";
    private static final String PERSISTED_ID = "DB12345";
    
    private static final String SAMPLE_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
            <FIToFICstmrCdtTrf>
                <GrpHdr>
                    <MsgId>MSG123456789</MsgId>
                    <CreDtTm>2025-10-21T14:30:00</CreDtTm>
                    <NbOfTxs>1</NbOfTxs>
                </GrpHdr>
            </FIToFICstmrCdtTrf>
        </Document>
        """;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @BeforeEach
    void setupMocks() throws Exception {
        // Mock all kamelet endpoints
        AdviceWith.adviceWith(camelContext, "database-persistence", 
            route -> route.mockEndpoints("kamelet:k-database-transaction"));
        
        AdviceWith.adviceWith(camelContext, "reference-enrichment", 
            route -> route.mockEndpoints("kamelet:k-referentiel-data-loader"));
        
        AdviceWith.adviceWith(camelContext, "validation-step", 
            route -> route.mockEndpoints("kamelet:k-ingestion-technical-validation"));
        
        AdviceWith.adviceWith(camelContext, "idempotence-check", 
            route -> route.mockEndpoints("kamelet:k-payment-idempotence-helper"));
        
        AdviceWith.adviceWith(camelContext, "kafka-publisher", 
            route -> route.mockEndpoints("kafka:*"));
        
        AdviceWith.adviceWith(camelContext, "rejection-handler", 
            route -> route.mockEndpoints("kafka:*"));
        
        AdviceWith.adviceWith(camelContext, "error-handler", 
            route -> route.mockEndpoints("kafka:*"));

        camelContext.start();
    }

    @Nested
    @DisplayName("Database Persistence Tests")
    class DatabasePersistenceTests {

        @Test
        @DisplayName("Successful database persistence")
        void testSuccessfulDatabasePersistence() throws Exception {
            MockEndpoint databaseMock = camelContext.getEndpoint(MOCK_DATABASE_ENDPOINT, MockEndpoint.class);
            databaseMock.expectedMessageCount(1);
            databaseMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("persistenceStatus", SUCCESS_STATUS);
                exchange.getIn().setHeader("persistedMessageId", PERSISTED_ID);
                exchange.getIn().setHeader("MessagePersisted", true);
            });

            Exchange result = producerTemplate.send("direct:database-persistence", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("messageSource", "HTTP_API");
            });

            databaseMock.assertIsSatisfied();
            assertEquals(SUCCESS_STATUS, result.getIn().getHeader("persistenceStatus"));
            assertEquals(PERSISTED_ID, result.getIn().getHeader("persistedMessageId"));
            assertEquals(true, result.getIn().getHeader("MessagePersisted"));
            assertEquals("DATABASE_PERSISTENCE", result.getIn().getHeader("ProcessingStage"));
        }

        @Test
        @DisplayName("Database persistence failure")
        void testDatabasePersistenceFailure() throws Exception {
            MockEndpoint databaseMock = camelContext.getEndpoint(MOCK_DATABASE_ENDPOINT, MockEndpoint.class);
            databaseMock.expectedMessageCount(1);
            databaseMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("persistenceStatus", FAILED_STATUS);
                exchange.getIn().setHeader("persistenceError", "Connection timeout");
                exchange.getIn().setHeader("MessagePersisted", false);
            });

            MockEndpoint errorMock = camelContext.getEndpoint("mock:kafka:test-payments-errors", MockEndpoint.class);
            errorMock.expectedMessageCount(1);

            Exchange result = producerTemplate.send("direct:database-persistence", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("messageSource", "HTTP_API");
            });

            databaseMock.assertIsSatisfied();
            errorMock.assertIsSatisfied();
            assertEquals(FAILED_STATUS, result.getIn().getHeader("persistenceStatus"));
            assertEquals(false, result.getIn().getHeader("MessagePersisted"));
            assertEquals("DATABASE_PERSISTENCE_FAILED", result.getIn().getHeader("RejectionReason"));
        }
    }

    @Nested
    @DisplayName("Reference Enrichment Tests")
    class ReferenceEnrichmentTests {

        @Test
        @DisplayName("Successful reference data enrichment")
        void testSuccessfulReferenceEnrichment() throws Exception {
            MockEndpoint referenceMock = camelContext.getEndpoint(MOCK_REFERENCE_ENDPOINT, MockEndpoint.class);
            referenceMock.expectedMessageCount(1);
            referenceMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("CmdMapping", "SEPA_CREDIT_TRANSFER");
                exchange.getIn().setHeader("Rail", "SEPA");
                exchange.getIn().setHeader("Mode", "INSTANT");
                exchange.getIn().setHeader("ExpectedMessageType", "pacs.008");
            });

            Exchange result = producerTemplate.send("direct:reference-enrichment", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("MessageId", "MSG123456789");
            });

            referenceMock.assertIsSatisfied();
            assertEquals("SEPA_CREDIT_TRANSFER", result.getIn().getHeader("CmdMapping"));
            assertEquals("SEPA", result.getIn().getHeader("Rail"));
            assertEquals("INSTANT", result.getIn().getHeader("Mode"));
            assertEquals("pacs.008", result.getIn().getHeader("ExpectedMessageType"));
            assertEquals("REFERENCE_ENRICHMENT", result.getIn().getHeader("ProcessingStage"));
        }

        @Test
        @DisplayName("Reference enrichment with unknown mapping")
        void testReferenceEnrichmentUnknownMapping() throws Exception {
            MockEndpoint referenceMock = camelContext.getEndpoint(MOCK_REFERENCE_ENDPOINT, MockEndpoint.class);
            referenceMock.expectedMessageCount(1);
            referenceMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("CmdMapping", "UNKNOWN");
                exchange.getIn().setHeader("Rail", "UNKNOWN");
                exchange.getIn().setHeader("Mode", "UNKNOWN");
            });

            Exchange result = producerTemplate.send("direct:reference-enrichment", exchange -> {
                exchange.getIn().setBody("<InvalidMessage/>");
                exchange.getIn().setHeader("MessageId", "INVALID123");
            });

            referenceMock.assertIsSatisfied();
            assertEquals("UNKNOWN", result.getIn().getHeader("CmdMapping"));
            assertEquals("UNKNOWN", result.getIn().getHeader("Rail"));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Successful message validation")
        void testSuccessfulValidation() throws Exception {
            MockEndpoint validationMock = camelContext.getEndpoint(MOCK_VALIDATION_ENDPOINT, MockEndpoint.class);
            validationMock.expectedMessageCount(1);
            validationMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("IsValid", true);
                exchange.getIn().setHeader("ValidationResult", "PASSED");
                exchange.getIn().setHeader("PrimaryIdentifier", "MSG123456789");
            });

            Exchange result = producerTemplate.send("direct:validation", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("MessageId", "MSG123456789");
            });

            validationMock.assertIsSatisfied();
            assertEquals(true, result.getIn().getHeader("IsValid"));
            assertEquals("PASSED", result.getIn().getHeader("ValidationResult"));
            assertEquals("MSG123456789", result.getIn().getHeader("PrimaryIdentifier"));
            assertEquals("VALIDATION", result.getIn().getHeader("ProcessingStage"));
        }

        @Test
        @DisplayName("Failed message validation")
        void testFailedValidation() throws Exception {
            MockEndpoint validationMock = camelContext.getEndpoint(MOCK_VALIDATION_ENDPOINT, MockEndpoint.class);
            validationMock.expectedMessageCount(1);
            validationMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("IsValid", false);
                exchange.getIn().setHeader("ValidationResult", "Missing required field: InstrId");
                exchange.getIn().setHeader("RejectionReason", "VALIDATION_FAILED");
            });

            Exchange result = producerTemplate.send("direct:validation", exchange -> {
                exchange.getIn().setBody("<InvalidXML/>");
                exchange.getIn().setHeader("MessageId", "INVALID123");
            });

            validationMock.assertIsSatisfied();
            assertEquals(false, result.getIn().getHeader("IsValid"));
            assertEquals("Missing required field: InstrId", result.getIn().getHeader("ValidationResult"));
            assertEquals("VALIDATION_FAILED", result.getIn().getHeader("RejectionReason"));
        }
    }

    @Nested
    @DisplayName("Idempotence Tests")
    class IdempotenceTests {

        @Test
        @DisplayName("Non-duplicate message processing")
        void testNonDuplicateMessage() throws Exception {
            MockEndpoint idempotenceMock = camelContext.getEndpoint(MOCK_IDEMPOTENCE_ENDPOINT, MockEndpoint.class);
            idempotenceMock.expectedMessageCount(1);
            idempotenceMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("IsDuplicate", false);
                exchange.getIn().setHeader("IdempotenceChecked", true);
            });

            Exchange result = producerTemplate.send("direct:idempotence-check", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("MessageId", "MSG123456789");
            });

            idempotenceMock.assertIsSatisfied();
            assertEquals(false, result.getIn().getHeader("IsDuplicate"));
            assertEquals(true, result.getIn().getHeader("IdempotenceChecked"));
            assertEquals("IDEMPOTENCE", result.getIn().getHeader("ProcessingStage"));
        }

        @Test
        @DisplayName("Duplicate message rejection")
        void testDuplicateMessageRejection() throws Exception {
            MockEndpoint idempotenceMock = camelContext.getEndpoint(MOCK_IDEMPOTENCE_ENDPOINT, MockEndpoint.class);
            idempotenceMock.expectedMessageCount(1);
            idempotenceMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("IsDuplicate", true);
                exchange.getIn().setHeader("ShouldReject", true);
                exchange.getIn().setHeader("RejectionReason", "DUPLICATE_MESSAGE");
                exchange.getIn().setHeader("CanProcess", false);
            });

            Exchange result = producerTemplate.send("direct:idempotence-check", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("MessageId", "DUPLICATE123");
            });

            idempotenceMock.assertIsSatisfied();
            assertEquals(true, result.getIn().getHeader("IsDuplicate"));
            assertEquals(true, result.getIn().getHeader("ShouldReject"));
            assertEquals("DUPLICATE_MESSAGE", result.getIn().getHeader("RejectionReason"));
            assertEquals(false, result.getIn().getHeader("CanProcess"));
        }

        @Test
        @DisplayName("Duplicate message with ignore action")
        void testDuplicateMessageIgnore() throws Exception {
            MockEndpoint idempotenceMock = camelContext.getEndpoint(MOCK_IDEMPOTENCE_ENDPOINT, MockEndpoint.class);
            idempotenceMock.expectedMessageCount(1);
            idempotenceMock.whenExchangeReceived(1, exchange -> {
                exchange.getIn().setHeader("IsDuplicate", true);
                exchange.getIn().setHeader("ShouldIgnore", true);
                exchange.getIn().setHeader("CanProcess", false);
            });

            Exchange result = producerTemplate.send("direct:idempotence-check", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("MessageId", "DUPLICATE456");
            });

            idempotenceMock.assertIsSatisfied();
            assertEquals(true, result.getIn().getHeader("IsDuplicate"));
            assertEquals(true, result.getIn().getHeader("ShouldIgnore"));
            assertEquals(false, result.getIn().getHeader("CanProcess"));
        }
    }

    @Nested
    @DisplayName("Kafka Publisher Tests")
    class KafkaPublisherTests {

        @Test
        @DisplayName("Publish PACS.008 message to correct topic")
        void testPacs008TopicRouting() throws Exception {
            MockEndpoint kafkaMock = camelContext.getEndpoint("mock:kafka:test-payments-pacs008", MockEndpoint.class);
            kafkaMock.expectedMessageCount(1);

            Exchange result = producerTemplate.send("direct:kafka-publisher", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("ExpectedMessageType", "pacs.008");
                exchange.getIn().setHeader("ReceiptChannel", "REST_API");
                exchange.getIn().setHeader("PrimaryIdentifier", "MSG123456789");
                exchange.getIn().setHeader("IsValid", true);
                exchange.getIn().setHeader("IdempotenceChecked", true);
            });

            kafkaMock.assertIsSatisfied();
            assertEquals("test-payments-pacs008", result.getIn().getHeader("KafkaTopic"));
            assertEquals("MSG123456789", result.getIn().getHeader("kafka.KEY"));
            assertEquals("KAFKA_PUBLISH", result.getIn().getHeader("ProcessingStage"));
            assertNotNull(result.getIn().getHeader("PublishTimestamp"));
        }

        @Test
        @DisplayName("Publish PAN.001 message to correct topic")
        void testPan001TopicRouting() throws Exception {
            MockEndpoint kafkaMock = camelContext.getEndpoint("mock:kafka:test-payments-pan001", MockEndpoint.class);
            kafkaMock.expectedMessageCount(1);

            Exchange result = producerTemplate.send("direct:kafka-publisher", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("ExpectedMessageType", "pan.001");
                exchange.getIn().setHeader("ReceiptChannel", "MQ_SERIES");
                exchange.getIn().setHeader("PrimaryIdentifier", "PAN123456789");
            });

            kafkaMock.assertIsSatisfied();
            assertEquals("test-payments-pan001", result.getIn().getHeader("KafkaTopic"));
            assertEquals("PAN123456789", result.getIn().getHeader("kafka.KEY"));
        }

        @Test
        @DisplayName("Publish unknown message type to default topic")
        void testDefaultTopicRouting() throws Exception {
            MockEndpoint kafkaMock = camelContext.getEndpoint("mock:kafka:test-payments-processed", MockEndpoint.class);
            kafkaMock.expectedMessageCount(1);

            Exchange result = producerTemplate.send("direct:kafka-publisher", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("ExpectedMessageType", "unknown.type");
                exchange.getIn().setHeader("ReceiptChannel", "FILE_CFT");
            });

            kafkaMock.assertIsSatisfied();
            assertEquals("test-payments-processed", result.getIn().getHeader("KafkaTopic"));
        }
    }

    @Nested
    @DisplayName("Error and Rejection Handler Tests")
    class ErrorHandlerTests {

        @Test
        @DisplayName("Rejection handler processing")
        void testRejectionHandler() throws Exception {
            MockEndpoint rejectionMock = camelContext.getEndpoint("mock:kafka:test-payments-rejected", MockEndpoint.class);
            rejectionMock.expectedMessageCount(1);

            Exchange result = producerTemplate.send("direct:rejection-handler", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("RejectionReason", "VALIDATION_FAILED");
                exchange.getIn().setHeader("ReceiptChannel", "REST_API");
                exchange.getIn().setHeader("PrimaryIdentifier", "MSG123456789");
                exchange.getIn().setHeader("ValidationResult", "Missing required field");
            });

            rejectionMock.assertIsSatisfied();
            assertEquals("REJECTION", result.getIn().getHeader("ProcessingStage"));
            assertNotNull(result.getIn().getHeader("RejectionTimestamp"));
        }

        @Test
        @DisplayName("Error handler processing")
        void testErrorHandler() throws Exception {
            MockEndpoint errorMock = camelContext.getEndpoint("mock:kafka:test-payments-errors", MockEndpoint.class);
            errorMock.expectedMessageCount(1);

            Exchange result = producerTemplate.send("direct:error-handler", exchange -> {
                exchange.getIn().setBody(SAMPLE_XML);
                exchange.getIn().setHeader("ErrorMessage", "Database connection failed");
                exchange.getIn().setHeader("ReceiptChannel", "MQ_SERIES");
                exchange.getIn().setHeader("ProcessingStage", "DATABASE_PERSISTENCE");
            });

            errorMock.assertIsSatisfied();
            assertEquals("ERROR", result.getIn().getHeader("ProcessingStage"));
        }
    }
}