package com.pixel.v2.persistence.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entity for persisting CDM (Common Data Model) messages after transformation from various payment
 * message formats (PACS.008, PAN.001, etc.)
 */
@Entity
@Table(name = "CDM_MESSAGE",
        indexes = {@Index(name = "idx_cdm_created_at", columnList = "createdAt"),
                @Index(name = "idx_cdm_message_id", columnList = "messageId"),
                @Index(name = "idx_cdm_original_message_id", columnList = "originalMessageId"),
                @Index(name = "idx_cdm_message_type", columnList = "messageType"),
                @Index(name = "idx_cdm_enrichment_status", columnList = "enrichmentStatus")})
public class CdmMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String messageId;

    @Column(length = 255)
    private String originalMessageId;

    @Column(nullable = false, length = 50)
    private String messageType; // PACS008, PAN001, etc.

    @Column(nullable = false, length = 50)
    private String source; // IBM_MQ, HTTP_API, CFT_FILE

    // CDM-specific fields
    @Column(length = 50)
    private String enrichmentStatus; // ENRICHED, PENDING, FAILED

    @Column(nullable = false)
    private OffsetDateTime creationDateTime;

    private Integer numberOfTransactions;

    // Processing metadata
    @Column(length = 50)
    private String processingStatus; // PROCESSED, PENDING, FAILED

    private OffsetDateTime processedAt;

    @Column(length = 1000)
    private String processingError;

    // CDM payload as JSON
    @Column(nullable = false, columnDefinition = "TEXT")
    private String cdmPayload;

    // Original message payload (for reference)
    @Column(columnDefinition = "TEXT")
    private String originalPayload;

    // Reference to original received message
    private Long originalReceivedMessageId;

    // Audit fields
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    // Default constructor
    public CdmMessage() {
        this.createdAt = OffsetDateTime.now();
        this.creationDateTime = OffsetDateTime.now();
        this.processingStatus = "PENDING";
        this.enrichmentStatus = "PENDING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEnrichmentStatus() {
        return enrichmentStatus;
    }

    public void setEnrichmentStatus(String enrichmentStatus) {
        this.enrichmentStatus = enrichmentStatus;
    }

    public OffsetDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(OffsetDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Integer getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(Integer numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getProcessingError() {
        return processingError;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }

    public String getCdmPayload() {
        return cdmPayload;
    }

    public void setCdmPayload(String cdmPayload) {
        this.cdmPayload = cdmPayload;
    }

    public String getOriginalPayload() {
        return originalPayload;
    }

    public void setOriginalPayload(String originalPayload) {
        this.originalPayload = originalPayload;
    }

    public Long getOriginalReceivedMessageId() {
        return originalReceivedMessageId;
    }

    public void setOriginalReceivedMessageId(Long originalReceivedMessageId) {
        this.originalReceivedMessageId = originalReceivedMessageId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CdmMessage{" + "id=" + id + ", messageId='" + messageId + '\''
                + ", originalMessageId='" + originalMessageId + '\'' + ", messageType='"
                + messageType + '\'' + ", source='" + source + '\'' + ", enrichmentStatus='"
                + enrichmentStatus + '\'' + ", processingStatus='" + processingStatus + '\''
                + ", createdAt=" + createdAt + '}';
    }
}
