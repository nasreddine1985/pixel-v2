# K-Log-TX Kamelet - Centralized Logging Transaction

## Overview

The `k-log-tx` kamelet provides centralized log management and persistence capabilities for the Pixel V2 payment processing system. It receives log events from various application components and persists them to a dedicated database table with comprehensive metadata tracking.

## Features

### 🎯 **Core Functionality**

- **Centralized Log Collection**: Receives log events from all application components
- **Database Persistence**: Stores log entries in a structured database table
- **Multi-Level Support**: Handles TRACE, DEBUG, INFO, WARN, and ERROR levels
- **Category Classification**: Supports ROUTE, BUSINESS, ERROR, AUDIT, and PERFORMANCE categories

### 📊 **Metadata Enrichment**

- **Camel Integration**: Automatically captures Exchange ID, Route ID, Breadcrumb ID
- **Performance Metrics**: Tracks processing time and message size
- **Correlation Tracking**: Links related log entries across components
- **Thread Information**: Captures thread names and execution context
- **Custom Headers**: Includes additional exchange headers as JSON data

### ⚡ **Performance Features**

- **Async Processing**: Optional asynchronous log processing for high-volume scenarios
- **Batch Operations**: Configurable batch sizes for database efficiency
- **Connection Pooling**: Optimized database connection management
- **Error Resilience**: Robust error handling with fallback logging

## Usage Examples

### Basic Logging

Send a simple log message to the kamelet:

```yaml
- to:
    uri: "kamelet:k-log-tx"
    parameters:
      logLevel: "INFO"
      logSource: "ingestion"
```

### Business Event Logging

Log business events with correlation tracking:

```yaml
- setHeader:
    name: "LogLevel"
    constant: "INFO"
- setHeader:
    name: "LogSource"
    constant: "payment-processing"
- setHeader:
    name: "LogCategory"
    constant: "BUSINESS"
- setHeader:
    name: "CorrelationId"
    simple: "${header.MessageId}"
- setBody:
    constant: "Payment processed successfully for amount ${header.Amount}"
- to: "kamelet:k-log-tx"
```

### Error Logging with Exception Details

Log errors with full exception information:

```yaml
- onException:
    - handled:
        constant: true
    - setHeader:
        name: "LogLevel"
        constant: "ERROR"
    - setHeader:
        name: "LogSource"
        constant: "validation"
    - setHeader:
        name: "LogCategory"
        constant: "ERROR"
    - setBody:
        simple: "Validation failed: ${exception.message}"
    - to: "kamelet:k-log-tx"
```

### Performance Monitoring

Log performance metrics:

```yaml
- setHeader:
    name: "ProcessingStartTime"
    simple: "${date:now}"

# ... processing steps ...

- setHeader:
    name: "ProcessingEndTime"
    simple: "${date:now}"
- setHeader:
    name: "ProcessingTime"
    simple: "${header.ProcessingEndTime} - ${header.ProcessingStartTime}"
- setHeader:
    name: "LogLevel"
    constant: "INFO"
- setHeader:
    name: "LogSource"
    constant: "transformer"
- setHeader:
    name: "LogCategory"
    constant: "PERFORMANCE"
- setBody:
    simple: "Message transformation completed in ${header.ProcessingTime}ms"
- to: "kamelet:k-log-tx"
```

### Async High-Volume Logging

For high-volume scenarios, use async mode:

```yaml
- to:
    uri: "kamelet:k-log-tx"
    parameters:
      logLevel: "DEBUG"
      logSource: "bulk-processor"
      asyncMode: true
      batchSize: 50
```

## Configuration Parameters

| Parameter                | Type    | Required | Default | Description                                               |
| ------------------------ | ------- | -------- | ------- | --------------------------------------------------------- |
| `logLevel`               | string  | Yes      | INFO    | Log severity level (TRACE, DEBUG, INFO, WARN, ERROR)      |
| `logSource`              | string  | Yes      | -       | Source component generating the log                       |
| `logCategory`            | string  | No       | -       | Log category (ROUTE, BUSINESS, ERROR, AUDIT, PERFORMANCE) |
| `correlationId`          | string  | No       | -       | Correlation ID for tracing related entries                |
| `enableAuditTrail`       | boolean | No       | true    | Enable detailed audit trails with metadata                |
| `batchSize`              | integer | No       | 1       | Number of entries to batch before committing              |
| `asyncMode`              | boolean | No       | false   | Process log entries asynchronously                        |
| `includeExchangeHeaders` | boolean | No       | true    | Include Camel exchange headers in additional data         |
| `maxMessageLength`       | integer | No       | 4000    | Maximum log message length (truncated if longer)          |

