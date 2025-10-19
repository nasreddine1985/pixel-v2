# Payment Ingestion Service

Spring Boot Apache Camel application that orchestrates the payment message ingestion flow using various kamelets for comprehensive payment processing.

## Overview

The Payment Ingestion Service provides a unified entry point for payment messages from multiple channels and orchestrates their processing through validation, idempotence checking, and publishing to Kafka topics.

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MQ Series     │    │   REST API       │    │   File (CFT)    │
│   Messages      │    │   Endpoint       │    │   Processing    │
└─────────┬───────┘    └─────────┬────────┘    └─────────┬───────┘
          │                      │                       │
          └──────────────────────┼───────────────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │   k-mq-receipt           │
                    │   k-api-receipt          │
                    │   k-file-receipt         │
                    └────────────┬─────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │   k-ref-loader           │
                    │   (Reference Enrichment) │
                    └────────────┬─────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │   k-ingest-validation    │
                    │   (Message Validation)   │
                    └────────────┬─────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │   k-idempotence          │
                    │   (Duplicate Prevention) │
                    └────────────┬─────────────┘
                                 │
           ┌─────────────────────┼─────────────────────┐
           │                     │                     │
  ┌────────▼─────────┐  ┌────────▼─────────┐  ┌───────▼──────┐
  │ Kafka Topic      │  │ Rejection Topic  │  │ Error Topic  │
  │ (Processed)      │  │ (Invalid Msgs)   │  │ (Errors)     │
  └──────────────────┘  └──────────────────┘  └──────────────┘
```

## Processing Flow

### 1. Message Receipt

- **MQ Series**: Receives messages from IBM MQ queues
- **REST API**: Accepts messages via HTTP POST endpoints
- **File Processing**: Monitors directories for XML payment files

### 2. Reference Data Enrichment

- Uses `k-ref-loader` kamelet to call reference APIs
- Enriches message headers with configuration and mapping data

### 3. Message Validation

- Uses `k-ingest-validation` kamelet for comprehensive validation
- Checks message structure, format, and compliance
- Supports both strict and lenient validation modes

### 4. Idempotence Checking

- Uses `k-idempotence` kamelet to prevent duplicate processing
- Tracks unique identifiers (InstrId, EndToEndId, MsgId)
- Configurable duplicate handling (ERROR, IGNORE, WARN)

### 5. Message Publishing

- Publishes valid messages to appropriate Kafka topics
- Routes rejected messages to dead letter topics
- Handles system errors with comprehensive error logging

## Configuration

### Application Properties

#### Server Configuration

```properties
server.port=8080
server.servlet.context-path=/ingestion
```

#### MQ Series Configuration

```properties
ingestion.mq.input.queue=PAYMENT_INPUT
ingestion.mq.host=localhost
ingestion.mq.port=1414
ingestion.mq.queue.manager=QM1
ingestion.mq.channel=DEV.ADMIN.SVRCONN
```

#### File Processing Configuration

```properties
ingestion.file.input.directory=/tmp/payments-in
ingestion.file.processed.directory=/tmp/payments-processed
ingestion.file.pattern=*.xml
```

#### Kafka Configuration

```properties
ingestion.kafka.brokers=localhost:9092
ingestion.kafka.topic.default=payments-processed
ingestion.kafka.topic.pacs008=payments-pacs008
ingestion.kafka.topic.pan001=payments-pan001
ingestion.kafka.topic.rejected=payments-rejected
ingestion.kafka.topic.errors=payments-errors
```

### Environment-Specific Profiles

- **Development**: `application-dev.properties` - Lenient validation, local services
- **Production**: `application-prod.properties` - Strict validation, environment variables
- **Test**: `application-test.properties` - Mock services, in-memory storage

## API Endpoints

### Payment Submission

```http
POST /ingestion/api/v1/payments
Content-Type: application/json

