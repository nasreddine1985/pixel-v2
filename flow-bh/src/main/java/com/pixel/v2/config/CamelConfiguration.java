package com.pixel.v2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Camel Configuration for PIXEL-V2 BH Application
 * 
 * Configures Camel components for Bahrain WPS integration.
 * Note: No ActiveMQ needed for BH flow - uses HTTP endpoints instead.
 */
@Configuration
public class CamelConfiguration {

    /**
     * UUID Generator bean for Camel routes
     */
    @Bean
    public org.apache.camel.spi.UuidGenerator uuidGenerator() {
        return new org.apache.camel.support.DefaultUuidGenerator();
    }
}
