# Flow Module - PACS Message Processing

## Overview

The Flow module is a Spring Boot Apache Camel application responsible for processing PACS (Payment and Cash Management) messages in the Pixel V2 system. It provides message consumption from ActiveMQ Artemis and persistence to PostgreSQL database.

## Features

### PACS.008 Message Processing

- Consumes messages from ActiveMQ Artemis using the `k-mq-message-receiver` kamelet
- Processes PACS.008 (Financial Institution to Financial Institution Customer Credit Transfer) messages
- Persists messages to PostgreSQL database using the `k-db-tx` kamelet
- Comprehensive error handling and logging

### Key Components

- **Pacs008Route**: Main Camel route for processing PACS.008 messages
- **JmsConfig**: ActiveMQ Artemis connection configuration
- **Pacs008Message**: JPA entity for storing processed messages
- **MessageError**: JPA entity for storing error information

## Configuration

### Database Configuration (PostgreSQL)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pixelv2
spring.datasource.username=postgres
spring.datasource.password=pixel
```

### ActiveMQ Artemis Configuration

```properties
spring.artemis.broker-url=tcp://localhost:61616
spring.artemis.user=artemis
spring.artemis.password=artemis
```

### Queue Configuration

- **Primary Queue**: `PACS008_QUEUE` - for incoming PACS.008 messages
- **Error Queue**: `PACS008_ERROR_QUEUE` - for failed message processing

## Database Schema

### pacs008_messages Table

- `id` (BIGINT, Primary Key)
- `jms_message_id` (VARCHAR 100) - JMS Message ID
- `jms_correlation_id` (VARCHAR 100) - JMS Correlation ID
- `message_type` (VARCHAR 50) - Message type (e.g., pacs.008.001.08)
- `message_body` (TEXT) - Complete message content
- `processed_timestamp` (TIMESTAMP) - Processing timestamp
- `processing_route` (VARCHAR 50) - Route that processed the message
- `jms_priority` (INTEGER) - JMS message priority
- `jms_timestamp` (BIGINT) - JMS timestamp
- `created_at` (TIMESTAMP) - Record creation timestamp

### message_errors Table

- `id` (BIGINT, Primary Key)
- `jms_message_id` (VARCHAR 100) - JMS Message ID
- `error_route` (VARCHAR 50) - Route where error occurred
- `error_message` (TEXT) - Error description
- `error_timestamp` (TIMESTAMP) - Error occurrence timestamp
- `message_body` (TEXT) - Message content that caused the error
- `created_at` (TIMESTAMP) - Record creation timestamp

## Dependencies

### Pixel V2 Kamelets

- `k-mq-message-receiver` - ActiveMQ Artemis message consumption
- `k-db-tx` - Database transaction processing

### External Dependencies

- Spring Boot 3.4.1
- Apache Camel 4.1.0
- PostgreSQL 42.6.0
- ActiveMQ Artemis 2.31.2

## Usage

### Running the Application

```bash
mvn spring-boot:run
```

### Building the Application

```bash
mvn clean package
```

### Testing Message Processing

1. Start ActiveMQ Artemis broker on localhost:61616
2. Start PostgreSQL database on localhost:5432
3. Send PACS.008 messages to the `PACS008_QUEUE`
4. Monitor logs for processing status
5. Check database tables for persisted messages

## Message Flow

1. **Message Reception**: k-mq-message-receiver kamelet consumes messages from ActiveMQ Artemis
2. **Header Extraction**: JMS properties are extracted into exchange headers
3. **Message Validation**: Message content is validated (non-empty)
4. **Database Persistence**: k-db-tx kamelet stores message in PostgreSQL
5. **Error Handling**: Failed messages are logged and stored in error table

## Monitoring and Logging

The application provides comprehensive logging at different levels:

- **DEBUG**: Detailed processing information
- **INFO**: General processing flow
- **ERROR**: Error conditions and exceptions

Key log entries include:

- Message reception with JMS headers
- Processing status and timestamps
- Database persistence confirmation
- Error details and troubleshooting information

## Error Handling

The module implements robust error handling:

- **Validation Errors**: Empty or null messages are caught and logged
- **Processing Errors**: Exceptions during message processing are captured
- **Database Errors**: Persistence failures are logged and stored
- **JMS Errors**: Connection and consumption issues are handled gracefully

## Performance Considerations

- **Connection Pooling**: JMS connections are cached for better performance
- **Batch Processing**: Messages are processed individually but efficiently
- **Database Optimization**: JPA entities are optimized for PostgreSQL
- **Memory Management**: Large message handling is optimized to prevent memory issues
