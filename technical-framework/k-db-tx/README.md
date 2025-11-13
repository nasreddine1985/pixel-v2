# K-DB-TX Persistence Layer

A comprehensive JPA-based persistence layer for the payment processing system. This module provides entities, repositories, services, and processors for handling database operations converted from Oracle to PostgreSQL.

## Overview

The `k-db-tx` module provides a complete persistence layer for four main database tables:

- **FLOW_REFERENCE**: Flow configuration and reference data
- **LOG_EVENT**: Event tracking and logging for payment flows
- **FLOW_SUMMARY**: Summary information for payment flows
- **PAYMENT_AUDIT_DETAILS**: Detailed audit information for payments

This module includes JPA entities, Spring Data repositories, transactional services, and Apache Camel processors for comprehensive database operations.

## Features

### Dual Entity Support

- **MESSAGE Mode**: Persists raw payment messages as ReceivedMessage entities
- **CDM Mode**: Persists transformed CDM objects as CdmMessage entities

### Operation Types

- **CREATE**: Creates new records in the database
- **UPDATE**: Updates existing records with new or enriched data

### Processing Capabilities

- **JPA Integration**: Uses Spring Data JPA for database operations
- **Transaction Management**: Supports configurable transaction handling with automatic rollback
- **JSON Parsing**: Automatic CDM message field extraction from JSON payload
- **Header-based Routing**: Intelligent routing based on entity type and operation
- **Audit Trail**: Optional audit trail creation for compliance
- **Error Handling**: Comprehensive error handling with meaningful responses
- **Batch Processing**: Configurable batch size for performance optimization
- **Timeout Management**: Configurable timeout for database operations

## Configuration

| Property                | Type    | Required | Default                  | Description                           |
| ----------------------- | ------- | -------- | ------------------------ | ------------------------------------- |
| `entityManagerRef`      | string  | Yes      | `"entityManager"`        | Reference to JPA EntityManager bean   |
| `persistenceUnitName`   | string  | No       | `"pixelPersistenceUnit"` | JPA persistence unit name             |
| `transactionManagerRef` | string  | No       | `"transactionManager"`   | Reference to Transaction Manager bean |
| `entityType`            | enum    | No       | `"MESSAGE"`              | Entity type: `MESSAGE` or `CDM`       |
| `persistenceOperation`  | enum    | No       | `"CREATE"`               | Operation: `CREATE` or `UPDATE`       |
| `enableAuditTrail`      | boolean | No       | `true`                   | Enable audit trail creation           |
| `batchSize`             | integer | No       | `1`                      | Batch size for operations             |
| `timeoutSeconds`        | integer | No       | `30`                     | Database operation timeout            |

## Usage

### In Camel Routes

```yaml
# Direct usage
- to: "kamelet:k-db-tx"

# With custom configuration
- to: "kamelet:k-db-tx?enableAuditTrail=false&batchSize=10"
```

### In Integration Files

```yaml
apiVersion: camel.apache.org/v1
kind: Integration
metadata:
  name: payment-persistence
spec:
  flows:
    - from:
        uri: "direct:persistMessage"
        steps:
          - to:
              uri: "kamelet:k-db-tx"
              parameters:
                entityManagerRef: "paymentEntityManager"
                enableAuditTrail: true
                batchSize: 5
```

## Message Headers

### Input Headers

- `messageId`: Unique identifier for the message
- `messageType`: Type of payment message (pacs.008, pan.001, etc.)
- `receiptTimestamp`: When the message was received

### Output Headers

- `persistenceTimestamp`: When persistence operation started
- `persistenceStatus`: SUCCESS or ERROR
- `persistedMessageId`: Database ID of the persisted message
- `persistenceError`: Error message if persistence failed

## Response Format

### Success Response

```json
{
  "status": "SUCCESS",
  "messageId": "MSG123456",
  "persistedId": "1001",
  "timestamp": "2023-10-19 10:30:05.123"
}
```

### Error Response

```json
{
  "status": "ERROR",
  "messageId": "MSG123456",
  "error": "Database connection timeout",
  "timestamp": "2023-10-19 10:30:05.123"
}
```

## Dependencies

### Required Beans

The kamelet expects the following beans to be available in the Camel context:

- `messagePersistenceProcessor`: Handles the actual JPA persistence
- `auditTrailProcessor`: Creates audit trail entries (if enabled)

### Database Dependencies

- Oracle JDBC driver
- JPA implementation (Hibernate)
- Spring Data JPA
- Transaction manager

## Error Handling

The kamelet provides comprehensive error handling:

1. **Validation**: Checks for empty message bodies
2. **Database Errors**: Catches and handles JPA exceptions
3. **Timeout**: Handles database operation timeouts
4. **Transaction Rollback**: Automatic rollback on errors

## Performance Considerations

- Use batch processing for high-volume scenarios
- Configure appropriate timeout values
- Monitor database connection pool
- Consider read replicas for audit queries

## Monitoring

The kamelet provides detailed logging at multiple levels:

- **DEBUG**: Detailed operation tracking
- **INFO**: Success/failure summaries
- **WARN**: Non-critical issues
- **ERROR**: Critical failures

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

## Example Integration

```yaml
# Complete integration example
- from:
    uri: "direct:processPayment"
    steps:
      - log: "Processing payment message: ${header.messageId}"

      # Validate message
      - choice:
          when:
            - simple: "${body} != null"
              steps:
                # Persist to database
                - to: "kamelet:k-db-tx"

                # Check persistence result
                - choice:
                    when:
                      - simple: "${header.persistenceStatus} == 'SUCCESS'"
                        steps:
                          - log: "Message persisted successfully"
                          - to: "direct:continueProcessing"
                    otherwise:
                      steps:
                        - log: "Persistence failed: ${header.persistenceError}"
                        - to: "direct:handleError"
```

## Troubleshooting

### Common Issues

1. **Database Connection**: Verify EntityManager bean configuration
2. **Transaction Issues**: Check transaction manager setup
3. **Performance**: Adjust batch size and timeout settings
4. **Memory**: Monitor heap usage with large messages

### Debug Configuration

Enable debug logging for detailed operation tracking:

```properties
logging.level.com.pixel.v2.kamelet.persistence=DEBUG
```
