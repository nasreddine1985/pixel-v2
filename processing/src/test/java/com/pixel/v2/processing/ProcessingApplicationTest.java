package com.pixel.v2.processing;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for Payment Message Processing Service
 */
@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class ProcessingApplicationTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject("mock:cdm-output")
    private MockEndpoint mockCdmOutput;

    @EndpointInject("mock:error-handling")
    private MockEndpoint mockErrorHandling;

    @Test
    void testApplicationStartsSuccessfully() {
        // Verify that the Camel context is running
        assert camelContext.getStatus().isStarted();
    }

    @Test
    void testHealthCheckRoute() throws Exception {
        // When
        String response = producerTemplate.requestBody("direct:health-check", null, String.class);

        // Then
        assert response.contains("UP");
        assert response.contains("payment-message-processing");
    }

    @Test
    void testMetricsRoute() throws Exception {
        // When
        String response = producerTemplate.requestBody("direct:metrics", null, String.class);

        // Then
        assert response.contains("processedMessages");
        assert response.contains("timestamp");
    }

    // Note: Full integration tests would require the transformer kamelets to be available
    // This test class focuses on application startup and basic route functionality
}