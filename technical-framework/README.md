# Technical Framework - Kamelets Collection

## Overview

The Technical Framework module contains all Apache Camel Kamelets used throughout the PIXEL-V2 system. This module serves as a centralized repository for reusable technical components that provide specific integration patterns and capabilities.

## Structure

```
technical-framework/
├── pom.xml                                    # Parent POM for all kamelets
├── k-mq-message-receiver/                    # MQ message consumption kamelet
├── k-http-message-receiver/                  # HTTP message reception kamelet
├── k-cft-data-receiver/                      # CFT file processing kamelet
├── k-db-tx/                                  # Database transaction kamelet
├── k-referentiel-data-loader/                # Reference data loading kamelet
├── k-ingestion-technical-validation/         # Technical validation kamelet
├── k-payment-idempotence-helper/             # Idempotence checking kamelet
├── k-log-tx/                                 # Centralized logging kamelet
├── k-kafka-message-receiver/                 # Kafka message consumption kamelet
├── k-pacs008-to-cdm-transformer/            # PACS.008 to CDM transformer
├── k-pacs009-to-cdm-transformer/            # PACS.009 to CDM transformer
├── k-pain001-to-cdm-transformer/            # PAIN.001 to CDM transformer
└── k-camt053-to-cdm-transformer/            # CAMT.053 to CDM transformer
```

## Kamelet Categories

### 🔄 Message Receivers

- **k-mq-message-receiver**: Consumes messages from IBM MQ queues with JMS integration
- **k-http-message-receiver**: Receives messages via HTTP REST endpoints
- **k-kafka-message-receiver**: Consumes messages from Kafka topics with metadata enrichment
- **k-cft-data-receiver**: Processes CFT file-based messages with batch optimization

### 🔧 Processing & Validation

- **k-ingestion-technical-validation**: Performs technical validation of incoming messages
- **k-payment-idempotence-helper**: Provides duplicate detection and idempotence checking
- **k-referentiel-data-loader**: Loads and enriches messages with reference data

### 🏗️ Infrastructure

- **k-db-tx**: Unified database persistence with transaction management
- **k-log-tx**: Centralized logging with audit trail capabilities

### 🔄 Transformers

- **k-pacs008-to-cdm-transformer**: Transforms PACS.008 messages to CDM format using XSLT
- **k-pacs009-to-cdm-transformer**: Transforms PACS.009 messages to CDM format using XSLT
- **k-pain001-to-cdm-transformer**: Transforms PAIN.001 messages to CDM format using XSLT
- **k-camt053-to-cdm-transformer**: Transforms CAMT.053 messages to CDM format using XSLT

## Technologies

- **Apache Camel 4.1.0**: Core integration framework
- **Spring Boot 3.4.1**: Application framework and dependency injection
- **Saxon XSLT Processor**: XML transformation engine
- **Jackson**: JSON processing
- **JPA/Hibernate**: Database persistence
- **Oracle/PostgreSQL**: Database connectivity

## Development

### Building the Technical Framework

```bash
# Build all kamelets
cd technical-framework
mvn clean install

# Build specific kamelet
cd k-log-tx
mvn clean install
```

### Testing

```bash
# Run all tests
mvn test

# Run specific kamelet tests
cd k-kafka-message-receiver
mvn test
```

### Adding New Kamelets

1. Create new module directory under `technical-framework/`
2. Add kamelet definition in `src/main/resources/kamelets/`
3. Add Java classes in `src/main/java/` if needed
4. Update parent `pom.xml` to include new module
5. Follow existing naming conventions: `k-{functionality}-{type}`

## Integration

Kamelets are used throughout the PIXEL-V2 system:

### In Ingestion Module

- k-mq-message-receiver
- k-http-message-receiver
- k-cft-data-receiver
- k-log-tx
- k-db-tx

### In Processing Module

- k-kafka-message-receiver
- k-pacs008-to-cdm-transformer
- k-pain001-to-cdm-transformer

### In Business Module

- k-kafka-message-receiver
- k-log-tx

## Configuration

Each kamelet can be configured through properties. See individual kamelet documentation for specific configuration options.

### Common Configuration Patterns

```yaml
# Database configuration (k-db-tx)
kamelet:k-db-tx:
  datasourceRef: "dataSource"
  entityClass: "com.pixel.v2.entity.Message"

# Logging configuration (k-log-tx)
kamelet:k-log-tx:
  logLevel: "INFO"
  logCategory: "BUSINESS"

# Kafka configuration (k-kafka-message-receiver)
kamelet:k-kafka-message-receiver:
  bootstrapServers: "localhost:9092"
  topics: "payment-events"
```

## Versioning

All kamelets follow the parent version: **1.0.1-SNAPSHOT**

When releasing:

1. Update parent version in `technical-framework/pom.xml`
2. All child kamelets inherit the version automatically
3. Build and test all kamelets together
4. Release as a cohesive technical framework unit

## Contributing

1. Follow existing code patterns and naming conventions
2. Add comprehensive tests for new functionality
3. Update documentation for new kamelets
4. Ensure compatibility with existing PIXEL-V2 modules

## Dependencies

The technical framework manages all Apache Camel and Spring Boot dependencies centrally, ensuring version consistency across all kamelets.
