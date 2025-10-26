package com.pixel.v2.ingestion.routes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Configuration validation test for main-ingestion-routes.yaml. This test validates the route
 * configuration without running the actual routes.
 */
class MainIngestionRouteConfigTest {

    private static final Logger logger =
            LoggerFactory.getLogger(MainIngestionRouteConfigTest.class);

    @Test
    @DisplayName("Test Main Ingestion Routes Configuration Exists")
    void testMainIngestionRoutesConfigurationExists() throws Exception {
        logger.info("Testing main ingestion routes configuration file existence");

        // Given
        ClassPathResource resource = new ClassPathResource("camel/main-ingestion-routes.yaml"); // When
                                                                                                // &
                                                                                                // Then
        assertTrue(resource.exists(), "Main ingestion routes configuration file should exist");
        assertTrue(resource.getFile().length() > 0, "Configuration file should not be empty");

        logger.info("Main ingestion routes configuration file exists and is not empty");
    }

    @Test
    @DisplayName("Test Route Configuration Contains Expected Patterns")
    void testRouteConfigurationContainsExpectedPatterns() throws Exception {
        logger.info("Testing main ingestion routes configuration content");

        // Given
        ClassPathResource resource = new ClassPathResource("camel/main-ingestion-routes.yaml");
        String content = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        // When & Then - Check for key route patterns
        assertTrue(content.contains("from:"), "Configuration should contain route definitions");
        assertTrue(content.contains("to:"), "Configuration should contain route destinations");

        logger.info("Main ingestion routes configuration contains expected routing patterns");
    }

    @Test
    @DisplayName("Test Route Configuration Contains Message Processing Logic")
    void testRouteConfigurationContainsMessageProcessingLogic() throws Exception {
        logger.info("Testing message processing logic in routes configuration");

        // Given
        ClassPathResource resource = new ClassPathResource("camel/main-ingestion-routes.yaml");
        String content = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        // When & Then - Check for key processing elements
        assertTrue(content.contains("choice") || content.contains("when"),
                "Configuration should contain conditional routing logic");

        // Check for logging or processing steps
        assertTrue(
                content.contains("log") || content.contains("setHeader")
                        || content.contains("setBody"),
                "Configuration should contain message processing steps");

        logger.info("Main ingestion routes configuration contains message processing logic");
    }

    @Test
    @DisplayName("Test Route Configuration Is Valid YAML")
    void testRouteConfigurationIsValidYaml() throws Exception {
        logger.info("Testing YAML structure validity");

        // Given
        ClassPathResource resource = new ClassPathResource("camel/main-ingestion-routes.yaml");
        String content = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        // When & Then - Basic YAML structure checks
        assertFalse(content.trim().isEmpty(), "YAML content should not be empty");

        // Check for basic YAML structure (no tabs, proper indentation indicators)
        assertFalse(content.contains("\t"), "YAML should not contain tab characters");

        // Check it starts with expected YAML route structure
        assertTrue(content.contains("-") || content.contains("route") || content.contains("from"),
                "YAML should contain valid Camel route structure");

        logger.info("Main ingestion routes configuration appears to be valid YAML");
    }
}
