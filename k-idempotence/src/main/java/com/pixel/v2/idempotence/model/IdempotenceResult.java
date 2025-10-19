package com.pixel.v2.idempotence.model;

import java.time.LocalDateTime;

/**
 * Represents the result of an idempotence check
 */
public class IdempotenceResult {
    
    private boolean isDuplicate;
    private String identifier;
    private String identifierType;
    private LocalDateTime firstProcessedAt;
    private LocalDateTime lastAccessedAt;
    private int accessCount;
    private String originalMessageId;
    private IdempotenceAction action;
    
    public enum IdempotenceAction {
        PROCESS, IGNORE, ERROR, WARN
    }
    
    public IdempotenceResult() {
        this.isDuplicate = false;
        this.accessCount = 0;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public IdempotenceResult(String identifier, String identifierType) {
        this();
        this.identifier = identifier;
        this.identifierType = identifierType;
    }
    
    // Getters and setters
    public boolean isDuplicate() {
        return isDuplicate;
    }
    
    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
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
    
    public int getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }
    
    public String getOriginalMessageId() {
        return originalMessageId;
    }
    
    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
    }
    
    public IdempotenceAction getAction() {
        return action;
    }
    
    public void setAction(IdempotenceAction action) {
        this.action = action;
    }
    
    // Helper methods
    public void incrementAccess() {
        this.accessCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public void markAsDuplicate(LocalDateTime firstProcessedAt, String originalMessageId) {
        this.isDuplicate = true;
        this.firstProcessedAt = firstProcessedAt;
        this.originalMessageId = originalMessageId;
        incrementAccess();
    }
    
    public void markAsFirstTime() {
        this.isDuplicate = false;
        this.firstProcessedAt = LocalDateTime.now();
        this.accessCount = 1;
        this.lastAccessedAt = this.firstProcessedAt;
    }
    
    @Override
    public String toString() {
        return String.format("IdempotenceResult{identifier='%s', type='%s', duplicate=%s, accessCount=%d}", 
                           identifier, identifierType, isDuplicate, accessCount);
    }
}