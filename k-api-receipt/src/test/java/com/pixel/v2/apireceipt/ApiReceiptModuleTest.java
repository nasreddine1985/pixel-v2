package com.pixel.v2.apireceipt;

import com.pixel.v2.apireceipt.model.ReceivedMessage;
import com.pixel.v2.apireceipt.processor.MessagePersistenceProcessor;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApiReceiptModuleTest {

    @Test
    void testReceivedMessageModel() {
        // Test the ReceivedMessage entity
        ReceivedMessage message = new ReceivedMessage();
        message.setId(1L);
        message.setPayload("{\"test\": \"data\"}");
        message.setSource("api");
        message.setReceivedAt(OffsetDateTime.now());

        assertNotNull(message.getId());
        assertEquals("{\"test\": \"data\"}", message.getPayload());
        assertEquals("api", message.getSource());
        assertNotNull(message.getReceivedAt());
    }

    @Test
    void testMessagePersistenceProcessor() {
        // Test that the processor can be instantiated
        MessagePersistenceProcessor processor = new MessagePersistenceProcessor();
        assertNotNull(processor);
    }
}