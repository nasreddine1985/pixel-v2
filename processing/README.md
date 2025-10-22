# Payment Message Processing Service

## Overview

The Payment Message Processing Service is a Spring Boot Apache Camel application that processes payment messages received from Kafka topics and routes them to appropriate CDM (Common Data Model) transformers based on message type detection. **Enhanced with intelligent conditional routing** that distributes processed messages based on their origin.

## Architecture

```
🔄 ENHANCED DUAL INPUT WITH INTELLIGENT CONDITIONAL ROUTING 🔄
═══════════════════════════════════════════════════════════════
📋 KAMELETS INTEGRATION ARCHITECTURE 📋

📥 INPUT SOURCES (with messageSource header setting)
┌─────────────────────────────────┐    ┌─────────────────────────────────┐
│ Kafka Messages                  │    │ Direct Messages                 │
│ (Batch Processing)              │    │ (Real-time Processing)          │
│                                 │    │                                 │
│ 🔗 k-kafka-message-receiver     │    │ Ingestion Module                │
│    KAMELET                      │    │ HTTP/MQ channels                │
│                                 │    │                                 │
│ ┌─────────────────────────────┐ │    │ • Interactive msgs              │
│ │ • Kafka topic consumption   │ │    │ • messageSource =               │
│ │ • Message deserialization   │ │    │   "HTTP_ENDPOINT" or            │
│ │ • Header enrichment         │ │    │   "MQ_QUEUE"                    │
│ │ • messageSource =           │ │    │ • Low latency                   │
│ │   "KAFKA_TOPIC"             │ │    │ • Direct routing                │
│ │ • Error handling            │ │    │                                 │
│ │ • Dead letter topic         │ │    │                                 │
│ └─────────────────────────────┘ │    │                                 │
│                                 │    │                                 │
│ • CFT file messages             │    │                                 │
│ • Batch optimization            │    │                                 │
│ • High throughput               │    │                                 │
└──────────────┬──────────────────┘    └──────────────┬──────────────────┘
               │                                      │
               └─────────────────┬────────────────────┘
                                 ▼
                ═══════════════════════════════════════
                📋 PAYMENT MESSAGE PROCESSING SERVICE
                ═══════════════════════════════════════
                🎯 Entry Point: direct:kafka-message-processing
                                 │
                                 ▼
                        Message Type Detection
                        (MessageTypeProcessor)
                        • Analyzes XML/JSON content
                        • Sets MessageType header
                        • Sets RouteTarget header
                        • Enriches with processing metadata
                                 │
                ┌────────────────┼────────────────┐
                ▼                ▼                ▼
            pacs.008         pan.001          Unknown
                │                │                │
                ▼                ▼                ▼
        ┌─────────────────┐ ┌─────────────────┐    │
        │ 🔗 k-pacs-008-  │ │ 🔗 k-pan-001-   │    │
        │    to-cdm       │ │    to-cdm       │    │
        │    KAMELET      │ │    KAMELET      │    │
        │                 │ │                 │    │
        │ ┌─────────────┐ │ │ ┌─────────────┐ │    │
        │ │• XSLT       │ │ │ │• XSLT       │ │    │
        │ │  transform  │ │ │ │  transform  │ │    │
        │ │• Saxon      │ │ │ │• Saxon      │ │    │
        │ │  processor  │ │ │ │  processor  │ │    │
        │ │• ISO 20022  │ │ │ │• ISO 20022  │ │    │
        │ │  to CDM     │ │ │ │  to CDM     │ │    │
        │ │• Schema     │ │ │ │• Schema     │ │    │
        │ │  validation │ │ │ │  validation │ │    │
        │ │• Error      │ │ │ │• Error      │ │    │
        │ │  handling   │ │ │ │  handling   │ │    │
        │ └─────────────┘ │ │ └─────────────┘ │    │
        └─────────────────┘ └─────────────────┘    │
                │                │                │
                └────────────────┼────────────────┘
                                 ▼                ▼
                        🎯 CDM Output Endpoint   Error Handler
                        (direct:cdm-output)      (direct:unknown-message)
                        • CDM validation         • Error classification
                        • Header enrichment      • Dead letter routing
                        • Transformation status  • Alert generation
                                 │
                                 ▼
                🔀 CONDITIONAL ROUTING ENGINE
                ════════════════════════════════
                Based on messageSource header value
                                 │
                ┌────────────────┼────────────────┐
                ▼                                 ▼
        messageSource ==                 messageSource !=
        "KAFKA_TOPIC"                    "KAFKA_TOPIC"
                │                                 │
                ▼                                 ▼
        ┌─────────────────┐              ┌─────────────────┐
        │ Kafka Output    │              │ Outbound Service│
        │ Route           │              │ Route           │
        │                 │              │                 │
        │ 📤 Kafka        │              │ 📤 HTTP POST    │
        │    Component    │              │    Component    │
        │                 │              │                 │
        │ • Topic: cdm-   │              │ • Endpoint:     │
        │   processed-    │              │   localhost:    │
        │   messages      │              │   8082/submit   │
        │ • Broker:       │              │ • Headers:      │
        │   localhost:    │              │   Content-Type, │
        │   9092          │              │   messageType   │
        │ • Preserves     │              │ • Timeout: 30s  │
        │   Kafka key     │              │ • Retry: 3x     │
        │ • Async         │              │ • Circuit       │
        │   delivery      │              │   breaker       │
        └─────────────────┘              └─────────────────┘
                │                                 │
                ▼                                 ▼
        ┌─────────────────┐              ┌─────────────────┐
        │ Kafka Cluster   │              │ 🔗 Outbound     │
        │                 │              │    Service      │
        │ • Batch         │              │    Integration  │
        │   processing    │              │                 │
        │ • High          │              │ ┌─────────────┐ │
        │   throughput    │              │ │• k-log-tx   │ │
        │ • Persistence   │              │ │  KAMELET    │ │
        │ • Partitioning  │              │ │• Centralized│ │
        │ • Consumer      │              │ │  logging    │ │
        │   groups        │              │ │• Audit      │ │
        │                 │              │ │  trails     │ │
        │ 📊 Monitoring:  │              │ │• Message    │ │
        │ • Topic metrics │              │ │  tracking   │ │
        │ • Consumer lag  │              │ └─────────────┘ │
        │ • Partition     │              │                 │
        │   distribution  │              │ 📊 Monitoring:  │
        └─────────────────┘              │ • HTTP metrics  │
                                         │ • Response time │
                                         │ • Success rate  │
                                         │ • Error tracking│
                                         └─────────────────┘

🔄 KAMELET ECOSYSTEM & ERROR HANDLING
════════════════════════════════════════
┌─────────────────────────────────────────────────────────────────────────┐
│                           INTEGRATED KAMELETS                          │
└─────────────────────────────────────────────────────────────────────────┘

🔗 INPUT KAMELETS                🔗 TRANSFORMATION KAMELETS         🔗 OUTPUT KAMELETS
┌─────────────────┐             ┌─────────────────┐                ┌─────────────────┐
│ k-kafka-message-│             │ k-pacs-008-to-  │                │ k-log-tx        │
│ receiver        │             │ cdm             │                │                 │
│                 │             │                 │                │ ┌─────────────┐ │
│ • Multi-topic   │             │ • XSLT 3.0      │                │ │• Multi-level│ │
│   consumption   │             │ • Saxon HE      │                │ │  logging    │ │
│ • Auto-commit   │             │ • ISO 20022     │                │ │• Categories │ │
│ • Batch config  │             │ • CDM mapping   │                │ │• Metadata   │ │
│ • Error topics  │             │ • Validation    │                │ │  enrichment │ │
│ • Metadata      │             │ • Error codes   │                │ │• Database   │ │
│   enrichment    │             └─────────────────┘                │ │  persistence│ │
└─────────────────┘                       │                       │ │• Async mode │ │
                                          ▼                       │ └─────────────┘ │
                                ┌─────────────────┐                └─────────────────┘
                                │ k-pan-001-to-   │
                                │ cdm             │
                                │                 │
                                │ • XSLT 3.0      │
                                │ • Saxon HE      │
                                │ • ISO 20022     │
                                │ • CDM mapping   │
                                │ • Validation    │
                                │ • Error codes   │
                                └─────────────────┘

🔄 ERROR HANDLING & MONITORING
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Unknown Message │    │ Kamelet         │    │ Routing Failure │
│ Types           │    │ Transformation  │    │ Handling        │
│                 │    │ Errors          │    │                 │
│ • direct:       │    │                 │    │ • HTTP timeout  │
│   unknown-      │    │ • k-pacs-008-   │    │ • Kafka         │
│   message       │    │   to-cdm fails  │    │   unavailable   │
│ • Error logging │    │ • k-pan-001-    │    │ • Retry logic   │
│ • DLQ routing   │    │   to-cdm fails  │    │ • Circuit       │
│ • k-log-tx      │    │ • Invalid XML   │    │   breaker       │
│   integration   │    │ • Schema errors │    │ • Fallback      │
└─────────────────┘    │ • XSLT errors   │    │   routes        │
        │               └─────────────────┘    └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                ▼
                    ┌─────────────────────────┐
                    │ Centralized Error       │
                    │ Handler + k-log-tx      │
                    │                         │
                    │ • Error classification  │
                    │ • Audit logging via     │
                    │   k-log-tx kamelet      │
                    │ • Metrics collection    │
                    │ • Alert generation      │
                    │ • Recovery procedures   │
                    │ • Dead letter queues    │
                    └─────────────────────────┘
```

