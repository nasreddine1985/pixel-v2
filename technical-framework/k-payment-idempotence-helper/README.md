# k-payment-idempotence-helper Kamelet

Ensures payment messages can be safely submitted multiple times without causing duplicate transactions by tracking unique identifiers like Instruction ID and End-to-End ID. This kamelet provides idempotence guarantees for payment processing systems.

## Features

- **Duplicate Prevention**: Tracks unique identifiers to prevent duplicate transaction processing
- **Multiple Identifier Types**: Supports Instruction ID, End-to-End ID, Message ID, and custom identifiers
- **Configurable Actions**: ERROR (reject), IGNORE (skip), or WARN (process with warning) on duplicates
- **Storage Backends**: In-memory, database, or distributed cache storage options
- **Message Hashing**: Optional content-based duplicate detection using SHA-256 hashing
- **Expiry Management**: Automatic cleanup of old identifiers based on configurable retention periods
- **Source Tracking**: Track originating systems for audit and debugging purposes

## Properties

- `duplicateAction` (optional): Action when duplicate detected - "ERROR", "IGNORE", "WARN". Default: `ERROR`.
- `repositoryType` (optional): Storage backend - "MEMORY", "DATABASE", "CACHE". Default: `MEMORY`.
- `identifierTypes` (optional): Comma-separated identifier types to track. Default: `InstrId,EndToEndId`.
- `enableHashing` (optional): Generate SHA-256 hash of message content for additional duplicate detection. Default: `false`.
- `expiryDays` (optional): Days after which identifiers expire (0 = never expire). Default: `30`.
- `sourceSystem` (optional): Identifier for the source system (for tracking). Default: empty.
- `trackingEnabled` (optional): Enable/disable idempotence tracking. Default: `true`.

## Supported Identifier Types

### Standard Payment Identifiers

- **InstrId**: Instruction Identification - unique identifier for the payment instruction
- **EndToEndId**: End-to-End Identification - unique identifier maintained throughout the payment chain
- **MsgId**: Message Identification - unique identifier for the message itself

### XPath Expressions Used

The kamelet automatically extracts identifiers using namespace-aware XPath:

- InstrId: `//*[local-name()='InstrId']`
- EndToEndId: `//*[local-name()='EndToEndId']`
- MsgId: `//*[local-name()='MsgId']`

## Headers Set by Kamelet

### Idempotence Results

- `IsDuplicate`: Boolean indicating if message is a duplicate
- `CanProcess`: Boolean indicating if message can be processed
- `IdempotenceChecked`: Boolean indicating check was performed
- `PrimaryIdentifier`: The first identifier found and used for checking
- `PrimaryIdentifierType`: Type of the primary identifier (InstrId, EndToEndId, etc.)
- `IdempotenceAction`: Action taken (PROCESS, ERROR, IGNORE, WARN)

### Duplicate Information (when duplicate detected)

- `DuplicateDetected`: Boolean flag for duplicate detection
- `FirstProcessedAt`: Timestamp when identifier was first processed
- `OriginalMessageId`: Message ID of the original message
- `AccessCount`: Number of times this identifier has been seen
- `DuplicateDetails`: Human-readable description of duplicate findings

### Routing Headers

- `ShouldReject`: Boolean indicating message should be rejected
- `ShouldIgnore`: Boolean indicating message should be ignored
- `DuplicateWarning`: Boolean indicating processing with warning
- `ProcessingDecision`: "PROCEED" or "REJECT" based on idempotence check
- `RouteToErrorQueue`: Boolean for routing duplicate rejections
- `RouteToIgnore`: Boolean for routing ignored duplicates

### Error Handling

- `IdempotenceError`: Boolean indicating an error during idempotence check
- `IdempotenceErrorMessage`: Description of the error
- `IdempotenceErrorWarning`: Boolean indicating error occurred but processing continues

## Duplicate Actions

### ERROR (Default)

```yaml
duplicateAction: "ERROR"
```

- Rejects duplicate messages
- Sets `ShouldReject=true` and `CanProcess=false`
- Routes to error handling

### IGNORE

```yaml
duplicateAction: "IGNORE"
```

- Silently ignores duplicate messages
- Sets `ShouldIgnore=true` and `CanProcess=true`
- Can be used for exactly-once processing semantics

### WARN

```yaml
duplicateAction: "WARN"
```

