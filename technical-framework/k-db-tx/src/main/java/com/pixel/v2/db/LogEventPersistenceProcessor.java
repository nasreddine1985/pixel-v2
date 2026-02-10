package com.pixel.v2.db;

import java.time.format.DateTimeFormatter;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.pixel.v2.model.ApplicationContext;
import com.pixel.v2.model.LogEvent;

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

                // Configure JavaTimeModule with custom LocalDateTime format
                JavaTimeModule javaTimeModule = new JavaTimeModule();
                DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

                SimpleModule customModule = new SimpleModule();
                customModule.addDeserializer(java.time.LocalDateTime.class,
                                new LocalDateTimeDeserializer(formatter));
                customModule.addSerializer(java.time.LocalDateTime.class,
                                new LocalDateTimeSerializer(formatter));

                this.objectMapper.registerModule(javaTimeModule);
                this.objectMapper.registerModule(customModule);
                this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }

        /**
         * Persists a LogEvent entity from JSON string to the database.
         * 
         * @param jsonBody the JSON string representing a LogEvent
         */
        @Handler
        @Transactional
        public void persistLogEventFromJson(@Body String jsonBody) {
                try {

                        // Convert JSON to LogEvent entity using our configured ObjectMapper
                        LogEvent logEvent = objectMapper.readValue(jsonBody, LogEvent.class);

                        logger.debug("K-DB-LOG-EVENT: LogEvent entity created - ID: {}, Code: {}, Component: {}",
                                        logEvent.getLogId(), logEvent.getCode(),
                                        logEvent.getComponent());

                        // Build ApplicationContext entities from applicationContextNames
                        buildApplicationContextsFromNames(logEvent);

                        persistLogEvent(logEvent);

                        logger.debug("[K-DB-LOG-EVENT] Successfully persisted LogEvent - ID: {}, Code: {}, Component: {}",
                                        logEvent.getLogId(), logEvent.getCode(),
                                        logEvent.getComponent());

                } catch (Exception e) {
                        logger.error("[K-DB-LOG-EVENT] Failed to parse JSON and persist LogEvent - JSON: {}, Error: {}",
                                        jsonBody, e.getMessage(), e);
                        throw new RuntimeException("Failed to parse JSON and persist LogEvent: "
                                        + e.getMessage(), e);
                }
        }

        /**
         * Builds ApplicationContext entities from applicationContextNames. Converts each string
         * name into a complete ApplicationContext entity.
         * 
         * @param logEvent the LogEvent to build application contexts for
         */
        private void buildApplicationContextsFromNames(LogEvent logEvent) {
                if (logEvent.getApplicationContextNames() == null
                                || logEvent.getApplicationContextNames().isEmpty()) {
                        return;
                }

                for (String contextName : logEvent.getApplicationContextNames()) {
                        if (contextName != null && !contextName.trim().isEmpty()) {
                                ApplicationContext appContext = new ApplicationContext();
                                appContext.setName(contextName.trim());
                                appContext.setLogId(logEvent.getLogId());
                                appContext.setDatats(logEvent.getDatats());
                                appContext.setFlowId(logEvent.getFlowId());
                                appContext.setLogEvent(logEvent);

                                logEvent.addApplicationContext(appContext);

                                logger.debug("[K-DB] Built ApplicationContext from string - Name: {}, LogId: {}",
                                                contextName, logEvent.getLogId());
                        }
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
                        logger.debug("K-DB: Persisting LogEvent entity - LogId: {}, FlowId: {}, Component: {}",
                                        logEvent.getLogId(), logEvent.getFlowId(),
                                        logEvent.getComponent());

                        // Set DBLOGTIMESTAMP to current timestamp
                        logEvent.setDbLogTimestamp(java.time.LocalDateTime.now());
                        logger.debug("K-DB: Set DBLOGTIMESTAMP to current timestamp: {}",
                                        logEvent.getDbLogTimestamp());

                        // Check if entity already exists using logId as primary key
                        LogEvent existingEntity = null;
                        if (logEvent.getLogId() != null) {
                                existingEntity = entityManager.find(LogEvent.class,
                                                logEvent.getLogId());
                        }

                        LogEvent result;
                        if (existingEntity != null) {
                                // Update existing entity
                                logger.debug("[K-DB] Updating existing LogEvent entity with LogId: {}",
                                                logEvent.getLogId());
                                result = entityManager.merge(logEvent);
                        } else {
                                // Persist new entity
                                logger.debug("[K-DB] Persisting new LogEvent entity with LogId: {}",
                                                logEvent.getLogId());
                                entityManager.persist(logEvent);
                                result = logEvent;
                        }

                        // Force synchronization to database
                        entityManager.flush();

                        logger.debug("K-DB: LogEvent entity persisted successfully - LogId: {}, FlowId: {}, Component: {}",
                                        result.getLogId(), result.getFlowId(),
                                        result.getComponent());

                        // Persist associated ApplicationContext entities if they exist
                        if (logEvent.getApplicationContexts() != null
                                        && !logEvent.getApplicationContexts().isEmpty()) {
                                persistApplicationContexts(result);
                        }

                        return result;

                } catch (Exception e) {
                        logger.error("❌ K-DB: Failed to persist LogEvent entity - LogId: {}, Error: {}",
                                        logEvent.getLogId(), e.getMessage(), e);
                        throw new RuntimeException("Failed to persist LogEvent entity with LogId: "
                                        + logEvent.getLogId(), e);
                }
        }

        /**
         * Persists ApplicationContext entities associated with a LogEvent.
         * 
         * @param logEvent the parent LogEvent entity with application contexts to persist
         */
        @Transactional
        private void persistApplicationContexts(LogEvent logEvent) {
                try {
                        for (ApplicationContext appContext : logEvent.getApplicationContexts()) {
                                // Ensure appContext has the same logId, datats, and flowId as the
                                // parent LogEvent
                                appContext.setLogId(logEvent.getLogId());
                                appContext.setDatats(logEvent.getDatats());
                                appContext.setFlowId(logEvent.getFlowId());
                                appContext.setLogEvent(logEvent);

                                logger.debug("[K-DB] Persisting ApplicationContext - LogId: {}, Name: {}, Value: {}",
                                                appContext.getLogId(), appContext.getName(),
                                                appContext.getValue() != null ? appContext
                                                                .getValue()
                                                                .substring(0, Math.min(50,
                                                                                appContext.getValue()
                                                                                                .length()))
                                                                : "null");

                                entityManager.persist(appContext);
                        }

                        // Force synchronization to database
                        entityManager.flush();

                        logger.debug("[K-DB] Successfully persisted {} ApplicationContext entities for LogId: {}",
                                        logEvent.getApplicationContexts().size(),
                                        logEvent.getLogId());

                } catch (Exception e) {
                        logger.error("❌ K-DB: Failed to persist ApplicationContext entities for LogId: {}, Error: {}",
                                        logEvent.getLogId(), e.getMessage(), e);
                        throw new RuntimeException(
                                        "Failed to persist ApplicationContext entities for LogId: "
                                                        + logEvent.getLogId(),
                                        e);
                }
        }
}
