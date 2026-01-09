package com.pixel.v2.common.headers;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.pixel.v2.common.headers.HeaderConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour HeaderUtils
 */
public class HeaderUtilsTest {

    private Exchange exchange;

    @BeforeEach
    void setUp() {
        exchange = new DefaultExchange(new DefaultCamelContext());
    }

    @Test
    void testSetAndGetFlowDataJson() {
        String testJson = "{\"flow\": {\"FlowID\": \"123\"}}";
        
        HeaderUtils.setFlowDataJson(exchange, testJson);
        String retrieved = HeaderUtils.getFlowDataJson(exchange);
        
        assertEquals(testJson, retrieved);
    }

    @Test
    void testGetFlowDataJsonWithFallback() {
        String testJson = "{\"flow\": {\"FlowID\": \"123\"}}";
        
        // Test avec REF_FLOW_DATA_JSON
        exchange.getIn().setHeader(REF_FLOW_DATA_JSON, testJson);
        String retrieved = HeaderUtils.getFlowDataJson(exchange);
        
        assertEquals(testJson, retrieved);
    }

    @Test
    void testSetAndGetProcessingMode() {
        HeaderUtils.setProcessingMode(exchange, PROCESSING_MODE_BATCH);
        String retrieved = HeaderUtils.getProcessingMode(exchange);
        
        assertEquals(PROCESSING_MODE_BATCH, retrieved);
    }

    @Test
    void testGetProcessingModeWithDefault() {
        // Sans header défini, doit retourner la valeur par défaut
        String retrieved = HeaderUtils.getProcessingMode(exchange);
        
        assertEquals(PROCESSING_MODE_NORMAL, retrieved);
    }

    @Test
    void testGenerateFlowOccurId() {
        String occurId = HeaderUtils.generateFlowOccurId("TEST");
        
        assertNotNull(occurId);
        assertTrue(occurId.startsWith("TEST-"));
        assertTrue(occurId.length() > 10); // Préfixe + timestamp
    }

    @Test
    void testGenerateMessageId() {
        String messageId = HeaderUtils.generateMessageId("MSG");
        
        assertNotNull(messageId);
        assertTrue(messageId.startsWith("MSG-"));
        assertTrue(messageId.contains("-")); // UUID contient des tirets
    }

    @Test
    void testGenerateTimestamp() {
        String timestamp = HeaderUtils.generateTimestamp();
        
        assertNotNull(timestamp);
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
    }

    @Test
    void testSetStandardProcessingHeaders() {
        HeaderUtils.setStandardProcessingHeaders(exchange, "FLOW", "MSG", PROCESSING_MODE_BATCH);
        
        assertNotNull(HeaderUtils.getFlowOccurId(exchange));
        assertNotNull(HeaderUtils.getMessageId(exchange));
        assertNotNull(HeaderUtils.getCorrelationId(exchange));
        assertNotNull(HeaderUtils.getReceivedTimestamp(exchange));
        assertEquals(PROCESSING_MODE_BATCH, HeaderUtils.getProcessingMode(exchange));
        assertEquals(BUSINESS_STATUS_PROCESSING, HeaderUtils.getBusinessStatus(exchange));
        assertEquals(TECHNICAL_STATUS_ACTIVE, HeaderUtils.getTechnicalStatus(exchange));
    }

    @Test
    void testHeaderConstantsValues() {
        // Vérifier que les constantes ont les bonnes valeurs
        assertEquals("FlowDataJson", FLOW_DATA_JSON);
        assertEquals("flowDataJson", FLOW_DATA_JSON_LC);
        assertEquals("RefFlowDataJson", REF_FLOW_DATA_JSON);
        assertEquals("techPivotXml", TECH_PIVOT_XML);
        assertEquals("NORMAL", PROCESSING_MODE_NORMAL);
        assertEquals("BATCH", PROCESSING_MODE_BATCH);
        assertEquals("generate", OPERATION_GENERATE);
        assertEquals("update", OPERATION_UPDATE);
    }

}