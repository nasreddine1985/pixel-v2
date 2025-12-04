package com.pixel.v2.kafka.starter.processor;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for KafkaStarterProcessor
 */
@ExtendWith(MockitoExtension.class)
public class KafkaStarterProcessorTest {

    private KafkaStarterProcessor processor;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        processor = new KafkaStarterProcessor();
        DefaultCamelContext camelContext = new DefaultCamelContext();
        exchange = new DefaultExchange(camelContext);
    }

    @Test
    void testProcessXmlMessage() throws Exception {
        // Given
        String xmlMessage = "<?xml version='1.0'?><pacs.008><FIToFICstmrCdtTrf><id>123</id></FIToFICstmrCdtTrf></pacs.008>";
        exchange.getIn().setBody(xmlMessage);
        exchange.getIn().setHeader("kafkaTopic", "payment-topic");
        exchange.getIn().setHeader("kafkaPartition", "0");
        exchange.getIn().setHeader("kafkaOffset", "1234");
        exchange.getIn().setHeader("kafkaKey", "msg-123");

        // When
        processor.process(exchange);

        // Then
        assertEquals("pacs.008", exchange.getIn().getHeader("detectedMessageType"));
        assertEquals(xmlMessage.length(), exchange.getIn().getHeader("messageLength"));
        assertEquals("RECEIVED_FROM_KAFKA", exchange.getIn().getHeader("processingStatus"));
        assertTrue((Boolean) exchange.getIn().getHeader("kafkaProcessed"));
        assertNotNull(exchange.getIn().getHeader("processingStartTime"));
        assertNotNull(exchange.getIn().getHeader("consumerProcessingTime"));
    }

    @Test
    void testProcessJsonMessage() throws Exception {
        // Given
        String jsonMessage = "{\"messageType\":\"payment\",\"amount\":1000.00,\"currency\":\"EUR\"}";
        exchange.getIn().setBody(jsonMessage);
        exchange.getIn().setHeader("kafkaTopic", "json-topic");
        exchange.getIn().setHeader("kafkaPartition", "1");
        exchange.getIn().setHeader("kafkaOffset", "5678");

        // When
        processor.process(exchange);

        // Then
        assertEquals("JSON_MESSAGE", exchange.getIn().getHeader("detectedMessageType"));
        assertEquals(jsonMessage.length(), exchange.getIn().getHeader("messageLength"));
        assertEquals("RECEIVED_FROM_KAFKA", exchange.getIn().getHeader("processingStatus"));
    }

    @Test
    void testProcessEmptyMessage() throws Exception {
        // Given
        exchange.getIn().setBody("");
        exchange.getIn().setHeader("kafkaTopic", "empty-topic");

        // When
        processor.process(exchange);

        // Then
        assertEquals("Empty message body received from Kafka", exchange.getIn().getHeader("processingError"));
        assertNull(exchange.getIn().getHeader("detectedMessageType"));
    }

    @Test
    void testProcessPan001Message() throws Exception {
        // Given
        String pan001Message = "<?xml version='1.0'?><pan.001><CstmrCdtTrfInitn><id>456</id></CstmrCdtTrfInitn></pan.001>";
        exchange.getIn().setBody(pan001Message);
        exchange.getIn().setHeader("kafkaTopic", "pan001-topic");

        // When
        processor.process(exchange);

        // Then
        assertEquals("pan.001", exchange.getIn().getHeader("detectedMessageType"));
        assertEquals("RECEIVED_FROM_KAFKA", exchange.getIn().getHeader("processingStatus"));
    }

    @Test
    void testProcessDelimitedMessage() throws Exception {
        // Given
        String delimitedMessage = "FIELD1|FIELD2|FIELD3|PAYMENT_DATA|1000.00|EUR";
        exchange.getIn().setBody(delimitedMessage);
        exchange.getIn().setHeader("kafkaTopic", "delimited-topic");

        // When
        processor.process(exchange);

        // Then
        assertEquals("DELIMITED_MESSAGE", exchange.getIn().getHeader("detectedMessageType"));
        assertEquals("RECEIVED_FROM_KAFKA", exchange.getIn().getHeader("processingStatus"));
    }

    @Test
    void testProcessUnknownMessage() throws Exception {
        // Given
        exchange.getIn().setBody(null);
        exchange.getIn().setHeader("kafkaTopic", "null-topic");

        // When
        processor.process(exchange);

        // Then
        assertEquals("Empty message body received from Kafka", exchange.getIn().getHeader("processingError"));
    }
}