- Processes duplicates but with warnings
- Sets `DuplicateWarning=true` and `CanProcess=true`
- Useful for monitoring and alerting

## Repository Types

### MEMORY (Default)

- In-memory storage using ConcurrentHashMap
- Fast access, suitable for single-instance deployments
- Data lost on restart
- No external dependencies

### DATABASE

- JPA-based persistent storage
- Survives restarts and supports clustering
- Requires database configuration
- Automatic schema creation

### CACHE

- Distributed cache storage (Infinispan/Hazelcast)
- Supports clustering and high availability
- Configurable expiry and eviction policies
- Requires cache infrastructure

## Example Usage

### Basic Idempotence Check

```yaml
- from:
    uri: "jms:queue:incoming-payments"
    steps:
      - to: "kamelet:k-idempotence"
      - choice:
          when:
            - simple: "${header.CanProcess} == true"
              steps:
                - to: "jms:queue:processing"
          otherwise:
            - to: "jms:queue:duplicates"
```

### Strict Duplicate Prevention

```yaml
- from:
    uri: "platform-http:/api/payments"
    steps:
      - to: "kamelet:k-idempotence?duplicateAction=ERROR&repositoryType=DATABASE"
      - choice:
          when:
            - simple: "${header.ShouldReject} == true"
              steps:
                - transform:
                    simple: |
                      {
                        "error": "Duplicate transaction",
                        "identifier": "${header.PrimaryIdentifier}",
                        "firstSeen": "${header.FirstProcessedAt}",
                        "accessCount": ${header.AccessCount}
                      }
                - setHeader:
                    name: "Content-Type"
                    constant: "application/json"
                - setHeader:
                    name: "CamelHttpResponseCode"
                    constant: 409
          otherwise:
            - to: "kamelet:k-ingest-validation"
            - to: "jms:queue:validated-payments"
```

### Lenient Processing with Warnings

```yaml
- from:
    uri: "file:input?include=*.xml"
    steps:
      - to: "kamelet:k-idempotence?duplicateAction=WARN&identifierTypes=InstrId,EndToEndId,MsgId"
      - choice:
          when:
            - simple: "${header.DuplicateWarning} == true"
              steps:
                - log: "Processing duplicate with warning: ${header.DuplicateDetails}"
                - to: "jms:topic:duplicate-warnings"
          otherwise:
            - log: "Processing new message: ${header.PrimaryIdentifier}"
      - to: "kamelet:k-pacs-008-to-cdm"
```

### Content-Based Duplicate Detection

```yaml
- from:
    uri: "timer:batch-process?period=60000"
    steps:
      - to: "sql:SELECT * FROM pending_payments"
      - split:
          simple: "${body}"
          steps:
            - to: "kamelet:k-idempotence?enableHashing=true&duplicateAction=IGNORE"
            - choice:
                when:
                  - simple: "${header.ShouldIgnore} != true"
                    steps:
                      - to: "kamelet:k-ingest-validation"
                      - to: "jms:queue:batch-processing"
```

### Multi-System Integration

```yaml
- from:
    uri: "jms:queue:system-a-payments"
    steps:
      - setHeader:
          name: "SourceSystem"
          constant: "SYSTEM_A"
      - to: "kamelet:k-idempotence?sourceSystem=SYSTEM_A&expiryDays=7"
      - to: "direct:common-processing"

- from:
    uri: "jms:queue:system-b-payments"
    steps:
      - setHeader:
          name: "SourceSystem"
          constant: "SYSTEM_B"
      - to: "kamelet:k-idempotence?sourceSystem=SYSTEM_B&expiryDays=14"
      - to: "direct:common-processing"

- from:
    uri: "direct:common-processing"
    steps:
      - choice:
          when:
            - simple: "${header.IsDuplicate} == true"
              steps:
                - log: "Duplicate from ${header.SourceSystem}: ${header.PrimaryIdentifier}"
                - to: "jms:queue:duplicate-handling"
          otherwise:
            - to: "kamelet:k-ref-loader"
            - to: "kamelet:k-ingest-validation"
            - to: "jms:queue:validated-processing"
```

### Error Handling and Monitoring

