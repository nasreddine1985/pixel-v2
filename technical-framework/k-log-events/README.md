# K-Log Events

## Overview

The `k-log-events` kamelet is designed to construct and publish detailed log event messages to Kafka for PACS008 payment processing monitoring. This kamelet creates comprehensive JSON log messages containing complete log event information including message details, processing context, and audit trail data.

## Features

- **Complete Log Event Structure**: Creates detailed log events matching the LogEvent entity structure
- **Message Body Capture**: Includes full message body in log events for audit purposes
- **Processing Context**: Captures comprehensive processing context and metadata
- **Kafka Publishing**: Publishes log events to configurable Kafka topics
- **Audit Trail**: Maintains complete audit trail with timestamps and correlation IDs
- **Structured Data**: Creates JSON log events with all database fields populated

## Parameters

### Required Parameters

- **step** (string): Current processing step status

  - Allowed values: `IN_PROGRESS`, `COMPLETED`, `ERROR`
  - Example: `"IN_PROGRESS"`

- **kafkaTopicName** (string): Name of the Kafka topic to publish log messages
  - Example: `"pacs008-flow-summary"`

### Optional Parameters

- **brokers** (string): Comma-separated list of Kafka broker addresses

  - Default: `"pixel-v2-kafka:9092"`

- **keySerializer** (string): Serializer class for message keys

  - Default: `"org.apache.kafka.common.serialization.StringSerializer"`

- **valueSerializer** (string): Serializer class for message values
  - Default: `"org.apache.kafka.common.serialization.StringSerializer"`

## Usage Example

```yaml
- to:
    uri: "kamelet:k-log-events"
    parameters:
      step: "IN_PROGRESS"
      kafkaTopicName: "pacs008-log-events"
      brokers: "pixel-v2-kafka:9092"
```

## Log Event Structure

The kamelet generates JSON log events with the following LogEvent entity structure:

```json
{
  "logId": "LOG-20251201103045123",
  "datats": "2025-12-01T10:30:45.123456",
  "flowId": "message-id-123",
  "halfFlowId": "message-id-123-HALF",
  "flowCode": "PACS008",
  "halfFlowCode": "PACS008-HALF",
  "contextId": "correlation-id-456",
  "clientLogTimestamp": "2025-12-01T10:30:45.123456",
  "dbLogTimestamp": "2025-12-01T10:30:45.123456",
  "txt": "PACS008 payment processing IN_PROGRESS",
  "longTxt": "Detailed PACS008 payment processing log for step IN_PROGRESS",
  "logRole": "SYSTEM",
  "code": "IN_PROGRESS",
  "customStep": "IN_PROGRESS",
  "component": "pacs008-processing-flow",
  "instanceId": "INST-001",
  "servicePath": "pacs008.input.queue",
  "processPath": "/pacs008/processing",
  "refFlowId": 1,
  "beginProcess": "2025-12-01T10:30:45.123456",
  "endProcess": null,
  "contextTimestamp": "2025-12-01T10:30:45.123456",
  "msgSentTimestamp": "2025-12-01T10:30:45.123456",
  "messagingType": "JMS",
  "msgId": "message-id-123",
  "msgPriority": 1,
  "msgCorrelationId": "correlation-id-456",
  "msgSourceSystem": "PIXEL-V2",
  "msgPrivateContext": "Header context information",
  "msgTransactionId": "message-id-123",
  "msgProperties": "{}",
  "msgBatchName": null,
  "msgBatchMsgNo": null,
  "msgBatchSize": null,
  "xmlMsgAction": null,
  "msgResubmitInd": "N",
  "msgBody": "Full message body content",
  "logDay": "2025-12-01"
}
```

## Log Event Behavior

### Processing Step Tracking

- **`code` and `customStep`**: Set to the current processing step (IN_PROGRESS, COMPLETED, ERROR)
- **`txt`**: Contains a human-readable description of the current step
- **`longTxt`**: Provides detailed information about the processing step

### Message Context Capture

- **`msgBody`**: Captures the complete message body for audit purposes
- **`msgId` and `msgCorrelationId`**: Preserve message identification and correlation
- **`msgPrivateContext`**: Stores header information and processing context
- **`msgProperties`**: Contains additional message properties in JSON format

### Audit Trail Information

- **Timestamps**: Multiple timestamp fields track different aspects of processing
  - `datats`: Main log event timestamp
  - `clientLogTimestamp`: Client-side timestamp
  - `dbLogTimestamp`: Database insertion timestamp
  - `contextTimestamp`: Processing context timestamp
- **Processing Path**: `servicePath` and `processPath` track the processing route
- **Instance Tracking**: `instanceId` and `component` identify the processing instance

## Dependencies

This kamelet requires the following Camel components:

- `camel-kamelet`
- `camel-kafka`
- `camel-jackson`
- `camel-jsonpath`

## Integration with PIXEL-V2

This kamelet is designed to integrate with the PIXEL-V2 payment processing system and provides detailed logging capabilities that complement other technical framework kamelets such as:

- `k-mq-starter` (for message receipt logging)
- `k-log-flow-summary` (for high-level flow tracking)
- `k-db-tx` (for database operations)
- `k-log-tx` (for transaction logging)

## Build and Installation

To build and install this kamelet:

1. Navigate to the kamelet directory:

   ```bash
   cd technical-framework/k-log-events
   ```

2. Build the kamelet:

   ```bash
   mvn clean install
   ```

3. The kamelet JAR will be available in the `target/` directory and installed in your local Maven repository.