### 🔄 Simplified Message Flow

```
Input Sources → Processing Service → CDM Transformation → Conditional Routing → Output Destinations

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Kafka     │    │   Message   │    │     CDM     │    │ Conditional │    │   Kafka     │
│  Messages   │───▶│    Type     │───▶│ Transform   │───▶│   Router    │───▶│   Output    │
│ (CFT Files) │    │  Detection  │    │  (pacs/pan) │    │             │    │  (Batch)    │
└─────────────┘    │             │    └─────────────┘    │             │    └─────────────┘
                   │             │                       │             │
┌─────────────┐    │             │                       │             │    ┌─────────────┐
│   Direct    │    │             │                       │             │    │  Outbound   │
│  Messages   │───▶│             │                       │             │───▶│   Service   │
│(HTTP/MQ)    │    │             │                       │             │    │ (Real-time) │
└─────────────┘    └─────────────┘                       └─────────────┘    └─────────────┘

Header Flow:       messageSource                         Decision Logic:
• KAFKA_TOPIC      header set by                         if messageSource == "KAFKA_TOPIC"
• HTTP_ENDPOINT    input source                            → Kafka Output
• MQ_QUEUE                                                else
                                                           → Outbound Service
```

## Features

- **Dual Input Support**: Processes messages from both Kafka (batch) and direct endpoints (real-time)
- **Message Type Detection**: Automatically detects pacs.008 and pan.001 message types from XML and JSON formats
- **Dynamic Routing**: Routes messages to appropriate transformer kamelets based on detected type
- **🆕 Conditional Distribution**: Intelligent routing of processed CDM messages based on origin
  - **Kafka-originated messages** → Routes back to Kafka output broker
  - **Non-Kafka messages** (HTTP/MQ) → Routes to outbound service
