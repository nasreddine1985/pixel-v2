package com.pixel.v2.validation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for XSD validation module
 */
@Configuration
@ComponentScan(basePackages = "com.pixel.v2.validation")
public class XsdValidationConfiguration {
    
    // Configuration class for component scanning
    // All beans will be discovered automatically via @Component annotation
    
}