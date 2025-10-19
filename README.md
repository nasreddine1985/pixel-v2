# PIXEL-V2 Project

## Overview

The PIXEL-V2 project is a Maven multi-module project implementing a payment message processing system. It provides a modular architecture with Apache Camel kamelets for message transformation, persistence, and orchestration.

## Project Structure

```
PIXEL-V2/
├── pom.xml                    # Parent POM
├── ingestion/                # Spring Boot orchestrator application
├── k-mq-receipt/             # Kamelet for MQ message receipt and persistence
├── k-api-receipt/            # Kamelet for API message receipt
├── k-file-receipt/           # Kamelet for file-based message receipt
├── k-ref-loader/             # Kamelet for XML enrichment via REST services
├── k-pacs-008-to-cdm/        # Kamelet for PACS.008 to CDM transformation
├── k-pan-001-to-cdm/         # Kamelet for PAN001 to CDM transformation
├── k-ingest-validation/      # Kamelet for message validation
└── k-idempotence/            # Kamelet for duplicate detection
```

## Modules

### ingestion

Spring Boot application that orchestrates payment message processing flow using Apache Camel routes and kamelets.

**Key Features:**

- Multi-channel message ingestion (MQ, API, File)
- Message enrichment and validation
- Idempotence checking
- Kafka publishing
- Error handling and redelivery
- Health check endpoints

### k-mq-receipt

Kamelet for receiving messages from IBM MQ and persisting them using JPA.

**Configuration:**

- MQ destination queue
- JMS connection factory reference
- JPA entity persistence

### k-ref-loader

Kamelet for enriching XML messages by calling external REST services.

**Configuration:**

- Service URL for enrichment calls
- HTTP POST request handling
- XML payload processing

### k-pacs-008-to-cdm

Kamelet for transforming PACS.008 XML messages to Common Data Model (CDM) format using XSLT.

**Features:**

- Saxon XSLT processor
- PACS.008 to CDM transformation
- Configurable XSLT resource

### k-pan-001-to-cdm

Kamelet for transforming PAN001 XML messages to Common Data Model (CDM) format using XSLT.

**Features:**

- Saxon XSLT processor
- PAN001 to CDM transformation
- Configurable XSLT resource

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21 or higher
- Apache Maven 3.9 or higher
- PostgreSQL database (for JPA persistence)
- IBM MQ (for message queue integration)

### Installation

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd PIXEL-V2
   ```

2. Build all modules:

   ```bash
   mvn clean install
   ```

3. Run the ingestion service:
   ```bash
   cd ingestion
   mvn spring-boot:run
   ```

### Configuration

Configure the application properties in `ingestion/src/main/resources/application.properties`:

```properties
# Payment Processing Configuration
payment.mq.queue=PAYMENT.INPUT.QUEUE
payment.mq.connectionFactoryRef=mqConnectionFactory
payment.enrichment.serviceUrl=http://enrichment-service/api/enrich
payment.file.watch.directory=/data/input
payment.kafka.topic.pacs008=pacs008-messages
payment.kafka.topic.pan001=pan001-messages
payment.kafka.topic.errors=error-messages
```

## Dependencies

**Core Technologies:**

- Spring Boot 3.1.5
- Apache Camel 4.3.0
- Jakarta Persistence API 3.1.0
- Saxon XSLT Processor
- PostgreSQL JDBC Driver
- IBM MQ Integration

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