- **Error Handling**: Comprehensive error handling for unknown message types and processing failures
- **Real-time Processing**: Direct integration with ingestion module for low-latency processing
- **Monitoring**: Built-in health checks, metrics, and logging
- **Spring Boot Integration**: Full Spring Boot auto-configuration and management endpoints

## Supported Message Types

### pacs.008 - Customer Credit Transfer Initiation

- **XML Format**: ISO 20022 XML with `pacs.008`, `FIToFICstmrCdtTrf`, or `CustomerCreditTransferInitiation`
- **JSON Format**: JSON objects containing `pacs008`, `FIToFICstmrCdtTrf`, or `CustomerCreditTransferInitiation`
- **Transformer**: Routes to `k-pacs-008-to-cdm` kamelet

### pan.001 - Customer Payment Status Report

- **XML Format**: ISO 20022 XML with `pan.001`, `CstmrPmtStsRpt`, or `CustomerPaymentStatusReport`
- **JSON Format**: JSON objects containing `pan001`, `CstmrPmtStsRpt`, or `CustomerPaymentStatusReport`
- **Transformer**: Routes to `k-pan-001-to-cdm` kamelet

## Configuration

### Application Properties

```properties
# Processing Configuration
processing.kafka.input.endpoint=direct:kafka-message-processing
processing.cdm.output.endpoint=direct:cdm-output
processing.error.endpoint=direct:error-handling

# 🆕 Conditional Routing Configuration
outbound.service.endpoint=http://localhost:8082/outbound/submit
kafka.output.broker=localhost:9092
kafka.output.topic=cdm-processed-messages

# Kafka Configuration (for batch processing)
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=payment-processing-service
kafka.topics.input=payment-events

# Transformer Configuration
transformers.pacs008.endpoint=kamelet:k-pacs-008-to-cdm
transformers.pan001.endpoint=kamelet:k-pan-001-to-cdm

# Integration with Ingestion Module
ingestion.integration.enabled=true
ingestion.realtime.endpoint=direct:kafka-message-processing
```

