package com.pixel.v2.db;

import java.time.format.DateTimeFormatter;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.pixel.v2.model.ErrorLogEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * ErrorLogEvent persistence processor for JPA operations.
 * 
 * This component handles the persistence of ErrorLogEvent entities to the database using JPA and
 * Spring transactions. Processes error events from the error handling pipeline and stores them in
 * the EXCEPTION table.
 */
@Component
public class ErrorLogEventPersistenceProcessor {

    private static final Logger logger =
            LoggerFactory.getLogger(ErrorLogEventPersistenceProcessor.class);

    @PersistenceContext
    private EntityManager entityManager;

    private ObjectMapper objectMapper;

    public ErrorLogEventPersistenceProcessor() {
        // Configure JavaTimeModule with custom LocalDateTime format
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(java.time.LocalDateTime.class,
                new LocalDateTimeDeserializer(formatter));
        customModule.addSerializer(java.time.LocalDateTime.class,
                new LocalDateTimeSerializer(formatter));

        // Build ObjectMapper with case-insensitive properties enabled
        this.objectMapper =
                JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                        .enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS)
                        .addModule(javaTimeModule).addModule(customModule)
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
    }

    /**
     * Persists an ErrorLogEvent entity from JSON string to the database.
     * 
     * @param jsonBody the JSON string representing an ErrorLogEvent
     */
    @Handler
    @Transactional
    public void persistErrorLogEventFromJson(@Body String jsonBody) {
        try {

            // Convert JSON to ErrorLogEvent entity using our configured ObjectMapper
            ErrorLogEvent errorLogEvent = objectMapper.readValue(jsonBody, ErrorLogEvent.class);

            logger.debug(
                    "K-DB-ERROR-LOG-EVENT: ErrorLogEvent entity created - ID: {}, Code: {}, Component: {}",
                    errorLogEvent.getLogId(), errorLogEvent.getCode(),
                    errorLogEvent.getComponent());

            persistErrorLogEvent(errorLogEvent);

            logger.debug(
                    "[K-DB-ERROR-LOG-EVENT] Successfully persisted ErrorLogEvent - ID: {}, Code: {}, Component: {}",
                    errorLogEvent.getLogId(), errorLogEvent.getCode(),
                    errorLogEvent.getComponent());

        } catch (Exception e) {
            logger.error(
                    "[K-DB-ERROR-LOG-EVENT] Failed to parse JSON and persist ErrorLogEvent - JSON: {}, Error: {}",
                    jsonBody, e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to parse JSON and persist ErrorLogEvent: " + e.getMessage(), e);
        }
    }

    /**
     * Persists an ErrorLogEvent entity to the database.
     * 
     * @param errorLogEvent the ErrorLogEvent entity to persist
     * @return the persisted ErrorLogEvent entity
     */
    @Transactional
    public ErrorLogEvent persistErrorLogEvent(ErrorLogEvent errorLogEvent) {
        try {
            logger.debug(
                    "K-DB: Persisting ErrorLogEvent entity - LogId: {}, Component: {}, Severity: {}",
                    errorLogEvent.getLogId(), errorLogEvent.getComponent(),
                    errorLogEvent.getSeverity());

            // Ensure DATATS is set to current timestamp if not already set
            if (errorLogEvent.getDatats() == null) {
                errorLogEvent.setDatats(java.time.LocalDateTime.now());
                logger.debug("K-DB: Set DATATS to current timestamp: {}",
                        errorLogEvent.getDatats());
            }

            // Check if entity already exists using logId as primary key
            ErrorLogEvent existingEntity = null;
            if (errorLogEvent.getLogId() != null) {
                existingEntity = entityManager.find(ErrorLogEvent.class, errorLogEvent.getLogId());
            }

            ErrorLogEvent result;
            if (existingEntity != null) {
                // Update existing entity
                logger.debug("[K-DB] Updating existing ErrorLogEvent entity with LogId: {}",
                        errorLogEvent.getLogId());
                result = entityManager.merge(errorLogEvent);
            } else {
                // Persist new entity
                logger.debug("[K-DB] Persisting new ErrorLogEvent entity with LogId: {}",
                        errorLogEvent.getLogId());
                entityManager.persist(errorLogEvent);
                result = errorLogEvent;
            }

            // Force synchronization to database
            entityManager.flush();

            logger.debug(
                    "[K-DB] ErrorLogEvent entity persisted successfully - LogId: {}, Component: {}, Severity: {}",
                    result.getLogId(), result.getComponent(), result.getSeverity());

            return result;

        } catch (Exception e) {
            logger.error("❌ K-DB: Failed to persist ErrorLogEvent entity - LogId: {}, Error: {}",
                    errorLogEvent.getLogId(), e.getMessage(), e);
            throw new RuntimeException("Failed to persist ErrorLogEvent entity with LogId: "
                    + errorLogEvent.getLogId(), e);
        }
    }
}
