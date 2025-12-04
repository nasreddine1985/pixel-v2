# k-mq-message-receiver Kamelet

Listens to **ActiveMQ Artemis** and automatically consumes messages for payment processing.

## Configuration

- Connects to ActiveMQ Artemis container running on localhost:61616
- Uses default Artemis credentials (username: artemis, password: artemis)
- Automatically configures JMS connection factory for Artemis

## ActiveMQ Artemis Setup

Start your ActiveMQ Artemis container:

```bash
docker run -d --name artemis \
  -p 61616:61616 \
  -p 8161:8161 \
  -e ARTEMIS_USERNAME=artemis \
  -e ARTEMIS_PASSWORD=artemis \
  apache/activemq-artemis:latest
```

## Properties

- `destination`: the Artemis queue name to consume from (default: QUEUE.IN)
- `host`: Artemis broker host (default: localhost)
- `port`: Artemis broker port (default: 61616)
- `username`: Artemis username (default: artemis)
- `password`: Artemis password (default: artemis)
- `jmsConnectionFactoryRef`: connection factory bean reference (default: artemisConnectionFactory)

## Example Usage

```yaml
- from:
    uri: "kamelet:k-mq-message-receiver?destination=PAYMENT.QUEUE"
    steps:
      - log: "Message received from Artemis: ${body}"
```

## Automatic Message Processing

The kamelet automatically:

1. Connects to your ActiveMQ Artemis container
2. Consumes messages from the specified queue
3. Sets processing headers (source, timestamp, channel)
4. Routes to `direct:process-mq-message` for further processing

## Integration

This kamelet is part of the PIXEL-V2 project's payment ingestion pipeline, handling message receipt from ActiveMQ Artemis and routing for business processing.
