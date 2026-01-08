# Technical Framework - Kamelets Collection

## Overview

The Technical Framework module contains all Apache Camel Kamelets used throughout the PIXEL-V2 system. This module serves as a centralized repository for reusable technical components that provide specific integration patterns and capabilities.

## Structure

```
technical-framework/
├── pom.xml                          # Parent POM for all kamelets
├── k-cft-starter/                   # CFT file processing kamelet
├── k-db/                           # Database operations kamelet
├── k-db-tx/                        # Database transaction kamelet
├── k-http-starter/                 # HTTP message reception kamelet
├── k-identification/               # Payment identification & caching kamelet
├── k-kafka-publisher/              # Kafka message publishing kamelet
├── k-kafka-starter/               # Kafka message consumption kamelet
├── k-log-events/                   # Detailed log events kamelet
├── k-log-flow-summary/             # Flow summary logging kamelet
├── k-log-tx/                       # Transaction logging kamelet
├── k-message-concat/               # Message concatenation kamelet
├── k-message-split/                # Message splitting kamelet
├── k-mq-starter/                   # JMS/MQ message processing kamelet
├── k-xsd-validation/               # XML Schema validation kamelet
└── k-xsl-transformation/           # XSLT transformation kamelet
```

## Kamelet Categories

### 🔄 Message Receivers & Starters

- **k-mq-starter**: JMS queue listener with flow processing, archiving to NAS, and Kafka integration
- **k-http-starter**: HTTP REST endpoint message reception kamelet
- **k-kafka-starter**: Kafka topic consumption with metadata enrichment
- **k-cft-starter**: CFT file processing kamelet for batch operations

### 🔧 Processing & Validation

- **k-identification**: Payment identification with Redis caching and referential data lookup
- **k-xsd-validation**: XML Schema validation using configurable XSD files
- **k-xsl-transformation**: XSLT transformation using configurable stylesheets
- **k-message-concat**: Message concatenation for multi-part processing
- **k-message-split**: Message splitting for parallel processing

### 🏗️ Infrastructure & Data

- **k-db**: Basic database operations kamelet
- **k-db-tx**: Database transaction management with rollback capabilities
- **k-log-tx**: Transaction-level logging with audit trails
- **k-log-events**: Detailed event logging with structured log format
- **k-log-flow-summary**: Flow processing summary and completion logging

### 📤 Publishers & Output

- **k-kafka-publisher**: Kafka message publishing with topic routing

## Key Kamelets Description

### K-MQ-Starter

**Purpose**: Main entry point for JMS message processing in payment flows

- Listens to configurable JMS queues (e.g., `pacs008.input.queue`)
- Generates unique flow occurrence IDs from database sequences
- Archives messages to NAS file system (`/opt/nas/CH/IN/{flowOccurId}/`)
- Publishes processing events to multiple Kafka topics
- Supports concurrent consumers for scalability

### K-Identification

**Purpose**: Payment identification and reference data caching

- Redis-based caching for performance optimization (configurable TTL)
- Integration with referential service for payment data lookup
- Generic payment processing (supports multiple payment types)
- Automatic cache warming and refresh mechanisms

### K-XSD-Validation

**Purpose**: XML Schema validation for payment messages

- Configurable XSD file selection (e.g., `pacs.008.001.02.ch.02.xsd`)
- Strict or lenient validation modes
- Detailed validation error reporting
- Support for multiple schema versions

### K-XSL-Transformation

**Purpose**: Message transformation using XSLT

- Configurable XSLT stylesheet selection
- Support for complex transformation scenarios
- Parameter passing to XSLT templates
- Error handling and transformation logging

### K-Log-Flow-Summary

**Purpose**: Flow processing summary and audit logging

- Captures processing milestones and completion status
- Publishes structured summaries to Kafka topics
- Integration with monitoring and alerting systems
- Audit trail maintenance for regulatory compliance

## Technologies

