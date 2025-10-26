package com.pixel.v2.distribution.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom processor for distribution messages
 * 
 * Handles the logic for processing distribution messages,
 * including message type detection, validation, and enrichment.
 */
@Component("distributionMessageProcessor")
public class DistributionMessageProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(DistributionMessageProcessor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String messageBody = exchange.getIn().getBody(String.class);
            logger.info("Processing distribution message: {}", messageBody);

            // Set processing start time
            exchange.getIn().setHeader("processingStartTime", System.currentTimeMillis());

            // Detect message type
            String messageType = detectMessageType(messageBody);
            exchange.getIn().setHeader("messageType", messageType);
            logger.info("Detected message type: {}", messageType);

            // Extract message identifiers based on type
            switch (messageType) {
                case "PAYMENT":
                    processPaymentMessage(exchange, messageBody);
                    break;
                case "TRANSACTION":
                    processTransactionMessage(exchange, messageBody);
                    break;
                case "NOTIFICATION":
                    processNotificationMessage(exchange, messageBody);
                    break;
                default:
                    processGenericMessage(exchange, messageBody);
                    break;
            }

            // Set processing result
            exchange.getIn().setHeader("processingResult", "SUCCESS");
            exchange.getIn().setHeader("processingEndTime", System.currentTimeMillis());
            
            long processingTime = (Long) exchange.getIn().getHeader("processingEndTime") - 
                                 (Long) exchange.getIn().getHeader("processingStartTime");
            exchange.getIn().setHeader("processingDuration", processingTime);

            logger.info("Message processing completed successfully in {} ms", processingTime);

        } catch (Exception e) {
            logger.error("Error processing distribution message: {}", e.getMessage(), e);
            exchange.getIn().setHeader("processingResult", "ERROR");
            exchange.getIn().setHeader("processingError", e.getMessage());
            exchange.getIn().setHeader("processingEndTime", System.currentTimeMillis());
        }
    }

    /**
     * Detect the type of message based on content structure
     */
    private String detectMessageType(String messageBody) {
        try {
            if (messageBody.trim().startsWith("<")) {
                // XML message - check for payment namespace or elements
                if (messageBody.contains("pacs.008") || messageBody.contains("FIToFICstmrCdtTrf")) {
                    return "PAYMENT";
                } else if (messageBody.contains("pain.001") || messageBody.contains("CstmrCdtTrfInitn")) {
                    return "TRANSACTION";
                }
                return "UNKNOWN";
            } else if (messageBody.trim().startsWith("{")) {
                // JSON message - parse and check structure
                JsonNode jsonNode = objectMapper.readTree(messageBody);
                
                if (jsonNode.has("paymentId") || jsonNode.has("amount") || jsonNode.has("creditor")) {
                    return "PAYMENT";
                } else if (jsonNode.has("transactionId") || jsonNode.has("transactionType")) {
                    return "TRANSACTION";
                } else if (jsonNode.has("notificationType") || jsonNode.has("recipient")) {
                    return "NOTIFICATION";
                }
                return "UNKNOWN";
            } else {
                // Plain text or other format
                return "NOTIFICATION";
            }
        } catch (Exception e) {
            logger.warn("Error detecting message type: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Process payment-specific messages
     */
    private void processPaymentMessage(Exchange exchange, String messageBody) {
        logger.info("Processing payment message");
        
        try {
            // Extract payment ID
            String paymentId = extractPaymentId(messageBody);
            exchange.getIn().setHeader("paymentId", paymentId);
            
            // Add payment-specific headers
            exchange.getIn().setHeader("messageCategory", "PAYMENT");
            exchange.getIn().setHeader("requiresValidation", true);
            exchange.getIn().setHeader("priority", "HIGH");
            
            logger.info("Payment message processed with ID: {}", paymentId);
            
        } catch (Exception e) {
            logger.error("Error processing payment message: {}", e.getMessage());
            throw new RuntimeException("Failed to process payment message", e);
        }
    }

    /**
     * Process transaction-specific messages
     */
    private void processTransactionMessage(Exchange exchange, String messageBody) {
        logger.info("Processing transaction message");
        
        try {
            // Extract transaction ID
            String transactionId = extractTransactionId(messageBody);
            exchange.getIn().setHeader("transactionId", transactionId);
            
            // Add transaction-specific headers
            exchange.getIn().setHeader("messageCategory", "TRANSACTION");
            exchange.getIn().setHeader("requiresValidation", true);
            exchange.getIn().setHeader("priority", "MEDIUM");
            
            logger.info("Transaction message processed with ID: {}", transactionId);
            
        } catch (Exception e) {
            logger.error("Error processing transaction message: {}", e.getMessage());
            throw new RuntimeException("Failed to process transaction message", e);
        }
    }

    /**
     * Process notification-specific messages
     */
    private void processNotificationMessage(Exchange exchange, String messageBody) {
        logger.info("Processing notification message");
        
        try {
            // Generate notification ID if not present
            String notificationId = "NOTIF_" + System.currentTimeMillis();
            exchange.getIn().setHeader("notificationId", notificationId);
            
            // Add notification-specific headers
            exchange.getIn().setHeader("messageCategory", "NOTIFICATION");
            exchange.getIn().setHeader("requiresValidation", false);
            exchange.getIn().setHeader("priority", "LOW");
            
            logger.info("Notification message processed with ID: {}", notificationId);
            
        } catch (Exception e) {
            logger.error("Error processing notification message: {}", e.getMessage());
            throw new RuntimeException("Failed to process notification message", e);
        }
    }

    /**
     * Process generic/unknown messages
     */
    private void processGenericMessage(Exchange exchange, String messageBody) {
        logger.info("Processing generic message");
        
        try {
            // Generate generic ID
            String genericId = "MSG_" + System.currentTimeMillis();
            exchange.getIn().setHeader("messageId", genericId);
            
            // Add generic headers
            exchange.getIn().setHeader("messageCategory", "GENERIC");
            exchange.getIn().setHeader("requiresValidation", false);
            exchange.getIn().setHeader("priority", "LOW");
            
            logger.info("Generic message processed with ID: {}", genericId);
            
        } catch (Exception e) {
            logger.error("Error processing generic message: {}", e.getMessage());
            throw new RuntimeException("Failed to process generic message", e);
        }
    }

    /**
     * Extract payment ID from message content
     */
    private String extractPaymentId(String messageBody) {
        try {
            if (messageBody.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(messageBody);
                if (jsonNode.has("paymentId")) {
                    return jsonNode.get("paymentId").asText();
                }
            } else if (messageBody.contains("<MsgId>")) {
                // Simple XML extraction
                int start = messageBody.indexOf("<MsgId>") + 7;
                int end = messageBody.indexOf("</MsgId>");
                if (start > 6 && end > start) {
                    return messageBody.substring(start, end);
                }
            }
            return "PAY_" + System.currentTimeMillis();
        } catch (Exception e) {
            logger.warn("Error extracting payment ID: {}", e.getMessage());
            return "PAY_" + System.currentTimeMillis();
        }
    }

    /**
     * Extract transaction ID from message content
     */
    private String extractTransactionId(String messageBody) {
        try {
            if (messageBody.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(messageBody);
                if (jsonNode.has("transactionId")) {
                    return jsonNode.get("transactionId").asText();
                }
            } else if (messageBody.contains("<TxId>")) {
                // Simple XML extraction
                int start = messageBody.indexOf("<TxId>") + 6;
                int end = messageBody.indexOf("</TxId>");
                if (start > 5 && end > start) {
                    return messageBody.substring(start, end);
                }
            }
            return "TXN_" + System.currentTimeMillis();
        } catch (Exception e) {
            logger.warn("Error extracting transaction ID: {}", e.getMessage());
            return "TXN_" + System.currentTimeMillis();
        }
    }
}