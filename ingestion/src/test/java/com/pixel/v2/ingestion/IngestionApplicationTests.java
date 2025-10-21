package com.pixel.v2.ingestion;

import com.pixel.v2.ingestion.config.TestConfig;
import com.pixel.v2.ingestion.route.PaymentIngestionRouteBuilder;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Payment Ingestion Application
 */
@SpringBootTest(classes = {IngestionApplication.class, TestConfig.class})
@ComponentScan(excludeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE, 
    classes = {PaymentIngestionRouteBuilder.class}
))
@ActiveProfiles("test")
class IngestionApplicationTests {

    @Autowired
    private CamelContext camelContext;

    @Test
    void contextLoads() {
        // Test that the Spring application context loads successfully
        assertNotNull(camelContext, "CamelContext should be loaded");
        assertTrue(camelContext.getStatus().isStarted(), "CamelContext should be started");
    }

    @Test
    void testRequiredRoutesAreLoaded() {
        // Verify that all required routes are loaded (test version)
        assertNotNull(camelContext.getRoute("payment-ingestion-orchestrator"), 
            "Payment ingestion orchestrator route should be loaded");
        assertNotNull(camelContext.getRoute("database-persistence"), 
            "Database persistence route should be loaded");
        assertNotNull(camelContext.getRoute("reference-enrichment"), 
            "Reference enrichment route should be loaded");
        assertNotNull(camelContext.getRoute("validation-step"), 
            "Validation step route should be loaded");
        assertNotNull(camelContext.getRoute("idempotence-check"), 
            "Idempotence check route should be loaded");
        assertNotNull(camelContext.getRoute("kafka-publisher"), 
            "Kafka publisher route should be loaded");
        assertNotNull(camelContext.getRoute("rejection-handler"), 
            "Rejection handler route should be loaded");
        assertNotNull(camelContext.getRoute("error-handler"), 
            "Error handler route should be loaded");
    }

    @Test
    void testReceiverRoutesAreLoaded() {
        // Verify that test receiver routes are loaded (no kamelets in test)
        assertNotNull(camelContext.getRoute("http-receipt-route"), 
            "HTTP receipt route should be loaded");
        assertNotNull(camelContext.getRoute("file-receipt-route"), 
            "File receipt route should be loaded");
        // Note: MQ route is not loaded in test profile to avoid JMS dependencies
    }

    @Test
    void testApplicationHealth() {
        // Test basic application health indicators
        assertFalse(camelContext.getRoutes().isEmpty(), 
            "Application should have routes configured");
        
        // Test that direct component is available for our test routes
        assertNotNull(camelContext.getComponent("direct"), 
            "Direct component should be available");
    }
}