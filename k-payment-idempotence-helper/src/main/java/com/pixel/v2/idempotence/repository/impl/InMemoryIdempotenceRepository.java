package com.pixel.v2.idempotence.repository.impl;

import com.pixel.v2.idempotence.model.ProcessedIdentifier;
import com.pixel.v2.idempotence.repository.IdempotenceRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of IdempotenceRepository
 * Suitable for single-instance deployments or testing
 */
public class InMemoryIdempotenceRepository implements IdempotenceRepository {
    
    private final Map<String, ProcessedIdentifier> identifiers = new ConcurrentHashMap<>();
    
    @Override
    public Optional<ProcessedIdentifier> findByIdentifierAndType(String identifier, String identifierType) {
        String key = createKey(identifier, identifierType);
        ProcessedIdentifier found = identifiers.get(key);
        
        // Check if expired
        if (found != null && found.isExpired()) {
            identifiers.remove(key);
            return Optional.empty();
        }
        
        return Optional.ofNullable(found);
    }
    
    @Override
    public ProcessedIdentifier save(ProcessedIdentifier processedIdentifier) {
        String key = createKey(processedIdentifier.getIdentifier(), processedIdentifier.getIdentifierType());
        
        // Simulate auto-generated ID
        if (processedIdentifier.getId() == null) {
            processedIdentifier.setId(System.currentTimeMillis());
        }
        
        identifiers.put(key, processedIdentifier);
        return processedIdentifier;
    }
    
    @Override
    public ProcessedIdentifier update(ProcessedIdentifier processedIdentifier) {
        String key = createKey(processedIdentifier.getIdentifier(), processedIdentifier.getIdentifierType());
        processedIdentifier.incrementAccess();
        identifiers.put(key, processedIdentifier);
        return processedIdentifier;
    }
    
    @Override
    public int deleteExpiredIdentifiers(LocalDateTime cutoffDate) {
        int deleted = 0;
        var iterator = identifiers.entrySet().iterator();
        
        while (iterator.hasNext()) {
            var entry = iterator.next();
            ProcessedIdentifier identifier = entry.getValue();
            
            if (identifier.isExpired() || 
                (identifier.getFirstProcessedAt() != null && identifier.getFirstProcessedAt().isBefore(cutoffDate))) {
                iterator.remove();
                deleted++;
            }
        }
        
        return deleted;
    }
    
    @Override
    public long count() {
        return identifiers.size();
    }
    
    @Override
    public long countByType(String identifierType) {
        return identifiers.values().stream()
                .filter(p -> identifierType.equals(p.getIdentifierType()))
                .count();
    }
    
    @Override
    public boolean isHealthy() {
        return true; // In-memory is always healthy if created
    }
    
    @Override
    public void clear() {
        identifiers.clear();
    }
    
    private String createKey(String identifier, String identifierType) {
        return identifierType + ":" + identifier;
    }
    
    /**
     * Get current size (for testing)
     */
    public int size() {
        return identifiers.size();
    }
}