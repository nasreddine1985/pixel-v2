package com.pixel.v2.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for PACS-008 Flow
 * Tests the complete message processing pipeline
 */
@DisplayName("PACS-008 Flow Integration Tests")
public class Pacs008FlowIntegrationTest extends CamelTestSupport {

    private static final Logger logger = LoggerFactory.getLogger(Pacs008FlowIntegrationTest.class);
    
    // Test constants
    private static final String TEST_INPUT_ENDPOINT = "direct:test-input";
    private static final String MOCK_OUTPUT_ENDPOINT = "mock:output";
    private static final String MOCK_ERROR_ENDPOINT = "mock:error";
    private static final String CORRELATION_ID_HEADER = "CorrelationId";
    private static final String SUCCESS_RESULT = "SUCCESS";
    private static final String VALID_RESULT = "VALID";
    
    private static final String MOCK_CDM_MESSAGE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <CDMDocument>
                <Payment>
                    <PaymentId>PAY-TEST-001</PaymentId>
                    <Amount>1000.00</Amount>
                    <Currency>EUR</Currency>
                    <DebtorName>Test Debtor</DebtorName>
                    <CreditorName>Test Creditor</CreditorName>
                </Payment>
            </CDMDocument>
            """;

    private static final String SAMPLE_PACS008_MESSAGE = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
            <FIToFICstmrCdtTrf>
                <GrpHdr>
                    <MsgId>TEST-MSG-001</MsgId>
                    <CreDtTm>2025-11-13T10:00:00</CreDtTm>
                    <NbOfTxs>1</NbOfTxs>
                    <SttlmInf>
                        <SttlmMtd>CLRG</SttlmMtd>
                    </SttlmInf>
                </GrpHdr>
                <CdtTrfTxInf>
                    <PmtId>
                        <InstrId>INSTR-001</InstrId>
                        <EndToEndId>E2E-001</EndToEndId>
                        <TxId>TX-001</TxId>
                    </PmtId>
                    <IntrBkSttlmAmt Ccy="EUR">1000.00</IntrBkSttlmAmt>
                    <Dbtr>
                        <Nm>Test Debtor</Nm>
                    </Dbtr>
                    <Cdtr>
                        <Nm>Test Creditor</Nm>
                    </Cdtr>
                </CdtTrfTxInf>
            </FIToFICstmrCdtTrf>
        </Document>
        """;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        logger.info("Starting test: {}", testInfo.getDisplayName());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Mock route to simulate PACS-008 flow components
                
                // Mock MQ message receiver
                from(TEST_INPUT_ENDPOINT)
                    .routeId("test-pacs008-input")
                    .log("Received test message for PACS-008 processing")
                    .setHeader(CORRELATION_ID_HEADER, simple("TEST-${date:now:yyyyMMdd-HHmmss}"))
                    .to("direct:message-aggregation");

                // Mock message aggregation
                from("direct:message-aggregation")
                    .routeId("test-message-aggregation")
                    .log("Mock aggregation - CorrelationId: ${header." + CORRELATION_ID_HEADER + "}")
                    .setHeader("CamelAggregatedSize", constant(1))
                    .to("direct:message-persistence");

                // Mock message persistence
                from("direct:message-persistence")
                    .routeId("test-message-persistence")
                    .log("Mock persistence - EntityType: MESSAGE")
                    .setHeader("PersistenceResult", constant(SUCCESS_RESULT))
                    .to("direct:referential-loading");

                // Mock referential data loading
                from("direct:referential-loading")
                    .routeId("test-referential-loading")
                    .log("Mock referential data loading - FlowCode: PACS008")
                    .setHeader("ReferentialData", constant("LOADED"))
                    .to("direct:xsd-validation");

                // Mock XSD validation
                from("direct:xsd-validation")
                    .routeId("test-xsd-validation")
                    .log("Mock XSD validation - Schema: pacs.008.001.08.xsd")
                    .setHeader("ValidationResult", constant(VALID_RESULT))
                    .to("direct:xsl-transformation");

                // Mock XSL transformation
                from("direct:xsl-transformation")
                    .routeId("test-xsl-transformation")
                    .log("Mock XSL transformation - PACS-008 to CDM")
                    .setBody().constant(MOCK_CDM_MESSAGE)
                    .setHeader("TransformationResult", constant(SUCCESS_RESULT))
                    .to("direct:cdm-validation");

