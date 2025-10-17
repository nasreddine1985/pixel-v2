package com.pixel.v2.flowpacs008.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestJmsConfig {

    @Bean
    @Primary
    public ConnectionFactory jmsConnectionFactory() {
        // Provide a mock JMS ConnectionFactory for testing
        return mock(ConnectionFactory.class);
    }
}
