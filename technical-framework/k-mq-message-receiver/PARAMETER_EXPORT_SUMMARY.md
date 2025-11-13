# K-MQ Message Receiver Kamelet Parameter Export Summary

## Overview

Successfully exported `concurrentConsumers` and `maxConcurrentConsumers` parameters from hardcoded values to configurable properties in the `k-mq-message-receiver` kamelet and updated all consuming modules.

## Changes Made

### 1. Kamelet Definition Updates

**File:** `/technical-framework/k-mq-message-receiver/src/main/resources/kamelets/k-mq-message-receiver.kamelet.yaml`

**Added Properties:**

```yaml
concurrentConsumers:
  title: "Concurrent Consumers"
  type: integer
  description: "Number of concurrent consumers for parallel message processing"
  default: 5

maxConcurrentConsumers:
  title: "Maximum Concurrent Consumers"
  type: integer
  description: "Maximum number of concurrent consumers that can be created dynamically"
  default: 10
```

**Updated Template Parameters:**

```yaml
# Before:
concurrentConsumers: 5
maxConcurrentConsumers: 10

# After:
concurrentConsumers: "{{concurrentConsumers}}"
maxConcurrentConsumers: "{{maxConcurrentConsumers}}"
```

### 2. Flow Module Updates

**File:** `/flow/src/main/java/com/pixel/v2/flow/routes/Pacs008RouteBuilder.java`

**Updated Kamelet Usage:**

```java
// Before:
from("kamelet:k-mq-message-receiver" + "?destination={{flow.pacs008.queue.name}}"
    + "&brokerUrl={{mq.broker-url}}" + "&user={{mq.user}}"
    + "&password={{mq.password}}"
    + "&acknowledgmentModeName=CLIENT_ACKNOWLEDGE" + "&transacted=true")

// After:
from("kamelet:k-mq-message-receiver" + "?destination={{flow.pacs008.queue.name}}"
    + "&brokerUrl={{mq.broker-url}}" + "&user={{mq.user}}"
    + "&password={{mq.password}}"
    + "&acknowledgmentModeName=CLIENT_ACKNOWLEDGE" + "&transacted=true"
    + "&concurrentConsumers={{flow.pacs008.concurrent-consumers}}"
    + "&maxConcurrentConsumers={{flow.pacs008.max-concurrent-consumers}}")
```

**File:** `/flow/src/main/resources/application.properties`

**Added Configuration:**

```properties
# Consumer Configuration
flow.pacs008.concurrent-consumers=5
flow.pacs008.max-concurrent-consumers=10
```

### 3. Ingestion Module Updates

**File:** `/ingestion/src/main/resources/camel/main-ingestion-routes.yaml`

**Updated Route:**

```yaml
# Before:
uri: kamelet:k-mq-message-receiver?queueName={{ingestion.mq.input.queue:PAYMENT_INPUT}}&host={{ingestion.mq.host:localhost}}&port={{ingestion.mq.port:1414}}&queueManager={{ingestion.mq.queue.manager:QM1}}&channel={{ingestion.mq.channel:DEV.ADMIN.SVRCONN}}&username={{ingestion.mq.username:admin}}&password={{ingestion.mq.password:admin}}

# After:
uri: kamelet:k-mq-message-receiver?queueName={{ingestion.mq.input.queue:PAYMENT_INPUT}}&host={{ingestion.mq.host:localhost}}&port={{ingestion.mq.port:1414}}&queueManager={{ingestion.mq.queue.manager:QM1}}&channel={{ingestion.mq.channel:DEV.ADMIN.SVRCONN}}&username={{ingestion.mq.username:admin}}&password={{ingestion.mq.password:admin}}&concurrentConsumers={{ingestion.mq.concurrent-consumers:5}}&maxConcurrentConsumers={{ingestion.mq.max-concurrent-consumers:10}}
```

**File:** `/ingestion/src/main/resources/application.properties`

**Added Configuration:**

```properties
# MQ Series Configuration
ingestion.mq.concurrent-consumers=5
ingestion.mq.max-concurrent-consumers=10
```

## Benefits Achieved

### 1. **External Configuration**

- Parameters can now be configured at deployment time without code changes
- Different environments can have different consumer settings
- Enables performance tuning based on environment needs

### 2. **Backward Compatibility**

- Default values maintain existing behavior (5 concurrent, 10 max)
- No breaking changes for existing deployments
- Smooth migration path for all consuming modules

### 3. **Environment-Specific Tuning**

- Production environments can use higher consumer counts for throughput
- Development environments can use lower counts to conserve resources
- Test environments can be configured for specific test scenarios

### 4. **Runtime Flexibility**

- No need to rebuild kamelet or consuming modules to change settings
- Configuration can be managed through standard Spring Boot property files
- Supports external configuration management systems

## Usage Examples

### Direct Parameter Override

```yaml
# High throughput environment
from:
  uri: "kamelet:k-mq-message-receiver"
  parameters:
    destination: "PAYMENT_QUEUE"
    concurrentConsumers: 15
    maxConcurrentConsumers: 25
```

### Property-Based Configuration

```properties
# Production settings
flow.pacs008.concurrent-consumers=12
flow.pacs008.max-concurrent-consumers=20

# Development settings
ingestion.mq.concurrent-consumers=2
ingestion.mq.max-concurrent-consumers=5
```

### Environment-Specific Profiles

```properties
# application-prod.properties
flow.pacs008.concurrent-consumers=20
flow.pacs008.max-concurrent-consumers=40

# application-dev.properties
flow.pacs008.concurrent-consumers=3
flow.pacs008.max-concurrent-consumers=6
```

## Performance Tuning Guidelines

### Concurrent Consumers

- **Low Volume**: 2-5 consumers
- **Medium Volume**: 5-10 consumers
- **High Volume**: 10-20 consumers
- **Very High Volume**: 20+ consumers

### Maximum Concurrent Consumers

- Typically 1.5-2x the concurrent consumers setting
- Should consider available CPU cores and memory
- Monitor queue depth and processing time to optimize

### Monitoring Points

- Queue depth (should not grow continuously)
- Consumer utilization (all consumers should be active)
- Message processing time (should remain consistent)
- System resource usage (CPU, memory, connections)

## Validation

All changes have been successfully applied and maintain backward compatibility while enabling flexible configuration for different deployment scenarios.