{
  "messageType": "pacs.008",
  "payload": "<?xml version='1.0'?>..."
}
```

### Health Check

```http
GET /ingestion/health
```

### Metrics

```http
GET /ingestion/metrics
```

## Message Flow Examples

### Successful Processing

```yaml
# MQ Message Reception
- Receive: MQ Series Queue → k-mq-receipt
- Enrich: k-ref-loader → Add reference data headers
- Validate: k-ingest-validation → Check message structure
- Dedupe: k-idempotence → Verify uniqueness
- Publish: Kafka Topic → payments-pacs008
```

### Validation Failure

```yaml
# Failed Validation Flow
- Receive: REST API → k-api-receipt
- Enrich: k-ref-loader → Add reference data headers
- Validate: k-ingest-validation → Validation fails
- Reject: Kafka Topic → payments-rejected
```

### Duplicate Message

```yaml
# Duplicate Detection Flow
- Receive: File CFT → k-file-receipt
- Enrich: k-ref-loader → Add reference data headers
- Validate: k-ingest-validation → Message valid
- Dedupe: k-idempotence → Duplicate detected
- Handle: Based on duplicate action (ERROR/IGNORE/WARN)
```

## Message Format

### Enriched Message (Success)

```json
{
  "metadata": {
    "receiptChannel": "MQ_SERIES",
    "receiptTimestamp": "2023-10-19 10:30:00",
    "ingestionStartTime": "2023-10-19 10:30:00",
    "publishTimestamp": "2023-10-19 10:30:05",
    "primaryIdentifier": "INSTR123456",
    "messageType": "pacs.008",
    "validationPassed": true,
    "duplicateCheck": true
  },
  "payload": "<?xml version='1.0'?>..."
}
```

### Rejection Message

```json
{
  "rejectionInfo": {
    "reason": "VALIDATION_FAILED",
    "timestamp": "2023-10-19 10:30:02",
    "receiptChannel": "REST_API",
    "originalMessageId": "MSG123",
    "errorDetails": "Missing required field: InstrId"
  },
  "originalMessage": "<?xml version='1.0'?>..."
}
```

## Monitoring

### Health Checks

- Application health: `/health`
- Camel routes health via Spring Boot Actuator
- Kafka connectivity monitoring

### Metrics

- Message throughput per channel
- Validation success/failure rates
- Duplicate detection statistics
- Processing latency metrics

### Logging

- Structured logging with correlation IDs
- Processing stage tracking
- Error categorization and alerting

## Running the Application

### Local Development

```bash
# Start the application
mvn spring-boot:run

# With specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker Deployment

```bash
# Build Docker image
mvn clean package
docker build -t pixel-v2/ingestion .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e KAFKA_BROKERS=kafka:9092 \
  pixel-v2/ingestion
```

### Production Deployment

```bash
# Using environment variables
export KAFKA_BROKERS=kafka-cluster:9092
export MQ_HOST=mq-server
export VALIDATION_STRICT=true
java -jar ingestion-1.0.1-SNAPSHOT.jar
```

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### Manual Testing

```bash
# Send test message via API
curl -X POST http://localhost:8080/ingestion/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"messageType":"pacs.008","payload":"<xml>test</xml>"}'

# Check health
curl http://localhost:8080/ingestion/health
```

## Dependencies

### Required Kamelets

- `k-mq-receipt`: MQ Series message reception
- `k-api-receipt`: REST API message reception
- `k-file-receipt`: File-based message reception
- `k-ref-loader`: Reference data enrichment
- `k-ingest-validation`: Message validation
- `k-idempotence`: Duplicate prevention

### External Services

- **IBM MQ**: Message queue system
- **Kafka**: Event streaming platform
- **Reference API**: Configuration and mapping service

## Troubleshooting

### Common Issues

1. **MQ Connection Failures**

   - Check MQ server availability and credentials
   - Verify queue names and permissions

2. **Kafka Publishing Errors**

   - Validate Kafka broker connectivity
   - Check topic existence and permissions

3. **Validation Failures**

   - Review message format and structure
   - Check validation rules and strictness settings

4. **Performance Issues**
   - Monitor memory usage and GC
   - Check Kafka producer/consumer configurations
   - Review batch processing settings

### Log Analysis

```bash
# Follow application logs
tail -f logs/ingestion.log

# Search for errors
grep "ERROR" logs/ingestion.log

# Monitor specific route
grep "payment-ingestion-orchestrator" logs/ingestion.log
```
