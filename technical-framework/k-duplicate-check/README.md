# K-Duplicate-Check Kamelet

## Overview

The K-Duplicate-Check kamelet implements a comprehensive duplicate check algorithm for PACS008 payment processing flows. It provides file size validation and database-based duplicate detection with retry mechanisms.

## Features

- Flow occurrence ID management
- Configurable global disable flags for DB and file size checks
- SHA-1 checksum computation (file-based or payload-based)
- Maximum file size validation with bypass conditions
- Database duplicate detection with retry logic
- Comprehensive error handling and logging

## Algorithm Implementation

The kamelet implements the following key steps:

1. **Flow Occurrence ID Retrieval**: Gets or generates a unique flow occurrence identifier
2. **Checkpoint Reading**: Reads replayed flag from checkpoint data
3. **Global DB Check Control**: Allows bypassing duplicate check entirely
4. **Checksum Calculation**: Computes SHA-1 checksum based on UsePayload configuration
5. **File Size Validation**: Checks file size against configured maximum (with bypass conditions)
6. **Database Operations**: Insert/search loop with retry mechanism for duplicate detection

## Configuration Properties

| Property                  | Type    | Default             | Description                                              |
| ------------------------- | ------- | ------------------- | -------------------------------------------------------- |
| `dataSource`              | string  | "dataSource"        | JDBC data source name                                    |
| `disableCheckDB`          | boolean | false               | Global flag to disable duplicate check                   |
| `disableCheckMaxFileSize` | boolean | false               | Flag to disable maximum file size check                  |
| `maxRetryCount`           | integer | 3                   | Maximum number of retry attempts for database operations |
| `retrySleepPeriod`        | integer | 1000                | Sleep period between retries in milliseconds             |
| `moduleName`              | string  | "k-duplicate-check" | Module name for logging and error reporting              |

## Database Schema

The kamelet requires the following database table:

```sql
CREATE TABLE TECH_DUPLICATE_CHECK (
    FLOWOCCUR_ID VARCHAR(255) NOT NULL,
    PARTNER_ID VARCHAR(255),
    CHECKSUM VARCHAR(255) NOT NULL,
    FLOWID VARCHAR(255) NOT NULL,
    RECEIPT_DTE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (FLOWOCCUR_ID),
    UNIQUE KEY unique_flow_checksum (FLOWID, CHECKSUM)
);
```

## Expected Headers

The kamelet expects the following headers in the exchange:

- `flowOccurId`: Flow occurrence identifier (auto-generated if missing)
- `flowId`: Flow identifier from TechPivotRoot.Flow.FlowID
- `flowCode`: Flow code for logging purposes
- `replayedFlag`: Checkpoint replayed flag

## Message Format

The kamelet processes JSON messages with the following structure:

```json
{
  "TechPivotRoot": {
    "Flow": {
      "FlowID": "string",
      "FlowCode": "string"
    },
    "FlowRules": {
      "FlowSeparation": {
        "UsePayload": boolean
      }
    }
  }
}
```

## Error Codes

- **TEC00064**: Maximum file size exceeded
- **DCH00001**: Duplicate flow detected

## Usage Example

```yaml
from:
  uri: "direct:duplicate-check"
  steps:
    - to:
        uri: "kamelet:k-duplicate-check"
        parameters:
          dataSource: "pixelDataSource"
          disableCheckDB: false
          maxRetryCount: 5
          retrySleepPeriod: 2000
```

## Dependencies

- Apache Camel Core
- Apache Camel Kamelet
- Apache Camel SQL/JDBC
- Apache Camel Jackson
- Apache Camel Bean
- Database driver (PostgreSQL/MySQL)

## Notes

- The kamelet automatically generates a flow occurrence ID if not provided
- File size check can be bypassed using global flags or checkpoint replayed status
- Database retry mechanism handles temporary connection issues
- All operations are logged for debugging and monitoring purposes
