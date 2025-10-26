package com.pixel.v2.idempotence.repository.impl;

import com.pixel.v2.idempotence.model.ProcessedIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryIdempotenceRepository
 */
class InMemoryIdempotenceRepositoryTest {
    
    private InMemoryIdempotenceRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryIdempotenceRepository();
    }
    
    @Test
    void testSaveAndFind() {
        ProcessedIdentifier identifier = new ProcessedIdentifier("INSTR123", "InstrId", "MSG123");
        
        ProcessedIdentifier saved = repository.save(identifier);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        
        Optional<ProcessedIdentifier> found = repository.findByIdentifierAndType("INSTR123", "InstrId");
        assertTrue(found.isPresent());
        assertEquals("INSTR123", found.get().getIdentifier());
        assertEquals("InstrId", found.get().getIdentifierType());
        assertEquals("MSG123", found.get().getMessageId());
    }
    
    @Test
    void testFindNonExistent() {
        Optional<ProcessedIdentifier> notFound = repository.findByIdentifierAndType("NONEXISTENT", "InstrId");
        assertFalse(notFound.isPresent());
    }
    
    @Test
    void testUpdate() {
        ProcessedIdentifier identifier = new ProcessedIdentifier("INSTR456", "InstrId", "MSG456");
        repository.save(identifier);
        
        ProcessedIdentifier updated = repository.update(identifier);
        assertEquals(2, updated.getAccessCount());
        
        Optional<ProcessedIdentifier> found = repository.findByIdentifierAndType("INSTR456", "InstrId");
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getAccessCount());
    }
    
    @Test
    void testCount() {
        assertEquals(0, repository.count());
        
        repository.save(new ProcessedIdentifier("INSTR1", "InstrId", "MSG1"));
        assertEquals(1, repository.count());
        
        repository.save(new ProcessedIdentifier("INSTR2", "InstrId", "MSG2"));
        assertEquals(2, repository.count());
    }
    
    @Test
    void testCountByType() {
        repository.save(new ProcessedIdentifier("INSTR1", "InstrId", "MSG1"));
        repository.save(new ProcessedIdentifier("E2E1", "EndToEndId", "MSG1"));
        repository.save(new ProcessedIdentifier("INSTR2", "InstrId", "MSG2"));
        
        assertEquals(2, repository.countByType("InstrId"));
        assertEquals(1, repository.countByType("EndToEndId"));
        assertEquals(0, repository.countByType("MsgId"));
    }
    
    @Test
    void testDeleteExpiredIdentifiers() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(5);
        
        // Create identifier with past date
        ProcessedIdentifier oldIdentifier = new ProcessedIdentifier("OLD123", "InstrId", "MSG123");
        oldIdentifier.setFirstProcessedAt(past);
        repository.save(oldIdentifier);
        
        // Create recent identifier
        ProcessedIdentifier newIdentifier = new ProcessedIdentifier("NEW123", "InstrId", "MSG124");
        repository.save(newIdentifier);
        
        assertEquals(2, repository.count());
        
        // Delete identifiers older than 3 days
        int deleted = repository.deleteExpiredIdentifiers(now.minusDays(3));
        assertEquals(1, deleted);
        assertEquals(1, repository.count());
        
        // Only the new identifier should remain
        assertTrue(repository.findByIdentifierAndType("NEW123", "InstrId").isPresent());
        assertFalse(repository.findByIdentifierAndType("OLD123", "InstrId").isPresent());
    }
    
    @Test
    void testExpiredIdentifierAutoRemoval() {
        ProcessedIdentifier identifier = new ProcessedIdentifier("EXPIRED123", "InstrId", "MSG123");
        identifier.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired 1 hour ago
        repository.save(identifier);
        
        // When searching for expired identifier, it should be automatically removed
        Optional<ProcessedIdentifier> found = repository.findByIdentifierAndType("EXPIRED123", "InstrId");
        assertFalse(found.isPresent());
        assertEquals(0, repository.count());
    }
    
    @Test
    void testClear() {
        repository.save(new ProcessedIdentifier("INSTR1", "InstrId", "MSG1"));
        repository.save(new ProcessedIdentifier("INSTR2", "InstrId", "MSG2"));
        
        assertEquals(2, repository.count());
        
        repository.clear();
        assertEquals(0, repository.count());
    }
    
    @Test
    void testIsHealthy() {
        assertTrue(repository.isHealthy());
    }
    
    @Test
    void testDifferentIdentifierTypes() {
        repository.save(new ProcessedIdentifier("SAME123", "InstrId", "MSG1"));
        repository.save(new ProcessedIdentifier("SAME123", "EndToEndId", "MSG2"));
        
        // Same identifier value but different types should be stored separately
        assertEquals(2, repository.count());
        
        assertTrue(repository.findByIdentifierAndType("SAME123", "InstrId").isPresent());
        assertTrue(repository.findByIdentifierAndType("SAME123", "EndToEndId").isPresent());
        assertFalse(repository.findByIdentifierAndType("SAME123", "MsgId").isPresent());
    }
}