package com.pixel.v2.ingestion.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Test configuration to override Camel configuration for testing
 * This ensures that no YAML routes are loaded during tests
 */
@TestConfiguration
public class IngestionTestConfiguration {

    @Bean
    @Primary
    public CamelContext testCamelContext() {
        // Create a simple CamelContext without any routes
        DefaultCamelContext context = new DefaultCamelContext();
        context.setAutoStartup(false);
        return context;
    }
}