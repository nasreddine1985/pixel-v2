package com.pixel.v2.aggregation.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message Concatenation Processor Prepares messages for aggregation and sets correlation headers
 */
public class MessageConcatenationProcessor implements Processor {

    private static final Logger logger =
            LoggerFactory.getLogger(MessageConcatenationProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {

        logger.debug("[MESSAGE-CONCAT] Processing message for concatenation");

        // Extract aggregation properties from headers or route parameters
        String correlationId = extractProperty(exchange, "correlationId", "CorrelationId");
        String aggregationSize = extractProperty(exchange, "size", "AggregationSize");
        String aggregationTimeout = extractProperty(exchange, "timeout", "AggregationTimeout");

        // Only process if we have at least one non-empty property
        if (hasValidAggregationProperties(correlationId, aggregationSize, aggregationTimeout)) {

            // Set correlation ID for aggregation
            if (correlationId != null && !correlationId.trim().isEmpty()) {
                exchange.getIn().setHeader("CorrelationId", correlationId);
                logger.debug("[MESSAGE-CONCAT] Set correlation ID: {}", correlationId);
            }

            // Set aggregation size if specified
            if (aggregationSize != null && !aggregationSize.trim().isEmpty()) {
                try {
                    int size = Integer.parseInt(aggregationSize.trim());
                    exchange.getIn().setHeader("AggregationSize", size);
                    logger.debug("[MESSAGE-CONCAT] Set aggregation size: {}", size);
                } catch (NumberFormatException e) {
                    logger.warn("[MESSAGE-CONCAT] Invalid aggregation size format: {}, ignoring",
                            aggregationSize);
                }
            }

            // Set aggregation timeout if specified
            if (aggregationTimeout != null && !aggregationTimeout.trim().isEmpty()) {
                try {
                    long timeout = Long.parseLong(aggregationTimeout.trim());
                    exchange.getIn().setHeader("AggregationTimeout", timeout);
                    logger.debug("[MESSAGE-CONCAT] Set aggregation timeout: {}ms", timeout);
                } catch (NumberFormatException e) {
                    logger.warn("[MESSAGE-CONCAT] Invalid aggregation timeout format: {}, ignoring",
                            aggregationTimeout);
                }
            }

            // Set processing metadata
            exchange.getIn().setHeader("MessageConcatenationEnabled", true);
            exchange.getIn().setHeader("MessageProcessingTime", System.currentTimeMillis());

            logger.info(
                    "[MESSAGE-CONCAT] ✅ Message prepared for aggregation - CorrelationId: {}, Size: {}, Timeout: {}ms",
                    correlationId, aggregationSize, aggregationTimeout);

        } else {
            // No valid aggregation properties - pass through without aggregation
            exchange.getIn().setHeader("MessageConcatenationEnabled", false);
            logger.info(
                    "[MESSAGE-CONCAT] ⚠️ No valid aggregation properties found, message will pass through without aggregation");
        }

        // Preserve original message body
        Object originalBody = exchange.getIn().getBody();
        if (originalBody != null) {
            exchange.getIn().setHeader("OriginalMessageSize", getMessageSize(originalBody));
            logger.debug("[MESSAGE-CONCAT] Original message size: {} bytes",
                    getMessageSize(originalBody));
        }
    }

    /**
     * Extract property value from exchange headers or route parameters
     */
    private String extractProperty(Exchange exchange, String propertyName, String headerName) {
        // First try to get from exchange headers
        String value = exchange.getIn().getHeader(headerName, String.class);

        // If not found, try alternative header name
        if (value == null || value.trim().isEmpty()) {
            value = exchange.getIn().getHeader(propertyName, String.class);
        }

        // Try property with different casing
        if (value == null || value.trim().isEmpty()) {
            value = exchange.getIn().getHeader(propertyName.toLowerCase(), String.class);
        }

        // Try property with camelCase
        if (value == null || value.trim().isEmpty()) {
            value = exchange.getIn().getHeader(toCamelCase(propertyName), String.class);
        }

        return value;
    }

    /**
     * Check if we have at least one non-empty aggregation property
     */
    private boolean hasValidAggregationProperties(String correlationId, String size,
            String timeout) {
        return (correlationId != null && !correlationId.trim().isEmpty())
                || (size != null && !size.trim().isEmpty())
                || (timeout != null && !timeout.trim().isEmpty());
    }

    /**
     * Convert property name to camelCase
     */
    private String toCamelCase(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '_' || ch == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(ch));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(ch));
            }
        }

        return result.toString();
    }

    /**
     * Get approximate message size
     */
    private long getMessageSize(Object message) {
        if (message == null) {
            return 0;
        }

        if (message instanceof String) {
            return ((String) message).getBytes().length;
        } else if (message instanceof byte[]) {
            return ((byte[]) message).length;
        } else {
            // Approximate size for other objects
            return message.toString().getBytes().length;
        }
    }
}
