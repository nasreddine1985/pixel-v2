# K-Kafka Starter Kamelet

A specialized Kamelet for starting Kafka message consumption and routing them to configurable endpoints for further processing.

## Overview

The `k-kafka-starter` kamelet provides a standardized way to consume messages from Kafka topics with comprehensive metadata enrichment and flexible routing capabilities.

## Features

- **Kafka Consumer**: Connects to Kafka brokers and consumes messages from specified topics
- **Metadata Enrichment**: Automatically adds Kafka-specific headers (partition, offset, key, timestamp)
- **Message Type Detection**: Intelligently detects message types (XML, JSON, delimited, etc.)
- **Flexible Routing**: Routes messages to configurable endpoints for further processing
- **Comprehensive Logging**: Detailed logging with `[KAFKA-STARTER]` prefix for traceability
- **Error Handling**: Robust error handling and status tracking
- **Configurable Consumer Settings**: Full control over Kafka consumer configuration

## Configuration Parameters

### Required Parameters

| Parameter          | Description                                     | Example          |
| ------------------ | ----------------------------------------------- | ---------------- |
| `bootstrapServers` | Comma-separated list of Kafka bootstrap servers | `localhost:9092` |
| `topic`            | Name of the Kafka topic to consume from         | `payment-events` |

### Optional Parameters

| Parameter              | Description                                    | Default                           | Example                    |
| ---------------------- | ---------------------------------------------- | --------------------------------- | -------------------------- |
| `groupId`              | Kafka consumer group identifier                | `k-kafka-receiver-group`          | `payment-processor-group`  |
| `offsetReset`          | Offset reset strategy (`earliest` or `latest`) | `latest`                          | `earliest`                 |
| `maxPollRecords`       | Maximum records returned in single poll        | `500`                             | `100`                      |
| `sessionTimeoutMs`     | Session timeout in milliseconds                | `30000`                           | `45000`                    |
| `enableAutoCommit`     | Enable automatic offset commits                | `true`                            | `false`                    |
| `autoCommitIntervalMs` | Auto-commit interval in milliseconds           | `5000`                            | `10000`                    |
| `valueDeserializer`    | Value deserializer class                       | `StringDeserializer`              | `JsonDeserializer`         |
| `keyDeserializer`      | Key deserializer class                         | `StringDeserializer`              | `StringDeserializer`       |
| `routingEndpoint`      | Endpoint to route messages to                  | `direct:kafka-message-processing` | `direct:payment-ingestion` |

## Message Headers Set

The kamelet automatically enriches messages with the following headers:

### Source Information

- `messageSource`: "KAFKA_TOPIC"
- `receiptTimestamp`: Current timestamp (yyyy-MM-dd HH:mm:ss.SSS)
- `receiptChannel`: "KAFKA_CONSUMER"

### Kafka Metadata

- `kafkaTopic`: Source topic name
- `kafkaGroupId`: Consumer group ID
- `kafkaPartition`: Message partition number
- `kafkaOffset`: Message offset within partition
- `kafkaKey`: Message key (if present)
- `kafkaTimestamp`: Kafka message timestamp

### Processing Information

- `detectedMessageType`: Auto-detected message type
- `processingStartTime`: Processing start timestamp
- `messageLength`: Message body length in bytes
- `processingStatus`: Current processing status
- `kafkaProcessed`: Boolean flag indicating Kafka processing completion

## Usage Examples

### Basic Usage

```yaml
- from:
    uri: "kamelet:k-kafka-starter?bootstrapServers=localhost:9092&topic=payment-events"
```

### Advanced Configuration

```yaml
- from:
    uri: "kamelet:k-kafka-starter"
    parameters:
      bootstrapServers: "kafka1:9092,kafka2:9092,kafka3:9092"
      topic: "payment-processing-events"
      groupId: "payment-ingestion-group"
      offsetReset: "earliest"
      maxPollRecords: 100
      routingEndpoint: "direct:payment-ingestion"
```

### With Custom Routing

```yaml
- from:
    uri: "kamelet:k-kafka-starter?bootstrapServers={{kafka.brokers}}&topic={{kafka.topic.payments}}&routingEndpoint=direct:custom-processing"
```

## Message Type Detection

The kamelet automatically detects message types based on content analysis:

- **pacs.008**: XML messages containing FIToFICstmrCdtTrf or pacs.008 elements
- **pan.001**: XML messages containing CstmrCdtTrfInitn or pan.001 elements
- **camt.056**: XML messages containing FIToFIPmtCxlReq or camt.056 elements
- **XML_MESSAGE**: Generic XML content (starts with `<?xml` or `<`)
- **JSON_MESSAGE**: JSON content (starts with `{` and ends with `}`)
- **DELIMITED_MESSAGE**: Pipe-delimited content (contains `|`)
- **TEXT_MESSAGE**: Plain text content (fallback)

