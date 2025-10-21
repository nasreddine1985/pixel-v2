package com.pixel.v2.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Database configuration for the persistence module
 */
@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration {
    
    // Configuration will be handled through application.properties
    // This class serves as a marker for component scanning
}