```yaml
- from:
    uri: "direct:idempotent-processing"
    steps:
      - to: "kamelet:k-idempotence"
      - choice:
          when:
            - simple: "${header.IdempotenceError} == true"
              steps:
                - log: "Idempotence check failed: ${header.IdempotenceErrorMessage}"
                - to: "jms:topic:idempotence-errors"
                # Continue processing on idempotence errors
                - to: "direct:process-anyway"
          when:
            - simple: "${header.IsDuplicate} == true"
              steps:
                - transform:
                    simple: |
                      Duplicate Alert:
                      - Identifier: ${header.PrimaryIdentifier} (${header.PrimaryIdentifierType})
                      - Original Message: ${header.OriginalMessageId}
                      - First Processed: ${header.FirstProcessedAt}
                      - Access Count: ${header.AccessCount}
                      - Source: ${header.SourceSystem}
                - to: "jms:topic:duplicate-alerts"
                - choice:
                    when:
                      - simple: "${header.ShouldReject} == true"
                        steps:
                          - to: "jms:queue:rejected-duplicates"
                    otherwise:
                      - to: "direct:process-duplicate"
          otherwise:
            - to: "direct:process-new"
```

## Integration with Other Kamelets

### Receipt Processing Pipeline

```yaml
# Message receipt with idempotence check
- from:
    uri: "jms:queue:raw-messages"
    steps:
      - to: "kamelet:k-http-message-receiver" # Store received message
      - to: "kamelet:k-idempotence" # Check for duplicates
      - choice:
          when:
            - simple: "${header.CanProcess} == true"
              steps:
                - to: "kamelet:k-ingest-validation" # Validate message
                - to: "kamelet:k-ref-loader" # Load configuration
                - to: "kamelet:k-pacs-008-to-cdm" # Transform message
                - to: "jms:queue:processed"
          otherwise:
            - to: "jms:queue:duplicates"
```

### Batch Processing with Idempotence

```yaml
- from:
    uri: "file:batch-input?include=*.xml"
    steps:
      - to: "kamelet:k-file-receipt"
      - split:
          xpath: "//PaymentMessage"
          steps:
            - to: "kamelet:k-idempotence?duplicateAction=WARN"
            - choice:
                when:
                  - simple: "${header.IsDuplicate} != true"
                    steps:
                      - to: "kamelet:k-ingest-validation"
                      - to: "jms:queue:batch-processing"
                otherwise:
                  - log: "Skipping duplicate in batch: ${header.PrimaryIdentifier}"
```

## Storage Configuration

### Database Repository Setup

```properties
# Application properties for DATABASE repository
spring.datasource.url=jdbc:postgresql://localhost:5432/payments
spring.datasource.username=payment_user
spring.datasource.password=payment_pass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

idempotence.repository.type=DATABASE
idempotence.database.cleanup.batch.size=1000
```

### Cache Repository Setup

```properties
# Application properties for CACHE repository
idempotence.repository.type=CACHE
idempotence.cache.max.size=100000
idempotence.cache.servers=cache1:11211,cache2:11211
```

## Performance Considerations

- **Memory Usage**: In-memory repository scales with unique identifier count
- **Database Performance**: Index on (identifier, identifier_type) for fast lookups
- **Cache Efficiency**: Configure appropriate TTL and eviction policies
- **Cleanup**: Regular cleanup of expired identifiers prevents unbounded growth
- **Identifier Selection**: Use most stable identifiers (InstrId preferred over MsgId)

## Security Considerations

- **Identifier Privacy**: Consider hashing sensitive identifiers
- **Access Control**: Secure repository access in multi-tenant environments
- **Audit Trail**: Track access patterns for compliance and monitoring
- **Data Retention**: Implement proper data retention policies for GDPR compliance

## Troubleshooting

### Common Issues

1. **Memory Growth**: Configure expiry settings and cleanup intervals
2. **False Duplicates**: Check identifier extraction XPath expressions
3. **Performance Issues**: Consider database indexing or cache tuning
4. **Repository Failures**: Implement fallback to allow processing on errors

### Monitoring

Monitor these metrics for operational health:

- Duplicate detection rate
- Repository size and growth
- Processing latency impact
- Error rates and types
- Cleanup effectiveness

## Integration Patterns

### Pattern 1: Gateway Idempotence

Use k-idempotence as the first step in message processing to catch duplicates early.

### Pattern 2: Business Logic Idempotence

Apply idempotence checks after validation but before business processing.

### Pattern 3: Multi-Layer Idempotence

Use different identifier types at different processing stages for comprehensive protection.

### Pattern 4: Cross-System Idempotence

Track identifiers across multiple systems using source system identification.
