# k-batch-aggregation Kamelet

## Description

The `k-batch-aggregation` kamelet provides configurable message batch aggregation functionality for Apache Camel routes. It collects incoming messages into batches using size-based and time-based completion criteria, making it ideal for efficient batch processing scenarios.

## Features

- **Configurable Batch Size**: Set maximum number of messages per batch
- **Timeout-based Completion**: Complete batches based on time intervals
- **Flexible Correlation**: Use custom correlation keys for message grouping
- **Message Type Support**: Handle different message types with proper logging
- **Integration Ready**: Uses MessageBatchAggregationStrategy for seamless integration

## Parameters

| Parameter        | Type    | Required | Default             | Description                                                 |
| ---------------- | ------- | -------- | ------------------- | ----------------------------------------------------------- |
| `batchSize`      | integer | Yes      | 1000                | Maximum number of messages to aggregate into a single batch |
| `timeoutMs`      | integer | Yes      | 5000                | Maximum time to wait for batch completion in milliseconds   |
| `correlationKey` | string  | Yes      | "DEFAULT_BATCH_KEY" | Key used to correlate messages for aggregation              |
| `messageType`    | string  | No       | "MESSAGE"           | Type of messages being aggregated for logging purposes      |

## Usage Example

```yaml
- to:
    uri: "kamelet:k-batch-aggregation"
    parameters:
      batchSize: 1000
      timeoutMs: 5000
      correlationKey: "PACS008_BATCH_KEY"
      messageType: "PACS.008"
```

## Components

This module includes:

### k-batch-aggregation Kamelet

- Configurable batch aggregation functionality
- YAML-based route definition
- Parameter-driven configuration

### MessageBatchAggregationStrategy

- Spring-managed aggregation strategy bean
- Collects exchanges into List<Exchange> for batch processing
- Auto-configured and discoverable via Spring Boot

## Dependencies

This kamelet requires:

- Apache Camel Core
- Apache Camel Kamelet Support
- Spring Context (for @Component annotation)
- Spring Boot Auto-Configuration (optional)

## Headers Set

The kamelet sets the following headers for batch tracking:

- `BatchMessageType`: Type of messages in the batch
- `BatchCorrelationKey`: Correlation key used for aggregation
- `BatchMaxSize`: Maximum batch size configured
- `BatchTimeoutMs`: Timeout value configured
- `BatchCompleted`: Flag indicating batch completion
- `BatchCompletionReason`: Reason for batch completion (size/timeout)
- `BatchProcessingTimestamp`: Timestamp when batch was completed

## Integration

This kamelet is designed to work with the PIXEL-V2 technical framework and integrates seamlessly with:

- `k-mq-message-receiver`: For message consumption
- `k-db-tx`: For batch persistence operations
- Custom persistence processors for batch handling
