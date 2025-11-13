package com.pixel.v2.split.processor;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageSplitProcessor
 */
class MessageSplitProcessorTest {

    private MessageSplitProcessor processor;
    private DefaultCamelContext camelContext;

    @BeforeEach
    void setUp() {
        processor = new MessageSplitProcessor();
        camelContext = new DefaultCamelContext();
    }

    @Test
    @DisplayName("Should split empty collection into empty list")
    void testSplitEmptyCollection() throws Exception {
        // Given
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(Collections.emptyList());

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertTrue(result.isEmpty());
        assertEquals(0, exchange.getIn().getHeader("splitCount"));
        assertEquals(0, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should split single message into single group")
    void testSplitSingleMessage() throws Exception {
        // Given
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(Collections.singletonList("test-message"));

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("test-message", result.get(0).get(0));
        assertEquals(1, exchange.getIn().getHeader("splitCount"));
        assertEquals(1, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should split by size into fixed chunks")
    void testSplitBySize() throws Exception {
        // Given
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3", "msg4", "msg5");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        exchange.getIn().setHeader("size", "2");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(3, result.size()); // 3 chunks: [2, 2, 1]
        assertEquals(2, result.get(0).size());
        assertEquals(2, result.get(1).size());
        assertEquals(1, result.get(2).size());
        assertEquals("msg1", result.get(0).get(0));
        assertEquals("msg2", result.get(0).get(1));
        assertEquals("msg5", result.get(2).get(0));
        assertEquals(3, exchange.getIn().getHeader("splitCount"));
        assertEquals(5, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should split by correlation ID")
    void testSplitByCorrelation() throws Exception {
        // Given
        List<Map<String, String>> messages = Arrays.asList(
            createMessage("orderId", "order1", "item1"),
            createMessage("orderId", "order2", "item2"),
            createMessage("orderId", "order1", "item3"),
            createMessage("orderId", "order2", "item4")
        );
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        exchange.getIn().setHeader("correlationId", "orderId");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(2, result.size()); // 2 correlation groups
        assertEquals(2, result.get(0).size()); // order1 group
        assertEquals(2, result.get(1).size()); // order2 group
        assertEquals(2, exchange.getIn().getHeader("splitCount"));
        assertEquals(4, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should handle messages without correlation property")
    void testSplitWithMissingCorrelation() throws Exception {
        // Given
        List<Map<String, String>> messages = Arrays.asList(
            createMessage("orderId", "order1", "item1"),
            createMessage("customerId", "cust1", "item2"), // Different property
            createMessage("orderId", "order1", "item3")
        );
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        exchange.getIn().setHeader("correlationId", "orderId");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(2, result.size()); // order1 group + DEFAULT group
        assertEquals(2, exchange.getIn().getHeader("splitCount"));
        assertEquals(3, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should split individually when no properties specified")
    void testSplitIndividually() throws Exception {
        // Given
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        // No correlation or size headers

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(3, result.size()); // Each message in separate group
        assertEquals(1, result.get(0).size());
        assertEquals(1, result.get(1).size());
        assertEquals(1, result.get(2).size());
        assertEquals("msg1", result.get(0).get(0));
        assertEquals("msg2", result.get(1).get(0));
        assertEquals("msg3", result.get(2).get(0));
        assertEquals(3, exchange.getIn().getHeader("splitCount"));
        assertEquals(3, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should handle array input")
    void testSplitArray() throws Exception {
        // Given
        String[] messages = {"msg1", "msg2", "msg3"};
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        exchange.getIn().setHeader("size", "2");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(2, result.size()); // 2 chunks: [2, 1]
        assertEquals(2, result.get(0).size());
        assertEquals(1, result.get(1).size());
        assertEquals(2, exchange.getIn().getHeader("splitCount"));
        assertEquals(3, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should handle single object input (wrap in collection)")
    void testSplitSingleObject() throws Exception {
        // Given
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("single-message");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("single-message", result.get(0).get(0));
        assertEquals(1, exchange.getIn().getHeader("splitCount"));
        assertEquals(1, exchange.getIn().getHeader("originalSize"));
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void testSplitNullInput() throws Exception {
        // Given
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(null);

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle invalid size gracefully")
    void testSplitInvalidSize() throws Exception {
        // Given
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        exchange.getIn().setHeader("size", "invalid");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(3, result.size()); // Falls back to individual split
        assertEquals(3, exchange.getIn().getHeader("splitCount"));
    }

    @Test
    @DisplayName("Should handle zero size gracefully")
    void testSplitZeroSize() throws Exception {
        // Given
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        exchange.getIn().setHeader("size", "0");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(3, result.size()); // Falls back to individual split
        assertEquals(3, exchange.getIn().getHeader("splitCount"));
    }

    @Test
    @DisplayName("Should use exchange properties if headers not available")
    void testSplitWithExchangeProperties() throws Exception {
        // Given
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3", "msg4");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(messages);
        exchange.setProperty("size", "2");

        // When
        processor.process(exchange);

        // Then
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) exchange.getIn().getBody();
        assertEquals(2, result.size()); // 2 chunks: [2, 2]
        assertEquals(2, result.get(0).size());
        assertEquals(2, result.get(1).size());
        assertEquals(2, exchange.getIn().getHeader("splitCount"));
        assertEquals(4, exchange.getIn().getHeader("originalSize"));
    }

    /**
     * Helper method to create test messages
     */
    private Map<String, String> createMessage(String key, String value, String content) {
        Map<String, String> message = new HashMap<>();
        message.put(key, value);
        message.put("content", content);
        return message;
    }
}