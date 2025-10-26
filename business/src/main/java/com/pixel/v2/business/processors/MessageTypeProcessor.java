package com.pixel.v2.business.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Message Type Detector Processor
 * 
 * This processor analyzes incoming messages to determine their type and sets
 * appropriate headers for routing to the correct transformer kamelet.
 * 
 * Supported message types:
 * - pacs.008: ISO 20022 Customer Credit Transfer Initiation
 * - pan.001: ISO 20022 Customer Payment Status Report
 * - Unknown: Messages that don't match known patterns
 */
@Component
public class MessageTypeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MessageTypeProcessor.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Message type constants
    public static final String MESSAGE_TYPE_HEADER = "MessageType";
    public static final String PACS_008_TYPE = "pacs.008";
    public static final String PACS_009_TYPE = "pacs.009";
    public static final String PAIN_001_TYPE = "pain.001";
    public static final String CAMT_053_TYPE = "camt.053";
    public static final String UNKNOWN_TYPE = "unknown";
    
    // Routing constants
    public static final String ROUTE_TARGET_HEADER = "RouteTarget";
    public static final String PACS_008_ROUTE = "direct:pacs-008-transform";
    public static final String PACS_009_ROUTE = "direct:pacs-009-transform";
    public static final String PAIN_001_ROUTE = "direct:pain-001-transform";
    public static final String CAMT_053_ROUTE = "direct:camt-053-transform";
    public static final String UNKNOWN_ROUTE = "direct:unknown-message";

    @Override
    public void process(Exchange exchange) throws Exception {
        String messageBody = exchange.getIn().getBody(String.class);
        
        log.debug("[MESSAGE-TYPE-PROCESSOR] Processing message with body length: {}", 
                 messageBody != null ? messageBody.length() : 0);
        
        if (messageBody == null || messageBody.trim().isEmpty()) {
            log.warn("[MESSAGE-TYPE-PROCESSOR] Received empty message body");
            setMessageType(exchange, UNKNOWN_TYPE, UNKNOWN_ROUTE);
            return;
        }
        
        String messageType = detectMessageType(messageBody);
        String routeTarget = getRouteForMessageType(messageType);
        
        setMessageType(exchange, messageType, routeTarget);
        
        log.info("[MESSAGE-TYPE-PROCESSOR] Message type detected: {} -> routing to: {}", 
                messageType, routeTarget);
    }
    
    /**
     * Detect the message type based on content analysis
     */
    private String detectMessageType(String messageBody) {
        String trimmedBody = messageBody.trim();
        
        // Check for pacs.008 patterns
        if (isPacs008Message(trimmedBody)) {
            return PACS_008_TYPE;
        }
        
        // Check for pacs.009 patterns
        if (isPacs009Message(trimmedBody)) {
            return PACS_009_TYPE;
        }
        
        // Check for pain.001 patterns
        if (isPain001Message(trimmedBody)) {
            return PAIN_001_TYPE;
        }
        
        // Check for camt.053 patterns
        if (isCamt053Message(trimmedBody)) {
            return CAMT_053_TYPE;
        }
        
        return UNKNOWN_TYPE;
    }
    
    /**
     * Check if the message is a pacs.008 message
     */
    private boolean isPacs008Message(String messageBody) {
        // XML format check for pacs.008
        if (messageBody.startsWith("<") && 
            (messageBody.contains("pacs.008") || 
             messageBody.contains("FIToFICstmrCdtTrf") ||
             messageBody.contains("CustomerCreditTransferInitiation"))) {
            return true;
        }
        
        // JSON format check for pacs.008
        if (messageBody.startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(messageBody);
                if (jsonNode.has("pacs008") || 
                    jsonNode.has("FIToFICstmrCdtTrf") ||
                    jsonNode.has("CustomerCreditTransferInitiation")) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("[MESSAGE-TYPE-PROCESSOR] JSON parsing failed for pacs.008 detection: {}", e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Check if the message is a pacs.009 message
     */
    private boolean isPacs009Message(String messageBody) {
        // XML format check for pacs.009
        if (messageBody.startsWith("<") && 
            (messageBody.contains("pacs.009") || 
             messageBody.contains("FIToFICstmrCdtTrfRtr") ||
             messageBody.contains("FinancialInstitutionCreditTransferReturn"))) {
            return true;
        }
        
        // JSON format check for pacs.009
        if (messageBody.startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(messageBody);
                if (jsonNode.has("pacs009") || 
                    jsonNode.has("FIToFICstmrCdtTrfRtr") ||
                    jsonNode.has("FinancialInstitutionCreditTransferReturn")) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("[MESSAGE-TYPE-PROCESSOR] JSON parsing failed for pacs.009 detection: {}", e.getMessage());
            }
        }
        
        return false;
    }

    /**
     * Check if the message is a pain.001 message
     */
    private boolean isPain001Message(String messageBody) {
        // XML format check for pain.001
        if (messageBody.startsWith("<") && 
            (messageBody.contains("pain.001") || 
             messageBody.contains("CstmrCdtTrfInitn") ||
             messageBody.contains("CustomerCreditTransferInitiation"))) {
            return true;
        }
        
        // JSON format check for pain.001
        if (messageBody.startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(messageBody);
                if (jsonNode.has("pain001") || 
                    jsonNode.has("CstmrCdtTrfInitn") ||
                    jsonNode.has("CustomerCreditTransferInitiation")) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("[MESSAGE-TYPE-PROCESSOR] JSON parsing failed for pain.001 detection: {}", e.getMessage());
            }
        }
        
        return false;
    }

    /**
     * Check if the message is a camt.053 message
     */
    private boolean isCamt053Message(String messageBody) {
        // XML format check for camt.053
        if (messageBody.startsWith("<") && 
            (messageBody.contains("camt.053") || 
             messageBody.contains("BkToCstmrStmt") ||
             messageBody.contains("BankToCustomerStatement"))) {
            return true;
        }
        
        // JSON format check for camt.053
        if (messageBody.startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(messageBody);
                if (jsonNode.has("camt053") || 
                    jsonNode.has("BkToCstmrStmt") ||
                    jsonNode.has("BankToCustomerStatement")) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("[MESSAGE-TYPE-PROCESSOR] JSON parsing failed for camt.053 detection: {}", e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Get the route target for a given message type
     */
    private String getRouteForMessageType(String messageType) {
        switch (messageType) {
            case PACS_008_TYPE:
                return PACS_008_ROUTE;
            case PACS_009_TYPE:
                return PACS_009_ROUTE;
            case PAIN_001_TYPE:
                return PAIN_001_ROUTE;
            case CAMT_053_TYPE:
                return CAMT_053_ROUTE;
            default:
                return UNKNOWN_ROUTE;
        }
    }
    
    /**
     * Set message type and route target headers
     */
    private void setMessageType(Exchange exchange, String messageType, String routeTarget) {
        exchange.getIn().setHeader(MESSAGE_TYPE_HEADER, messageType);
        exchange.getIn().setHeader(ROUTE_TARGET_HEADER, routeTarget);
        
        // Also set processing metadata
        exchange.getIn().setHeader("ProcessingTimestamp", System.currentTimeMillis());
        exchange.getIn().setHeader("ProcessedBy", "MessageTypeProcessor");
    }
}