### Environment Variables

| Variable                     | Description                  | Default                      |
| ---------------------------- | ---------------------------- | ---------------------------- |
| `KAFKA_BOOTSTRAP_SERVERS`    | Kafka cluster connection     | `localhost:9092`             |
| `KAFKA_CONSUMER_GROUP_ID`    | Consumer group ID            | `payment-processing-service` |
| `KAFKA_INPUT_TOPIC`          | Input topic name             | `payment-events`             |
| `CDM_OUTPUT_ENDPOINT`        | CDM output endpoint          | `direct:cdm-persistence`     |
| `🆕 CDM_PERSISTENCE_ENABLED` | Enable CDM persistence       | `true`                       |
| `🆕 CDM_AUDIT_ENABLED`       | Enable CDM audit trail       | `true`                       |
| `🆕 INGESTION_INTEGRATION`   | Enable ingestion integration | `true`                       |

## Installation

### Prerequisites

- Java 21+
- Apache Maven 3.8+
- Apache Kafka 3.0+ (for batch processing)
- k-pacs-008-to-cdm kamelet
- k-pan-001-to-cdm kamelet
- k-kafka-message-receiver kamelet (for batch processing)
- **🆕 k-db-tx kamelet** (for CDM persistence)
- **🆕 Oracle Database** (for CDM and message persistence)
- **🆕 Ingestion Module** (for real-time integration)

### Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR
java -jar target/processing-1.0.1-SNAPSHOT.jar
```

### Docker Deployment

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/processing-1.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Usage

### Integration with k-kafka-message-receiver

The processing service is designed to work with the k-kafka-message-receiver kamelet:

```yaml
# k-kafka-message-receiver configuration
apiVersion: camel.apache.org/v1alpha1
kind: Kamelet
metadata:
  name: k-kafka-message-receiver
spec:
  definition:
    properties:
      bootstrapServers: "localhost:9092"
      topics: "payment-events"
      consumerGroupId: "payment-receiver-group"
      routingEndpoint: "direct:kafka-message-processing"
