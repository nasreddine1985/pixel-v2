# K-Log Flow Summary

## Overview

The `k-log-flow-summary` kamelet is designed to construct and publish structured flow summary log messages to Kafka for PACS008 payment processing monitoring. This kamelet creates detailed JSON log messages containing flow status, processing metrics, partner information, and timestamps.

## Features

- **Flow Status Tracking**: Monitors IN_PROGRESS, COMPLETED, and ERROR states
- **Structured Logging**: Creates comprehensive JSON log messages with all relevant flow information
- **Kafka Publishing**: Publishes log messages to configurable Kafka topics
- **Error Handling**: Properly handles and logs error scenarios
- **Metadata Enrichment**: Automatically enriches messages with timestamps, IDs, and processing metrics

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
    uri: "kamelet:k-log-flow-summary"
    parameters:
      step: "IN_PROGRESS"
      kafkaTopicName: "pacs008-flow-summary"
      brokers: "pixel-v2-kafka:9092"
```

## Log Message Structure

The kamelet generates JSON log messages with the following structure:

```json
{
  "flowOccurId": "message-id-123",
  "flowCode": "PACS008",
  "flowStatusCode": "IN_PROGRESS",
  "flowCountryCode": "FR",
  "flowCountryId": 250,
  "flowTypeId": 1,
  "flowComment": "PACS008 payment processing in progress",
  "nbOutExpected": 1,
  "nbOutCompleted": 0,
  "nbError": 0,
  "nbRemitance": 0,
  "nbTransaction": 1,
  "nbReplay": 0,
  "issuingPartnerCode": "MQ_ACTIVEMQ",
  "issuingPartnerLink": "tcp://pixel-v2-activemq:61616",
  "recipientPartnerCode": "KAFKA_CLUSTER",
  "recipientPartnerLink": "kafka:29092",
  "lastLogId": "LOG-20251201103045123",
  "lastLogComponent": "pacs008-processing-flow",
  "lastLogDatetime": "2025-12-01T10:30:45.123456",
  "lastLogStatusCode": "SUCCESS",
  "lastUpdateDatetime": "2025-12-01T10:30:45.123456",
  "lastUpdateUser": "system",
  "rootErrorCode": null,
  "rootErrorLogId": null,
  "rootErrorDatetime": null,
  "inputFilePath": "pacs008.input.queue",
  "inputFileSize": "1024B",
  "refFlowId": "message-id-123",
  "beginFlowDatetime": "2025-12-01T10:30:45.123456",
  "endFlowDatetime": null,
  "currentClientDatetime": "2025-12-01T10:30:45.123456",
  "beginFlowDate": "2025-12-01",
  "endFlowDate": null,
  "lastUpdateDate": "2025-12-01",
  "replayId": null,
  "region": "EU"
}
```

## Flow Status Behavior

### IN_PROGRESS

- Sets `flowStatusCode` to "IN_PROGRESS"
- Sets `flowComment` to "PACS008 payment processing in progress"
- `nbOutCompleted` remains 0
- `endFlowDatetime` and `endFlowDate` remain null

### COMPLETED

- Sets `flowStatusCode` to "COMPLETED"
- Sets `flowComment` to "PACS008 payment processing completed successfully"
- Sets `nbOutCompleted` to 1
- Populates `endFlowDatetime` and `endFlowDate` with current timestamp

### ERROR

- Sets `flowStatusCode` to "ERROR"
- Sets `flowComment` to "PACS008 payment processing failed with error"
- Sets `nbError` to 1
- Sets `lastLogStatusCode` to "ERROR"
- Populates error-related fields (`rootErrorCode`, `rootErrorLogId`, `rootErrorDatetime`)
- Populates `endFlowDatetime` and `endFlowDate` with current timestamp

## Dependencies

This kamelet requires the following Camel components:

- `camel-kamelet`
- `camel-kafka`
- `camel-jackson`
- `camel-jsonpath`

## Integration with PIXEL-V2

This kamelet is designed to integrate with the PIXEL-V2 payment processing system and can be used in conjunction with other technical framework kamelets such as:

- `k-mq-message-receiver`
- `k-kafka-message-receiver`
- `k-db-tx`
- `k-log-tx`

## Build and Installation

To build and install this kamelet:

1. Navigate to the kamelet directory:

   ```bash
   cd technical-framework/k-log-flow-summary
   ```

2. Build the kamelet:

   ```bash
   mvn clean install
   ```

3. The kamelet JAR will be available in the `target/` directory and installed in your local Maven repository.
