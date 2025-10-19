package com.pixel.v2.idempotence.repository;

import com.pixel.v2.idempotence.model.ProcessedIdentifier;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for managing processed identifiers
 */
public interface IdempotenceRepository {
    
    /**
     * Check if an identifier has been processed before
     * @param identifier the unique identifier
     * @param identifierType the type of identifier (InstrId, EndToEndId, etc.)
     * @return Optional containing the processed identifier if found
     */
    Optional<ProcessedIdentifier> findByIdentifierAndType(String identifier, String identifierType);
    
    /**
     * Save a new processed identifier
     * @param processedIdentifier the identifier to save
     * @return the saved identifier
     */
    ProcessedIdentifier save(ProcessedIdentifier processedIdentifier);
    
    /**
     * Update an existing processed identifier (increment access count)
     * @param processedIdentifier the identifier to update
     * @return the updated identifier
     */
    ProcessedIdentifier update(ProcessedIdentifier processedIdentifier);
    
    /**
     * Delete expired identifiers
     * @param cutoffDate identifiers older than this date will be deleted
     * @return number of deleted records
     */
    int deleteExpiredIdentifiers(LocalDateTime cutoffDate);
    
    /**
     * Count total processed identifiers
     * @return total count
     */
    long count();
    
    /**
     * Count processed identifiers by type
     * @param identifierType the type to count
     * @return count for the specified type
     */
    long countByType(String identifierType);
    
    /**
     * Check if repository is available/healthy
     * @return true if repository can be accessed
     */
    boolean isHealthy();
    
    /**
     * Clear all processed identifiers (for testing)
     */
    void clear();
}