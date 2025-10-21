package com.pixel.v2.kafka.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Kafka Message Receiver Kamelet
 */
@SpringBootApplication
public class KafkaMessageReceiverApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaMessageReceiverApplication.class, args);
    }
}