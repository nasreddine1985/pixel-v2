package com.pixel.v2.ingestion.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for main-ingestion-routes.yaml Tests the complete ingestion orchestration flow
 * including: - Main orchestrator routing logic - CFT vs HTTP/MQ intelligent routing - Database
 * persistence workflows - Reference data enrichment - Validation and idempotence checking - Kafka
 * publishing for batch processing - Processing module integration for real-time processing
 */
@CamelSpringBootTest
@SpringBootTest
@UseAdviceWith
@ActiveProfiles("integration-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MainIngestionRoutesIntegrationTest {

    private static final Logger logger =
            LoggerFactory.getLogger(MainIngestionRoutesIntegrationTest.class);

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    // Mock endpoints for testing
    @EndpointInject("mock:database-persistence-result")
    private MockEndpoint databasePersistenceResult;

    @EndpointInject("mock:reference-enrichment-result")
    private MockEndpoint referenceEnrichmentResult;

    @EndpointInject("mock:validation-result")
    private MockEndpoint validationResult;

    @EndpointInject("mock:idempotence-result")
    private MockEndpoint idempotenceResult;

    @EndpointInject("mock:kafka-publisher-result")
    private MockEndpoint kafkaPublisherResult;

    @EndpointInject("mock:processing-module-result")
    private MockEndpoint processingModuleResult;

    @EndpointInject("mock:log-tx-result")
    private MockEndpoint logTxResult;

    @BeforeEach
    void setUp() throws Exception {
        // Advice the routes to intercept kamelet calls and direct endpoints
        AdviceWith.adviceWith(camelContext, "payment-ingestion-orchestrator", routeBuilder -> {
            // Mock kamelet calls
            routeBuilder.interceptSendToEndpoint("kamelet:k-log-tx").skipSendToOriginalEndpoint()
                    .to("mock:log-tx-result");

            routeBuilder.interceptSendToEndpoint("kamelet:k-db-tx").skipSendToOriginalEndpoint()
                    .to("mock:database-persistence-result");

            routeBuilder.interceptSendToEndpoint("kamelet:k-referentiel-data-loader")
                    .skipSendToOriginalEndpoint().to("mock:reference-enrichment-result");

            // Mock direct endpoints
            routeBuilder.interceptSendToEndpoint("direct:database-persistence")
                    .skipSendToOriginalEndpoint().to("mock:database-persistence-result");

            routeBuilder.interceptSendToEndpoint("direct:reference-enrichment")
                    .skipSendToOriginalEndpoint().to("mock:reference-enrichment-result");

            routeBuilder.interceptSendToEndpoint("direct:validation").skipSendToOriginalEndpoint()
                    .to("mock:validation-result");

            routeBuilder.interceptSendToEndpoint("direct:idempotence-check")
                    .skipSendToOriginalEndpoint().to("mock:idempotence-result");

            routeBuilder.interceptSendToEndpoint("direct:kafka-publisher")
                    .skipSendToOriginalEndpoint().to("mock:kafka-publisher-result");

            routeBuilder.interceptSendToEndpoint("direct:processing-module")
                    .skipSendToOriginalEndpoint().to("mock:processing-module-result");
        });

        // Start the Camel context and routes
        camelContext.start();

        // When using @UseAdviceWith, routes need to be started explicitly
        camelContext.getRouteController().startAllRoutes();

        // Reset all mock endpoints
        MockEndpoint.resetMocks(camelContext);
    }

    @Test
    @DisplayName("Test CFT Message Routing - Should route to Kafka for batch processing")
    void testCftMessageRouting() throws Exception {
        // Given
        logger.info("Testing CFT message routing to Kafka for batch processing");

        // Setup expectations
        kafkaPublisherResult.expectedMessageCount(1);
        processingModuleResult.expectedMessageCount(0); // CFT should not go to processing module
        idempotenceResult.expectedMessageCount(1);
        idempotenceResult.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CanProcess", true);
        });

        // Prepare test message
        String testMessage = """
                {
                    "messageId": "CFT_TEST_001",
                    "messageType": "PACS.008",
                    "payload": "<?xml version='1.0'?><Document>...</Document>"
                }""";

        Map<String, Object> headers = createTestHeaders("CFT_TEST_001", "CFT", "PACS.008");

        // When
        producerTemplate.sendBodyAndHeaders("direct:payment-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(camelContext);

        // Verify CFT routing behavior
        assertEquals(1, kafkaPublisherResult.getReceivedCounter(),
                "CFT message should be routed to Kafka");
        assertEquals(0, processingModuleResult.getReceivedCounter(),
                "CFT message should NOT be routed to processing module");

        logger.info(
                "✅ CFT message routing test passed - message routed to Kafka for batch processing");
    }

    @Test
    @DisplayName("Test HTTP Message Routing - Should route to processing module for real-time processing")
    void testHttpMessageRouting() throws Exception {
        // Given
        logger.info("Testing HTTP message routing to processing module for real-time processing");

        // Setup expectations
        processingModuleResult.expectedMessageCount(1);
        kafkaPublisherResult.expectedMessageCount(0); // HTTP should not go to Kafka
        idempotenceResult.expectedMessageCount(1);
        idempotenceResult.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CanProcess", true);
        });

        // Prepare test message
        String testMessage = """
                {
                    "messageId": "HTTP_TEST_001",
                    "messageType": "PACS.009",
                    "payload": "<?xml version='1.0'?><Document>...</Document>"
                }""";

        Map<String, Object> headers = createTestHeaders("HTTP_TEST_001", "HTTP", "PACS.009");

        // When
        producerTemplate.sendBodyAndHeaders("direct:payment-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(camelContext);

        // Verify HTTP routing behavior
        assertEquals(1, processingModuleResult.getReceivedCounter(),
                "HTTP message should be routed to processing module");
        assertEquals(0, kafkaPublisherResult.getReceivedCounter(),
                "HTTP message should NOT be routed to Kafka");

        logger.info(
                "✅ HTTP message routing test passed - message routed to processing module for real-time processing");
    }

    @Test
    @DisplayName("Test MQ Message Routing - Should route to processing module for real-time processing")
    void testMqMessageRouting() throws Exception {
        // Given
        logger.info("Testing MQ message routing to processing module for real-time processing");

        // Setup expectations
        processingModuleResult.expectedMessageCount(1);
        kafkaPublisherResult.expectedMessageCount(0); // MQ should not go to Kafka
        idempotenceResult.expectedMessageCount(1);
        idempotenceResult.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CanProcess", true);
        });

        // Prepare test message
        String testMessage = """
                {
                    "messageId": "MQ_TEST_001",
                    "messageType": "PAIN.001",
                    "payload": "<?xml version='1.0'?><Document>...</Document>"
                }""";

        Map<String, Object> headers = createTestHeaders("MQ_TEST_001", "MQ", "PAIN.001");

        // When
        producerTemplate.sendBodyAndHeaders("direct:payment-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(camelContext);

        // Verify MQ routing behavior
        assertEquals(1, processingModuleResult.getReceivedCounter(),
                "MQ message should be routed to processing module");
        assertEquals(0, kafkaPublisherResult.getReceivedCounter(),
                "MQ message should NOT be routed to Kafka");

        logger.info(
                "✅ MQ message routing test passed - message routed to processing module for real-time processing");
    }

    @Test
    @DisplayName("Test Idempotence Rejection - Should not process duplicate messages")
    void testIdempotenceRejection() throws Exception {
        // Given
        logger.info("Testing idempotence rejection for duplicate messages");

        // Setup expectations - idempotence check returns false (duplicate detected)
        processingModuleResult.expectedMessageCount(0);
        kafkaPublisherResult.expectedMessageCount(0);
        idempotenceResult.expectedMessageCount(1);
        idempotenceResult.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CanProcess", false);
        });

        // Prepare test message
        String testMessage = """
                {
                    "messageId": "DUPLICATE_TEST_001",
                    "messageType": "PACS.008",
                    "payload": "<?xml version='1.0'?><Document>...</Document>"
                }""";

        Map<String, Object> headers = createTestHeaders("DUPLICATE_TEST_001", "HTTP", "PACS.008");

        // When
        producerTemplate.sendBodyAndHeaders("direct:payment-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(camelContext);

        // Verify no further processing occurs for duplicates
        assertEquals(0, processingModuleResult.getReceivedCounter(),
                "Duplicate message should not be processed");
        assertEquals(0, kafkaPublisherResult.getReceivedCounter(),
                "Duplicate message should not be published to Kafka");

        logger.info("✅ Idempotence rejection test passed - duplicate message not processed");
    }

    @Test
    @DisplayName("Test Complete Orchestration Flow - All stages should be executed in order")
    void testCompleteOrchestrationFlow() throws Exception {
        // Given
        logger.info("Testing complete ingestion orchestration flow");

        // Setup expectations for all stages
        databasePersistenceResult.expectedMessageCount(1);
        referenceEnrichmentResult.expectedMessageCount(1);
        validationResult.expectedMessageCount(1);
        idempotenceResult.expectedMessageCount(1);
        kafkaPublisherResult.expectedMessageCount(1);
        logTxResult.expectedMinimumMessageCount(1); // Multiple log entries expected

        // Configure idempotence to allow processing
        idempotenceResult.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CanProcess", true);
        });

        // Prepare test message
        String testMessage =
                """
                        {
                            "messageId": "COMPLETE_TEST_001",
                            "messageType": "PACS.008",
                            "payload": "<?xml version='1.0'?><Document xmlns='urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08'>...</Document>",
                            "amount": "1000.00",
                            "currency": "EUR"
                        }""";

        Map<String, Object> headers = createTestHeaders("COMPLETE_TEST_001", "CFT", "PACS.008");

        // When
        producerTemplate.sendBodyAndHeaders("direct:payment-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(camelContext);

        // Verify all orchestration stages were executed
        assertTrue(databasePersistenceResult.getReceivedCounter() >= 1,
                "Database persistence should be called");
        assertTrue(referenceEnrichmentResult.getReceivedCounter() >= 1,
                "Reference enrichment should be called");
        assertTrue(validationResult.getReceivedCounter() >= 1, "Validation should be called");
        assertTrue(idempotenceResult.getReceivedCounter() >= 1,
                "Idempotence check should be called");
        assertTrue(kafkaPublisherResult.getReceivedCounter() >= 1,
                "Kafka publisher should be called for CFT message");
        assertTrue(logTxResult.getReceivedCounter() >= 1,
                "Logging should occur throughout the process");

        logger.info("✅ Complete orchestration flow test passed - all stages executed successfully");
    }

    @Test
    @DisplayName("Test Message Header Propagation - Headers should be maintained through the flow")
    void testMessageHeaderPropagation() throws Exception {
        // Given
        logger.info("Testing message header propagation through orchestration flow");

        // Setup expectations
        idempotenceResult.expectedMessageCount(1);
        idempotenceResult.whenAnyExchangeReceived(exchange -> {
            exchange.getIn().setHeader("CanProcess", true);
        });
        kafkaPublisherResult.expectedMessageCount(1);

        // Prepare test message with specific headers
        String testMessage = """
                {
                    "messageId": "HEADER_TEST_001",
                    "messageType": "CAMT.053"
                }""";

        Map<String, Object> headers = createTestHeaders("HEADER_TEST_001", "CFT", "CAMT.053");
        headers.put("CustomHeader", "CustomValue");
        headers.put("OriginalTimestamp", System.currentTimeMillis());

        // When
        producerTemplate.sendBodyAndHeaders("direct:payment-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(camelContext);

        // Verify headers are propagated to the final endpoint
        assertNotNull(
                kafkaPublisherResult.getReceivedExchanges().get(0).getIn().getHeader("MessageId"));
        assertNotNull(kafkaPublisherResult.getReceivedExchanges().get(0).getIn()
                .getHeader("ReceiptChannel"));
        assertNotNull(kafkaPublisherResult.getReceivedExchanges().get(0).getIn()
                .getHeader("MessageType"));
        assertEquals("CustomValue", kafkaPublisherResult.getReceivedExchanges().get(0).getIn()
                .getHeader("CustomHeader"));
        assertNotNull(kafkaPublisherResult.getReceivedExchanges().get(0).getIn()
                .getHeader("OriginalTimestamp"));

        logger.info("✅ Message header propagation test passed - headers maintained through flow");
    }

    @Test
    @DisplayName("Test Error Handling - Should handle processing errors gracefully")
    void testErrorHandling() {
        // Given
        logger.info("Testing error handling in orchestration flow");

        // Setup expectations - simulate validation failure
        validationResult.expectedMessageCount(1);
        validationResult.whenExchangeReceived(1, exchange -> {
            throw new RuntimeException("Validation failed: Invalid message format");
        });

        // Prepare test message
        String testMessage = """
                {
                    "messageId": "ERROR_TEST_001",
                    "messageType": "INVALID"
                }""";

        Map<String, Object> headers = createTestHeaders("ERROR_TEST_001", "HTTP", "INVALID");

        // When & Then - expect exception to be thrown
        assertThrows(Exception.class, () -> {
            producerTemplate.sendBodyAndHeaders("direct:payment-ingestion", testMessage, headers);
        }, "Should throw exception for validation error");

        logger.info("✅ Error handling test passed - validation errors handled appropriately");
    }

    /**
     * Helper method to create standard test headers
     */
    private Map<String, Object> createTestHeaders(String messageId, String receiptChannel,
            String messageType) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("MessageId", messageId);
        headers.put("ReceiptChannel", receiptChannel);
        headers.put("MessageType", messageType);
        headers.put("CorrelationId", "CORR_" + messageId);
        headers.put("Timestamp", System.currentTimeMillis());
        return headers;
    }
}
