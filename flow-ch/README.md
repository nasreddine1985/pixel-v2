# CH Payment Processing Flow

## Overview

The CH Payment Processing Flow is a Spring Boot Camel application designed to process PACS.008 payment messages in the Swiss (CH) payment system. This flow integrates with Redis for caching, PostgreSQL for data persistence, and includes comprehensive message validation, transformation, and archiving capabilities.

## Architecture

The application uses Apache Camel with Spring Boot and implements a modular architecture using custom Kamelets:

```
JMS Queue → k-mq-starter → k-identification → k-xsd-validation → k-xsl-transformation → k-log-flow-summary
```

## Key Components

### 1. K-MQ-Starter Kamelet

- **Purpose**: JMS queue listener and message processing initiator
- **Features**:
  - Consumes messages from configurable JMS queues
  - Generates unique flow occurrence IDs from database sequence
  - Archives messages to NAS file system (`/opt/nas/CH/IN/{flowOccurId}/`)
  - Publishes processing events to Kafka topics

### 2. K-Identification Kamelet

- **Purpose**: Payment identification and reference data retrieval
- **Features**:
  - Redis-based caching for performance optimization
  - Integration with referential service
  - Generic payment processing (not CH-specific)
  - Configurable cache TTL

### 3. K-XSD-Validation Kamelet

- **Purpose**: XML Schema validation
- **Schema**: `pacs.008.001.02.ch.02.xsd` (Swiss PACS.008 format)
- **Mode**: STRICT validation

### 4. K-XSL-Transformation Kamelet

- **Purpose**: Message transformation using XSLT
- **Template**: `overall-xslt-ch-pacs008-001-02.xsl`
- **Mode**: STRICT transformation

### 5. K-Log-Flow-Summary Kamelet

- **Purpose**: Processing summary and audit logging
- **Output**: Kafka topics for monitoring and audit trails

## Configuration

### Application Properties

```properties
# Server Configuration
server.port=8080
spring.application.name=pixel-camel-app

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/pixelv2
spring.datasource.username=pixelv2
spring.datasource.password=pixelv2_secure_password

# Redis Configuration (for k-identification)
pixel.redis.host=redis
pixel.redis.port=6379

# ActiveMQ Configuration
pixel.activemq.broker-url=tcp://localhost:61616
pixel.activemq.username=admin
pixel.activemq.password=admin

# Kafka Configuration
camel.component.kafka.brokers=localhost:9092
pixel.kafka.brokers=kafka:29092

# NAS File Archive Configuration
pixel.nas.ch.smb.url=file:///opt/nas/CH
pixel.nas.shared.smb.url=file:///opt/nas/shared
pixel.nas.data.smb.url=file:///opt/nas/data

# K-MQ Starter Configuration
kmq.starter.mqFileName=pacs008.input.queue
kmq.starter.connectionFactory=jmsConnectionFactory
kmq.starter.flowCode=ICHSIC
kmq.starter.payementType=PACS008
kmq.starter.messageType=PACS.008.001.02
kmq.starter.kafkaFlowSummaryTopicName=ch-flow-summary
kmq.starter.kafkaLogTopicName=ch-log-events
kmq.starter.kafkaDistributionTopicName=ch-distribution

# Referential Service Configuration
pixel.referentiel.service.url=http://pixel-v2-referentiel:8099
pixel.cache.ttl=3600
```

## File Archiving

The application automatically archives processed messages to the NAS file system:

- **Archive Path**: `/opt/nas/CH/IN/{flowOccurId}/`
- **File Pattern**: `{flowCode}_{flowOccurId}_{timestamp}.txt`
- **Example**: `ICHSIC_78_20251213132116163.txt`
- **Features**:
  - Automatic directory creation (`autoCreate=true`)
  - Atomic file operations using temporary files (`.tmp` prefix)
  - Network-attached storage for shared access

## Docker Integration

### Volume Mounts

The application requires NAS volume mounts for file operations:

```yaml
volumes:
  - nas_shared:/opt/nas/shared
  - nas_data:/opt/nas/data
  - nas_ch:/opt/nas/CH
```

### Dependencies

- PostgreSQL database
- Redis cache
- ActiveMQ message broker
- Kafka message streaming
- NAS (Samba) file service

## Management Operations

Use the provided `manage-nas.sh` script for NAS operations:

```bash
# Restart NAS container and fix permissions
./manage-nas.sh restart

# List archived files in IN directory
./manage-nas.sh list-in

# List processed files in OUT directory
./manage-nas.sh list-out

# Show NAS container status
./manage-nas.sh status
```

## Monitoring and Health Checks

### Health Endpoints

- **Application Health**: `http://localhost:8082/actuator/health`
- **Metrics**: `http://localhost:8082/actuator/metrics`
- **Camel Routes**: `http://localhost:8082/actuator/camel`

### Kafka Topics

- **Flow Summary**: `ch-flow-summary` - Processing completion events
- **Log Events**: `ch-log-events` - Detailed processing logs
- **Distribution**: `ch-distribution` - Message distribution events

## Error Handling

The application implements comprehensive error handling:

- **Global Exception Handler**: Captures and processes all exceptions
- **Error Headers**: Sets ErrorType and ErrorReason for debugging
- **Dead Letter Handling**: Routes failed messages to error handling endpoint
- **Retry Configuration**: Maximum redeliveries set to 0 for immediate failure handling

## Development

### Prerequisites

- Java 21
- Maven 3.8+
- Docker and Docker Compose
- Access to required external services

### Building

```bash
mvn clean compile
```

### Running Locally

```bash
# Start infrastructure services
cd ../docker
docker-compose up -d postgresql redis activemq kafka nas

# Run application
cd ../flow-ch
mvn spring-boot:run
```

### Testing Message Processing

```bash
# Send test message to JMS queue
# Check archived files
./manage-nas.sh list-in

# View file content
docker exec pixel-v2-app-spring-1 cat /opt/nas/CH/IN/{flowOccurId}/{filename}
```

## Technical Stack

- **Framework**: Spring Boot 3.4.1
- **Integration**: Apache Camel 4.1.0
- **Database**: PostgreSQL with Hibernate JPA
- **Cache**: Redis with Spring Data Redis
- **Messaging**: ActiveMQ (JMS) + Apache Kafka
- **File System**: Network-attached storage (Samba/CIFS)
- **Build Tool**: Maven
- **Containerization**: Docker

## File Structure

```
flow-ch/
├── src/main/java/com/pixel/v2/
│   └── routes/
│       └── ChProcessingRoute.java          # Main processing route
├── src/main/resources/
│   ├── application.properties              # Configuration
│   └── logback.xml                        # Logging configuration
├── pom.xml                                # Maven dependencies
└── README.md                              # This file
```

## Security Considerations

- Database credentials should be externalized using environment variables
- Redis access should be secured in production environments
- JMS credentials should use secure authentication
- File system permissions are automatically configured for the `pixel` user (UID=100, GID=101)

## Troubleshooting

### Common Issues

1. **File Permission Errors**:

   - Run `./manage-nas.sh restart` to fix NAS permissions
   - Verify application container can write to `/opt/nas/CH/`

2. **Database Connection Issues**:

   - Check PostgreSQL container status
   - Verify database credentials and connection URL

3. **Redis Connection Issues**:

   - Ensure Redis container is running
   - Check Redis host and port configuration

4. **JMS Queue Issues**:
   - Verify ActiveMQ broker is accessible
   - Check queue configuration and permissions

### Logs and Debugging

- Application logs: Available through Docker logs
- Camel route tracing: Enabled in DEBUG mode
- Health check: Monitor via actuator endpoints
