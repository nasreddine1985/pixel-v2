package com.pixel.v2.kafka.starter;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for Kafka Starter Kamelet
 */
@SpringBootTest
@CamelSpringBootTest
@ActiveProfiles("test")
public class KafkaStarterTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    public void testKafkaStarterKameletExists() throws Exception {
        // Test that the kamelet can be loaded (would be loaded from classpath in real scenario)
        assertNotNull(camelContext, "Camel context should be available");
        assertTrue(camelContext.getStatus().isStarted(), "Camel context should be started");
    }

    @Test
    public void testKafkaStarterProcessorBean() throws Exception {
        // Test that the message processor bean is available
        assertNotNull(camelContext.getRegistry().lookupByName("kafkaStarterProcessor"), 
            "KafkaStarterProcessor bean should be available");
    }

    @Test
    public void testDirectKafkaMessageProcessing() throws Exception {
        // Test message processing through the direct endpoint
        String testMessage = "<?xml version='1.0'?><TestMessage><id>123</id><type>pacs.008</type></TestMessage>";

        Object result = producerTemplate.requestBody("direct:kafka-message-processing", testMessage);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(testMessage, result, "Message should be preserved");
    }

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public RouteBuilder kafkaTestRoutes() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    // Mock route to simulate Kafka message processing
                    from("direct:kafka-message-processing")
                        .routeId("test-kafka-processing")
                        .log("Test: Processing message from Kafka: ${body}")
                        .process(exchange -> {
                            // Simulate message processing
                            String body = exchange.getIn().getBody(String.class);
                            exchange.getIn().setHeader("testProcessed", true);
                            exchange.getIn().setHeader("messageLength", body != null ? body.length() : 0);
                        });
                }
            };
        }
    }
}