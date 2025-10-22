# Outbound Message Service

Spring Boot Apache Camel-based service for centralized outbound message processing, distribution, and delivery with **intelligent integration** with the processing module's conditional routing.

## Overview

The Outbound service is designed to:

- **🆕 Receive CDM-processed messages** from the processing module via HTTP POST
- Consume messages from direct endpoints or Kafka topics using the k-kafka-message-receiver kamelet
- Log all messages using the k-log-tx kamelet for centralized logging and audit trails
- Process and route messages based on their type (Payment, Transaction, Notification)
- **🆕 Serve as the primary distribution hub** for non-Kafka originated messages
- Provide a flexible architecture for outbound message handling and delivery

## Architecture

### 🔄 Complete Outbound Service Architecture

```
🎯 CENTRALIZED OUTBOUND MESSAGE DISTRIBUTION HUB 🎯
═══════════════════════════════════════════════════════

📥 INPUT SOURCES (Multiple Integration Points)
┌─────────────────────────────────────────────────────────────────────────┐
│                           INPUT LAYER                                  │
└─────────────────────────────────────────────────────────────────────────┘

🔵 PRIMARY SOURCE                🔶 SECONDARY SOURCES              🔷 ALTERNATIVE SOURCES
┌─────────────────────┐         ┌─────────────────────┐           ┌─────────────────────┐
│ Processing Module   │         │ Direct Endpoints    │           │ Kafka Topics        │
│ (Conditional Router)│         │                     │           │                     │
│                     │         │ • API calls         │           │ • k-kafka-message-  │
│ • messageSource ≠   │         │ • Internal services │           │   receiver kamelet  │
│   "KAFKA_TOPIC"     │         │ • direct:outbound-  │           │ • Batch messages    │
│ • CDM-processed     │         │   input             │           │ • Topic: processed- │
│   messages          │         │ • Testing/debugging │           │   payments          │
│ • HTTP POST         │         │                     │           │                     │
│ • :8082/submit      │         │                     │           │                     │
└──────────┬──────────┘         └──────────┬──────────┘           └──────────┬──────────┘
           │                               │                                 │
           └───────────────────────────────┼─────────────────────────────────┘
                                           ▼
                        ┌─────────────────────────────────────┐
                        │         OUTBOUND SERVICE            │
                        │      (Port 8082 - Main Hub)        │
                        └─────────────────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      PROCESSING PIPELINE                               │
└─────────────────────────────────────────────────────────────────────────┘

🔄 STEP 1: HTTP RECEPTION & VALIDATION
┌─────────────────────────┐    ┌─────────────────────────┐    ┌─────────────────────────┐
│ OutboundController      │    │ Header Extraction       │    │ CDM Validation          │
│                         │    │                         │    │                         │
│ • POST /submit          │───▶│ • messageType           │───▶│ • CDM structure         │
│ • Request parsing       │    │ • processingStage       │    │ • Required fields       │
│ • Content-Type check    │    │ • TransformationComplete│    │ • Schema validation     │
│ • Initial logging       │    │ • Correlation IDs       │    │ • Format verification   │
└─────────────────────────┘    └─────────────────────────┘    └─────────────────────────┘
                                           │
                                           ▼
🔄 STEP 2: MESSAGE PROCESSING PIPELINE
┌─────────────────────────┐    ┌─────────────────────────┐    ┌─────────────────────────┐
│ Camel Route Injection   │    │ k-log-tx Integration    │    │ Message Type Detection  │
│                         │    │                         │    │                         │
│ • direct:outbound-input │───▶│ • Centralized logging   │───▶│ OutboundMessageProcessor│
│ • ProducerTemplate      │    │ • Audit trail creation │    │ • PAYMENT detection     │
│ • Header propagation    │    │ • Log level: INFO       │    │ • TRANSACTION detection │
│ • Body preservation     │    │ • Category: BUSINESS    │    │ • NOTIFICATION detection│
└─────────────────────────┘    └─────────────────────────┘    └─────────────────────────┘
                                           │
                                           ▼
🔄 STEP 3: INTELLIGENT MESSAGE ROUTING
┌─────────────────────────────────────────────────────────────────────────┐
│                     MESSAGE ROUTER                                     │
│                   (outbound-message-router)                            │
└─────────────────────────────────────────────────────────────────────────┘
                                           │
                        ┌──────────────────┼──────────────────┐
                        ▼                  ▼                  ▼
            ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
            │ PAYMENT Handler │  │TRANSACTION      │  │ NOTIFICATION    │
            │                 │  │ Handler         │  │ Handler         │
            │ • Payment       │  │                 │  │                 │
            │   validation    │  │ • Transaction   │  │ • Notification  │
            │ • Amount checks │  │   processing    │  │   formatting    │
            │ • Party info    │  │ • Status update │  │ • Recipient     │
            │ • Routing rules │  │ • Approval flow │  │   management    │
            │ • External APIs │  │ • Settlement    │  │ • Channel       │
            └─────────────────┘  └─────────────────┘  │   selection     │
                    │                    │            └─────────────────┘
                    │                    │                    │
                    └────────────────────┼────────────────────┘
                                         ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      OUTPUT DESTINATIONS                               │
└─────────────────────────────────────────────────────────────────────────┘

🎯 DELIVERY CHANNELS (Based on Message Type & Business Rules)
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Payment Networks│  │ Core Banking    │  │ Notification    │  │ Audit & Archive │
│                 │  │ Systems         │  │ Services        │  │                 │
│ • SWIFT Network │  │                 │  │                 │  │ • Database      │
│ • ACH Systems   │  │ • Account       │  │ • Email Gateway │  │   logging       │
│ • Real-time     │  │   updates       │  │ • SMS Service   │  │ • File archive  │
│   payments      │  │ • Balance       │  │ • Push          │  │ • Compliance    │
│ • Cross-border  │  │   management    │  │   notifications │  │   reporting     │
│   transfers     │  │ • Transaction   │  │ • Mobile apps   │  │ • Metrics       │
└─────────────────┘  │   history       │  └─────────────────┘  │   collection    │
                     └─────────────────┘                       └─────────────────┘

🔄 MONITORING & ERROR HANDLING
┌─────────────────────────────────────────────────────────────────────────┐
│                     OPERATIONAL LAYER                                  │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Health Checks   │  │ Metrics         │  │ Error Handling  │  │ Circuit Breaker │
│                 │  │ Collection      │  │                 │  │                 │
│ • Service status│  │                 │  │ • Retry logic   │  │ • Failure       │
│ • Dependency    │  │ • Message       │  │ • Dead letter   │  │   detection     │
│   health        │  │   throughput    │  │   queues        │  │ • Automatic     │
│ • Database      │  │ • Processing    │  │ • Error         │  │   recovery      │
│   connectivity  │  │   latency       │  │   classification│  │ • Fallback      │
│ • External API  │  │ • Success rates │  │ • Alert         │  │   mechanisms    │
│   availability  │  │ • Error rates   │  │   generation    │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 📋 Message Sources Priority & Usage

| Priority | Source Type       | Use Case                 | Volume | Latency  |
| -------- | ----------------- | ------------------------ | ------ | -------- |
| **1**    | Processing Module | Primary CDM distribution | High   | Low      |
| **2**    | Direct Endpoints  | Internal service calls   | Medium | Very Low |
| **3**    | Kafka Topics      | Batch processing backup  | Low    | Medium   |

### 🔄 Integration Patterns

#### Primary Integration: Processing Module → Outbound Service

```
Processing Module Conditional Router:
if (messageSource != "KAFKA_TOPIC") {
    HTTP POST → localhost:8082/outbound/submit
    Headers: messageType, processingStage, TransformationComplete
    Body: CDM-processed JSON payload
}
```

### 🔄 Simplified Message Flow

```
Input Reception → Validation → Processing → Type Detection → Routing → External Delivery

┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│ Processing  │   │    HTTP     │   │ k-log-tx    │   │   Message   │   │   Type-     │   │  External   │
│  Module     │──▶│  Endpoint   │──▶│  Logging    │──▶│  Processor  │──▶│  Specific   │──▶│  Systems    │
│(Conditional)│   │ :8082/submit│   │ (Audit)     │   │ (Detection) │   │  Routing    │   │ (Delivery)  │
└─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘
       │                  │                  │                  │                  │                  │
       ▼                  ▼                  ▼                  ▼                  ▼                  ▼
CDM-processed      Message            Centralized        Type Detection       Business Rules    Final Delivery
messages from      validation         audit trail       (PAYMENT/TRANS/      application       to payment
conditional        & header           creation           NOTIFICATION)        based on type     networks
router             enrichment

Flow Details:
1. Processing Module → HTTP POST with CDM payload and headers
2. OutboundController → Request validation and header extraction
3. Camel Routes → direct:outbound-input message injection
4. k-log-tx → Centralized logging and audit trail creation
5. OutboundMessageProcessor → Intelligent type detection and enrichment
6. Message Router → Type-specific routing (payment/transaction/notification)
7. External Systems → Final delivery to appropriate external channels
```

### Message Sources

1. **🆕 Processing Module (Primary)**: Receives CDM-processed messages via HTTP POST from processing module's conditional router
2. **Direct Endpoints**: Messages can be sent directly to `direct:outbound-input`
3. **Kafka Topics**: Messages are consumed from configured Kafka topics using k-kafka-message-receiver

### Message Flow

#### 🆕 Primary Flow (from Processing Module)

1. **HTTP Reception**: Processing module POSTs CDM-processed messages to `/outbound/submit`
2. **Message Validation**: Validates CDM format and required headers
3. **Centralized Logging**: Logs with k-log-tx for audit trail
4. **Type Detection**: Detects message type from CDM content
5. **Message Routing**: Routes based on detected type and business rules
6. **Delivery Processing**: Handles final delivery to external systems
7. **Completion Logging**: Logs successful processing and delivery status

#### Traditional Flow (Kafka/Direct)

1. Message Reception (direct or Kafka)
2. Logging with k-log-tx
3. Message Processing (type detection, validation, enrichment)
4. Message Routing based on type
5. Final output and audit logging

### Supported Message Types

- **🆕 CDM_PROCESSED**: CDM-transformed messages from processing module (primary type)
- **PAYMENT**: Payment messages (pacs.008, JSON with payment fields)
- **TRANSACTION**: Transaction messages (pain.001, JSON with transaction fields)
- **NOTIFICATION**: Notification messages (text, JSON with notification fields)
- **UNKNOWN/GENERIC**: Fallback for unrecognized message types

### 🆕 CDM Message Processing

The outbound service has enhanced capabilities for handling CDM-processed messages:

- **CDM Validation**: Validates CDM structure and required fields
- **Header Enrichment**: Adds outbound-specific headers for routing
- **Correlation Tracking**: Maintains correlation with original source messages
- **Processing Stage Awareness**: Recognizes messages are already CDM-transformed

## Configuration

### Application Properties

Key configuration properties in `application.properties`:

```properties
# Service Configuration
spring.application.name=outbound-service
server.port=8082

