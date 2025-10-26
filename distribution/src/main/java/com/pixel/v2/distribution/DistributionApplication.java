package com.pixel.v2.distribution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Distribution Message Service Application
 * 
 * Spring Boot application that handles message distribution and routing.
 * Can consume messages from direct endpoints or Kafka topics, log them using k-log-tx,
 * and route them to appropriate destinations.
 */
@SpringBootApplication(scanBasePackages = {
    "com.pixel.v2.distribution",
    "com.pixel.v2.kafkareceiver",
    "com.pixel.v2.logtx"
})
public class DistributionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributionApplication.class, args);
    }
}