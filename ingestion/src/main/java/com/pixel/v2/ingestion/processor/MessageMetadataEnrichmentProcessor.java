package com.pixel.v2.ingestion.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Processor to enrich messages with comprehensive metadata for processing module integration.
 * This processor creates a standardized message format with metadata that supports both
 * Kafka and direct processing routing paths.
 */
@Component
public class MessageMetadataEnrichmentProcessor implements Processor {
    
    private static final Logger log = LoggerFactory.getLogger(MessageMetadataEnrichmentProcessor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Header name constants
    private static final String RECEIPT_CHANNEL_HEADER = "ReceiptChannel";
    private static final String MESSAGE_SOURCE_HEADER = "MessageSource";
    private static final String PERSISTED_MESSAGE_ID_HEADER = "persistedMessageId";
    private static final String RECEIPT_TIMESTAMP_HEADER = "ReceiptTimestamp";
    private static final String INGESTION_START_TIME_HEADER = "IngestionStartTime";
    private static final String PUBLISH_TIMESTAMP_HEADER = "PublishTimestamp";
    private static final String PROCESSING_STAGE_HEADER = "ProcessingStage";
    private static final String ROUTING_DESTINATION_HEADER = "RoutingDestination";
    private static final String PRIMARY_IDENTIFIER_HEADER = "PrimaryIdentifier";
    private static final String MESSAGE_ID_HEADER = "MessageId";
    private static final String EXPECTED_MESSAGE_TYPE_HEADER = "ExpectedMessageType";
    private static final String IS_VALID_HEADER = "IsValid";
    private static final String IS_DUPLICATE_HEADER = "IsDuplicate";
    private static final String CAN_PROCESS_HEADER = "CanProcess";
    private static final String MESSAGE_PERSISTED_HEADER = "MessagePersisted";
    private static final String ENRICHED_DATA_PERSISTED_HEADER = "EnrichedDataPersisted";
    
    @Override
    public void process(Exchange exchange) throws Exception {
        log.debug("[METADATA-ENRICHMENT] Starting message metadata enrichment for processing module");
        
        try {
            // Create enriched message structure
            ObjectNode enrichedMessage = objectMapper.createObjectNode();
            
            // Add metadata section
            ObjectNode metadata = createMetadataSection(exchange);
            enrichedMessage.set("metadata", metadata);
            
            // Add original payload
            String originalPayload = exchange.getIn().getBody(String.class);
            enrichedMessage.put("payload", originalPayload);
            
            // Set the enriched message as the new body
            exchange.getIn().setBody(enrichedMessage.toString());
            
            // Add headers for processing module routing
            exchange.getIn().setHeader("ContentType", "application/json");
            exchange.getIn().setHeader("EnrichedForProcessing", true);
            
            log.info("[METADATA-ENRICHMENT] Message enriched with metadata for processing module. " +
                    "Route: {} → Processing Module", 
                    exchange.getIn().getHeader(RECEIPT_CHANNEL_HEADER));
            
        } catch (Exception e) {
            log.error("[METADATA-ENRICHMENT] Failed to enrich message metadata: {}", e.getMessage(), e);
            
            // Set error information for downstream handling
            exchange.getIn().setHeader("MetadataEnrichmentError", e.getMessage());
            exchange.getIn().setHeader(PROCESSING_STAGE_HEADER, "METADATA_ENRICHMENT_ERROR");
            
            throw e;
        }
    }
    
    /**
     * Creates comprehensive metadata section for the processing module
     */
    private ObjectNode createMetadataSection(Exchange exchange) {
        ObjectNode metadata = objectMapper.createObjectNode();
        
        // Receipt and timing information
        metadata.put("receiptChannel", getHeaderAsString(exchange, RECEIPT_CHANNEL_HEADER));
        metadata.put("messageSource", getHeaderAsString(exchange, MESSAGE_SOURCE_HEADER));
        metadata.put("receiptTimestamp", getHeaderAsString(exchange, RECEIPT_TIMESTAMP_HEADER));
        metadata.put("ingestionStartTime", getHeaderAsString(exchange, INGESTION_START_TIME_HEADER));
        metadata.put("publishTimestamp", getHeaderAsString(exchange, PUBLISH_TIMESTAMP_HEADER));
        
        // Processing information
        metadata.put("processingStage", getHeaderAsString(exchange, PROCESSING_STAGE_HEADER));
        metadata.put("routingDestination", getHeaderAsString(exchange, ROUTING_DESTINATION_HEADER));
        metadata.put("exchangeId", exchange.getExchangeId());
        
        // Message identification
        metadata.put("primaryIdentifier", getHeaderAsString(exchange, PRIMARY_IDENTIFIER_HEADER));
        metadata.put("messageId", getHeaderAsString(exchange, MESSAGE_ID_HEADER));
        metadata.put("messageType", getHeaderAsString(exchange, EXPECTED_MESSAGE_TYPE_HEADER));
        
        // Validation and processing status
        metadata.put("validationPassed", getHeaderAsBoolean(exchange, IS_VALID_HEADER));
        metadata.put("duplicateCheck", !getHeaderAsBoolean(exchange, IS_DUPLICATE_HEADER));
        metadata.put("canProcess", getHeaderAsBoolean(exchange, CAN_PROCESS_HEADER));
        
        // Database persistence status
        metadata.put("messagePersisted", getHeaderAsBoolean(exchange, MESSAGE_PERSISTED_HEADER));
        metadata.put("enrichedDataPersisted", getHeaderAsBoolean(exchange, ENRICHED_DATA_PERSISTED_HEADER));
        
        // Persistence IDs if available
        if (exchange.getIn().getHeader(PERSISTED_MESSAGE_ID_HEADER) != null) {
            metadata.put("persistedMessageId", getHeaderAsString(exchange, PERSISTED_MESSAGE_ID_HEADER));
        }
        
        // Processing module specific metadata
        metadata.put("processingModuleRoute", true);
        metadata.put("metadataEnrichmentTimestamp", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        
        // Additional routing information
        Map<String, Object> routingInfo = new HashMap<>();
        routingInfo.put("sourceChannel", getHeaderAsString(exchange, RECEIPT_CHANNEL_HEADER));
        routingInfo.put("targetEndpoint", "direct:kafka-message-processing");
        routingInfo.put("bypassedKafka", true);
        routingInfo.put("realTimeProcessing", true);
        
        metadata.set("routingInfo", objectMapper.valueToTree(routingInfo));
        
        return metadata;
    }
    
    /**
     * Safely extracts header value as string
     */
    private String getHeaderAsString(Exchange exchange, String headerName) {
        Object headerValue = exchange.getIn().getHeader(headerName);
        return headerValue != null ? headerValue.toString() : "";
    }
    
    /**
     * Safely extracts header value as boolean
     */
    private boolean getHeaderAsBoolean(Exchange exchange, String headerName) {
        Object headerValue = exchange.getIn().getHeader(headerName);
        if (headerValue instanceof Boolean) {
            return (Boolean) headerValue;
        }
        if (headerValue instanceof String) {
            return Boolean.parseBoolean((String) headerValue);
        }
        return false;
    }
}