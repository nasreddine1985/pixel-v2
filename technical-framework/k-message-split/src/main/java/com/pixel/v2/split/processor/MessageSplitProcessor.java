package com.pixel.v2.split.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * MessageSplitProcessor is responsible for splitting a collection of messages based on configured
 * properties. This processor performs the inverse operation of message concatenation.
 * 
 * The processor examines the input collection and splits it based on property rules: -
 * correlationId: Messages are grouped by this identifier - size: Maximum number of messages per
 * group - timeout: Not applicable for splitting (used for aggregation)
 * 
 * Only properties with non-empty values are considered for splitting logic.
 * 
 * @author Pixel V2 Framework
 * @since 1.0.0
 */
@Component
public class MessageSplitProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(MessageSplitProcessor.class);

    // Constants for property names
    private static final String CORRELATION_ID_PROPERTY = "correlationId";
    private static final String SIZE_PROPERTY = "size";
    private static final String SPLIT_STRATEGY_PROPERTY = "splitStrategy";

    // Split strategy constants
    private static final String STRATEGY_BY_CORRELATION = "BY_CORRELATION";

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.debug("Starting message split processing");

        try {
            // Get the input collection from message body
            Object body = exchange.getIn().getBody();
            if (body == null) {
                logger.warn("Message body is null, no splitting to perform");
                exchange.getIn().setBody(Collections.emptyList());
                exchange.getIn().setHeader("splitCount", 0);
                exchange.getIn().setHeader("originalSize", 0);
                return;
            }

            Collection<?> inputCollection = extractCollection(body);
            if (inputCollection.isEmpty()) {
                logger.info("Input collection is empty, no splitting to perform");
                exchange.getIn().setBody(Collections.emptyList());
                exchange.getIn().setHeader("splitCount", 0);
                exchange.getIn().setHeader("originalSize", 0);
                return;
            }

            logger.info("Processing collection with {} messages for splitting",
                    inputCollection.size());

            // Get split configuration from headers/properties
            SplitConfiguration config = extractSplitConfiguration(exchange);

            // Perform the split operation
            List<List<Object>> splitResult = performSplit(inputCollection, config);

            // Set the result in the exchange
            exchange.getIn().setBody(splitResult);
            exchange.getIn().setHeader("splitCount", splitResult.size());
            exchange.getIn().setHeader("originalSize", inputCollection.size());

            logger.info("Successfully split collection into {} groups", splitResult.size());

        } catch (Exception e) {
            logger.error("Error during message splitting: {}", e.getMessage(), e);
            exchange.getIn().setHeader("splitError", e.getMessage());
            exchange.getIn().setBody(Collections.emptyList());
            throw e;
        }
    }

    /**
     * Extracts a collection from the input body
     */
    @SuppressWarnings("unchecked")
    private Collection<?> extractCollection(Object body) {
        if (body instanceof Collection) {
            return (Collection<?>) body;
        } else if (body instanceof Object[]) {
            return Arrays.asList((Object[]) body);
        } else {
            // Single object - wrap in collection
            return Collections.singletonList(body);
        }
    }

    /**
     * Extracts split configuration from exchange headers and properties
     */
    private SplitConfiguration extractSplitConfiguration(Exchange exchange) {
        Message message = exchange.getIn();

        String correlationId = getStringProperty(message, CORRELATION_ID_PROPERTY);
        String sizeStr = getStringProperty(message, SIZE_PROPERTY);
        String strategy = getStringProperty(message, SPLIT_STRATEGY_PROPERTY);

        Integer size = null;
        if (StringUtils.isNotBlank(sizeStr)) {
            try {
                size = Integer.valueOf(sizeStr);
                if (size <= 0) {
                    logger.warn("Invalid size value: {}, ignoring", size);
                    size = null;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid size format: {}, ignoring", sizeStr);
            }
        }

        SplitConfiguration config = new SplitConfiguration();
        config.setCorrelationId(correlationId);
        config.setSize(size);
        config.setSplitStrategy(
                StringUtils.isNotBlank(strategy) ? strategy : STRATEGY_BY_CORRELATION);

        logger.debug("Split configuration: correlationId={}, size={}, strategy={}", correlationId,
                size, config.getSplitStrategy());

        return config;
    }

    /**
     * Gets a string property from message headers or exchange properties
     */
    private String getStringProperty(Message message, String propertyName) {
        // First check message headers
        Object value = message.getHeader(propertyName);
        if (value != null) {
            return value.toString().trim();
        }

        // Then check exchange properties
        value = message.getExchange().getProperty(propertyName);
        if (value != null) {
            return value.toString().trim();
        }

        return null;
    }

    /**
     * Performs the actual split operation based on configuration
     */
    private List<List<Object>> performSplit(Collection<?> inputCollection,
            SplitConfiguration config) {
        List<Object> inputList = new ArrayList<>(inputCollection);

        // Determine split strategy
        if (StringUtils.isNotBlank(config.getCorrelationId())) {
            return splitByCorrelation(inputList, config);
        } else if (config.getSize() != null) {
            return splitBySize(inputList, config.getSize());
        } else {
            // Default: return each message as separate group
            return splitIndividually(inputList);
        }
    }

    /**
     * Splits messages by correlation ID
     */
    private List<List<Object>> splitByCorrelation(List<Object> inputList,
            SplitConfiguration config) {
        Map<String, List<Object>> correlationGroups = new LinkedHashMap<>();
        String correlationProperty = config.getCorrelationId();

        for (Object message : inputList) {
            String correlationValue = extractCorrelationValue(message, correlationProperty);

            if (StringUtils.isBlank(correlationValue)) {
                correlationValue = "DEFAULT"; // Default group for messages without correlation
            }

            correlationGroups.computeIfAbsent(correlationValue, k -> new ArrayList<>())
                    .add(message);
        }

        logger.debug("Split by correlation into {} groups", correlationGroups.size());
        return new ArrayList<>(correlationGroups.values());
    }

    /**
     * Splits messages by size (fixed-size chunks)
     */
    private List<List<Object>> splitBySize(List<Object> inputList, int chunkSize) {
        List<List<Object>> result = new ArrayList<>();

        for (int i = 0; i < inputList.size(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, inputList.size());
            result.add(new ArrayList<>(inputList.subList(i, endIndex)));
        }

        logger.debug("Split by size {} into {} chunks", chunkSize, result.size());
        return result;
    }

    /**
     * Splits each message into individual groups
     */
    private List<List<Object>> splitIndividually(List<Object> inputList) {
        List<List<Object>> result = new ArrayList<>();

        for (Object message : inputList) {
            result.add(Collections.singletonList(message));
        }

        logger.debug("Split individually into {} groups", result.size());
        return result;
    }

    /**
     * Extracts correlation value from a message object
     */
    private String extractCorrelationValue(Object message, String correlationProperty) {
        if (message == null || StringUtils.isBlank(correlationProperty)) {
            return null;
        }

        try {
            // Try different approaches to extract the correlation value
            if (message instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageMap = (Map<String, Object>) message;
                Object value = messageMap.get(correlationProperty);
                return value != null ? value.toString() : null;
            }

            // Try reflection for bean properties
            String reflectionValue = extractUsingReflection(message, correlationProperty);
            if (reflectionValue != null) {
                return reflectionValue;
            }

            // Fallback: use toString and hope for the best
            String messageStr = message.toString();
            if (messageStr.contains(correlationProperty)) {
                // Simple pattern matching - this could be enhanced
                return extractFromString(messageStr, correlationProperty);
            }

        } catch (Exception e) {
            logger.debug("Failed to extract correlation value from message: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Extracts value using reflection
     */
    private String extractUsingReflection(Object message, String correlationProperty) {
        try {
            String methodName = "get" + StringUtils.capitalize(correlationProperty);
            Object value = message.getClass().getMethod(methodName).invoke(message);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            logger.debug("Reflection extraction failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Simple string-based extraction (fallback method)
     */
    private String extractFromString(String messageStr, String property) {
        try {
            String pattern = property + "=";
            int startIndex = messageStr.indexOf(pattern);
            if (startIndex >= 0) {
                startIndex += pattern.length();
                int endIndex = messageStr.indexOf(",", startIndex);
                if (endIndex < 0) {
                    endIndex = messageStr.indexOf("}", startIndex);
                }
                if (endIndex < 0) {
                    endIndex = messageStr.length();
                }
                return messageStr.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            logger.debug("String extraction failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Configuration class for split operations
     */
    private static class SplitConfiguration {
        private String correlationId;
        private Integer size;
        private String splitStrategy;

        public String getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getSplitStrategy() {
            return splitStrategy;
        }

        public void setSplitStrategy(String splitStrategy) {
            this.splitStrategy = splitStrategy;
        }
    }
}
