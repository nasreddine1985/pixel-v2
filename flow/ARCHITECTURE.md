# Flow Module - PACS Message Processing

## Overview

The **Flow** module is a Spring Boot Apache Camel-based application designed to process PACS.008 messages from ActiveMQ and persist them in a PostgreSQL database. This module follows enterprise integration patterns and provides a robust foundation for financial message processing.

## Architecture

### Technologies Used

- **Spring Boot 3.4.1** - Application framework
- **Apache Camel 4.1.0** - Enterprise Integration Patterns
- **Spring Data JPA** - Database abstraction layer
- **Apache ActiveMQ Artemis** - Message broker integration
- **PostgreSQL** - Primary database (H2 for testing)
- **Maven** - Build tool

### Module Structure

```
flow/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/pixel/v2/flow/
│   │   │       ├── FlowApplication.java          # Main Spring Boot application
│   │   │       ├── config/
│   │   │       │   └── JmsConfig.java            # JMS connection configuration
│   │   │       ├── model/
│   │   │       │   ├── MessageError.java        # Error tracking entity
│   │   │       │   └── Pacs008Message.java      # PACS.008 message entity
│   │   │       └── route/
│   │   │           └── Pacs008Route.java         # Camel route definition
│   │   └── resources/
│   │       └── application.properties            # Configuration properties
│   └── test/
│       └── java/
│           └── com/pixel/v2/flow/
│               └── FlowApplicationTests.java     # Integration tests
├── pom.xml                                       # Maven configuration
└── README.md                                     # Module documentation
```

## Message Processing Flow

### PACS.008 Route

1. **Message Consumption**: Messages are consumed from the `PACS008_QUEUE` ActiveMQ queue
2. **Message Validation**: Basic validation ensures messages are not empty
3. **Metadata Enhancement**: Route adds processing metadata (timestamps, route info)
4. **Database Persistence**: Messages are persisted using the database persistence route
5. **Error Handling**: Invalid messages are routed to error handling

### Route Configuration

```java
// Main processing route
from("jms:queue:PACS008_QUEUE")
    .routeId("pacs008-processing-route")
    .log("Received PACS.008 message")
    .setHeader("ProcessingRoute", constant("PACS008"))
    .setHeader("MessageType", constant("pacs.008.001.08"))
    .choice()
        .when(simple("${body} == null || ${body} == ''"))
            .to("direct:error-handler")
        .otherwise()
            .to("direct:persist-message")
    .end();
```

## Data Model

### PACS008Message Entity

| Field              | Type          | Description                    |
| ------------------ | ------------- | ------------------------------ |
| id                 | Long          | Primary key                    |
| jmsMessageId       | String        | JMS Message ID                 |
| jmsCorrelationId   | String        | JMS Correlation ID             |
| jmsTimestamp       | Long          | JMS Timestamp                  |
| jmsPriority        | Integer       | JMS Priority                   |
| messageType        | String        | Message type (pacs.008.001.08) |
| processingRoute    | String        | Processing route name          |
| messageBody        | String        | Raw message content            |
| createdAt          | LocalDateTime | Database creation time         |
| processedTimestamp | LocalDateTime | Processing completion time     |

### MessageError Entity

| Field          | Type          | Description                |
| -------------- | ------------- | -------------------------- |
| id             | Long          | Primary key                |
| jmsMessageId   | String        | JMS Message ID             |
| errorRoute     | String        | Route where error occurred |
| errorMessage   | String        | Error description          |
| messageBody    | String        | Original message content   |
| errorTimestamp | LocalDateTime | Error occurrence time      |
| createdAt      | LocalDateTime | Database creation time     |

## Configuration

### Application Properties

```properties
# JMS Configuration for ActiveMQ Artemis
spring.artemis.broker-url=tcp://localhost:61616
spring.artemis.user=artemis
spring.artemis.password=artemis
spring.artemis.mode=native

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/pixel_v2
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Camel Configuration
camel.springboot.main-run-controller=true
camel.springboot.name=pixel-v2-flow-camel
camel.component.jms.connection-factory=#artemisConnectionFactory
```

## Integration Points

### Kamelet Integration (Future Enhancement)

The module is designed to integrate with custom kamelets:

- **k-mq-message-receiver**: For consuming messages from ActiveMQ queues
- **k-db-tx**: For database transaction management and persistence

### Current Implementation

The current implementation uses direct JMS integration for simplicity and testability. The kamelet integration can be activated by:

1. Updating the route to use `kamelet:k-mq-message-receiver`
2. Configuring the k-db-tx kamelet for database operations
3. Ensuring proper kamelet dependencies are available

## Testing

### Test Configuration

Tests use the `@Profile("!test")` annotation to exclude JMS routes during testing, ensuring:

- Fast test execution
- No external dependencies required
- H2 in-memory database for isolation

### Running Tests

```bash
mvn test                    # Run unit tests
mvn clean package          # Full build with tests
```

## Deployment

### Prerequisites

1. **ActiveMQ Artemis** - Message broker running on `localhost:61616`
2. **PostgreSQL** - Database server with `pixel_v2` database
3. **Java 21+** - Runtime environment

### Build and Run

```bash
# Build the module
mvn clean package

# Run the application
java -jar target/flow-1.0.1-SNAPSHOT.jar
```

## Monitoring and Operations

### Logging

The application provides structured logging for:

- Message reception and processing
- Database persistence operations
- Error conditions and routing

### Health Checks

Spring Boot Actuator endpoints (if enabled) provide:

- Application health status
- JMS connection health
- Database connection status
- Camel route statistics

## Future Enhancements

1. **Kamelet Integration**: Full integration with custom kamelets
2. **Message Validation**: XML schema validation for PACS.008 messages
3. **Dead Letter Queue**: Enhanced error handling with DLQ support
4. **Metrics Integration**: Prometheus/Micrometer metrics
5. **Security**: Message encryption and authentication
6. **Clustering**: Multi-instance deployment support

## Dependencies

Key dependencies and their purposes:

- `spring-boot-starter-web`: Web framework
- `spring-boot-starter-data-jpa`: Database integration
- `camel-spring-boot-starter`: Camel integration
- `camel-jms`: JMS component
- `artemis-jms-client-all`: ActiveMQ Artemis client
- `postgresql`: PostgreSQL driver
- `h2`: In-memory database for testing

## Contributing

When contributing to this module:

1. Follow the existing code structure and patterns
2. Ensure all tests pass
3. Update documentation for new features
4. Follow Spring Boot and Apache Camel best practices
5. Consider backward compatibility for configuration changes
