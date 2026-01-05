package com.pixel.v2.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson configuration for proper LocalDateTime handling.
 * 
 * This configuration ensures that Jackson can properly deserialize
 * Java 8 date/time types like LocalDateTime.
 */
@Configuration
public class JacksonConfig {

    @Bean("kDbObjectMapper")
    public ObjectMapper kDbObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule for LocalDateTime support
        mapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}