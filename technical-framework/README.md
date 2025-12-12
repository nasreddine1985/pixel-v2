# Technical Framework - Kamelets Collection

## Overview

The Technical Framework module contains all Apache Camel Kamelets used throughout the PIXEL-V2 system. This module serves as a centralized repository for reusable technical components that provide specific integration patterns and capabilities.

## Structure

```
technical-framework/
├── pom.xml                                    # Parent POM for all kamelets
├── k-mq-message-receiver/                    # MQ message consumption kamelet
├── k-http-starter/                          # HTTP message reception kamelet
├── k-cft-starter/                           # CFT file processing kamelet
├── k-db-tx/                                  # Database transaction kamelet
├── k-log-tx/                                 # Centralized logging kamelet
├── k-log-events/                             # Detailed log events kamelet
├── k-kafka-starter/                 # Kafka message consumption kamelet
├── k-pacs008-to-cdm-transformer/            # PACS.008 to CDM transformer
├── k-pacs009-to-cdm-transformer/            # PACS.009 to CDM transformer
├── k-pain001-to-cdm-transformer/            # PAIN.001 to CDM transformer
└── k-camt053-to-cdm-transformer/            # CAMT.053 to CDM transformer
```

## Kamelet Categories

### 🔄 Message Receivers

- **k-mq-message-receiver**: Consumes messages from IBM MQ queues with JMS integration
- **k-http-starter**: Receives messages via HTTP REST endpoints
- **k-kafka-starter**: Starts Kafka message consumption from topics with metadata enrichment
- **k-cft-starter**: Processes CFT file-based messages with batch optimization

### 🔧 Processing & Validation

### 🏗️ Infrastructure

- **k-db-tx**: Unified database persistence with transaction management
- **k-log-tx**: Centralized logging with audit trail capabilities
- **k-log-events**: Detailed log event generation with complete LogEvent structure

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
