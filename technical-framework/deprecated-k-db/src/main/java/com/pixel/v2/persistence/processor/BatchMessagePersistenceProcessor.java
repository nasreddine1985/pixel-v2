package com.pixel.v2.persistence.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Processor for batch insertion of messages into the database. Handles collections of messages and
 * performs batch SQL operations for better performance.
 */
@Component("batchMessagePersistenceProcessor")
public class BatchMessagePersistenceProcessor implements Processor {

    private static final Logger logger =
            LoggerFactory.getLogger(BatchMessagePersistenceProcessor.class);

    private static final String INSERT_SQL =
            "INSERT INTO pixel_v2.tb_messages (message_id, correlation_id, message_type, source, payload, processing_status, received_at, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, 'RECEIVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();

        if (!(body instanceof Collection)) {
            throw new IllegalArgumentException("Body must be a Collection for batch processing");
        }

        @SuppressWarnings("unchecked")
        Collection<Object> messages = (Collection<Object>) body;

        if (messages.isEmpty()) {
            logger.info("k-db-tx-messages: No messages to process in batch");
            exchange.getIn().setHeader("PersistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("BatchSize", 0);
            return;
        }

        // Get DataSource from Camel Registry
        DataSource dataSource = exchange.getContext().getRegistry()
                .lookupByNameAndType("dataSource", DataSource.class);
        if (dataSource == null) {
            throw new IllegalStateException(
                    "DataSource bean 'dataSource' not found in Camel registry");
        }

        int batchSize = messages.size();
        int successCount = 0;
        int failureCount = 0;

        logger.info("k-db-tx-messages: Starting batch persistence of {} messages", batchSize);

        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            connection.setAutoCommit(false); // Start transaction

            for (Object messageObj : messages) {
                try {
                    // Handle different message formats
                    Map<String, Object> messageData = extractMessageData(messageObj, exchange);

                    // Set parameters for the prepared statement
                    preparedStatement.setString(1, (String) messageData.get("messageId"));
                    preparedStatement.setString(2, (String) messageData.get("correlationId"));
                    preparedStatement.setString(3, (String) messageData.get("messageType"));
                    preparedStatement.setString(4, (String) messageData.get("source"));
                    preparedStatement.setString(5, (String) messageData.get("payload"));

                    preparedStatement.addBatch();
                    successCount++;

                } catch (Exception e) {
                    logger.error("k-db-tx-messages: Failed to prepare message for batch: {}",
                            e.getMessage());
                    failureCount++;
                }
            }

            if (successCount > 0) {
                // Execute batch
                preparedStatement.executeBatch();
                connection.commit();

                logger.info(
                        "k-db-tx-messages: ✅ Batch persistence completed - Success: {}, Failures: {}, Total: {}",
                        successCount, failureCount, batchSize);
            } else {
                connection.rollback();
                logger.warn("k-db-tx-messages: No messages could be prepared for batch insertion");
            }

            // Set result headers
            exchange.getIn().setHeader("PersistenceStatus",
                    failureCount == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
            exchange.getIn().setHeader("BatchSize", batchSize);
            exchange.getIn().setHeader("SuccessCount", successCount);
            exchange.getIn().setHeader("FailureCount", failureCount);
            exchange.getIn().setHeader("PersistenceOperation", "BATCH_INSERT");

            if (failureCount > 0) {
                String errorMsg = String.format(
                        "Batch processing completed with %d failures out of %d messages",
                        failureCount, batchSize);
                exchange.getIn().setHeader("PersistenceError", errorMsg);

                // If all messages failed, throw exception
                if (successCount == 0) {
                    throw new RuntimeException(
                            "All messages in batch failed to persist: " + errorMsg);
                }
            }

        } catch (SQLException e) {
            logger.error("k-db-tx-messages: ❌ Batch persistence failed - Error: {}",
                    e.getMessage());
            exchange.getIn().setHeader("PersistenceStatus", "FAILED");
            exchange.getIn().setHeader("PersistenceError", e.getMessage());
            exchange.getIn().setHeader("PersistenceOperation", "BATCH_INSERT_FAILED");

            // Re-throw as RuntimeException for parent route error handling
            throw new RuntimeException("Batch database persistence failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts message data from various message formats. Supports Map, String (JSON), and objects
     * with standard properties.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMessageData(Object messageObj, Exchange exchange)
            throws Exception {
        if (messageObj instanceof Map) {
            Map<String, Object> messageMap = (Map<String, Object>) messageObj;

            // Use message data or fallback to headers
            return Map
                    .of("messageId",
                            getValueOrDefault(messageMap, "messageId",
                                    exchange.getIn().getHeader("messageId", String.class)),
                            "correlationId",
                            getValueOrDefault(messageMap, "correlationId",
                                    exchange.getIn().getHeader("correlationId", String.class)),
                            "messageType",
                            getValueOrDefault(messageMap, "messageType",
                                    exchange.getIn().getHeader("messageType", String.class)),
                            "source",
                            getValueOrDefault(messageMap, "source",
                                    exchange.getIn().getHeader("source", String.class)),
                            "payload",
                            getValueOrDefault(messageMap, "payload", messageObj.toString()));
        } else if (messageObj instanceof String) {
            // Treat string as payload, use headers for other fields
            return Map.of("messageId", generateUniqueId(), "correlationId",
                    exchange.getIn().getHeader("correlationId", String.class), "messageType",
                    exchange.getIn().getHeader("messageType", String.class), "source",
                    exchange.getIn().getHeader("source", String.class), "payload",
                    (String) messageObj);
        } else {
            // For other object types, convert to string and use headers
            return Map.of("messageId", generateUniqueId(), "correlationId",
                    exchange.getIn().getHeader("correlationId", String.class), "messageType",
                    exchange.getIn().getHeader("messageType", String.class), "source",
                    exchange.getIn().getHeader("source", String.class), "payload",
                    messageObj.toString());
        }
    }

    private Object getValueOrDefault(Map<String, Object> map, String key, Object defaultValue) {
        Object value = map.get(key);
        return value != null ? value : defaultValue;
    }

    private String generateUniqueId() {
        return "MSG-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8);
    }
}
