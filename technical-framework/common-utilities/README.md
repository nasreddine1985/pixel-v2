# Common Utilities for Pixel V2 Kamelets

This module provides shared utilities and constants for all kamelets in the Pixel V2 technical framework.

## Features

### 🎯 HeaderConstants

Centralized constants for all header names used across kamelets to ensure consistency and avoid typos.

### 🔧 HeaderUtils

Utility methods for reading and writing headers with automatic fallback support.

## Usage

### 1. Adding the Dependency

Add to your kamelet's `pom.xml`:

```xml
<dependency>
    <groupId>com.pixel.v2</groupId>
    <artifactId>common-utilities</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. Using HeaderConstants

```java
import static com.pixel.v2.common.headers.HeaderConstants.*;

// Instead of using string literals:
.setHeader("FlowDataJson", jsonData)           // ❌ Prone to typos
.setHeader("processingMode", "BATCH")          // ❌ Inconsistent naming

// Use constants:
.setHeader(FLOW_DATA_JSON, jsonData)           // ✅ Safe and consistent
.setHeader(PROCESSING_MODE, PROCESSING_MODE_BATCH) // ✅ Type-safe values
```

### 3. Using HeaderUtils

```java
import com.pixel.v2.common.headers.HeaderUtils;

// Reading headers with automatic fallback
String flowData = HeaderUtils.getFlowDataJson(exchange);          // Tries FlowDataJson, flowDataJson, RefFlowDataJson, refFlowDataJson
String mode = HeaderUtils.getProcessingMode(exchange);            // Returns NORMAL if not set

// Writing headers
HeaderUtils.setFlowDataJson(exchange, jsonData);
HeaderUtils.setProcessingMode(exchange, PROCESSING_MODE_BATCH);

// Auto-generation
String flowId = HeaderUtils.generateFlowOccurId("PROC");          // PROC-20260109-143022
String msgId = HeaderUtils.generateMessageId("MSG");              // MSG-a1b2c3d4-e5f6...
String timestamp = HeaderUtils.generateTimestamp();               // 2026-01-09T14:30:22.123

// Set all standard headers at once
HeaderUtils.setStandardProcessingHeaders(exchange,
    "FLOW_PREFIX", "MSG_PREFIX", PROCESSING_MODE_BATCH);
```

## Available Constants

### Header Names

- `FLOW_DATA_JSON` / `FLOW_DATA_JSON_LC` - Flow JSON data
- `REF_FLOW_DATA_JSON` / `REF_FLOW_DATA_JSON_LC` - Reference flow JSON
- `FLOW_OCCUR_ID` / `FLOW_OCCUR_ID_CC` - Flow occurrence ID
- `MESSAGE_ID` / `MESSAGE_ID_LC` - Message identifier
- `CORRELATION_ID` / `CORRELATION_ID_LC` - Correlation identifier
- `RECEIVED_TIMESTAMP` / `RECEIVED_TIMESTAMP_LC` - Reception timestamp
- `PROCESSING_MODE` / `PROCESSING_MODE_LC` - Processing mode
- `BUSINESS_STATUS` / `BUSINESS_STATUS_LC` - Business status
- `TECHNICAL_STATUS` / `TECHNICAL_STATUS_LC` - Technical status
- `TECH_PIVOT_XML` - Generated TechnicalPivot XML
- `EXISTING_TECH_PIVOT_XML` - Existing XML for updates

### Processing Modes

- `PROCESSING_MODE_NORMAL` - "NORMAL"
- `PROCESSING_MODE_BATCH` - "BATCH"
- `PROCESSING_MODE_REALTIME` - "REALTIME"
- `PROCESSING_MODE_FILE_BATCH` - "FILE_BATCH"

### Business Status Values

- `BUSINESS_STATUS_PENDING` - "PENDING"
- `BUSINESS_STATUS_PROCESSING` - "PROCESSING"
- `BUSINESS_STATUS_VALIDATED` - "VALIDATED"
- `BUSINESS_STATUS_COMPLETED` - "COMPLETED"
- `BUSINESS_STATUS_INITIALIZED` - "INITIALIZED"

### Technical Status Values

- `TECHNICAL_STATUS_PROCESSING` - "PROCESSING"
- `TECHNICAL_STATUS_ACTIVE` - "ACTIVE"
- `TECHNICAL_STATUS_SUCCESS` - "SUCCESS"
- `TECHNICAL_STATUS_FINALIZED` - "FINALIZED"
- `TECHNICAL_STATUS_ERROR` - "ERROR"

### Operations

- `OPERATION_GENERATE` - "generate"
- `OPERATION_UPDATE` - "update"
- `OPERATION_VALIDATE` - "validate"

## Benefits

✅ **Type Safety**: Compile-time checking of header names  
✅ **Consistency**: Same header names across all kamelets  
✅ **Maintainability**: Single place to update header names  
✅ **Auto-completion**: IDE support for header constants  
✅ **Fallback Support**: Automatic handling of CamelCase/lowercase variations  
✅ **Utility Methods**: Common operations simplified

## Migration Guide

### Before (using string literals)

```java
.setHeader("FlowDataJson", data)
.setHeader("flowOccurId", "FLOW-" + System.currentTimeMillis())
.setHeader("ProcessingMode", "BATCH")

// Reading with manual fallback
String flowData = exchange.getIn().getHeader("FlowDataJson", String.class);
if (flowData == null) {
    flowData = exchange.getIn().getHeader("flowDataJson", String.class);
}
```

### After (using common utilities)

```java
import static com.pixel.v2.common.headers.HeaderConstants.*;

.setHeader(FLOW_DATA_JSON, data)
.setHeader(FLOW_OCCUR_ID, HeaderUtils.generateFlowOccurId("FLOW"))
.setHeader(PROCESSING_MODE, PROCESSING_MODE_BATCH)

// Reading with automatic fallback
String flowData = HeaderUtils.getFlowDataJson(exchange);
```

## Testing

Run tests:

```bash
mvn test
```

The module includes comprehensive unit tests for all utilities and constants.