```

### Processing Flow

#### 🔄 Dual Processing with Conditional Output

**Batch Processing (CFT Messages via Kafka)**

1. **Message Reception**: Receives from k-kafka-message-receiver via `direct:kafka-message-processing`
2. **Type Detection**: `MessageTypeProcessor` analyzes message content
3. **Header Setting**: Sets `MessageType`, `RouteTarget`, and `messageSource=KAFKA_TOPIC` headers
4. **Dynamic Routing**: Routes to appropriate transformer based on headers
5. **Transformation**: Calls transformer kamelet (k-pacs-008-to-cdm or k-pan-001-to-cdm)
6. **🆕 Conditional Routing**: Routes to Kafka output broker (based on messageSource header)
7. **🆕 Kafka Output**: Sends to `cdm-processed-messages` topic

**Real-time Processing (HTTP/MQ Messages via Ingestion)**

1. **Direct Reception**: Receives from ingestion module via `direct:kafka-message-processing`
2. **Type Detection**: `MessageTypeProcessor` analyzes message content
3. **Header Setting**: Sets `MessageType`, `RouteTarget`, and `messageSource≠KAFKA_TOPIC` headers
4. **Dynamic Routing**: Routes to appropriate transformer based on headers
5. **Transformation**: Calls transformer kamelet with real-time optimization
6. **🆕 Conditional Routing**: Routes to outbound service (based on messageSource header)
7. **🆕 HTTP Delivery**: POST to outbound service at `localhost:8082/outbound/submit`

#### 🔀 Conditional Routing Logic

```java
// CDM Output Processing with Conditional Routing
from(cdmOutputEndpoint)
    .choice()
        .when(header("messageSource").isEqualTo("KAFKA_TOPIC"))
            .log("Routing Kafka-originated message to Kafka output broker")
            .to("kafka:cdm-processed-messages?brokers=localhost:9092")
        .otherwise()
            .log("Routing non-Kafka message to outbound service")
            .to("http://localhost:8082/outbound/submit")
    .end();
