package com.pixel.v2.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Ingestion Service Application
 * 
 * Spring Boot application that orchestrates the payment message ingestion flow
 * using Apache Camel and various kamelets for receipt, validation, and processing.
 */
@SpringBootApplication(scanBasePackages = {
    "com.pixel.v2.ingestion",
    "com.pixel.v2.apireceipt",
    "com.pixel.v2.mqreceipt", 
    "com.pixel.v2.filereceipt",
    "com.pixel.v2.refloader",
    "com.pixel.v2.ingestvalidation",
    "com.pixel.v2.idempotence"
})
public class IngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestionApplication.class, args);
    }
}