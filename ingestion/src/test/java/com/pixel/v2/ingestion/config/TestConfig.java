package com.pixel.v2.ingestion.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.Mockito;

import jakarta.jms.ConnectionFactory;

/**
 * Test configuration for providing mock beans in test environment
 */
@TestConfiguration
@ActiveProfiles("test")
public class TestConfig {

    /**
     * Mock JMS ConnectionFactory for testing MQ kamelet
     */
    @Bean(name = "mqConnectionFactory")
    @Primary
    public ConnectionFactory mqConnectionFactory() {
        // Create a mock connection factory for testing
        return Mockito.mock(ConnectionFactory.class);
    }

    /**
     * Alternative bean name that might be referenced in kamelets
     */
    @Bean(name = "jmsConnectionFactory") 
    public ConnectionFactory jmsConnectionFactory() {
        return mqConnectionFactory();
    }
}