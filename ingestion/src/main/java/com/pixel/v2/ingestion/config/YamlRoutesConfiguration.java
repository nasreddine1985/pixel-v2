package com.pixel.v2.ingestion.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for YAML-based Camel Routes
 * 
 * This configuration enables YAML-based route definitions when the property
 * 'camel.routes.yaml.enabled' is set to true. When enabled, it prevents the Java-based
 * PaymentIngestionRouteBuilder from being loaded.
 * 
 * Usage: - Set camel.routes.yaml.enabled=true to use YAML routes - Set
 * camel.routes.yaml.enabled=false to use Java RouteBuilder - Default behavior uses Java
 * RouteBuilder for backward compatibility
 */
@Configuration
@Profile("!test") // Exclude during test execution to avoid conflicts with test mocks
@ConditionalOnProperty(name = "camel.routes.yaml.enabled", havingValue = "true")
public class YamlRoutesConfiguration {
    // This configuration class serves as a marker for YAML route activation
    // The actual routes are loaded from src/main/resources/camel/*.yaml files
}
