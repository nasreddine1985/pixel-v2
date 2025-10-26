package com.pixel.v2.business.processors;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageTypeProcessor
 */
class MessageTypeProcessorTest {

    private MessageTypeProcessor processor;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        processor = new MessageTypeProcessor();
        exchange = new DefaultExchange(new DefaultCamelContext());
    }

    @Test
    void testProcessPacs008XmlMessage() throws Exception {
        // Given
        String pacs008Message = "<?xml version=\"1.0\"?>\n" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02\">\n" +
                "  <FIToFICstmrCdtTrf>\n" +
                "    <GrpHdr>\n" +
                "      <MsgId>MSG001</MsgId>\n" +
                "    </GrpHdr>\n" +
                "  </FIToFICstmrCdtTrf>\n" +
                "</Document>";
        
        exchange.getIn().setBody(pacs008Message);

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.PACS_008_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.PACS_008_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
        assertNotNull(exchange.getIn().getHeader("ProcessingTimestamp"));
        assertEquals("MessageTypeProcessor", exchange.getIn().getHeader("ProcessedBy"));
    }

    @Test
    void testProcessPacs008JsonMessage() throws Exception {
        // Given
        String pacs008JsonMessage = "{\n" +
                "  \"pacs008\": {\n" +
                "    \"FIToFICstmrCdtTrf\": {\n" +
                "      \"GrpHdr\": {\n" +
                "        \"MsgId\": \"MSG001\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        exchange.getIn().setBody(pacs008JsonMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.PACS_008_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.PACS_008_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
    }

    @Test
    void testProcessPan001XmlMessage() throws Exception {
        // Given
        String pan001Message = "<?xml version=\"1.0\"?>\n" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pan.001.001.03\">\n" +
                "  <CstmrPmtStsRpt>\n" +
                "    <GrpHdr>\n" +
                "      <MsgId>MSG002</MsgId>\n" +
                "    </GrpHdr>\n" +
                "  </CstmrPmtStsRpt>\n" +
                "</Document>";
        
        exchange.getIn().setBody(pan001Message);

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.PAN_001_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.PAN_001_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
    }

    @Test
    void testProcessPan001JsonMessage() throws Exception {
        // Given
        String pan001JsonMessage = "{\n" +
                "  \"pan001\": {\n" +
                "    \"CstmrPmtStsRpt\": {\n" +
                "      \"GrpHdr\": {\n" +
                "        \"MsgId\": \"MSG002\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        exchange.getIn().setBody(pan001JsonMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.PAN_001_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.PAN_001_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
    }

    @Test
    void testProcessUnknownMessage() throws Exception {
        // Given
        String unknownMessage = "This is some unknown message format";
        exchange.getIn().setBody(unknownMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.UNKNOWN_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.UNKNOWN_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
    }

    @Test
    void testProcessEmptyMessage() throws Exception {
        // Given
        exchange.getIn().setBody("");

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.UNKNOWN_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.UNKNOWN_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
    }

    @Test
    void testProcessNullMessage() throws Exception {
        // Given
        exchange.getIn().setBody(null);

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.UNKNOWN_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.UNKNOWN_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
    }

    @Test
    void testProcessInvalidJsonMessage() throws Exception {
        // Given
        String invalidJsonMessage = "{ invalid json format";
        exchange.getIn().setBody(invalidJsonMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals(MessageTypeProcessor.UNKNOWN_TYPE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.MESSAGE_TYPE_HEADER));
        assertEquals(MessageTypeProcessor.UNKNOWN_ROUTE, 
                    exchange.getIn().getHeader(MessageTypeProcessor.ROUTE_TARGET_HEADER));
    }
}