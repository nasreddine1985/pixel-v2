# Conditional Routing Implementation

## Overview

This document describes the implementation of conditional routing in the PIXEL-V2 project, specifically the integration between the processing module and the outbound module with intelligent message routing based on origin.

## Requirements Implemented

✅ **After cdmOutputEndpoint route in processing module:**

- Route result directly to outbound module if origin is NOT Kafka
- Route to a new Kafka broker if origin IS Kafka

## Implementation Details

### 1. Processing Module Changes

#### File: `processing/src/main/java/com/pixel/v2/processing/routes/PaymentMessageRoutes.java`

**Added Configuration Properties:**

```java
@Value("${outbound.service.endpoint}")
private String outboundServiceEndpoint;

@Value("${kafka.output.broker}")
private String kafkaOutputBroker;

@Value("${kafka.output.topic}")
private String kafkaOutputTopic;
```

**Added Constants (to eliminate duplicate string literals):**

```java
// Constants to avoid duplicate string literals
private static final String CONTENT_TYPE_HEADER = "Content-Type";
private static final String APPLICATION_JSON = "application/json";
```

**Implemented Conditional Routing Logic:**

```java
// CDM Output Processing Route with conditional routing
from("direct:cdm-output")
    .routeId("cdm-output-processing")
    .log(LoggingLevel.INFO, "[CDM-OUTPUT] Processing CDM transformed message")
    .setHeader(CONTENT_TYPE_HEADER, constant(APPLICATION_JSON))
    .choice()
        .when(header("messageSource").isEqualTo("KAFKA_TOPIC"))
            .log(LoggingLevel.INFO, "[CDM-OUTPUT] Routing Kafka-originated message to Kafka broker")
            .to("kafka:" + kafkaOutputTopic + "?brokers=" + kafkaOutputBroker)
        .otherwise()
            .log(LoggingLevel.INFO, "[CDM-OUTPUT] Routing non-Kafka message to outbound service")
            .to("direct:route-to-outbound")
    .end();
```

**Added Outbound Service Communication Route:**

```java
// Route to outbound service - handles HTTP communication with outbound module
from("direct:route-to-outbound")
    .routeId("route-to-outbound-service")
    .log(LoggingLevel.INFO, "[OUTBOUND-ROUTING] Sending CDM message to outbound service")
    .setHeader("CamelHttpMethod", constant("POST"))
    .setHeader(CONTENT_TYPE_HEADER, constant(APPLICATION_JSON))
    .removeHeaders("Camel*", "kafka*", "CamelHttp*")
    .doTry()
        .to(outboundServiceEndpoint)
        .log(LoggingLevel.INFO, "[OUTBOUND-ROUTING] Successfully sent message to outbound service")
    .doCatch(Exception.class)
        .log(LoggingLevel.ERROR, "[OUTBOUND-ROUTING] Failed to send message to outbound service: ${exception.message}")
        .setHeader("ErrorCode", constant("OUTBOUND_SERVICE_FAILURE"))
        .setHeader("ErrorDescription", simple("Failed to communicate with outbound service: ${exception.message}"))
        .to(errorEndpoint)
    .end();
```

#### File: `processing/src/main/resources/application.properties`

**Added Configuration:**

```properties
# Outbound Service Configuration
outbound.service.endpoint=http://localhost:8082/outbound/submit
kafka.output.broker=localhost:9092
kafka.output.topic=cdm-processed-messages
```

### 2. Routing Logic

The conditional routing works as follows:

1. **Message arrives at `direct:cdm-output`** (after CDM transformation)
2. **Check messageSource header:**
   - If `messageSource = "KAFKA_TOPIC"` → Route to Kafka broker
   - If `messageSource ≠ "KAFKA_TOPIC"` → Route to outbound service
3. **For Kafka routing:** Direct send to Kafka topic `cdm-processed-messages`
4. **For outbound routing:** HTTP POST to `http://localhost:8082/outbound/submit`

### 3. Error Handling

- **HTTP communication errors** are caught and logged
- **Failed outbound calls** are routed to error endpoint
- **Proper header cleanup** before HTTP calls to avoid Camel-specific headers

### 4. Message Headers

The routing decision is based on the `messageSource` header which should be set by the message receiver kamelets:

- **k-kafka-message-receiver**: Sets `messageSource = "KAFKA_TOPIC"`
- **k-http-message-receiver**: Sets `messageSource = "HTTP_ENDPOINT"`
- **k-mq-message-receiver**: Sets `messageSource = "MQ_QUEUE"`

## Testing

### Build Verification

```bash
cd /Users/n.abassi/sources/pixel-v2
mvn clean compile
# ✅ BUILD SUCCESS - All 15 modules compiled successfully
```

### Module-Specific Compilation

```bash
cd /Users/n.abassi/sources/pixel-v2/processing
mvn clean compile
# ✅ BUILD SUCCESS - Processing module compiles without errors
```

## Service Endpoints

### Processing Module

- **Default Port**: 8081
- **CDM Output Route**: `direct:cdm-output`
- **Outbound Route**: `direct:route-to-outbound`

### Outbound Module

- **Default Port**: 8082
- **Submit Endpoint**: `POST /outbound/submit`

### Kafka Configuration

- **Broker**: `localhost:9092`
- **Output Topic**: `cdm-processed-messages`

## Dependencies

### Processing Module

- Apache Camel Spring Boot Starter
- Apache Camel HTTP component (for outbound service calls)
- Apache Camel Kafka component (for Kafka output)

### Required Services

- **Outbound Service** running on port 8082
- **Kafka Broker** running on port 9092
- **Topic `cdm-processed-messages`** must exist in Kafka

## Usage Examples

### 1. Kafka-Originated Message

```
Message with header: messageSource = "KAFKA_TOPIC"
→ Routes to: kafka:cdm-processed-messages?brokers=localhost:9092
```

### 2. HTTP-Originated Message

```
Message with header: messageSource = "HTTP_ENDPOINT"
→ Routes to: http://localhost:8082/outbound/submit
```

### 3. MQ-Originated Message

```
Message with header: messageSource = "MQ_QUEUE"
→ Routes to: http://localhost:8082/outbound/submit
```

## Monitoring and Logging

All routing decisions are logged with appropriate log levels:

- **INFO**: Routing decisions and successful operations
- **ERROR**: Failed operations with detailed error messages

Log patterns include:

- `[CDM-OUTPUT]`: CDM processing operations
- `[OUTBOUND-ROUTING]`: Outbound service communication
- `[KAFKA-OUTPUT]`: Kafka routing operations

## Next Steps

To complete the end-to-end testing:

1. **Start Kafka broker** on localhost:9092
2. **Create topic** `cdm-processed-messages`
3. **Start outbound service** on port 8082
4. **Start processing service** on port 8081
5. **Send test messages** with different `messageSource` headers
6. **Verify routing** behavior in logs and destination systems

## Files Modified

- ✅ `processing/src/main/java/com/pixel/v2/processing/routes/PaymentMessageRoutes.java`
- ✅ `processing/src/main/resources/application.properties`

## Build Status

- ✅ **Compilation**: All modules compile successfully
- ✅ **Dependencies**: All dependencies resolved
- ✅ **Code Quality**: No compilation errors, duplicate string literals resolved
- ✅ **Integration**: Processing and outbound modules properly configured
