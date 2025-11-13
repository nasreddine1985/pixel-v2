package com.pixel.v2.aggregation.strategy;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Message Concatenation Aggregation Strategy Aggregates multiple messages into a collection based
 * on correlation properties
 */
public class MessageConcatenationStrategy implements AggregationStrategy {

    private static final Logger logger =
            LoggerFactory.getLogger(MessageConcatenationStrategy.class);

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        if (oldExchange == null) {
            // First message - initialize the aggregation
            logger.debug("[MESSAGE-CONCAT] Starting new aggregation with first message");

            List<Object> messageCollection = new ArrayList<>();
            messageCollection.add(newExchange.getIn().getBody());

            newExchange.getIn().setBody(messageCollection);

            // Copy important headers from the first message
            copyAggregationHeaders(newExchange);

            // Set aggregation metadata
            newExchange.getIn().setHeader("AggregationCount", 1);
            newExchange.getIn().setHeader("AggregationStartTime", System.currentTimeMillis());

            logger.debug("[MESSAGE-CONCAT] Initialized aggregation with message count: 1");
            return newExchange;
        }

        // Add new message to existing aggregation
        @SuppressWarnings("unchecked")
        List<Object> existingMessages = (List<Object>) oldExchange.getIn().getBody();

        if (existingMessages == null) {
            existingMessages = new ArrayList<>();
        }

        // Add the new message to the collection
        existingMessages.add(newExchange.getIn().getBody());

        // Update the body with the enhanced collection
        oldExchange.getIn().setBody(existingMessages);

        // Update aggregation metadata
        int currentCount = existingMessages.size();
        oldExchange.getIn().setHeader("AggregationCount", currentCount);

        // Update timing information
        Long startTime = oldExchange.getIn().getHeader("AggregationStartTime", Long.class);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            oldExchange.getIn().setHeader("AggregationDuration", duration);
        }

        // Copy any new important headers
        mergeHeaders(oldExchange, newExchange);

        logger.debug("[MESSAGE-CONCAT] Added message to aggregation, total count: {}",
                currentCount);

        return oldExchange;
    }

    /**
     * Copy important headers for aggregation tracking
     */
    private void copyAggregationHeaders(Exchange exchange) {
        // Preserve correlation and aggregation control headers
        String correlationId = exchange.getIn().getHeader("CorrelationId", String.class);
        String aggregationSize = exchange.getIn().getHeader("AggregationSize", String.class);
        String aggregationTimeout = exchange.getIn().getHeader("AggregationTimeout", String.class);

        if (correlationId != null) {
            exchange.getIn().setHeader("AggregationCorrelationId", correlationId);
        }
        if (aggregationSize != null) {
            exchange.getIn().setHeader("TargetAggregationSize", aggregationSize);
        }
        if (aggregationTimeout != null) {
            exchange.getIn().setHeader("AggregationTimeoutMs", aggregationTimeout);
        }

        logger.debug(
                "[MESSAGE-CONCAT] Copied aggregation headers - CorrelationId: {}, Size: {}, Timeout: {}",
                correlationId, aggregationSize, aggregationTimeout);
    }

    /**
     * Merge important headers from new exchange into old exchange
     */
    private void mergeHeaders(Exchange oldExchange, Exchange newExchange) {
        // Update last message timestamp
        oldExchange.getIn().setHeader("LastMessageTime", System.currentTimeMillis());

        // Merge any dynamic properties
        String newProperties = newExchange.getIn().getHeader("MessageProperties", String.class);
        if (newProperties != null && !newProperties.trim().isEmpty()) {
            String existingProperties =
                    oldExchange.getIn().getHeader("AggregatedProperties", String.class);
            if (existingProperties == null) {
                existingProperties = newProperties;
            } else {
                existingProperties = existingProperties + "," + newProperties;
            }
            oldExchange.getIn().setHeader("AggregatedProperties", existingProperties);
        }
    }
}
