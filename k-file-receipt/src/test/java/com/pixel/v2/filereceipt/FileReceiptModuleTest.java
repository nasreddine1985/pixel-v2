package com.pixel.v2.filereceipt;

import com.pixel.v2.filereceipt.model.ReceivedMessage;
import com.pixel.v2.filereceipt.processor.MessagePersistenceProcessor;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileReceiptModuleTest {

    @Test
    void testReceivedMessageModel() {
        // Test the ReceivedMessage entity
        ReceivedMessage message = new ReceivedMessage();
        message.setId(1L);
        message.setPayload("<?xml version=\"1.0\"?><Document>test</Document>");
        message.setSource("file");
        message.setFileName("payment_001.xml");
        message.setLineNumber(5L);
        message.setReceivedAt(OffsetDateTime.now());

        assertNotNull(message.getId());
        assertEquals("<?xml version=\"1.0\"?><Document>test</Document>", message.getPayload());
        assertEquals("file", message.getSource());
        assertEquals("payment_001.xml", message.getFileName());
        assertEquals(5L, message.getLineNumber());
        assertNotNull(message.getReceivedAt());
    }

    @Test
    void testMessagePersistenceProcessor() {
        // Test that the processor can be instantiated
        MessagePersistenceProcessor processor = new MessagePersistenceProcessor();
        assertNotNull(processor);
    }
}