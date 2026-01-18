package com.pixel.v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * PIXEL-V2 Camel Application
 * 
 * Standalone Spring Boot application with Apache Camel integration for PIXEL-V2 payment processing
 * flows.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.pixel.v2"})
@EntityScan(basePackages = {"com.pixel.v2.persistence.model", "com.pixel.v2.model"})
public class PixelCamelApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixelCamelApplication.class, args);
    }
}
