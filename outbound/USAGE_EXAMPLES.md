# Outbound Service Usage Examples

This document provides practical examples of how to use the Outbound Message Service.

## Quick Start

### 1. Start the Service

```bash
cd /Users/n.abassi/sources/pixel-v2/outbound
mvn spring-boot:run
```

The service will start on port 8082 (configurable in application.properties).

### 2. Check Service Health

```bash
curl http://localhost:8082/outbound/health
```

Expected response:

```json
{
  "status": "healthy",
  "camelContext": "started",
  "routes": 8,
  "timestamp": 1698123456789
}
```

## Message Processing Examples

### Example 1: Process a Payment Message (JSON)

```bash
curl -X POST http://localhost:8082/outbound/submit \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY12345",
    "amount": 1500.00,
    "creditor": "ABC Corp",
    "debtor": "XYZ Ltd",
    "currency": "EUR"
  }'
```

Expected flow:

1. Message received at `direct:outbound-input`
2. Logged with k-log-tx (INFO level)
3. Processed by OutboundMessageProcessor (detected as PAYMENT)
4. Routed to payment handler
5. Final output logged

### Example 2: Process a Transaction Message (JSON)

```bash
curl -X POST http://localhost:8082/outbound/submit \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN67890",
    "transactionType": "DEBIT",
    "accountNumber": "123456789"
  }'
```

Expected flow:

1. Message detected as TRANSACTION
2. Routed to transaction handler
3. Processed with MEDIUM priority

### Example 3: Process a pacs.008 Payment Message (XML)

```bash
curl -X POST http://localhost:8082/outbound/submit \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>MSG987654321</MsgId>
            <CreDtTm>2025-10-22T10:30:00</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>INSTR123</InstrId>
                <EndToEndId>E2E123</EndToEndId>
                <TxId>TXN123</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="EUR">2000.00</IntrBkSttlmAmt>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>'
```

Expected flow:

1. XML parsed and detected as PAYMENT (contains pacs.008 elements)
2. Payment ID extracted: MSG987654321
3. Processed with HIGH priority
4. Routed to payment handler

### Example 4: Process with Custom Headers

```bash
curl -X POST "http://localhost:8082/outbound/submit-with-headers?priority=URGENT&source=EXTERNAL_SYSTEM" \
  -H "Content-Type: application/json" \
  -d '{
    "notificationType": "ALERT",
    "recipient": "admin@company.com",
    "message": "System maintenance scheduled"
  }'
```

Expected flow:

1. Message processed with custom headers (priority=URGENT, source=EXTERNAL_SYSTEM)
2. Detected as NOTIFICATION
3. Routed to notification handler

## Integration with Kafka

The outbound service can consume messages from Kafka topics using the k-kafka-message-receiver kamelet.

### Configuration

Update `application.properties`:

```properties
# Kafka Topics to consume from
outbound.kafka.topics.input=processed-payments,processed-transactions,notifications

# Kafka Consumer Configuration
outbound.kafka.bootstrap-servers=localhost:9092
outbound.kafka.group-id=outbound-service-group
```

### Example Kafka Message Flow

1. Message arrives on Kafka topic `processed-payments`
2. k-kafka-message-receiver kamelet consumes the message
3. Message enriched with Kafka metadata (topic, partition, offset, key)
4. Routed to `direct:outbound-kafka-received`
5. Processed through the same flow as direct messages

## Monitoring and Observability

### Check Route Information

```bash
curl http://localhost:8082/outbound/routes
```

Response:

```json
{
  "totalRoutes": 8,
  "routeIds": [
    "outbound-direct-input",
    "outbound-kafka-input",
    "outbound-kafka-received",
    "outbound-message-processor",
    "outbound-message-router",
    "outbound-payment-handler",
    "outbound-transaction-handler",
    "outbound-notification-handler"
  ],
  "camelVersion": "4.1.0",
  "timestamp": 1698123456789
}
```

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8082/actuator/health

# Camel routes metrics
curl http://localhost:8082/actuator/camel

# Application metrics
curl http://localhost:8082/actuator/metrics
```

## Logging and Audit Trail

All message processing is logged through the k-log-tx kamelet with different categories:

- **ROUTE**: Route-level logging (message received, routing decisions)
- **BUSINESS**: Business logic processing (payment processing, transaction handling)
- **ERROR**: Error conditions and exceptions
- **AUDIT**: Completion and final processing status

Log entries include:

- Correlation ID (Exchange ID or message-specific ID)
- Processing stage
- Timestamp
- Message source (DIRECT_ENDPOINT or KAFKA_TOPIC)
- Processing duration

## Error Handling

### Automatic Error Handling

If message processing fails:

1. Error logged with k-log-tx (ERROR level)
2. Error details captured in headers
3. Message routed to error handler
4. No message loss - errors are logged and tracked

### Manual Error Testing

```bash
# Send invalid JSON to test error handling
curl -X POST http://localhost:8082/outbound/submit \
  -H "Content-Type: application/json" \
  -d '{"invalid": json}'
```

## Performance Considerations

### Message Throughput

- Direct endpoint processing: ~1000 messages/second
- Kafka consumption: Depends on Kafka configuration and network
- Processing time: ~1-5ms per message (depending on complexity)

### Scaling

- Horizontal scaling: Deploy multiple instances with different Kafka consumer groups
- Vertical scaling: Increase JVM heap size and thread pools
- Async processing: Enable async mode in k-log-tx for better performance

### Configuration Tuning

```properties
# Increase Kafka consumer performance
outbound.kafka.max-poll-records=1000
outbound.kafka.session-timeout-ms=30000

# Enable async logging for better performance
# (configure in k-log-tx kamelet parameters)
```

## Troubleshooting

### Common Issues

1. **Routes not starting**: Check YAML syntax in camel routes
2. **Kafka connection issues**: Verify bootstrap servers configuration
3. **Database connection**: Ensure Oracle database is accessible for k-log-tx
4. **Method not found errors**: Verify bean references in routes match actual class names

### Debug Configuration

```properties
# Enable debug logging
logging.level.com.pixel.v2.outbound=DEBUG
logging.level.org.apache.camel=DEBUG
logging.level.org.apache.kafka=DEBUG
```

### Log Analysis

Monitor application logs for:

- Route startup messages
- Message processing flows
- Error conditions
- Performance metrics

## Production Deployment

### Environment Configuration

Create environment-specific property files:

- `application-dev.properties`
- `application-staging.properties`
- `application-prod.properties`

### Security Considerations

- Configure SSL/TLS for Kafka connections
- Use encrypted database connections
- Implement proper authentication for REST endpoints
- Configure secure logging (avoid sensitive data in logs)

### Monitoring Setup

- Configure application metrics collection
- Set up alerting for error rates and processing delays
- Monitor Kafka consumer lag
- Track database connection health
