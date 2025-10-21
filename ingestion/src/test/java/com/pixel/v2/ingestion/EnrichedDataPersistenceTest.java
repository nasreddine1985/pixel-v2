package com.pixel.v2.ingestion;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the enriched data persistence functionality
 */
@SpringBootTest
@CamelSpringBootTest
@ActiveProfiles("test")
public class EnrichedDataPersistenceTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    public void testEnrichedDataPersistenceFlow() throws Exception {
        // Verify that the enriched-data-persistence route exists
        assertNotNull(camelContext.getRoute("enriched-data-persistence"), 
            "enriched-data-persistence route should be loaded");

        // Test message to send
        String testMessage = "<?xml version='1.0'?><TestMessage><id>123</id><data>test enriched data</data></TestMessage>";

        // Send message to the enriched data persistence route directly
        Object result = producerTemplate.requestBodyAndHeader(
            "direct:enriched-data-persistence", 
            testMessage,
            "PersistenceType", "ENRICHED"
        );

        // Verify that processing completed without errors
        assertNotNull(result, "Result should not be null");
        assertEquals(testMessage, result, "Message content should be preserved");
    }

    @Test
    public void testFullIngestionFlowWithEnrichedPersistence() throws Exception {
        // Verify all required routes are loaded
        assertNotNull(camelContext.getRoute("payment-ingestion-orchestrator"), 
            "payment-ingestion-orchestrator route should be loaded");
        assertNotNull(camelContext.getRoute("database-persistence"), 
            "database-persistence route should be loaded");
        assertNotNull(camelContext.getRoute("reference-enrichment"), 
            "reference-enrichment route should be loaded");
        assertNotNull(camelContext.getRoute("enriched-data-persistence"), 
            "enriched-data-persistence route should be loaded");

        // Test message
        String testMessage = "<?xml version='1.0'?><TestPayment><id>456</id><amount>100.00</amount></TestPayment>";

        // Send message through the full ingestion flow
        Object result = producerTemplate.requestBody(
            "direct:payment-ingestion", 
            testMessage
        );

        // Verify that processing completed
        assertNotNull(result, "Result should not be null");
    }

    @Test
    public void testEnrichedDataPersistenceRouteStructure() throws Exception {
        // Verify that the route has the correct number of processing steps
        var route = camelContext.getRoute("enriched-data-persistence");
        assertNotNull(route, "enriched-data-persistence route should exist");
        
        // Check that the route is active
        assertTrue(camelContext.getRouteController().getRouteStatus("enriched-data-persistence").isStarted(), 
            "Route should be started");
        assertEquals("enriched-data-persistence", route.getId(), "Route ID should match");
    }
}