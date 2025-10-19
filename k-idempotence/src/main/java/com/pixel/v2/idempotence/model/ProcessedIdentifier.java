package com.pixel.v2.idempotence.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for persisting processed payment identifiers to prevent duplicates
 */
@Entity
@Table(name = "processed_identifiers", 
       indexes = {
           @Index(name = "idx_identifier_type", columnList = "identifier, identifier_type", unique = true),
           @Index(name = "idx_first_processed", columnList = "first_processed_at"),
           @Index(name = "idx_last_accessed", columnList = "last_accessed_at")
       })
public class ProcessedIdentifier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "identifier", nullable = false, length = 100)
    private String identifier;
    
    @Column(name = "identifier_type", nullable = false, length = 50)
    private String identifierType;
    
    @Column(name = "message_id", length = 100)
    private String messageId;
    
    @Column(name = "first_processed_at", nullable = false)
    private LocalDateTime firstProcessedAt;
    
    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "access_count", nullable = false)
    private Integer accessCount;
    
    @Column(name = "message_hash", length = 64)
    private String messageHash;
    
    @Column(name = "source_system", length = 50)
    private String sourceSystem;
    
    @Column(name = "processing_status", length = 20)
    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    public enum ProcessingStatus {
        PROCESSED, FAILED, PENDING, DUPLICATE
    }
    
    public ProcessedIdentifier() {
        this.firstProcessedAt = LocalDateTime.now();
        this.lastAccessedAt = this.firstProcessedAt;
        this.accessCount = 1;
        this.processingStatus = ProcessingStatus.PROCESSED;
    }
    
    public ProcessedIdentifier(String identifier, String identifierType, String messageId) {
        this();
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.messageId = messageId;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getIdentifierType() {
        return identifierType;
    }
    
    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public LocalDateTime getFirstProcessedAt() {
        return firstProcessedAt;
    }
    
    public void setFirstProcessedAt(LocalDateTime firstProcessedAt) {
        this.firstProcessedAt = firstProcessedAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    public Integer getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }
    
    public String getMessageHash() {
        return messageHash;
    }
    
    public void setMessageHash(String messageHash) {
        this.messageHash = messageHash;
    }
    
    public String getSourceSystem() {
        return sourceSystem;
    }
    
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
    
    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }
    
    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    // Helper methods
    public void incrementAccess() {
        this.accessCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
    
    @PrePersist
    public void prePersist() {
        if (firstProcessedAt == null) {
            firstProcessedAt = LocalDateTime.now();
        }
        if (lastAccessedAt == null) {
            lastAccessedAt = firstProcessedAt;
        }
        if (accessCount == null) {
            accessCount = 1;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        lastAccessedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("ProcessedIdentifier{id=%d, identifier='%s', type='%s', messageId='%s', accessCount=%d}", 
                           id, identifier, identifierType, messageId, accessCount);
    }
}