- **Apache Camel 4.1.0**: Core integration framework with Kamelet support
- **Spring Boot 3.4.1**: Application framework and dependency injection
- **Spring Data Redis**: Redis integration for caching (k-identification)
- **Spring Data JPA**: Database operations and transaction management
- **Apache Kafka**: Message streaming and event publishing
- **ActiveMQ**: JMS messaging and queue management
- **Jackson**: JSON processing and serialization
- **Saxon XSLT**: XML transformation engine
- **PostgreSQL**: Primary database for persistence
- **File Component**: NAS file operations with automatic directory creation

## Configuration

### Common Parameters

All kamelets support standard configuration patterns:

```yaml
# Database Configuration
dataSource: "#dataSource" # Spring Data Source bean

# Kafka Configuration
brokers: "kafka:29092" # Kafka broker list
kafkaTopicName: "topic-name" # Target topic name

# Processing Configuration
flowCode: "ICHSIC" # Flow identifier
messageType: "PACS.008.001.02" # Message type identifier
```

### Environment-Specific Configuration

Kamelets are designed to work across different environments:

- **Development**: Local file paths, embedded databases
- **Testing**: Docker containers, shared volumes
- **Production**: Network storage, clustered databases

## Building and Testing

### Build All Kamelets

```bash
cd technical-framework
mvn clean compile
```

### Test Specific Kamelet

```bash
cd k-identification
mvn test
```

### Package for Distribution

```bash
mvn clean package
```

## Integration Patterns

### Error Handling

All kamelets implement consistent error handling:

- Exception capture and logging
- Error headers for downstream processing
- Dead letter queue routing for failed messages
- Retry mechanisms with configurable backoff

### Monitoring & Observability

Kamelets provide comprehensive monitoring:

- Processing metrics via JMX
- Health checks for dependency validation
- Distributed tracing support
- Custom business metrics

### Security

Security considerations for kamelet usage:

- Database credentials externalization
- Redis connection encryption in production
- File system permissions (NAS access)
- Kafka authentication and authorization

## Usage Examples

### CH Payment Processing Flow

```java
from(K_MQ_STARTER_ENDPOINT)
  .to(K_IDENTIFICATION_ENDPOINT)
  .to(K_XSD_VALIDATION_ENDPOINT)
  .to(K_XSL_TRANSFORMATION_ENDPOINT)
  .to(K_LOG_FLOW_SUMMARY_ENDPOINT);
```

### Custom Kamelet Integration

```yaml
# application.properties
kamelet.k-identification.referentialServiceUrl=http://referential:8099
kamelet.k-identification.cacheTtl=3600
kamelet.k-mq-starter.nasArchiveUrl=file:///opt/nas/CH
```

## Versioning

All kamelets follow the parent version managed in `technical-framework/pom.xml`

**Current Version**: 1.0.0-SNAPSHOT

### Release Process:

1. Update parent version in `technical-framework/pom.xml`
2. All child kamelets inherit the version automatically
3. Build and test all kamelets together: `mvn clean verify`
4. Release as a cohesive technical framework unit
5. Tag release in Git: `git tag v1.0.0`

## Contributing

### Development Guidelines:

1. **Naming Convention**: All kamelets use `k-` prefix followed by functional name
2. **Code Patterns**: Follow existing kamelet structure and configuration patterns
3. **Testing**: Add comprehensive unit and integration tests
4. **Documentation**: Update kamelet YAML descriptions and this README
5. **Compatibility**: Ensure backward compatibility with existing PIXEL-V2 modules

### Adding New Kamelets:

1. Create new directory under `technical-framework/`
2. Follow existing POM structure with parent reference
3. Implement kamelet YAML in `src/main/resources/kamelets/`
4. Add configuration documentation
5. Update this README with kamelet description

## Dependencies

The technical framework manages all dependencies centrally:

- **Apache Camel Components**: All required Camel components and starters
- **Spring Boot Starters**: Data, Web, Actuator, and Redis starters
- **Database Drivers**: PostgreSQL and Oracle JDBC drivers
- **Testing**: JUnit, Testcontainers, and Camel Test Support
- **Utilities**: Jackson, Commons, and validation libraries

This ensures version consistency and compatibility across all kamelets in the PIXEL-V2 ecosystem.