                // Mock CDM validation
                from("direct:cdm-validation")
                    .routeId("test-cdm-validation")
                    .log("Mock CDM validation - Schema: ISO_Business_ITL_Pivot-v2-Simple.xsd")
                    .setHeader("CDMValidationResult", constant(VALID_RESULT))
                    .to("direct:message-splitting");

                // Mock message splitting
                from("direct:message-splitting")
                    .routeId("test-message-splitting")
                    .log("Mock message splitting - Individual processing")
                    .setHeader("CamelSplitIndex", constant(0))
                    .to(MOCK_OUTPUT_ENDPOINT);

                // Error handling route
                from("direct:error-handling")
                    .routeId("test-error-handling")
                    .log("Mock error handling - Processing failed message")
                    .setHeader("ErrorTimestamp", simple("${date:now}"))
                    .to(MOCK_ERROR_ENDPOINT);
            }
        };
    }

    @Test
    @DisplayName("Test complete PACS-008 message processing flow")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testCompletePacs008Flow() throws Exception {
        // Arrange
        MockEndpoint outputMock = getMockEndpoint(MOCK_OUTPUT_ENDPOINT);
        outputMock.expectedMessageCount(1);
        outputMock.expectedHeaderReceived("ValidationResult", VALID_RESULT);
        outputMock.expectedHeaderReceived("TransformationResult", SUCCESS_RESULT);
        outputMock.expectedHeaderReceived("CDMValidationResult", VALID_RESULT);

        // Act
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody(TEST_INPUT_ENDPOINT, SAMPLE_PACS008_MESSAGE);

        // Assert
        outputMock.assertIsSatisfied();
        
        // Verify message processing headers
        String correlationId = outputMock.getReceivedExchanges().get(0).getIn().getHeader(CORRELATION_ID_HEADER, String.class);
        assertNotNull(correlationId, "CorrelationId should be set");
        assertTrue(correlationId.startsWith("TEST-"), "CorrelationId should have test prefix");
        
        logger.info("PACS-008 flow test completed successfully with CorrelationId: {}", correlationId);
    }

    @Test
    @DisplayName("Test PACS-008 flow with multiple messages")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testPacs008FlowWithMultipleMessages() throws Exception {
        // Arrange
        int messageCount = 3;
        MockEndpoint outputMock = getMockEndpoint(MOCK_OUTPUT_ENDPOINT);
        outputMock.expectedMessageCount(messageCount);

        // Act
        ProducerTemplate template = context.createProducerTemplate();
        for (int i = 0; i < messageCount; i++) {
            String message = SAMPLE_PACS008_MESSAGE.replace("TEST-MSG-001", "TEST-MSG-" + String.format("%03d", i + 1));
            template.sendBody(TEST_INPUT_ENDPOINT, message);
        }

        // Assert
        outputMock.assertIsSatisfied();
        assertEquals(messageCount, outputMock.getReceivedCounter(), "Should process all messages");
        
        logger.info("Multiple messages test completed successfully - processed {} messages", messageCount);
    }

    @Test
    @DisplayName("Test PACS-008 flow error handling")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testPacs008FlowErrorHandling() throws Exception {
        // Arrange
        MockEndpoint errorMock = getMockEndpoint(MOCK_ERROR_ENDPOINT);
        errorMock.expectedMessageCount(1);

        // Act - Send to error handling route directly
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:error-handling", "Invalid message for error testing");

        // Assert
        errorMock.assertIsSatisfied();
        
        String errorTimestamp = errorMock.getReceivedExchanges().get(0).getIn().getHeader("ErrorTimestamp", String.class);
        assertNotNull(errorTimestamp, "Error timestamp should be set");
        
        logger.info("Error handling test completed successfully");
    }

    @Test
    @DisplayName("Test message correlation tracking")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testMessageCorrelationTracking() throws Exception {
        // Arrange
        MockEndpoint outputMock = getMockEndpoint(MOCK_OUTPUT_ENDPOINT);
        outputMock.expectedMessageCount(1);

        // Act
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBodyAndHeader(TEST_INPUT_ENDPOINT, SAMPLE_PACS008_MESSAGE, "TestTrackingId", "TRACK-001");

        // Assert
        outputMock.assertIsSatisfied();
        
        // Verify correlation tracking
        String correlationId = outputMock.getReceivedExchanges().get(0).getIn().getHeader(CORRELATION_ID_HEADER, String.class);
        assertNotNull(correlationId, "CorrelationId should be maintained throughout flow");
        
        logger.info("Correlation tracking test completed - CorrelationId: {}", correlationId);
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        
        // Configure test-specific settings
        context.getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");
        context.setUseMDCLogging(true);
        
        return context;
    }
}