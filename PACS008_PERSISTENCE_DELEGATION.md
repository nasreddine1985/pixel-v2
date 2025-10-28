# PACS008 Persistence Delegation to k-db-tx Kamelet

## Overview

This document describes the migration of all persistence logic from the flow-pacs-008 module to the k-db-tx kamelet, achieving complete separation of concerns between business logic and data persistence.

## Architecture Changes

### Before (Direct Persistence)

```
flow-pacs-008 → Direct Processors → Database
├── MessagePersistenceProcessor
├── MessageBatchPersistenceProcessor
└── ErrorPersistenceProcessor
```

### After (Kamelet-based Persistence)

```
flow-pacs-008 → k-db-tx Kamelet → Database
└── Clean business logic only

k-db-tx Kamelet:
├── Pacs008Message Entity
├── MessageError Entity
├── Pacs008MessageRepository
├── MessageErrorRepository
├── MessagePersistenceProcessor (generic)
├── Pacs008BatchPersistenceProcessor (specific)
└── Pacs008ErrorPersistenceProcessor (specific)
```

## Components Migrated to k-db-tx

### 1. Data Models

- **Pacs008Message.java** - PACS.008 message entity with JMS metadata
- **MessageError.java** - Error tracking entity for failed processing

### 2. Repositories

- **Pacs008MessageRepository** - JPA repository for PACS.008 message queries
- **MessageErrorRepository** - JPA repository for error tracking queries

### 3. Processors (New PACS008-specific)

- **Pacs008BatchPersistenceProcessor** - Handles batch persistence for performance
- **Pacs008ErrorPersistenceProcessor** - Handles error logging and persistence

### 4. Updated Dependencies

Added to k-db-tx/pom.xml:

- Spring Data JPA 3.1.5
- Hibernate Core 6.2.9.Final

## Route Changes

### Updated PACS008 Routes

The flow module routes now delegate to k-db-tx kamelet:

```yaml
# Batch persistence via k-db-tx
- to:
    uri: "kamelet:k-db-tx"
    parameters:
      entityType: "MESSAGE"
      persistenceOperation: "CREATE"
      enableAuditTrail: "true"
```

### Headers Set for k-db-tx Integration

- `messageType`: "PACS.008"
- `messageSource`: "JMS_QUEUE"
- `EntityType`: "MESSAGE"
- `PersistenceOperation`: "CREATE"

## Benefits Achieved

### 1. Separation of Concerns

- **flow-pacs-008**: Pure business logic, message routing, transformations
- **k-db-tx**: Pure persistence logic, database operations, transaction management

### 2. Reusability

- k-db-tx kamelet can be used by any module requiring persistence
- PACS008-specific processors can handle other payment message types
- Generic message processor handles common persistence patterns

### 3. Maintainability

- Persistence logic centralized in one location
- Database schema changes only affect k-db-tx kamelet
- Business logic changes don't impact persistence layer

### 4. Testability

- Can test business logic without database dependencies
- Can test persistence logic independently
- Mock kamelet for integration testing

### 5. Performance

- Batch processing optimized in dedicated processors
- Connection pooling and transaction management centralized
- JPA second-level caching can be configured centrally

## Current State

### Working Components ✅

- k-mq-message-receiver kamelet consuming from ActiveMQ Artemis
- Message batch aggregation (1000 messages or 1 second timeout)
- YAML route syntax fully functional
- Application processing messages successfully

### Migrated Components ✅

- Pacs008Message and MessageError entities in k-db-tx
- Repository interfaces with Spring Data JPA
- PACS008-specific processors for batch and error handling
- Updated dependencies in k-db-tx pom.xml
- Updated routes to use k-db-tx kamelet

### Next Steps 🔄

1. **Build and Test**: Compile k-db-tx with new dependencies
2. **Remove Legacy**: Clean up old processors from flow module
3. **Configuration**: Ensure JPA configuration works with kamelet
4. **Integration Test**: Verify end-to-end message processing
5. **Performance Test**: Validate batch processing performance

## Configuration Notes

### JPA Configuration

The k-db-tx kamelet uses these configuration properties:

- `entityManagerRef`: "entityManager"
- `persistenceUnitName`: "pixelPersistenceUnit"
- `transactionManagerRef`: "transactionManager"

### Kamelet Parameters

- `entityType`: Controls which processor to use (MESSAGE/CDM)
- `persistenceOperation`: CREATE for new records, UPDATE for existing
- `enableAuditTrail`: Whether to create audit trail entries

## Error Handling

### Persistence Errors

- Errors set `persistenceStatus` header to "ERROR"
- Error details available in `persistenceError` header
- Transactions rolled back on exceptions

### Processing Errors

- PACS008ErrorPersistenceProcessor logs processing failures
- Error entities track stack traces and metadata
- Separate error handling route processes failures

## Performance Considerations

### Batch Processing

- Default batch size: 1000 messages
- Timeout: 1 second for partial batches
- Batch processor handles List<Exchange> or List<String>

### Transaction Management

- Each persistence operation uses separate transaction
- Batch operations more efficient than individual saves
- Error handling preserves transaction boundaries

## Monitoring and Logging

### Success Logging

- Batch persistence logs message counts
- Individual persistence logs message IDs
- Kamelet provides structured response JSON

### Error Logging

- Processing errors logged with full context
- Stack traces captured for debugging
- JMS message IDs preserved for tracing

This architecture provides a clean, maintainable, and performant foundation for PACS008 message processing with complete persistence delegation to the k-db-tx kamelet.
