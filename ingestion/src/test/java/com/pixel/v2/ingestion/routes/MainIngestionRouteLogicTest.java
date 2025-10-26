package com.pixel.v2.ingestion.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple unit test for main ingestion route logic using CamelTestSupport. This test focuses on core
 * routing behavior without Spring Boot complexity.
 */
class MainIngestionRouteLogicTest extends CamelTestSupport {

    private static final Logger logger = LoggerFactory.getLogger(MainIngestionRouteLogicTest.class);

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();

        // Add a simple test route that mimics the main orchestrator logic
        context.addRoutes(new org.apache.camel.builder.RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Main ingestion orchestrator logic
                from("direct:test-ingestion").log("Processing message: ${body}")
                        .setHeader("LogLevel", constant("INFO"))
                        .setHeader("LogSource", constant("ingestion")).to("mock:log-result")
                        .choice().when(header("ReceiptChannel").isEqualTo("CFT"))
                        .log("CFT message - routing to Kafka").to("mock:kafka-result").otherwise()
                        .log("HTTP/MQ message - routing to processing module")
                        .to("mock:processing-result");
            }
        });

        return context;
    }

    @Test
    @DisplayName("Test CFT Message Routing Logic")
    void testCftMessageRoutingLogic() throws Exception {
        // Given
        logger.info("Testing CFT message routing logic");

        MockEndpoint kafkaResult = getMockEndpoint("mock:kafka-result");
        MockEndpoint processingResult = getMockEndpoint("mock:processing-result");
        MockEndpoint logResult = getMockEndpoint("mock:log-result");

        kafkaResult.expectedMessageCount(1);
        processingResult.expectedMessageCount(0);
        logResult.expectedMessageCount(1);

        String testMessage = """
                {
                    "messageId": "CFT_TEST_001",
                    "messageType": "PACS.008"
                }""";

        Map<String, Object> headers = new HashMap<>();
        headers.put("MessageId", "CFT_TEST_001");
        headers.put("ReceiptChannel", "CFT");
        headers.put("MessageType", "PACS.008");

        // When
        template.sendBodyAndHeaders("direct:test-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(context);

        logger.info("✅ CFT message routing logic test passed");
    }

    @Test
    @DisplayName("Test HTTP Message Routing Logic")
    void testHttpMessageRoutingLogic() throws Exception {
        // Given
        logger.info("Testing HTTP message routing logic");

        MockEndpoint kafkaResult = getMockEndpoint("mock:kafka-result");
        MockEndpoint processingResult = getMockEndpoint("mock:processing-result");
        MockEndpoint logResult = getMockEndpoint("mock:log-result");

        kafkaResult.expectedMessageCount(0);
        processingResult.expectedMessageCount(1);
        logResult.expectedMessageCount(1);

        String testMessage = """
                {
                    "messageId": "HTTP_TEST_001",
                    "messageType": "PACS.009"
                }""";

        Map<String, Object> headers = new HashMap<>();
        headers.put("MessageId", "HTTP_TEST_001");
        headers.put("ReceiptChannel", "HTTP");
        headers.put("MessageType", "PACS.009");

        // When
        template.sendBodyAndHeaders("direct:test-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(context);

        logger.info("✅ HTTP message routing logic test passed");
    }

    @Test
    @DisplayName("Test MQ Message Routing Logic")
    void testMqMessageRoutingLogic() throws Exception {
        // Given
        logger.info("Testing MQ message routing logic");

        MockEndpoint kafkaResult = getMockEndpoint("mock:kafka-result");
        MockEndpoint processingResult = getMockEndpoint("mock:processing-result");
        MockEndpoint logResult = getMockEndpoint("mock:log-result");

        kafkaResult.expectedMessageCount(0);
        processingResult.expectedMessageCount(1);
        logResult.expectedMessageCount(1);

        String testMessage = """
                {
                    "messageId": "MQ_TEST_001",
                    "messageType": "PAIN.001"
                }""";

        Map<String, Object> headers = new HashMap<>();
        headers.put("MessageId", "MQ_TEST_001");
        headers.put("ReceiptChannel", "MQ");
        headers.put("MessageType", "PAIN.001");

        // When
        template.sendBodyAndHeaders("direct:test-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(context);

        logger.info("✅ MQ message routing logic test passed");
    }

    @Test
    @DisplayName("Test Message Header Propagation")
    void testMessageHeaderPropagation() throws Exception {
        // Given
        logger.info("Testing message header propagation");

        MockEndpoint kafkaResult = getMockEndpoint("mock:kafka-result");

        kafkaResult.expectedMessageCount(1);
        kafkaResult.expectedHeaderReceived("MessageId", "HEADER_TEST_001");
        kafkaResult.expectedHeaderReceived("ReceiptChannel", "CFT");
        kafkaResult.expectedHeaderReceived("MessageType", "CAMT.053");

        String testMessage = """
                {
                    "messageId": "HEADER_TEST_001",
                    "messageType": "CAMT.053"
                }""";

        Map<String, Object> headers = new HashMap<>();
        headers.put("MessageId", "HEADER_TEST_001");
        headers.put("ReceiptChannel", "CFT");
        headers.put("MessageType", "CAMT.053");
        headers.put("CustomHeader", "CustomValue");

        // When
        template.sendBodyAndHeaders("direct:test-ingestion", testMessage, headers);

        // Then
        MockEndpoint.assertIsSatisfied(context);

        // Verify custom headers are also propagated
        assertEquals("CustomValue",
                kafkaResult.getReceivedExchanges().get(0).getIn().getHeader("CustomHeader"));

        logger.info("✅ Message header propagation test passed");
    }
}
