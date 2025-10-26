package com.pixel.v2.ingestion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.pixel.v2.ingestion.processor.MessageMetadataEnrichmentProcessor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic Spring Boot integration tests for the Ingestion Application. These tests verify that the
 * Spring context can load without Camel components.
 */
@SpringBootTest(classes = IngestionApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.main.web-application-type=none",
                "logging.level.org.apache.camel=OFF"})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IngestionApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
        assertNotNull(applicationContext.getBean(IngestionApplication.class),
                "IngestionApplication bean should be loaded");
    }

    @Test
    void messageMetadataEnrichmentProcessorIsLoaded() {
        assertTrue(applicationContext.containsBean("messageMetadataEnrichmentProcessor"),
                "MessageMetadataEnrichmentProcessor bean should be available");
        MessageMetadataEnrichmentProcessor processor = applicationContext.getBean(
                "messageMetadataEnrichmentProcessor", MessageMetadataEnrichmentProcessor.class);
        assertNotNull(processor, "MessageMetadataEnrichmentProcessor should not be null");
    }
}
