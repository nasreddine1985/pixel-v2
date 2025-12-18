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
import com.pixel.v2.persistence.model.FlowSummary;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * FlowSummary persistence processor for JPA operations.
 * 
 * This component handles the persistence of FlowSummary entities to the database using JPA and
 * Spring transactions.
 */
@Component
public class FlowSummaryPersistenceProcessor {

    private static final Logger logger =
            LoggerFactory.getLogger(FlowSummaryPersistenceProcessor.class);

    @PersistenceContext
    private EntityManager entityManager;

    private ObjectMapper objectMapper;

    public FlowSummaryPersistenceProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Persists a FlowSummary entity from JSON string to the database.
     * 
     * @param jsonBody the JSON string representing a FlowSummary
     */
    @Handler
    @Transactional
    public void persistFlowSummaryFromJson(@Body String jsonBody) {
        try {


            // Convert JSON to FlowSummary entity using our configured ObjectMapper
            FlowSummary flowSummary = objectMapper.readValue(jsonBody, FlowSummary.class);

            logger.debug("K-DB-FLOW-SUMMARY: FlowSummary entity created - ID: {}, Code: {}",
                    flowSummary.getFlowOccurId(), flowSummary.getFlowCode());

            persistFlowSummary(flowSummary);

            logger.debug("K-DB-FLOW-SUMMARY: Successfully persisted FlowSummary - ID: {}, Code: {}",
                    flowSummary.getFlowOccurId(), flowSummary.getFlowCode());

        } catch (Exception e) {
            logger.error(
                    "K-DB-FLOW-SUMMARY: Failed to parse JSON and persist FlowSummary - JSON: {}, Error: {}",
                    jsonBody, e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to parse JSON and persist FlowSummary: " + e.getMessage(), e);
        }
    }

    /**
     * Persists a FlowSummary entity to the database.
     * 
     * @param flowSummary the FlowSummary entity to persist
     * @return the persisted FlowSummary entity
     */
    @Transactional
    public FlowSummary persistFlowSummary(FlowSummary flowSummary) {
        try {
            logger.debug("K-DB: Persisting FlowSummary entity - FlowOccurId: {}, FlowCode: {}",
                    flowSummary.getFlowOccurId(), flowSummary.getFlowCode());

            // Check if entity already exists using flowOccurId as primary key
            FlowSummary existingEntity = null;
            if (flowSummary.getFlowOccurId() != null) {
                existingEntity =
                        entityManager.find(FlowSummary.class, flowSummary.getFlowOccurId());
            }

            FlowSummary result;
            if (existingEntity != null) {
                // Update existing entity
                try {
                    logger.debug(
                            "K-DB: Updating existing FlowSummary entity with FlowOccurId: {} - JSON: {}",
                            flowSummary.getFlowOccurId(),
                            objectMapper.writeValueAsString(flowSummary));
                } catch (Exception e) {
                    logger.error(
                            "K-DB: Updating existing FlowSummary entity with FlowOccurId: {} - JSON serialization failed: {}",
                            flowSummary.getFlowOccurId(), e.getMessage());
                }

                result = entityManager.merge(flowSummary);
            } else {
                // Persist new entity
                logger.debug("K-DB: Persisting new FlowSummary entity with FlowOccurId: {}",
                        flowSummary.getFlowOccurId());
                entityManager.persist(flowSummary);
                result = flowSummary;
            }

            // Force synchronization to database
            entityManager.flush();

            logger.debug(
                    "K-DB: FlowSummary entity persisted successfully - FlowOccurId: {}, FlowCode: {}",
                    result.getFlowOccurId(), result.getFlowCode());

            return result;

        } catch (Exception e) {
            logger.error("K-DB: Failed to persist FlowSummary entity - FlowOccurId: {}, Error: {}",
                    flowSummary.getFlowOccurId(), e.getMessage(), e);
            throw new RuntimeException("Failed to persist FlowSummary entity with FlowOccurId: "
                    + flowSummary.getFlowOccurId(), e);
        }
    }
}
