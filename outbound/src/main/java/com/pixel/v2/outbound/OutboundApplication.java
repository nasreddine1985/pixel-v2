package com.pixel.v2.outbound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Outbound Message Service Application
 * 
 * Spring Boot application that handles outbound message processing and routing.
 * Can consume messages from direct endpoints or Kafka topics, log them using k-log-tx,
 * and route them to appropriate destinations.
 */
@SpringBootApplication(scanBasePackages = {
    "com.pixel.v2.outbound",
    "com.pixel.v2.kafkareceiver",
    "com.pixel.v2.logtx"
})
public class OutboundApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutboundApplication.class, args);
    }
}