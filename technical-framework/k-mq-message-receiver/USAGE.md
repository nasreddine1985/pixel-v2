# K-MQ Message Receiver Kamelet Usage Examples

## Updated Parameters

The k-mq-message-receiver kamelet now supports configurable JMS connection parameters:

- `destination`: Queue name (required)
- `brokerUrl`: ActiveMQ Artemis broker URL (optional, default: tcp://localhost:61616)
- `user`: Username for broker connection (optional, default: artemis)
- `password`: Password for broker connection (optional, default: artemis)

## Usage Examples

### 1. Basic Usage (using defaults)

```yaml
from:
  uri: "kamelet:k-mq-message-receiver?destination=MY_QUEUE"
```

### 2. Custom Broker Configuration

```yaml
from:
  uri: "kamelet:k-mq-message-receiver?destination=MY_QUEUE&brokerUrl=tcp://prod-broker:61616&user=produser&password=prodpass"
```

### 3. Using Property Placeholders

```yaml
from:
  uri: "kamelet:k-mq-message-receiver?destination={{queue.name}}&brokerUrl={{broker.url}}&user={{broker.user}}&password={{broker.password}}"
```

### 4. Environment-Specific Configuration

```yaml
# Development
from:
  uri: "kamelet:k-mq-message-receiver?destination=DEV_QUEUE&brokerUrl=tcp://dev-artemis:61616&user=dev&password=dev123"

# Production
from:
  uri: "kamelet:k-mq-message-receiver?destination=PROD_QUEUE&brokerUrl=tcp://prod-artemis:61616&user=prod&password={{prod.password}}"
```

## Application Properties Support

You can also configure default values in application.properties:

```properties
# Default connection settings (used when kamelet parameters are not provided)
mq.broker-url=tcp://localhost:61616
mq.user=artemis
mq.password=artemis

# Queue names
flow.pacs008.queue.name=PACS008_QUEUE
```

## Migration from Previous Version

**Before (static configuration):**

```yaml
from:
  uri: "kamelet:k-mq-message-receiver?destination=MY_QUEUE"
  # Used hardcoded broker connection from application.properties
```

**After (flexible configuration):**

```yaml
from:
  uri: "kamelet:k-mq-message-receiver?destination=MY_QUEUE&brokerUrl=tcp://my-broker:61616&user=myuser&password=mypass"
  # Can specify broker connection per kamelet usage
```

## Benefits

1. **Environment Flexibility**: Different brokers per environment
2. **Multi-Broker Support**: Connect to multiple brokers in same application
3. **Security**: Avoid hardcoded credentials in configuration files
4. **Reusability**: Same kamelet for different broker configurations
5. **Backward Compatibility**: Works with existing configurations using defaults
