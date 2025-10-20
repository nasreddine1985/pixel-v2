package com.pixel.v2.idempotence.config;

import com.pixel.v2.idempotence.processor.IdempotenceProcessor;
import com.pixel.v2.idempotence.repository.IdempotenceRepository;
import com.pixel.v2.idempotence.repository.impl.InMemoryIdempotenceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for k-idempotence kamelet components
 */
@Configuration
public class IdempotenceConfiguration {
    
    /**
     * Register IdempotenceRepository as a Spring bean
     * Default to in-memory implementation
     */
    @Bean("IdempotenceRepository")
    public IdempotenceRepository idempotenceRepository() {
        return new InMemoryIdempotenceRepository();
    }
    
    /**
     * Register IdempotenceProcessor as a Spring bean
     * so it can be referenced in Camel routes
     */
    @Bean("IdempotenceProcessor")
    public IdempotenceProcessor idempotenceProcessor() {
        IdempotenceProcessor processor = new IdempotenceProcessor(idempotenceRepository());
        
        // Configure default settings
        processor.setDuplicateAction("ERROR");
        processor.setEnableHashing(false);
        processor.setTrackMessageHash(true);
        
        return processor;
    }
}