## Integration with Payment Ingestion

### Adding to Ingestion Flow

To integrate with the existing payment ingestion service:

1. **Add Dependency** to `ingestion/pom.xml`:

```xml
<dependency>
    <groupId>com.pixel.v2</groupId>
    <artifactId>k-kafka-starter</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

2. **Add Route** to `PaymentIngestionRouteBuilder.java`:

```java
// Kafka receipt route using k-kafka-starter kamelet
from("kamelet:k-kafka-starter?bootstrapServers={{kafka.brokers}}&topic={{kafka.topic.incoming}}&routingEndpoint=direct:payment-ingestion")
    .routeId("kafka-receipt-route")
    .log("Message received from k-kafka-starter kamelet")
    .to("direct:payment-ingestion");
```

3. **Update Configuration** in `application.properties`:

```properties
# Kafka configuration for message reception
kafka.brokers=localhost:9092
kafka.topic.incoming=incoming-payments
kafka.consumer.group.id=payment-ingestion-consumers
```

### YAML Route Configuration

Add to `payment-ingestion-routes.yaml`:

```yaml
- route:
    id: kafka-receipt-route
    from:
      uri: kamelet:k-kafka-starter?bootstrapServers={{kafka.brokers}}&topic={{kafka.topic.incoming}}&groupId={{kafka.consumer.group.id}}&routingEndpoint=direct:payment-ingestion
      steps:
        - log:
            message: "Message received from k-kafka-starter kamelet"
        - to:
            uri: direct:payment-ingestion
```

## Monitoring and Logging

### Log Messages

The kamelet produces structured log messages with `[KAFKA-RECEIVER]` prefix:

```
[KAFKA-RECEIVER] Received message from topic 'payment-events' with key: MSG123, partition: 0, offset: 1234
[KAFKA-RECEIVER] Message enriched with Kafka metadata, routing to: direct:payment-ingestion
[KAFKA-PROCESSOR] Processing message from topic 'payment-events', partition: 0, offset: 1234, key: MSG123
[KAFKA-PROCESSOR] Message processed successfully - Type: pacs.008, Length: 2048 bytes
```

### Monitoring Headers

Monitor processing through headers:

- Check `processingStatus` for processing state
- Monitor `processingError` for error conditions
- Track `kafkaOffset` and `kafkaPartition` for Kafka consumption progress

## Error Handling

### Consumer Errors

- Connection failures to Kafka brokers
- Topic not found or access denied
- Deserialization errors
- Consumer group coordination issues

### Processing Errors

- Empty or null message bodies
- Routing endpoint not available
- Message processing exceptions

All errors are logged with appropriate context and set error headers for downstream handling.

## Testing

### Unit Tests

```bash
mvn test -Dtest=KafkaMessageProcessorTest
```

### Integration Tests

```bash
mvn test -Dtest=KafkaMessageReceiverTest
```

### Manual Testing with Kafka

1. **Start Kafka** (using Docker):

```bash
docker run -p 9092:9092 apache/kafka:2.13-3.5.1
```

2. **Create Test Topic**:

```bash
kafka-topics.sh --create --topic test-payments --bootstrap-server localhost:9092
```

3. **Send Test Messages**:

```bash
echo '<?xml version="1.0"?><pacs.008><id>123</id></pacs.008>' | kafka-console-producer.sh --topic test-payments --bootstrap-server localhost:9092
```

4. **Run Kamelet**:

```bash
mvn spring-boot:run -Dkafka.receiver.topic=test-payments
```

## Dependencies

- Apache Camel 4.1.0+
- Spring Boot 3.4.1+
- Kafka Clients
- Jackson (for JSON processing)

## Architecture Integration

The kamelet fits into the payment processing architecture as a fourth message receipt channel:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  k-mq-message-  │    │ k-http-message-  │    │ k-cft-message-  │    │ k-kafka-message-│
│    receiver     │    │     receiver     │    │    receiver     │    │    receiver     │
│ (MQ Reception)  │    │ (API Reception)  │    │ (File Reception)│    │(Kafka Reception)│
└─────────┬───────┘    └─────────┬────────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                       │                       │
          └──────────────────────┼───────────────────────┼───────────────────────┘
                                 │                       │
                    ┌────────────▼───────────────────────▼─────┐
                    │        Ingestion Orchestrator           │
                    │    (PaymentIngestionRouteBuilder)       │
                    └─────────────────────────────────────────┘
```
