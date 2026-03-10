package com.pixel.v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * PIXEL-V2 BH (Bahrain) Application
 * Wages Protection System (WPS) Payment Processing
 * 
 * This application handles Bahrain WPS payment flows including:
 * - Account validation requests from WPS Benefit
 * - Payroll file processing
 * - Integration with ATLAS2 for account validation
 * - Integration with DOME for payment processing
 * - EFTS (Bahrain) settlement processing
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.pixel.v2"})
public class PixelBhApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixelBhApplication.class, args);
    }
}
