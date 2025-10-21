package com.pixel.v2.persistence.processor;

import com.pixel.v2.persistence.model.ReceivedMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Unified message persistence processor that handles messages from all sources:
 * - MQ messages
 * - HTTP API messages  
 * - File-based messages
 */
@Component("messagePersistenceProcessor")
public class MessagePersistenceProcessor implements Processor {

    private static final Logger logger = Logger.getLogger(MessagePersistenceProcessor.class.getName());
    
    // Constants for headers and status values
    private static final String PERSISTENCE_STATUS_HEADER = "persistenceStatus";
    private static final String PERSISTENCE_ERROR_HEADER = "persistenceError";
    private static final String PERSISTED_MESSAGE_ID_HEADER = "persistedMessageId";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void process(Exchange exchange) throws Exception {
        try {
            String body = exchange.getIn().getBody(String.class);
            
            if (body == null || body.trim().isEmpty()) {
                logger.warning("Received empty or null message body");
                exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_ERROR);
                exchange.getIn().setHeader(PERSISTENCE_ERROR_HEADER, "Empty message body");
                return;
            }

            String persistenceType = exchange.getIn().getHeader("PersistenceType", String.class);
            String entityType = exchange.getIn().getHeader("EntityType", String.class);
            boolean isEnrichedPersistence = "ENRICHED".equals(persistenceType);
            boolean isCdmEntity = "CDM".equals(entityType);
            
            ReceivedMessage message;
            
            // Handle CDM messages separately
            if (isCdmEntity) {
                // Delegate to CDM processor for CDM messages
                exchange.getIn().setHeader("PersistenceOperation", isEnrichedPersistence ? "UPDATE" : "CREATE");
                // This should be handled by a separate route that calls cdmPersistenceProcessor
                logger.info("CDM entity type detected - should be handled by CDM processor route");
                exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_SUCCESS);
                exchange.getIn().setHeader("CdmProcessingRequired", true);
                return;
            }
            
            if (isEnrichedPersistence) {
                // For enriched data persistence, update existing record
                message = updateExistingMessage(exchange, body);
            } else {
                // For initial persistence, create new record
                message = createReceivedMessage(exchange, body);
            }
            
            if (em != null) {
                if (isEnrichedPersistence) {
                    message = em.merge(message); // Update existing record
                } else {
                    em.persist(message); // Create new record
                }
                em.flush(); // Ensure we get the generated ID
                
                // Set success headers
                exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_SUCCESS);
                exchange.getIn().setHeader(PERSISTED_MESSAGE_ID_HEADER, message.getId());
                
                String operation = isEnrichedPersistence ? "updated with enriched data" : "persisted";
                logger.info(String.format("[persistence] Message %s successfully - ID: %d, Source: %s, Type: %s", 
                    operation, message.getId(), message.getSource(), message.getMessageType()));
                    
            } else {
                logger.severe("EntityManager is null - cannot persist message");
                exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_ERROR);
                exchange.getIn().setHeader(PERSISTENCE_ERROR_HEADER, "EntityManager not available");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error persisting message", e);
            exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_ERROR);
            exchange.getIn().setHeader(PERSISTENCE_ERROR_HEADER, e.getMessage());
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    /**
     * Updates an existing ReceivedMessage entity with enriched data
     */
    private ReceivedMessage updateExistingMessage(Exchange exchange, String body) {
        // Get the existing message ID from headers
        Long existingMessageId = exchange.getIn().getHeader(PERSISTED_MESSAGE_ID_HEADER, Long.class);
        
        if (existingMessageId == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("No existing message ID found for enriched data persistence - creating new record");
            }
            return createReceivedMessage(exchange, body);
        }
        
        // Find existing message
        ReceivedMessage existingMessage = em.find(ReceivedMessage.class, existingMessageId);
        
        if (existingMessage == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(String.format("Existing message with ID %d not found - creating new record", existingMessageId));
            }
            return createReceivedMessage(exchange, body);
        }
        
        // Update with enriched data
        existingMessage.setPayload(body); // Updated payload with enriched data
        existingMessage.setUpdatedAt(OffsetDateTime.now());
        existingMessage.setProcessedAt(OffsetDateTime.now());
        
        // Update processing status to indicate enrichment completion
        String enrichmentStatus = exchange.getIn().getHeader("enrichmentStatus", String.class);
        if (enrichmentStatus != null) {
            existingMessage.setProcessingStatus(enrichmentStatus);
        } else {
            existingMessage.setProcessingStatus("ENRICHED");
        }
        
        return existingMessage;
    }

    /**
     * Creates a ReceivedMessage entity from the exchange data
     */
    private ReceivedMessage createReceivedMessage(Exchange exchange, String body) {
        ReceivedMessage message = new ReceivedMessage();
        
        // Set payload
        message.setPayload(body);
        
        // Set timestamps
        message.setReceivedAt(OffsetDateTime.now());
        
        // Extract message source from headers or endpoint
        String messageSource = exchange.getIn().getHeader("messageSource", String.class);
        if (messageSource == null) {
            // Fallback to endpoint URI analysis
            String endpointUri = exchange.getFromEndpoint() != null ? 
                exchange.getFromEndpoint().getEndpointUri() : "unknown";
            messageSource = determineSourceFromEndpoint(endpointUri);
        }
        message.setSource(messageSource);
        
        // Extract message metadata
        String messageId = exchange.getIn().getHeader("messageId", String.class);
        if (messageId == null) {
            messageId = exchange.getIn().getHeader("CamelMessageId", String.class);
        }
        message.setMessageId(messageId);
        
        String messageType = exchange.getIn().getHeader("messageType", String.class);
        message.setMessageType(messageType);
        
        // File-specific metadata (for CFT messages)
        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
        message.setFileName(fileName);
        
        Long splitIndex = exchange.getIn().getHeader("CamelSplitIndex", Long.class);
        if (splitIndex != null) {
            message.setLineNumber(splitIndex + 1); // Line numbers start from 1
        }
        
        // Processing metadata
        String receiptTimestamp = exchange.getIn().getHeader("receiptTimestamp", String.class);
        if (receiptTimestamp != null) {
            try {
                message.setReceivedAt(OffsetDateTime.parse(receiptTimestamp));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not parse receiptTimestamp: {0}", receiptTimestamp);
            }
        }
        
        return message;
    }

    /**
     * Determines message source from endpoint URI when not explicitly provided
     */
    private String determineSourceFromEndpoint(String endpointUri) {
        if (endpointUri == null) {
            return "UNKNOWN";
        }
        
        if (endpointUri.startsWith("jms:") || endpointUri.contains("mq")) {
            return "IBM_MQ";
        } else if (endpointUri.startsWith("netty-http:") || endpointUri.startsWith("rest:") 
                   || endpointUri.contains("http")) {
            return "HTTP_API";
        } else if (endpointUri.startsWith("file:") || endpointUri.contains("file")) {
            return "CFT_FILE";
        } else {
            return "UNKNOWN";
        }
    }
}