```

### Message Headers

The service adds the following headers to processed messages:

| Header                      | Description               | Values                                     |
| --------------------------- | ------------------------- | ------------------------------------------ |
| `MessageType`               | Detected message type     | `pacs.008`, `pan.001`, `unknown`           |
| `RouteTarget`               | Target route endpoint     | `direct:pacs-008-transform`, etc.          |
| `ProcessingTimestamp`       | Processing timestamp      | Milliseconds since epoch                   |
| `ProcessedBy`               | Processor identifier      | `MessageTypeProcessor`                     |
| `🆕 messageSource`          | Message origin identifier | `KAFKA_TOPIC`, `HTTP_ENDPOINT`, `MQ_QUEUE` |
| `🆕 TransformationComplete` | CDM transformation status | `true`, `false`                            |
| `🆕 OutputTimestamp`        | Output routing timestamp  | ISO 8601 format                            |
| `🆕 messageType`            | Outbound message type     | `CDM_PROCESSED`                            |
| `🆕 processingStage`        | Current processing stage  | `CDM_TRANSFORMATION_COMPLETE`              |

## API Endpoints

### Health Check

- **Endpoint**: `GET /actuator/health`
- **Description**: Application health status
- **Response**:

```json
{
  "status": "UP",
  "components": {
    "camel": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

### Metrics

- **Endpoint**: `GET /actuator/metrics`
- **Description**: Application metrics
- **Camel Metrics**: `GET /actuator/camel/routes`

### Direct Endpoints (Internal)

| Endpoint                          | Purpose                  | Usage                                  |
| --------------------------------- | ------------------------ | -------------------------------------- |
| `direct:kafka-message-processing` | Main input (dual mode)   | Receives from Kafka & ingestion module |
| `direct:pacs-008-transform`       | pacs.008 processing      | Internal routing                       |
| `direct:pan-001-transform`        | pan.001 processing       | Internal routing                       |
| `direct:unknown-message`          | Unknown message handling | Error cases                            |
| `🆕 direct:cdm-output`            | CDM output routing       | Routes to conditional router           |
| `🆕 direct:route-to-outbound`     | Outbound service routing | HTTP calls to outbound module          |
| `direct:error-handling`           | Error processing         | Error cases                            |
| `direct:health-check`             | Health status            | Monitoring                             |
| `direct:metrics`                  | Metrics collection       | Monitoring                             |

## 🆕 CDM Persistence Integration

### CDM Transformation and Persistence Flow

After successful transformation to CDM format, the processing module automatically handles persistence:

```yaml
# CDM Processing Pipeline
- Transform: pacs.008/pan.001 → CDM JSON format
- Validate: CDM schema validation
- Route: cdmOutputEndpoint → direct:cdm-persistence
- Persist: k-db-tx → CdmMessage entity
- Audit: Link CDM record to source message
- Response: Processing completion status
```

### CDM Persistence Features

- **Automatic Persistence**: No manual intervention required for CDM storage
- **Transaction Management**: Ensures data consistency across operations
- **Relationship Tracking**: CDM objects linked to original payment messages
- **Error Isolation**: CDM persistence failures don't affect message processing
- **Audit Trail**: Complete processing history maintained

### CDM Database Schema

The processing module integrates with the `CdmMessage` entity:

```java
// CdmMessage entity fields automatically populated
- cdmType: PAYMENT_INSTRUCTION, PAYMENT_STATUS, etc.
- instructionId: Extracted from CDM payload
- endToEndId: Transaction identifier
- amount/currency: Payment amount information
- debtorInfo/creditorInfo: Party information
- processingDate: Transformation timestamp
- sourceMessageId: Link to original ReceivedMessage
```

### CDM Output Configuration

```properties
# CDM Output Endpoint Configuration
cdmOutputEndpoint=direct:cdm-persistence

# CDM Persistence Settings
processing.cdm.persistence.enabled=true
processing.cdm.persistence.auto-link=true
processing.cdm.persistence.validation.enabled=true
```

## Monitoring and Logging

### Log Levels

```properties
logging.level.com.pixel.v2.processing=INFO
logging.level.org.apache.camel=INFO
logging.level.org.springframework.kafka=WARN
```

### Key Log Messages

- `[PROCESSING-MAIN] Received message for processing from: {source}`
- `[PROCESSING-MAIN] Message type: {type}, routing to: {route}`
- `[PACS-008-TRANSFORM] Successfully transformed pacs.008 to CDM`
- `[PAN-001-TRANSFORM] Successfully transformed pan.001 to CDM`
- `🆕 [CDM-PERSISTENCE] Routing CDM object to k-db-tx`
- `🆕 [CDM-PERSISTENCE] CDM object persisted successfully: {cdmId}`
- `🆕 [CDM-AUDIT] Linked CDM record {cdmId} to source message {messageId}`
- `[UNKNOWN-MESSAGE] Received message with unknown type`
- `[ERROR-HANDLER] Processing error: {code} - {description}`
- `🆕 [CDM-ERROR] CDM persistence failed: {error}`

### Metrics

Available through Spring Boot Actuator:

- Message processing rates (batch vs real-time)
- CDM transformation success/failure rates
- **🆕 CDM persistence metrics**
- **🆕 Real-time vs batch processing latency**
- Error rates and types
- Route execution times
- **🆕 k-db-tx kamelet performance**
- Memory and CPU usage
- Camel route status

## Error Handling

### Error Types

1. **Unknown Message Type**: Messages that don't match pacs.008 or pan.001 patterns
2. **JSON Parsing Errors**: Invalid JSON format in message body
3. **Transformation Errors**: Failures in kamelet transformation
4. **Routing Errors**: Issues with message routing
5. **🆕 CDM Persistence Errors**: Failures in k-db-tx kamelet
6. **🆕 CDM Validation Errors**: Invalid CDM schema or field mapping issues
7. **🆕 Audit Trail Errors**: Issues linking CDM objects to source messages

### Error Response Format

```json
{
  "ErrorCode": "UNKNOWN_MESSAGE_TYPE",
  "ErrorDescription": "Message type could not be determined",
  "ErrorHandled": true,
  "ErrorTimestamp": "2025-10-21T21:35:00.000Z"
}
```

### Retry Configuration

```properties
error.retry.max-attempts=3
error.retry.delay=1000
error.deadletter.endpoint=direct:dead-letter-queue
```

## Testing

### Unit Tests

```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=MessageTypeProcessorTest
```

### Integration Tests

```bash
# Run integration tests
mvn verify

# Run with test profile
mvn test -Dspring.profiles.active=test
```

### Test Message Examples

#### pacs.008 XML Message

```xml
<?xml version="1.0"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>MSG001</MsgId>
    </GrpHdr>
  </FIToFICstmrCdtTrf>
</Document>
```

#### pan.001 JSON Message

```json
{
  "pan001": {
    "CstmrPmtStsRpt": {
      "GrpHdr": {
        "MsgId": "MSG002"
      }
    }
  }
}
```

## Performance Considerations

### Throughput Optimization

- Configure Kafka consumer batch sizes
- Tune thread pool settings
- Enable Camel route optimization

### Memory Management

- Monitor message size and processing times
- Configure appropriate JVM heap settings
- Use streaming for large messages

### Scalability

- Deploy multiple instances for horizontal scaling
- Use Kafka partitioning for load distribution
- Configure appropriate consumer group settings

## Troubleshooting

### Common Issues

1. **Transformer Kamelet Not Found**

   - Ensure k-pacs-008-to-cdm and k-pan-001-to-cdm are available
   - Check kamelet registration in Camel context

2. **🆕 CDM Persistence Issues**

   - Verify k-db-tx kamelet is available
   - Check database connection and CdmMessage table schema
   - Ensure cdmOutputEndpoint is properly configured
   - Validate CDM JSON schema and field mappings

3. **Kafka Connection Issues**

   - Verify bootstrap servers configuration
   - Check network connectivity to Kafka cluster
   - Validate consumer group permissions

4. **🆕 Integration Issues**

   - Verify ingestion module direct:kafka-message-processing endpoint
   - Check real-time vs batch message routing
   - Validate dual input source configuration

5. **Message Processing Failures**
   - Check message format and content
   - Verify transformer kamelet compatibility
   - Review error logs for specific failures
   - **🆕 Check CDM persistence logs for transformation issues**
   - **🆕 Verify k-db-tx kamelet configuration and availability**

### Debug Mode

Enable debug logging:

```properties
logging.level.com.pixel.v2.processing=DEBUG
logging.level.org.apache.camel=DEBUG
```

## Dependencies

### Required Kamelets

- `k-kafka-message-receiver`: Message source (batch processing)
- k-pacs-008-to-cdm: pacs.008 transformer
- k-pan-001-to-cdm: pan.001 transformer
- **🆕 `k-db-tx`: CDM persistence and audit trail**

### Integration Dependencies

- **🆕 Ingestion Module**: For real-time message processing integration
- **🆕 Oracle Database**: For CDM and message persistence
- **🆕 CdmMessage Entity**: JPA entity for CDM object storage

### Maven Dependencies

- Spring Boot 3.4.1
- Apache Camel 4.1.0
- Jackson JSON processing
- JUnit 5 for testing

## Contributing

1. Follow existing code style and patterns
2. Add unit tests for new functionality
3. Update documentation for configuration changes
4. Test integration with required kamelets

## Version History

- **1.0.1-SNAPSHOT**: Enhanced release with CDM persistence and dual input support
- **🆕 CDM Persistence**: Automatic CDM object persistence using k-db-tx
- **🆕 Dual Input Architecture**: Supports both Kafka (batch) and direct (real-time) processing
- **🆕 Ingestion Integration**: Direct integration with ingestion module for real-time processing
- **🆕 Audit Trail**: Complete CDM object lifecycle tracking and source message linking
- Message type detection for XML and JSON formats
- Integration with k-kafka-message-receiver
- Comprehensive error handling and monitoring
