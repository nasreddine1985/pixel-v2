package com.pixel.v2.kafka.starter.processor;

import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Kafka starter processor for handling consumed messages
 * Enriches messages with additional metadata and processing information
 */
@Component("kafkaStarterProcessor")
public class KafkaStarterProcessor implements Processor {

    private static final Logger logger = Logger.getLogger(KafkaStarterProcessor.class.getName());

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String messageBody = exchange.getIn().getBody(String.class);
            
            // Log the received message details
            String topic = exchange.getIn().getHeader("kafkaTopic", String.class);
            String partition = exchange.getIn().getHeader("kafkaPartition", String.class);
            String offset = exchange.getIn().getHeader("kafkaOffset", String.class);
            String key = exchange.getIn().getHeader("kafkaKey", String.class);
            
            logger.info(String.format("[KAFKA-STARTER-PROCESSOR] Processing message from topic '%s', partition: %s, offset: %s, key: %s", 
                topic, partition, offset, key));
            
            // Validate message body
            if (messageBody == null || messageBody.trim().isEmpty()) {
                logger.warning("[KAFKA-STARTER-PROCESSOR] Received empty message body");
                exchange.getIn().setHeader("processingError", "Empty message body received from Kafka");
                return;
            }
            
            // Set processing metadata
            exchange.getIn().setHeader("processingStartTime", OffsetDateTime.now().toString());
            exchange.getIn().setHeader("messageLength", messageBody.length());
            exchange.getIn().setHeader("processingStatus", "RECEIVED_FROM_KAFKA");
            
            // Determine message type based on content (simple heuristics)
            String messageType = determineMessageType(messageBody);
            exchange.getIn().setHeader("detectedMessageType", messageType);
            
            // Set Kafka-specific processing headers
            exchange.getIn().setHeader("kafkaProcessed", true);
            exchange.getIn().setHeader("consumerProcessingTime", System.currentTimeMillis());
            
            logger.info(String.format("[KAFKA-STARTER-PROCESSOR] Message processed successfully - Type: %s, Length: %d bytes", 
                messageType, messageBody.length()));
                
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[KAFKA-STARTER-PROCESSOR] Error processing Kafka message", e);
            exchange.getIn().setHeader("processingError", e.getMessage());
            exchange.getIn().setHeader("processingStatus", "ERROR");
            throw e;
        }
    }
    
    /**
     * Determines message type based on content analysis
     * @param messageBody The message content
     * @return Detected message type
     */
    private String determineMessageType(String messageBody) {
        if (messageBody == null) {
            return "UNKNOWN";
        }
        
        String trimmedBody = messageBody.trim();
        
        // Check for XML content
        if (trimmedBody.startsWith("<?xml") || trimmedBody.startsWith("<")) {
            // Check for specific payment message types
            if (trimmedBody.contains("pacs.008") || trimmedBody.contains("FIToFICstmrCdtTrf")) {
                return "pacs.008";
            } else if (trimmedBody.contains("pan.001") || trimmedBody.contains("CstmrCdtTrfInitn")) {
                return "pan.001";
            } else if (trimmedBody.contains("camt.056") || trimmedBody.contains("FIToFIPmtCxlReq")) {
                return "camt.056";
            } else {
                return "XML_MESSAGE";
            }
        }
        
        // Check for JSON content
        if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
            return "JSON_MESSAGE";
        }
        
        // Check for fixed-length or delimited formats
        if (trimmedBody.contains("|")) {
            return "DELIMITED_MESSAGE";
        }
        
        // Default fallback
        return "TEXT_MESSAGE";
    }
}