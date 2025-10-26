package com.pixel.v2.persistence.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixel.v2.persistence.model.CdmMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor for persisting CDM (Common Data Model) messages
 * Handles both creation of new CDM records and updates to existing ones
 */
@Component("cdmPersistenceProcessor")
public class CdmPersistenceProcessor implements Processor {

    private static final Logger logger = Logger.getLogger(CdmPersistenceProcessor.class.getName());
    
    // Constants for headers and status values
    private static final String PERSISTENCE_STATUS_HEADER = "persistenceStatus";
    private static final String PERSISTENCE_ERROR_HEADER = "persistenceError";
    private static final String PERSISTED_CDM_ID_HEADER = "persistedCdmId";
    private static final String MESSAGE_ID_HEADER = "messageId";
    private static final String ENRICHMENT_STATUS_HEADER = "enrichmentStatus";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    @PersistenceContext
    private EntityManager em;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void process(Exchange exchange) throws Exception {
        try {
            String body = exchange.getIn().getBody(String.class);
            
            if (body == null || body.trim().isEmpty()) {
                logger.warning("Received empty or null CDM message body");
                exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_ERROR);
                exchange.getIn().setHeader(PERSISTENCE_ERROR_HEADER, "Empty CDM message body");
                return;
            }

            String persistenceOperation = exchange.getIn().getHeader("PersistenceOperation", String.class);
            boolean isUpdate = "UPDATE".equals(persistenceOperation);
            
            CdmMessage cdmMessage;
            
            if (isUpdate) {
                // For updates, find and update existing CDM record
                cdmMessage = updateExistingCdmMessage(exchange, body);
            } else {
                // For new messages, create new CDM record
                cdmMessage = createCdmMessage(exchange, body);
            }
            
            if (em != null) {
                if (isUpdate && cdmMessage.getId() != null) {
                    cdmMessage = em.merge(cdmMessage); // Update existing record
                } else {
                    em.persist(cdmMessage); // Create new record
                }
                em.flush(); // Ensure we get the generated ID
                
                // Set success headers
                exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_SUCCESS);
                exchange.getIn().setHeader(PERSISTED_CDM_ID_HEADER, cdmMessage.getId());
                
                String operation = isUpdate ? "updated" : "persisted";
                logger.info(String.format("[CDM-persistence] CDM message %s successfully - ID: %d, MessageId: %s, Type: %s", 
                    operation, cdmMessage.getId(), cdmMessage.getMessageId(), cdmMessage.getMessageType()));
                    
            } else {
                logger.severe("EntityManager is null - cannot persist CDM message");
                exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_ERROR);
                exchange.getIn().setHeader(PERSISTENCE_ERROR_HEADER, "EntityManager not available");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error persisting CDM message", e);
            exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_ERROR);
            exchange.getIn().setHeader(PERSISTENCE_ERROR_HEADER, e.getMessage());
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    /**
     * Updates an existing CdmMessage entity
     */
    private CdmMessage updateExistingCdmMessage(Exchange exchange, String cdmPayload) {
        // Try to find existing CDM message by various identifiers
        String messageId = exchange.getIn().getHeader(MESSAGE_ID_HEADER, String.class);
        Long existingCdmId = exchange.getIn().getHeader(PERSISTED_CDM_ID_HEADER, Long.class);
        
        CdmMessage existingMessage = null;
        
        // Try to find by ID first
        if (existingCdmId != null) {
            existingMessage = em.find(CdmMessage.class, existingCdmId);
        }
        
        // If not found by ID, try to find by messageId
        if (existingMessage == null && messageId != null) {
            try {
                TypedQuery<CdmMessage> query = em.createQuery(
                    "SELECT c FROM CdmMessage c WHERE c.messageId = :messageId ORDER BY c.createdAt DESC", 
                    CdmMessage.class);
                query.setParameter(MESSAGE_ID_HEADER, messageId);
                query.setMaxResults(1);
                
                var results = query.getResultList();
                if (!results.isEmpty()) {
                    existingMessage = results.get(0);
                }
            } catch (Exception e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Error finding CDM message by messageId: " + messageId, e);
                }
            }
        }
        
        if (existingMessage == null) {
            logger.warning("No existing CDM message found for update - creating new record");
            return createCdmMessage(exchange, cdmPayload);
        }
        
        // Update existing message with new CDM payload
        existingMessage.setCdmPayload(cdmPayload);
        existingMessage.setUpdatedAt(OffsetDateTime.now());
        existingMessage.setProcessedAt(OffsetDateTime.now());
        
        // Update enrichment status if provided
        String enrichmentStatus = exchange.getIn().getHeader(ENRICHMENT_STATUS_HEADER, String.class);
        if (enrichmentStatus != null) {
            existingMessage.setEnrichmentStatus(enrichmentStatus);
        } else {
            existingMessage.setEnrichmentStatus("ENRICHED");
        }
        
        // Update processing status
        existingMessage.setProcessingStatus("PROCESSED");
        
        // Parse and update CDM-specific fields from payload
        try {
            updateCdmFieldsFromPayload(existingMessage, cdmPayload);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not parse CDM payload for field updates", e);
        }
        
        return existingMessage;
    }

    /**
     * Creates a new CdmMessage entity from the exchange data and CDM payload
     */
    private CdmMessage createCdmMessage(Exchange exchange, String cdmPayload) {
        CdmMessage cdmMessage = new CdmMessage();
        
        // Set CDM payload
        cdmMessage.setCdmPayload(cdmPayload);
        
        // Extract metadata from headers
        String messageId = exchange.getIn().getHeader(MESSAGE_ID_HEADER, String.class);
        String originalMessageId = exchange.getIn().getHeader("originalMessageId", String.class);
        String messageType = exchange.getIn().getHeader("messageType", String.class);
        String messageSource = exchange.getIn().getHeader("messageSource", String.class);
        
        cdmMessage.setMessageId(messageId);
        cdmMessage.setOriginalMessageId(originalMessageId != null ? originalMessageId : messageId);
        cdmMessage.setMessageType(messageType);
        cdmMessage.setSource(messageSource != null ? messageSource : "UNKNOWN");
        
        // Set original payload if available
        String originalPayload = exchange.getIn().getHeader("originalPayload", String.class);
        cdmMessage.setOriginalPayload(originalPayload);
        
        // Set reference to original received message if available
        Long originalReceivedMessageId = exchange.getIn().getHeader("originalReceivedMessageId", Long.class);
        cdmMessage.setOriginalReceivedMessageId(originalReceivedMessageId);
        
        // Set initial processing status
        cdmMessage.setProcessingStatus("PROCESSED");
        cdmMessage.setProcessedAt(OffsetDateTime.now());
        
        // Parse CDM-specific fields from JSON payload
        try {
            updateCdmFieldsFromPayload(cdmMessage, cdmPayload);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not parse CDM payload for field extraction", e);
            // Set defaults if parsing fails
            cdmMessage.setEnrichmentStatus("PENDING");
        }
        
        return cdmMessage;
    }

    /**
     * Extracts and updates CDM-specific fields from the JSON payload
     */
    private void updateCdmFieldsFromPayload(CdmMessage cdmMessage, String cdmPayload) throws Exception {
        JsonNode cdmNode = objectMapper.readTree(cdmPayload);
        
        // Extract messageId if not already set
        if (cdmMessage.getMessageId() == null && cdmNode.has(MESSAGE_ID_HEADER)) {
            cdmMessage.setMessageId(cdmNode.get(MESSAGE_ID_HEADER).asText());
        }
        
        // Extract creationDateTime
        if (cdmNode.has("creationDateTime")) {
            try {
                String dateTimeStr = cdmNode.get("creationDateTime").asText();
                OffsetDateTime creationDateTime = OffsetDateTime.parse(dateTimeStr);
                cdmMessage.setCreationDateTime(creationDateTime);
            } catch (DateTimeParseException e) {
                logger.log(Level.WARNING, "Could not parse creationDateTime from CDM payload", e);
            }
        }
        
        // Extract numberOfTransactions
        if (cdmNode.has("numberOfTransactions")) {
            cdmMessage.setNumberOfTransactions(cdmNode.get("numberOfTransactions").asInt());
        }
        
        // Extract enrichmentStatus
        if (cdmNode.has(ENRICHMENT_STATUS_HEADER)) {
            cdmMessage.setEnrichmentStatus(cdmNode.get(ENRICHMENT_STATUS_HEADER).asText());
        }
        
        // Extract any additional fields that might be in the CDM structure
        if (cdmNode.has("processingStatus")) {
            cdmMessage.setProcessingStatus(cdmNode.get("processingStatus").asText());
        }
    }
}