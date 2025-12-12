package com.pixel.v2.db;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pixel.v2.persistence.model.LogEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * LogEvent persistence processor for JPA operations.
 * 
 * This component handles the persistence of LogEvent entities to the database using JPA and Spring
 * transactions.
 */
@Component
public class LogEventPersistenceProcessor {

    private static final Logger logger =
            LoggerFactory.getLogger(LogEventPersistenceProcessor.class);

    @PersistenceContext
    private EntityManager entityManager;

    private ObjectMapper objectMapper;

    public LogEventPersistenceProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Persists a LogEvent entity from JSON string to the database.
     * 
     * @param jsonBody the JSON string representing a LogEvent
     * @return the persisted LogEvent entity
     */
    @Handler
    @Transactional
    public LogEvent persistLogEventFromJson(@Body String jsonBody) {
        try {

            // Convert JSON to LogEvent entity using our configured ObjectMapper
            LogEvent logEvent = objectMapper.readValue(jsonBody, LogEvent.class);

            logger.debug(
                    "K-DB-LOG-EVENT: LogEvent entity created - ID: {}, Code: {}, Component: {}",
                    logEvent.getLogId(), logEvent.getCode(), logEvent.getComponent());

            return persistLogEvent(logEvent);

        } catch (Exception e) {
            logger.error(
                    "K-DB-LOG-EVENT: Failed to parse JSON and persist LogEvent - JSON: {}, Error: {}",
                    jsonBody, e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to parse JSON and persist LogEvent: " + e.getMessage(), e);
        }
    }

    /**
     * Persists a LogEvent entity to the database.
     * 
     * @param logEvent the LogEvent entity to persist
     * @return the persisted LogEvent entity
     */
    @Transactional
    public LogEvent persistLogEvent(LogEvent logEvent) {
        try {
            logger.info("K-DB: Persisting LogEvent entity - LogId: {}, FlowId: {}, Component: {}",
                    logEvent.getLogId(), logEvent.getFlowId(), logEvent.getComponent());

            // Check if entity already exists using logId as primary key
            LogEvent existingEntity = null;
            if (logEvent.getLogId() != null) {
                existingEntity = entityManager.find(LogEvent.class, logEvent.getLogId());
            }

            LogEvent result;
            if (existingEntity != null) {
                // Update existing entity
                logger.debug("K-DB: Updating existing LogEvent entity with LogId: {}",
                        logEvent.getLogId());
                result = entityManager.merge(logEvent);
            } else {
                // Persist new entity
                logger.debug("K-DB: Persisting new LogEvent entity with LogId: {}",
                        logEvent.getLogId());
                entityManager.persist(logEvent);
                result = logEvent;
            }

            // Force synchronization to database
            entityManager.flush();

            logger.info(
                    "K-DB: LogEvent entity persisted successfully - LogId: {}, FlowId: {}, Component: {}",
                    result.getLogId(), result.getFlowId(), result.getComponent());

            return result;

        } catch (Exception e) {
            logger.error("❌ K-DB: Failed to persist LogEvent entity - LogId: {}, Error: {}",
                    logEvent.getLogId(), e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to persist LogEvent entity with LogId: " + logEvent.getLogId(), e);
        }
    }
}