# 🆕 Processing Module Integration
processing.integration.enabled=true
processing.module.endpoint=http://localhost:8081/processing

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=outbound-service-group
spring.kafka.consumer.auto-offset-reset=earliest

# Camel Configuration
camel.springboot.name=outbound-camel
camel.springboot.main-run-controller=true
camel.springboot.jmx-enabled=true

# Direct Endpoint Configuration
outbound.direct.input-endpoint=direct:outbound-input
outbound.direct.output-endpoint=direct:outbound-output

# 🆕 CDM Processing Configuration
outbound.cdm.validation.enabled=true
outbound.cdm.correlation.tracking=true

# Logging Configuration
logging.level.com.pixel.v2.outbound=INFO
logging.level.org.apache.camel=INFO
```

### Camel Routes

Routes are defined in YAML DSL format in `src/main/resources/camel/outbound-routes.yaml`:

- `outbound-direct-input`: Handles direct endpoint messages
- `outbound-kafka-input`: Handles Kafka messages via k-kafka-message-receiver
- `outbound-message-processor`: Main processing logic
- `outbound-message-router`: Routes messages by type
- Type-specific handlers: payment, transaction, notification, default
- `outbound-error-handler`: Error handling and logging

## Components

### OutboundMessageProcessor

Custom processor (`com.pixel.v2.outbound.processor.OutboundMessageProcessor`) that:

- Detects message type based on content structure
- Extracts message identifiers (payment ID, transaction ID, etc.)
- Enriches messages with processing metadata
- Handles errors gracefully

### OutboundController

REST controller (`com.pixel.v2.outbound.controller.OutboundController`) providing:

- **🆕 `/outbound/submit`** - **Primary endpoint** for receiving CDM messages from processing module
- `/outbound/submit-with-headers` - Submit messages with custom headers
- `/outbound/health` - Service health check
- `/outbound/routes` - Active Camel routes information

#### 🆕 Processing Module Integration

The `/outbound/submit` endpoint is specifically designed for processing module integration:

```java
@PostMapping("/submit")
public ResponseEntity<Map<String, Object>> submitMessage(
    @RequestBody String messageBody,
    HttpServletRequest request) {

    // Extract headers from processing module
    Map<String, Object> headers = extractProcessingHeaders(request);

    // Validate CDM format if messageType is CDM_PROCESSED
    if ("CDM_PROCESSED".equals(headers.get("messageType"))) {
        validateCdmMessage(messageBody);
    }

    // Send to Camel route for processing
    producerTemplate.sendBodyAndHeaders("direct:outbound-input", messageBody, headers);

    return ResponseEntity.ok(createSuccessResponse());
}
```

## Usage

### Starting the Service

```bash
mvn spring-boot:run
```

### 🆕 Submitting CDM Messages from Processing Module

The primary use case - processing module sending CDM-processed messages:

```bash
# This is automatically done by the processing module's conditional router
curl -X POST http://localhost:8082/outbound/submit \
  -H "Content-Type: application/json" \
  -H "messageType: CDM_PROCESSED" \
  -H "processingStage: CDM_TRANSFORMATION_COMPLETE" \
  -H "TransformationComplete: true" \
  -d '{
    "cdmType": "PAYMENT_INSTRUCTION",
    "instructionId": "INST123",
    "amount": 1000.00,
    "currency": "EUR",
    "debtorInfo": {...},
    "creditorInfo": {...}
  }'
