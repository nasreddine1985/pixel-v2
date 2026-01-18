# K-Kafka Log Starter Kamelet

Kafka starter kamelet specialized for consuming log event messages from Kafka topics.

## Features

- Consumes messages from Kafka topics with configurable consumer group
- Includes error handling with doTry/doCatch
- Sets processing metadata headers (timestamp, offset, partition)
- Logs all incoming events for monitoring
- Supports multiple deserialization strategies

## Usage

```yaml
from:
  uri: "kamelet:k-kafka-log-starter?bootstrapServers=kafka:9092&topic=pixel-logs&groupId=log-processor-group"
  steps:
    # Process the log event...
    - to: "direct:process-log"
```

## Parameters

- **bootstrapServers**: Kafka bootstrap servers (required)
- **topic**: Kafka topic to consume from (required)
- **groupId**: Consumer group ID (default: k-kafka-log-receiver-group)
- **offsetReset**: Offset reset strategy - earliest or latest (default: latest)
- **maxPollRecords**: Max records per poll (default: 500)
- **sessionTimeoutMs**: Session timeout (default: 30000)
- **autoCommitIntervalMs**: Auto-commit interval (default: 5000)
