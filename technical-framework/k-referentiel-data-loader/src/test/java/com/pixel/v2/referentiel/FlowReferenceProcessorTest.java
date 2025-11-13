package com.pixel.v2.referentiel;

import com.pixel.v2.referentiel.processor.FlowReferenceProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FlowReferenceProcessor
 */
class FlowReferenceProcessorTest {

    private FlowReferenceProcessor processor;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        processor = new FlowReferenceProcessor();
        exchange = new DefaultExchange(new DefaultCamelContext());
    }

    @Test
    void testProcessValidFlowReferenceResponse() throws Exception {
        // Arrange
        String flowCode = "PACS008";
        String serviceUrl = "http://localhost:8099";
        String jsonResponse = """
                {
                    "flowId": "FLOW_PACS008_001",
                    "flowCode": "PACS008",
                    "flowName": "PACS.008 Credit Transfer",
                    "status": "ACTIVE",
                    "flowType": "PAYMENT",
                    "railMode": "INSTANT",
                    "priority": 1,
                    "slaMaxLatencyMs": 5000,
                    "sourceChannel": "HTTP",
                    "sourceSystem": "BANK_A",
                    "sourceFormat": "XML",
                    "targetSystem": "CORE_BANKING",
                    "targetChannel": "KAFKA",
                    "splitEnabled": "TRUE",
                    "splitChunkSize": 500,
                    "concatEnabled": "FALSE",
                    "retentionInEnabled": "TRUE",
                    "retentionInDays": 90,
                    "shapingEnabled": "TRUE",
                    "shapingMaxTrxPerMin": 2000,
                    "piiLevel": "HIGH",
                    "encryptionRequired": "TRUE",
                    "version": "1.2.0",
                    "comments": "Production PACS.008 configuration"
                }
                """;

        exchange.getIn().setHeader("FlowCode", flowCode);
        exchange.getIn().setHeader("ServiceUrl", serviceUrl);
        exchange.getIn().setBody(jsonResponse);

        // Act
        processor.process(exchange);

        // Assert
        assertTrue(exchange.getIn().getHeader("FlowReferenceLoaded", Boolean.class));
        assertEquals("FLOW_PACS008_001", exchange.getIn().getHeader("FlowId"));
        assertEquals("PACS008", exchange.getIn().getHeader("FlowCode"));
        assertEquals("PACS.008 Credit Transfer", exchange.getIn().getHeader("FlowName"));
        assertEquals("ACTIVE", exchange.getIn().getHeader("FlowStatus"));
        assertEquals("INSTANT", exchange.getIn().getHeader("RailMode"));
        assertEquals(1, exchange.getIn().getHeader("Priority"));
        assertEquals(5000, exchange.getIn().getHeader("SlaMaxLatencyMs"));
        assertEquals("TRUE", exchange.getIn().getHeader("SplitEnabled"));
        assertEquals(500, exchange.getIn().getHeader("SplitChunkSize"));

        // Verify JSON body
        String resultBody = exchange.getIn().getBody(String.class);
        assertNotNull(resultBody);
        assertTrue(resultBody.contains("\"flowCode\":\"PACS008\""));
        assertTrue(resultBody.contains("\"flowName\":\"PACS.008 Credit Transfer\""));
        assertNotNull(exchange.getIn().getHeader("FlowReferenceLoadTime"));
    }

    @Test
    void testProcessEmptyResponse() throws Exception {
        // Arrange
        String flowCode = "UNKNOWN_FLOW";
        String serviceUrl = "http://localhost:8099";

        exchange.getIn().setHeader("FlowCode", flowCode);
        exchange.getIn().setHeader("ServiceUrl", serviceUrl);
        exchange.getIn().setBody("");

        // Act
        processor.process(exchange);

        // Assert - Should create default FlowReference
        assertFalse(exchange.getIn().getHeader("FlowReferenceLoaded", Boolean.class));
        assertEquals("DEFAULT_UNKNOWN_FLOW", exchange.getIn().getHeader("FlowId"));
        assertEquals("UNKNOWN_FLOW", exchange.getIn().getHeader("FlowCode"));
        assertEquals("Default Flow Configuration for UNKNOWN_FLOW",
                exchange.getIn().getHeader("FlowName"));
        assertEquals("ACTIVE", exchange.getIn().getHeader("FlowStatus"));
        assertEquals("STANDARD", exchange.getIn().getHeader("RailMode"));
        assertEquals(5, exchange.getIn().getHeader("Priority"));

        // Verify default JSON body
        String resultBody = exchange.getIn().getBody(String.class);
        assertNotNull(resultBody);
        assertTrue(resultBody.contains("\"flowCode\":\"UNKNOWN_FLOW\""));
        assertTrue(resultBody.contains("Default configuration - service unavailable"));
    }

    @Test
    void testProcessInvalidJsonResponse() throws Exception {
        // Arrange
        String flowCode = "PACS009";
        String serviceUrl = "http://localhost:8099";
        String invalidJson = "{ invalid json content";

        exchange.getIn().setHeader("FlowCode", flowCode);
        exchange.getIn().setHeader("ServiceUrl", serviceUrl);
        exchange.getIn().setBody(invalidJson);

        // Act
        processor.process(exchange);

        // Assert - Should create default FlowReference due to parse error
        assertFalse(exchange.getIn().getHeader("FlowReferenceLoaded", Boolean.class));
        assertEquals("DEFAULT_PACS009", exchange.getIn().getHeader("FlowId"));
        assertEquals("PACS009", exchange.getIn().getHeader("FlowCode"));
        assertNotNull(exchange.getIn().getHeader("FlowReferenceError"));

        // Verify fallback JSON body
        String resultBody = exchange.getIn().getBody(String.class);
        assertNotNull(resultBody);
        assertTrue(resultBody.contains("\"flowCode\":\"PACS009\""));
    }

    @Test
    void testProcessNullBody() throws Exception {
        // Arrange
        String flowCode = "PAIN001";
        String serviceUrl = "http://localhost:8099";

        exchange.getIn().setHeader("FlowCode", flowCode);
        exchange.getIn().setHeader("ServiceUrl", serviceUrl);
        exchange.getIn().setBody(null);

        // Act
        processor.process(exchange);

        // Assert - Should create default FlowReference
        assertFalse(exchange.getIn().getHeader("FlowReferenceLoaded", Boolean.class));
        assertEquals("DEFAULT_PAIN001", exchange.getIn().getHeader("FlowId"));
        assertEquals("PAIN001", exchange.getIn().getHeader("FlowCode"));
        assertEquals("Default Flow Configuration for PAIN001",
                exchange.getIn().getHeader("FlowName"));

        // Verify all expected headers are set
        assertNotNull(exchange.getIn().getHeader("SourceChannel"));
        assertNotNull(exchange.getIn().getHeader("TargetSystem"));
        assertNotNull(exchange.getIn().getHeader("SplitEnabled"));
        assertNotNull(exchange.getIn().getHeader("RetentionInEnabled"));
        assertNotNull(exchange.getIn().getHeader("ShapingEnabled"));
        assertNotNull(exchange.getIn().getHeader("EncryptionRequired"));
        assertNotNull(exchange.getIn().getHeader("FlowVersion"));
    }

    @Test
    void testDefaultFlowReferenceStructure() throws Exception {
        // Arrange
        String flowCode = "TEST_FLOW";
        exchange.getIn().setHeader("FlowCode", flowCode);
        exchange.getIn().setHeader("ServiceUrl", "http://test.com");
        exchange.getIn().setBody(null);

        // Act
        processor.process(exchange);

        // Assert all critical headers are present
        assertNotNull(exchange.getIn().getHeader("FlowId"));
        assertNotNull(exchange.getIn().getHeader("FlowCode"));
        assertNotNull(exchange.getIn().getHeader("FlowName"));
        assertNotNull(exchange.getIn().getHeader("FlowStatus"));
        assertNotNull(exchange.getIn().getHeader("FlowType"));
        assertNotNull(exchange.getIn().getHeader("RailMode"));
        assertNotNull(exchange.getIn().getHeader("Priority"));
        assertNotNull(exchange.getIn().getHeader("SlaMaxLatencyMs"));

        // Verify JSON structure
        String resultJson = exchange.getIn().getBody(String.class);
        assertNotNull(resultJson);
        assertTrue(resultJson.contains("\"flowId\""));
        assertTrue(resultJson.contains("\"flowCode\""));
        assertTrue(resultJson.contains("\"flowName\""));
        assertTrue(resultJson.contains("\"status\""));
        assertTrue(resultJson.contains("\"lastUpdate\""));
    }
}
