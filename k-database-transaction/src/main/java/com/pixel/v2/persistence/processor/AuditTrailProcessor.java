package com.pixel.v2.persistence.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

/**
 * Processor for creating audit trail entries for persisted messages
 */
@Component("auditTrailProcessor")
public class AuditTrailProcessor implements Processor {

    private static final Logger logger = Logger.getLogger(AuditTrailProcessor.class.getName());

    @Override
    public void process(Exchange exchange) throws Exception {
        // Extract audit information from headers
        String messageId = exchange.getIn().getHeader("messageId", String.class);
        String persistedMessageId = exchange.getIn().getHeader("persistedMessageId", String.class);
        String source = exchange.getIn().getHeader("messageSource", String.class);
        String messageType = exchange.getIn().getHeader("messageType", String.class);
        
        // Create audit entry (this could be stored in a separate audit table)
        String auditEntry = String.format(
            "AUDIT: Message persisted - ID: %s, DB_ID: %s, Source: %s, Type: %s, Timestamp: %s",
            messageId, persistedMessageId, source, messageType, OffsetDateTime.now()
        );
        
        logger.info(auditEntry);
        
        // Set audit confirmation header
        exchange.getIn().setHeader("auditTrailCreated", true);
        exchange.getIn().setHeader("auditTimestamp", OffsetDateTime.now().toString());
    }

    /**
     * Creates audit entry for message persistence operations
     */
    public void createAuditEntry(Exchange exchange) throws Exception {
        process(exchange);
    }
}