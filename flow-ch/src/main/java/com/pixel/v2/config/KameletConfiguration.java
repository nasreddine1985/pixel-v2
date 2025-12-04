package com.pixel.v2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pixel.v2.logging.processor.LogPersistenceProcessor;

/**
 * Configuration class to register technical framework processors and components
 * 
 * Since the technical framework modules don't have Spring dependencies,
 * we need to manually register their components as Spring beans
 * so they are discovered by Camel routes.
 */
@Configuration
public class KameletConfiguration {

    /**
     * Register Log Persistence Processor as a Spring bean
     */
    @Bean
    public LogPersistenceProcessor logPersistenceProcessor() {
        return new LogPersistenceProcessor();
    }

    /**
     * Note: K-Log Flow Summary and K-MQ Starter are now pure YAML kamelets
     * They are automatically discovered from the classpath resources/kamelets directory
     * No need to register them as Spring beans
     */
}