```

### Direct Message Submission

```bash
curl -X POST http://localhost:8082/outbound/submit \
  -H "Content-Type: application/json" \
  -d '{"paymentId": "PAY123", "amount": 1000.00, "creditor": "John Doe"}'
```

#### Message with Headers

```bash
curl -X POST "http://localhost:8082/outbound/submit-with-headers?messageType=PAYMENT&priority=HIGH" \
  -H "Content-Type: application/json" \
  -d '{"paymentId": "PAY456", "amount": 2000.00}'
```

### Health Check

```bash
curl http://localhost:8082/outbound/health
```

### Route Information

```bash
curl http://localhost:8082/outbound/routes
```

## Dependencies

### Kamelets Used

- **k-kafka-message-receiver**: For consuming messages from Kafka topics
- **k-log-tx**: For centralized logging and audit trails

### Key Dependencies

- Spring Boot 3.4.1
- Apache Camel 4.1.0
- Jackson for JSON processing
- Oracle JDBC driver
- Camel Kafka component
- Camel YAML DSL

## Integration

### With Other Services

The outbound service is designed to integrate with:

- **🆕 Processing Service (Primary)**: Receives CDM-processed messages via conditional routing HTTP POST
- **Ingestion Service**: Can receive messages from the ingestion pipeline
- **External Systems**: Routes messages to external payment networks, notification systems, etc.
- **🆕 Monitoring Systems**: Provides comprehensive logging and metrics for all processed messages

### 🆕 Processing Module Conditional Routing Integration

The outbound service is the **primary destination** for non-Kafka messages in the processing module's conditional routing:

```java
// Processing Module Route Configuration
choice()
    .when(header("messageSource").isEqualTo("KAFKA_TOPIC"))
        .to("kafka:cdm-processed-messages?brokers=localhost:9092")
    .otherwise()
        .to("http://localhost:8082/outbound/submit")  // Routes to outbound service
```

**Message Flow:**

```
HTTP/MQ Messages → Processing Module → CDM Transformation → Outbound Service
```

**Benefits:**

- **Centralized Distribution**: All non-Kafka CDM messages go through outbound service
- **Unified Logging**: Comprehensive audit trail via k-log-tx
- **Flexible Routing**: Outbound service can route to multiple external systems
- **Error Handling**: Centralized error processing and retry logic

### Kafka Integration

Configure Kafka topics in application properties:

```properties
outbound.kafka.topics.input=processed-payments,processed-transactions,notifications
```

### Database Integration

Uses the same database configuration as other PIXEL-V2 services for logging via k-log-tx.

## Monitoring

### Actuator Endpoints

- `/actuator/health` - Spring Boot health check
- `/actuator/camel` - Camel-specific metrics
- `/actuator/metrics` - Application metrics

### Logging

All message processing is logged through k-log-tx with different log levels:

- INFO: Normal processing flow
- WARN: Unknown message types
- ERROR: Processing failures

### Custom Monitoring

The service provides custom endpoints for monitoring:

- Route status and statistics
- Message processing metrics
- Error rates and patterns

## Development

### Adding New Message Types

1. Update `OutboundMessageProcessor.detectMessageType()` method
2. Add new processing method (e.g., `processCustomMessage()`)
3. Add new route handler in `outbound-routes.yaml`
4. Update routing logic in `outbound-message-router`

### Extending Routing Logic

Add new routes in `outbound-routes.yaml` following the existing pattern:

```yaml
- route:
    id: "outbound-custom-handler"
    from:
      uri: "direct:outbound-custom-handler"
      steps:
        - log:
            message: "[CUSTOM-HANDLER] Processing custom message"
        # Add custom processing steps
```

## Testing

### Unit Tests

Run unit tests with:

```bash
mvn test
```

### Integration Tests

Integration tests use embedded Kafka and H2 database for testing complete message flows.

### Manual Testing

Use the REST endpoints to submit test messages and verify processing through logs and database entries.

## Troubleshooting

### Common Issues

1. **Kafka Connection**: Check bootstrap servers configuration
2. **Database Connection**: Verify Oracle database connectivity
3. **Route Failures**: Check Camel route definitions and dependencies
4. **Message Processing**: Review OutboundMessageProcessor logic and logs

### Debug Logging

Enable debug logging for troubleshooting:

```properties
logging.level.com.pixel.v2.outbound=DEBUG
logging.level.org.apache.camel=DEBUG
```
