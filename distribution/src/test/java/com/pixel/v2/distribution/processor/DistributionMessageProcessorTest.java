package com.pixel.v2.distribution.processor;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class DistributionMessageProcessorTest {

    private DistributionMessageProcessor processor;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        processor = new DistributionMessageProcessor();
        exchange = new DefaultExchange(new DefaultCamelContext());
    }

    @Test
    void testProcessPaymentMessage() throws Exception {
        // Given
        String paymentMessage = "<?xml version=\"1.0\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\"><FIToFICstmrCdtTrf><GrpHdr><MsgId>PAY123</MsgId></GrpHdr></FIToFICstmrCdtTrf></Document>";
        exchange.getIn().setBody(paymentMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals("SUCCESS", exchange.getIn().getHeader("processingResult"));
        assertEquals("PAYMENT", exchange.getIn().getHeader("messageType"));
        assertEquals("PAY123", exchange.getIn().getHeader("paymentId"));
        assertNotNull(exchange.getIn().getHeader("processingStartTime"));
        assertNotNull(exchange.getIn().getHeader("processingEndTime"));
    }

    @Test
    void testProcessJsonPaymentMessage() throws Exception {
        // Given
        String jsonPaymentMessage = "{\"paymentId\":\"PAY456\",\"amount\":1000.00,\"creditor\":\"John Doe\"}";
        exchange.getIn().setBody(jsonPaymentMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals("SUCCESS", exchange.getIn().getHeader("processingResult"));
        assertEquals("PAYMENT", exchange.getIn().getHeader("messageType"));
        assertEquals("PAY456", exchange.getIn().getHeader("paymentId"));
        assertEquals("PAYMENT", exchange.getIn().getHeader("messageCategory"));
        assertEquals(true, exchange.getIn().getHeader("requiresValidation"));
        assertEquals("HIGH", exchange.getIn().getHeader("priority"));
    }

    @Test
    void testProcessTransactionMessage() throws Exception {
        // Given
        String transactionMessage = "{\"transactionId\":\"TXN789\",\"transactionType\":\"CREDIT\"}";
        exchange.getIn().setBody(transactionMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals("SUCCESS", exchange.getIn().getHeader("processingResult"));
        assertEquals("TRANSACTION", exchange.getIn().getHeader("messageType"));
        assertEquals("TXN789", exchange.getIn().getHeader("transactionId"));
        assertEquals("TRANSACTION", exchange.getIn().getHeader("messageCategory"));
        assertEquals("MEDIUM", exchange.getIn().getHeader("priority"));
    }

    @Test
    void testProcessNotificationMessage() throws Exception {
        // Given
        String notificationMessage = "{\"notificationType\":\"STATUS_UPDATE\",\"recipient\":\"user@example.com\"}";
        exchange.getIn().setBody(notificationMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals("SUCCESS", exchange.getIn().getHeader("processingResult"));
        assertEquals("NOTIFICATION", exchange.getIn().getHeader("messageType"));
        assertNotNull(exchange.getIn().getHeader("notificationId"));
        assertEquals("NOTIFICATION", exchange.getIn().getHeader("messageCategory"));
        assertEquals("LOW", exchange.getIn().getHeader("priority"));
    }

    @Test
    void testProcessUnknownMessage() throws Exception {
        // Given
        String unknownMessage = "This is just plain text";
        exchange.getIn().setBody(unknownMessage);

        // When
        processor.process(exchange);

        // Then
        assertEquals("SUCCESS", exchange.getIn().getHeader("processingResult"));
        assertEquals("NOTIFICATION", exchange.getIn().getHeader("messageType")); // Plain text defaults to notification
        assertNotNull(exchange.getIn().getHeader("notificationId"));
    }
}