## Database Schema

The kamelet creates and maintains the `LOG_ENTRIES` table:

```sql
CREATE TABLE LOG_ENTRIES (
    id NUMBER PRIMARY KEY,
    logTimestamp TIMESTAMP NOT NULL,
    logLevel VARCHAR2(10) NOT NULL,
    logSource VARCHAR2(100) NOT NULL,
    logCategory VARCHAR2(50),
    message CLOB NOT NULL,
    correlationId VARCHAR2(100),
    exchangeId VARCHAR2(100),
    routeId VARCHAR2(100),
    breadcrumbId VARCHAR2(100),
    exception CLOB,
    additionalData CLOB,
    processingTime NUMBER,
    messageSize NUMBER,
    threadName VARCHAR2(100),
    createdAt TIMESTAMP NOT NULL
);

-- Indexes for query performance
CREATE INDEX idx_log_timestamp ON LOG_ENTRIES(logTimestamp);
CREATE INDEX idx_log_level ON LOG_ENTRIES(logLevel);
CREATE INDEX idx_log_source ON LOG_ENTRIES(logSource);
CREATE INDEX idx_correlation_id ON LOG_ENTRIES(correlationId);
```

## Integration Architecture

### Flow Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │───▶│   K-Log-TX      │───▶│   Log Database  │
│   Components    │    │   Kamelet       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                        │                        │
        ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ • Ingestion     │    │ • Validation    │    │ • Structured    │
│ • Processing    │    │ • Enrichment    │    │   Storage       │
│ • Validation    │    │ • Persistence   │    │ • Indexing      │
│ • Transformation│    │ • Error Handling│    │ • Querying      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Integration with Existing Components

#### Ingestion Module

```yaml
# In payment-ingestion-routes.yaml
- log:
    message: "Processing payment message"
- setHeader:
    name: "LogSource"
    constant: "ingestion"
- setHeader:
    name: "LogCategory"
    constant: "BUSINESS"
- to: "kamelet:k-log-tx"
```

#### Processing Module

```yaml
# In processing routes
- setHeader:
    name: "LogSource"
    constant: "processing"
- setHeader:
    name: "LogCategory"
    constant: "ROUTE"
- to: "kamelet:k-log-tx"
```

#### Error Handling

```yaml
# Global error handler
- onException:
    - setHeader:
        name: "LogLevel"
        constant: "ERROR"
    - setHeader:
        name: "LogSource"
        simple: "${routeId}"
    - setHeader:
        name: "LogCategory"
        constant: "ERROR"
    - to: "kamelet:k-log-tx"
```

## Performance Considerations

### Sync vs Async Mode

- **Sync Mode**: Use for critical logs that must be persisted immediately
- **Async Mode**: Use for high-volume debug/trace logs to avoid performance impact

### Batch Configuration

- **Small Batches (1-10)**: Better for synchronous logging and error tracking
- **Large Batches (50-100)**: Better for high-volume scenarios and performance

### Database Optimization

- Indexes are automatically created for common query patterns
- Connection pooling is configured for concurrent access
- Batch inserts are used to improve performance

## Monitoring and Troubleshooting

### Health Checks

The kamelet provides health information through:

- Log persistence success/failure headers
- Processing time metrics
- Error count tracking

### Common Issues

1. **Database Connection**: Verify Oracle database connectivity and credentials
2. **Table Creation**: Ensure the application has DDL permissions
3. **Performance**: Monitor async queue size and processing times

## Dependencies

The kamelet requires the following dependencies:

- Spring Boot Data JPA
- Oracle JDBC Driver
- Hibernate ORM
- Apache Camel Jackson
- Apache Camel JPA

## Security Considerations

- Database credentials should be externalized using environment variables
- Log content should be sanitized to avoid sensitive data exposure
- Access to log tables should be restricted based on security requirements
- Consider encryption for sensitive log data

## Future Enhancements

- **Log Retention**: Automatic cleanup of old log entries
- **Search API**: REST endpoints for log querying
- **Dashboard**: Web interface for log visualization
- **Alerting**: Integration with monitoring systems for critical errors
- **Export**: Bulk export capabilities for compliance and analysis
