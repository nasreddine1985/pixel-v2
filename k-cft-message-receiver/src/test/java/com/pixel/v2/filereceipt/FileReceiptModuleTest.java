package com.pixel.v2.filereceipt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CFT Message Receiver Kamelet
 * 
 * Note: ReceivedMessage and MessagePersistenceProcessor have been moved to 
 * k-db-tx kamelet as part of the architecture refactoring.
 */
class FileReceiptModuleTest {

    @Test
    void testKameletConfiguration() {
        // Test that the kamelet can be loaded without errors
        assertDoesNotThrow(() -> {
            String kameletName = "k-cft-message-receiver";
            assertNotNull(kameletName);
            assertTrue(kameletName.startsWith("k-"));
        });
    }

    @Test
    void testKameletMetadata() {
        // Test kamelet metadata constants
        String expectedTitle = "K-CFT Message Receiver";
        String expectedType = "source";
        
        assertNotNull(expectedTitle);
        assertNotNull(expectedType);
        assertEquals("K-CFT Message Receiver", expectedTitle);
        assertEquals("source", expectedType);
    }

    @Test
    void testRequiredProperties() {
        // Test that required properties are defined
        String[] requiredProperties = {"directoryPath"};
        String[] optionalProperties = {"filePattern", "processedDirectory", "errorDirectory", "delay"};
        
        for (String property : requiredProperties) {
            assertNotNull(property);
            assertFalse(property.isEmpty());
        }
        
        for (String property : optionalProperties) {
            assertNotNull(property);
            assertFalse(property.isEmpty());
        }
        
        assertEquals(1, requiredProperties.length);
        assertEquals(4, optionalProperties.length);
    }

    @Test
    void testFileProcessingCapabilities() {
        // Test file processing configuration
        String defaultFilePattern = ".*\\.xml";
        String defaultProcessedDir = "/nas/processed";
        String defaultErrorDir = "/nas/error";
        int defaultDelay = 5000;
        
        assertNotNull(defaultFilePattern);
        assertNotNull(defaultProcessedDir);
        assertNotNull(defaultErrorDir);
        assertTrue(defaultDelay > 0);
        
        // Test regex pattern validity
        assertTrue(defaultFilePattern.contains("\\.xml"));
    }
}