package com.pixel.v2.processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Message Processing Service Application
 * 
 * This Spring Boot application processes payment messages from Kafka topics
 * and routes them to appropriate CDM transformers based on message type.
 * 
 * The service listens to messages from k-kafka-message-receiver and uses
 * Apache Camel to route messages to:
 * - k-pacs-008-to-cdm transformer for pacs.008 messages
 * - k-pan-001-to-cdm transformer for pan.001 messages
 * 
 * @author Pixel V2 Team
 * @version 1.0.1-SNAPSHOT
 */
@SpringBootApplication
public class ProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessingApplication.class, args);
    }
}