# ⚙️ Business Module

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green.svg)](https://spring.io/projects/spring-boot)
[![Apache Camel](https://img.shields.io/badge/Apache%20Camel-4.1.0-orange.svg)](https://camel.apache.org/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/projects/jdk/21/)

> ⚙️ **Core Payment Processing Engine** - Intelligent message transformation and routing with CDM conversion.

## Overview

The Business Module processes payment messages from the ingestion service, detects message types, transforms them to CDM format using specialized kamelets, and routes the results to the appropriate destination.

**Key Features:**

- 🔍 **Smart Message Detection**: Automatic identification of PACS.008, PACS.009, PAIN.001, CAMT.053
- � **CDM Transformation**: ISO 20022 to Common Data Model conversion
- 🚀 **Dual Processing**: Real-time (HTTP) and batch (Kafka) support
- 📊 **Conditional Routing**: Routes based on message origin (Kafka → Kafka, HTTP → Outbound)

## 🏗️ Processing Flow

`````mermaid

flowchart TD

### 🎯 Core Capabilities A[📥 Kafka Messages<br/>CFT Files] --> C[🧠 Message Type Processor]

    B[📥 Direct Messages<br/>HTTP/MQ] --> C

| Capability | Description | Technology | C --> D{📋 Message Type}

|------------|-------------|------------| D -->|pacs.008| E[🔄 PACS.008 Transformer<br/>k-pacs-008-to-cdm]

| **Intelligent Message Detection** | Automatic detection of PACS.008, PACS.009, PAIN.001, CAMT.053 | Custom Processor | D -->|pacs.009| F[🔄 PACS.009 Transformer<br/>k-pacs-009-to-cdm]

| **Dual-Channel Processing** | HTTP direct + Kafka consumer support | Apache Camel Routes | D -->|pain.001| G[🔄 PAIN.001 Transformer<br/>k-pain001-to-cdm-transformer]

| **Dynamic Routing** | Route messages to appropriate transformer kamelets | Camel Recipient List | D -->|camt.053| H[🔄 CAMT.053 Transformer<br/>k-camt053-to-cdm-transformer]

| **CDM Transformation** | Convert ISO 20022 to Common Data Model | Transformer Kamelets | D -->|Unknown| I[⚠️ Error Handler]

| **Error Handling** | Comprehensive error processing and recovery | Exception Handling | E --> J[✅ CDM Output]

    F --> J

## 📊 Message Flow Architecture G --> J

    H --> J

````mermaid J --> K{🚦 Message Source}

graph TD    K -->|Kafka Origin| L[📤 Kafka Publisher]

    A[Ingestion Module] --> B{Processing Channel}    K -->|HTTP/MQ Origin| M[📤 Distribution Service]

        L --> N[📊 Kafka Topics<br/>Batch Processing]

    %% Real-time path (HTTP/MQ messages)    M --> O[🌐 External Systems<br/>Real-time]

    B -->|HTTP Direct| C[HTTP Endpoints]    I --> P[🔗 k-log-tx<br/>Error Logging]

    C --> C1["/pacs-008-transform"]```

    C --> C2["/pacs-009-transform"]

    C --> C3["/pain-001-transform"]### 📋 Core Components

    C --> C4["/camt-053-transform"]

    C --> C5["/kafka-message-processing"]#### 🧠 Message Type Processor



    %% Batch path (CFT messages)- **Purpose**: Intelligent content analysis and routing

    B -->|Kafka Topics| D[k-kafka-message-receiver]- **Features**: XML/JSON parsing, header enrichment, metadata extraction

    D --> E[direct:kafka-message-processing]- **Output**: MessageType and RouteTarget headers



    %% Message type processing#### 🔄 CDM Transformers

    C1 --> F1[direct:pacs-008-transform]

    C2 --> F2[direct:pacs-009-transform]| Kamelet                          | Purpose                             | Technology     | Output           |

    C3 --> F3[direct:pain-001-transform] | -------------------------------- | ----------------------------------- | -------------- | ---------------- |

    C4 --> F4[direct:camt-053-transform]| **k-pacs-008-to-cdm**            | ISO 20022 PACS.008 → CDM conversion | Saxon XSLT 3.0 | Standardized CDM |

    C5 --> G[MessageTypeProcessor]| **k-pacs-009-to-cdm**            | ISO 20022 PACS.009 → CDM conversion | Saxon XSLT 3.0 | Standardized CDM |

    E --> G| **k-pain001-to-cdm-transformer** | ISO 20022 PAIN.001 → CDM conversion | Saxon XSLT 3.0 | Standardized CDM |

    | **k-camt053-to-cdm-transformer** | ISO 20022 CAMT.053 → CDM conversion | Saxon XSLT 3.0 | Standardized CDM |

    G --> H{Message Type}

    H -->|pacs.008| F1#### 🚦 Conditional Router

    H -->|pacs.009| F2

    H -->|pain.001| F3- **Logic**: Routes based on message origin (`messageSource` header)

    H -->|camt.053| F4- **Kafka Origin** (`"KAFKA_TOPIC"`) → Returns to Kafka ecosystem for batch processing

    H -->|unknown| I[direct:unknown-message]- **HTTP/MQ Origin** (`"HTTP_ENDPOINT"`, `"MQ_QUEUE"`) → Forwards to Distribution Service



    %% Transformer kamelets### 🎯 Input Sources

    F1 --> J1[k-pacs-008-to-cdm]

    F2 --> J2[k-pacs009-to-cdm-transformer]| Source           | Description                | Processing Type | Entry Point                       | messageSource Header             |

    F3 --> J3[k-pain001-to-cdm-transformer]| ---------------- | -------------------------- | --------------- | --------------------------------- | -------------------------------- |

    F4 --> J4[k-camt053-to-cdm-transformer]| **Kafka Topics** | Batch CFT file messages    | Asynchronous    | `direct:kafka-message-processing` | `"KAFKA_TOPIC"`                  |

    | **Direct Calls** | Real-time HTTP/MQ messages | Synchronous     | `direct:business-input`           | `"HTTP_ENDPOINT"` / `"MQ_QUEUE"` |

    %% CDM processing

    J1 --> K[direct:cdm-output]### 📤 Output Destinations

    J2 --> K

    J3 --> K| Destination              | Condition                        | Purpose                       | Technology     | Configuration           |

    J4 --> K| ------------------------ | -------------------------------- | ----------------------------- | -------------- | ----------------------- |

    | **Kafka Topics**         | `messageSource == "KAFKA_TOPIC"` | Batch processing continuation | Kafka Producer | `localhost:9092`        |

    %% Output routing| **Distribution Service** | `messageSource != "KAFKA_TOPIC"` | Real-time message delivery    | HTTP Client    | `localhost:8082/submit` |

    K --> L{Message Source}

    L -->|Kafka Origin| M[Kafka Output Topic]### � Kamelet Ecosystem

    L -->|HTTP/MQ Origin| N[Outbound Service]

    #### Input Kamelets

    %% Error handling

    I --> O[direct:error-handling]| Kamelet                      | Function                | Features                                               |

    | ---------------------------- | ----------------------- | ------------------------------------------------------ |

    style A fill:#e1f5fe| **k-kafka-message-receiver** | Kafka topic consumption | Multi-topic, auto-commit, batch config, error handling |

    style C fill:#f3e5f5

    style D fill:#fff3e0#### Transformation Kamelets

    style G fill:#e8f5e8

    style K fill:#fce4ec| Kamelet                          | Function                | Features                                          |

    style M fill:#fff8e1| -------------------------------- | ----------------------- | ------------------------------------------------- |

    style N fill:#e0f2f1| **k-pacs-008-to-cdm**            | PACS.008 transformation | XSLT 3.0, Saxon HE, ISO 20022 mapping, validation |

    style O fill:#ffebee| **k-pacs-009-to-cdm**            | PACS.009 transformation | XSLT 3.0, Saxon HE, ISO 20022 mapping, validation |

```| **k-pain001-to-cdm-transformer** | PAIN.001 transformation | XSLT 3.0, Saxon HE, ISO 20022 mapping, validation |



## 🔧 Route Configuration Details#### Output Kamelets



### Main Processing Routes| Kamelet      | Function            | Features                                                                   |

| ------------ | ------------------- | -------------------------------------------------------------------------- |

#### 1. **Main Message Processing Route**| **k-log-tx** | Transaction logging | Multi-level logging, categories, metadata enrichment, database persistence |

```yaml

- route:### ⚠️ Error Handling Strategy

    id: payment-message-processing-main

    from: "{{processing.kafka.input.endpoint:direct:kafka-message-processing}}"```mermaid

    steps:flowchart LR

      - process: ref: "messageTypeProcessor"    A[Error Detected] --> B{Error Type}

      - recipientList: simple: "${header.RouteTarget}"    B -->|Unknown Message| C[direct:unknown-message]

```    B -->|Transformation Error| D[Kamelet Error Handler]

    B -->|Routing Failure| E[Circuit Breaker]

**Purpose**: Entry point for message processing with intelligent routing    C --> F[k-log-tx Logger]

**Input**: Messages from Kafka or HTTP endpoints      D --> F

**Process**: Message type detection → Dynamic routing    E --> F

**Output**: Routes to appropriate transformer based on message type    F --> G[Dead Letter Queue]

    F --> H[Alert Generation]

#### 2. **Message-Type-Specific Transformation Routes**```



**PACS.008 Route** (Customer Credit Transfer):| Error Type                | Handler                   | Actions                                      | Recovery                   |

```yaml| ------------------------- | ------------------------- | -------------------------------------------- | -------------------------- |

- route:| **Unknown Message Types** | `direct:unknown-message`  | Error logging, DLQ routing                   | Manual review              |

    id: pacs-008-transformation| **Transformation Errors** | Kamelet error handlers    | Invalid XML, schema errors, XSLT errors      | Retry with fallback        |

    from: "direct:pacs-008-transform"| **Routing Failures**      | Circuit breaker pattern   | HTTP timeout, Kafka unavailable              | Automatic retry → fallback |

    steps:| **System Errors**         | Centralized error handler | Error classification, audit logging, metrics | Recovery procedures        |

      - to: "kamelet:k-pacs-008-to-cdm"

      - to: "{{processing.cdm.output.endpoint:direct:cdm-output}}"## ⭐ Key Features

`````

### 🔄 Dual Processing Architecture

**PACS.009 Route** (Credit Transfer Return):

````yaml- **Batch Processing**: High-throughput Kafka message processing for CFT files

- route:- **Real-time Processing**: Low-latency direct message processing for HTTP/MQ channels

    id: pacs-009-transformation  - **Intelligent Routing**: Origin-aware conditional routing based on `messageSource` header

    from: "direct:pacs-009-transform"

    steps:### 🧠 Message Intelligence

      - to: "kamelet:k-pacs009-to-cdm-transformer"

      - to: "{{processing.cdm.output.endpoint:direct:cdm-output}}"- **Auto-Detection**: Identifies pacs.008, pacs.009, and pain.001 message types from XML/JSON content

```- **Dynamic Routing**: Routes to appropriate transformer kamelets based on message type

- **Header Enrichment**: Adds processing metadata and routing information

**PAIN.001 Route** (Payment Initiation):

```yaml### 🔀 Conditional Distribution

- route:

    id: pain-001-transformation- **Origin-Based Routing**:

    from: "direct:pain-001-transform"   - Kafka-originated messages → Return to Kafka ecosystem

    steps:  - HTTP/MQ messages → Forward to Distribution Service

      - to: "kamelet:k-pain001-to-cdm-transformer"- **Load Balancing**: Optimal resource utilization based on message origin

      - to: "{{processing.cdm.output.endpoint:direct:cdm-output}}"

```### 🛡️ Resilience & Monitoring



**CAMT.053 Route** (Account Statement):- **Error Handling**: Comprehensive error recovery with dead letter queues

```yaml  - **Circuit Breaker**: Automatic failover for external service calls

- route:- **Health Monitoring**: Real-time health checks and performance metrics

    id: camt-053-transformation- **Audit Logging**: Complete transaction traceability with k-log-tx integration

    from: "direct:camt-053-transform"

    steps:## 📋 Supported Message Types

      - to: "kamelet:k-camt053-to-cdm-transformer"

      - to: "{{processing.cdm.output.endpoint:direct:cdm-output}}"### 💳 PACS.008 - Customer Credit Transfer Initiation

````

| Attribute | Details |

#### 3. **HTTP Direct Integration Routes**| ----------------------- | ------------------------------------------------------------------- |

| **Standard** | ISO 20022 |

These routes enable direct HTTP integration with the ingestion module for real-time processing:| **Purpose** | Initiate customer credit transfers between financial institutions |

| **XML Identifiers** | `pacs.008`, `FIToFICstmrCdtTrf`, `CustomerCreditTransferInitiation` |

```yaml| **JSON Identifiers**    | `pacs008`, `FIToFICstmrCdtTrf`, `CustomerCreditTransferInitiation` |

# PACS.008 Direct HTTP Endpoint| **Transformer** | `k-pacs-008-to-cdm` |

- route:| **Output Format** | Common Data Model (CDM) |

  id: http-pacs-008-direct| **Processing Priority** | Real-time capable |

  from: "platform-http:/business/api/direct/pacs-008-transform"

  steps:#### Sample XML Structure

      - setHeader: name: "MessageType", constant: "pacs.008"

      - to: "direct:pacs-008-transform"```xml

<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">

# PACS.009 Direct HTTP Endpoint <FIToFICstmrCdtTrf>

- route: <GrpHdr>...</GrpHdr>

  id: http-pacs-009-direct <CdtTrfTxInf>...</CdtTrfTxInf>

  from: "platform-http:/business/api/direct/pacs-009-transform" </FIToFICstmrCdtTrf>

  steps:</Document>

      - setHeader: name: "MessageType", constant: "pacs.009"```

      - to: "direct:pacs-009-transform"

### � PACS.009 - Financial Institution Credit Transfer Reversal

# PAIN.001 Direct HTTP Endpoint

- route:| Attribute | Details |

  id: http-pain-001-direct| ----------------------- | --------------------------------------------------------------------------------- |

  from: "platform-http:/business/api/direct/pain-001-transform"| **Standard** | ISO 20022 |

  steps:| **Purpose** | Reverse previously initiated credit transfers |

      - setHeader: name: "MessageType", constant: "pain.001" | **XML Identifiers**     | `pacs.009`, `FIToFICstmrCdtTrfRvsl`, `FinancialInstitutionCreditTransferReversal` |

      - to: "direct:pain-001-transform"| **JSON Identifiers**    | `pacs009`, `FIToFICstmrCdtTrfRvsl`, `FinancialInstitutionCreditTransferReversal`  |

| **Transformer** | `k-pacs-009-to-cdm` |

# CAMT.053 Direct HTTP Endpoint| **Output Format** | Common Data Model (CDM) |

- route:| **Processing Priority** | Real-time capable |

  id: http-camt-053-direct

  from: "platform-http:/business/api/direct/camt-053-transform"#### Sample XML Structure

  steps:

      - setHeader: name: "MessageType", constant: "camt.053"```xml

      - to: "direct:camt-053-transform"<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08">

    <FIToFICstmrCdtTrfRvsl>

# General Processing Direct HTTP Endpoint <GrpHdr>...</GrpHdr>

- route: <CdtTrfTxInfRvsl>...</CdtTrfTxInfRvsl>

  id: http-general-processing-direct </FIToFICstmrCdtTrfRvsl>

  from: "platform-http:/business/api/direct/kafka-message-processing"</Document>

  steps:```

      - process: ref: "messageTypeProcessor"

      - recipientList: simple: "${header.RouteTarget}"### � PAIN.001 - Customer Payment Initiation

````

| Attribute               | Details                                                            |

#### 4. **CDM Output Processing Route**| ----------------------- | ------------------------------------------------------------------ |

| **Standard**            | ISO 20022                                                          |

```yaml| **Purpose**             | Initiate customer payment requests to financial institution        |

- route:| **XML Identifiers**     | `pain.001`, `CstmrCdtTrfInitn`, `CustomerCreditTransferInitiation` |

    id: cdm-output-processing| **JSON Identifiers**    | `pain001`, `CstmrCdtTrfInitn`, `CustomerCreditTransferInitiation`  |

    from: "{{processing.cdm.output.endpoint:direct:cdm-output}}"| **Transformer**         | `k-pain001-to-cdm-transformer`                                     |

    steps:| **Output Format**       | Common Data Model (CDM)                                            |

      - choice:| **Processing Priority** | Real-time capable                                                  |

          when:

            - expression: simple: "${header.messageSource} == 'KAFKA_TOPIC'"#### Sample JSON Structure

              steps:

                - to: "kafka:{{processing.kafka.output.topic:cdm-processed-messages}}"```json

          otherwise:{

            steps:  "CustomerCreditTransferInitiation": {

              - to: "direct:route-to-outbound"    "GrpHdr": {...},

```    "PmtInf": {...}

  }

**Purpose**: Route transformed CDM messages to appropriate destination}

**Logic**: ```

- Kafka origin → Route back to Kafka topics for batch processing

- HTTP/MQ origin → Route to outbound service for real-time delivery### ❌ Unknown Message Types



## 🧠 MessageTypeProcessor| Scenario                | Action                       | Handler                  |

| ----------------------- | ---------------------------- | ------------------------ |

The intelligent message type detection component that analyzes incoming messages:| **Unrecognized Format** | Route to error handler       | `direct:unknown-message` |

| **Invalid Structure**   | Log error and generate alert | `k-log-tx` kamelet       |

### Detection Capabilities| **Processing Failure**  | Dead letter queue routing    | Circuit breaker pattern  |



| Message Type | XML Patterns | JSON Patterns |## ⚙️ Configuration

|--------------|--------------|---------------|

| **PACS.008** | `<pacs.008>`, `<FIToFICstmrCdtTrf>` | `{"pacs008": {...}}`, `{"FIToFICstmrCdtTrf": {...}}` |### 📝 Application Properties

| **PACS.009** | `<pacs.009>`, `<FIToFICstmrCdtTrfRtr>` | `{"pacs009": {...}}`, `{"FIToFICstmrCdtTrfRtr": {...}}` |

| **PAIN.001** | `<pain.001>`, `<CstmrCdtTrfInitn>` | `{"pain001": {...}}`, `{"CstmrCdtTrfInitn": {...}}` |  #### Core Processing Settings

| **CAMT.053** | `<camt.053>`, `<BkToCstmrStmt>` | `{"camt053": {...}}`, `{"BkToCstmrStmt": {...}}` |

```properties

### Headers Set by Processor# Main Processing Endpoints

processing.kafka.input.endpoint=direct:kafka-message-processing

```javaprocessing.cdm.output.endpoint=direct:cdm-output

// Message identificationprocessing.error.endpoint=direct:error-handling

exchange.getIn().setHeader("MessageType", "pacs.008|pacs.009|pain.001|camt.053|unknown");

exchange.getIn().setHeader("RouteTarget", "direct:pacs-008-transform|...|direct:unknown-message");# Conditional Routing Configuration

business.service.endpoint=http://localhost:8082/submit

// Processing metadata  business.service.timeout=30000

exchange.getIn().setHeader("ProcessingTimestamp", System.currentTimeMillis());business.service.retry.attempts=3

exchange.getIn().setHeader("ProcessedBy", "MessageTypeProcessor");kafka.output.broker=localhost:9092

```kafka.output.topic=cdm-processed-messages



## 🔧 Kamelet Integration# Message Type Detection

message.detection.xml.enabled=true

The business module integrates with multiple transformer kamelets from the technical framework:message.detection.json.enabled=true

message.detection.fallback.route=direct:unknown-message

### Transformer Kamelets Used```



| Kamelet | Purpose | Input Format | Output Format |#### 🔄 Kamelet Configuration

|---------|---------|--------------|---------------|

| **k-pacs-008-to-cdm** | PACS.008 → CDM transformation | ISO 20022 PACS.008 XML | CDM JSON |```properties

| **k-pacs009-to-cdm-transformer** | PACS.009 → CDM transformation | ISO 20022 PACS.009 XML | CDM JSON |# Transformer Kamelets

| **k-pain001-to-cdm-transformer** | PAIN.001 → CDM transformation | ISO 20022 PAIN.001 XML | CDM JSON |transformers.pacs008.endpoint=kamelet:k-pacs-008-to-cdm

| **k-camt053-to-cdm-transformer** | CAMT.053 → CDM transformation | ISO 20022 CAMT.053 XML | CDM JSON |transformers.pain001.endpoint=kamelet:k-pain001-to-cdm-transformer

transformers.logging.endpoint=kamelet:k-log-tx

### Kamelet Integration Flow

# Kamelet Parameters

```kamelet.k-pacs-008-to-cdm.xslt.file=/xslt/k-pacs-008-to-cdm.xslt

Message Input → MessageTypeProcessor → Route Selection → Kamelet Transformation → CDM Outputkamelet.k-pain001-to-cdm-transformer.xslt.file=/xslt/k-pain-001-to-cdm.xslt

```kamelet.k-log-tx.level=INFO

kamelet.k-log-tx.category=BUSINESS_PROCESSING

Each transformer kamelet receives:```

- **Input**: Original ISO 20022 message (XML format)

- **Headers**: Message metadata (type, routing info, timestamps)#### 🌐 Integration Settings

- **Processing**: XSLT-based transformation to CDM format

- **Output**: Transformed CDM message (JSON format)```properties

# Spring Kafka (Batch Processing)

### Configurationspring.kafka.bootstrap-servers=localhost:9092

spring.kafka.consumer.group-id=business-processing-service

Kamelet endpoints are configurable via application properties:spring.kafka.consumer.auto-offset-reset=earliest

kafka.topics.input=payment-events

```properties

# Transformer Kamelet Endpoints# HTTP Client (Real-time Processing)

transformers.pacs008.endpoint=kamelet:k-pacs-008-to-cdmhttp.client.connection-timeout=5000

transformers.pacs009.endpoint=kamelet:k-pacs009-to-cdm-transformer  http.client.read-timeout=30000

transformers.pain001.endpoint=kamelet:k-pain001-to-cdm-transformerhttp.client.max-connections=50

transformers.camt053.endpoint=kamelet:k-camt053-to-cdm-transformerkafka.output.topic=cdm-processed-messages

````

# Kafka Configuration (for batch processing)

## 📋 Configurationspring.kafka.bootstrap-servers=localhost:9092

spring.kafka.consumer.group-id=payment-processing-service

### Application Propertieskafka.topics.input=payment-events

#### Server Configuration# Transformer Configuration

````propertiestransformers.pacs008.endpoint=kamelet:k-pacs-008-to-cdm

# Server Configuration for HTTP endpoints  transformers.pan001.endpoint=kamelet:k-pan-001-to-cdm

server.port=8081

server.servlet.context-path=/# Integration with Ingestion Module

ingestion.integration.enabled=true

# Application Infoingestion.realtime.endpoint=direct:kafka-message-processing

spring.application.name=payment-message-processing```

````

### Environment Variables

#### Apache Camel Configuration

````properties| Variable                     | Description                  | Default                      |

# Camel Configuration| ---------------------------- | ---------------------------- | ---------------------------- |

camel.springboot.name=payment-processing-camel| `KAFKA_BOOTSTRAP_SERVERS`    | Kafka cluster connection     | `localhost:9092`             |

camel.springboot.main-run-controller=true| `KAFKA_CONSUMER_GROUP_ID`    | Consumer group ID            | `payment-processing-service` |

camel.springboot.jmx-enabled=true| `KAFKA_INPUT_TOPIC`          | Input topic name             | `payment-events`             |

| `CDM_OUTPUT_ENDPOINT`        | CDM output endpoint          | `direct:cdm-persistence`     |

# YAML Routes Configuration| `🆕 CDM_PERSISTENCE_ENABLED` | Enable CDM persistence       | `true`                       |

camel.main.routes-include-pattern=classpath:camel/main-business-routes.yaml| `🆕 CDM_AUDIT_ENABLED`       | Enable CDM audit trail       | `true`                       |

```| `🆕 INGESTION_INTEGRATION`   | Enable ingestion integration | `true`                       |



#### Processing Configuration  ## Installation

```properties

# Processing Endpoints### Prerequisites

processing.kafka.input.endpoint=direct:kafka-message-processing

processing.cdm.output.endpoint=direct:cdm-output- Java 21+

processing.error.endpoint=direct:error-handling- Apache Maven 3.8+

```- Apache Kafka 3.0+ (for batch processing)

- k-pacs-008-to-cdm kamelet

#### Kafka Configuration- k-pan-001-to-cdm kamelet

```properties- k-kafka-message-receiver kamelet (for batch processing)

# Kafka Configuration (for batch processing)- **🆕 k-db-tx kamelet** (for CDM persistence)

spring.kafka.bootstrap-servers=localhost:9092- **🆕 Oracle Database** (for CDM and message persistence)

spring.kafka.consumer.group-id=payment-processing-service- **🆕 Ingestion Module** (for real-time integration)

spring.kafka.consumer.auto-offset-reset=earliest

### Build and Run

# Kafka Topics

kafka.topics.input=payment-events```bash

kafka.topics.cdm-output=cdm-transformed-messages# Build the application

```mvn clean install



#### HTTP Direct Integration# Run the application

```properties  mvn spring-boot:run

# Direct HTTP Integration (for real-time processing)

business.http.direct.enabled=true# Or run the JAR

business.http.direct.base-path=/business/api/directjava -jar target/processing-1.0.1-SNAPSHOT.jar

````

#### Outbound Service Configuration### Docker Deployment

````properties

# Outbound Service Configuration```dockerfile

outbound.service.endpoint=http://localhost:8082/outbound/submitFROM openjdk:21-jdk-slim

kafka.output.broker=localhost:9092COPY target/processing-1.0.1-SNAPSHOT.jar app.jar

kafka.output.topic=cdm-processed-messagesEXPOSE 8080

```ENTRYPOINT ["java", "-jar", "/app.jar"]

````

## 🚀 API Endpoints

## Usage

### HTTP Direct Integration Endpoints

### Integration with k-kafka-message-receiver

These endpoints enable direct integration with the ingestion module for real-time processing:

The processing service is designed to work with the k-kafka-message-receiver kamelet:

#### Message-Type-Specific Endpoints

````yaml

```http# k-kafka-message-receiver configuration

POST /business/api/direct/pacs-008-transformapiVersion: camel.apache.org/v1alpha1

Content-Type: application/jsonkind: Kamelet

metadata:

# Direct processing of PACS.008 messages  name: k-kafka-message-receiver

```spec:

  definition:

```http    properties:

POST /business/api/direct/pacs-009-transform      bootstrapServers: "localhost:9092"

Content-Type: application/json        topics: "payment-events"

      consumerGroupId: "payment-receiver-group"

# Direct processing of PACS.009 messages      routingEndpoint: "direct:kafka-message-processing"

````

```http### Processing Flow

POST /business/api/direct/pain-001-transform

Content-Type: application/json#### 🔄 Dual Processing with Conditional Output



# Direct processing of PAIN.001 messages**Batch Processing (CFT Messages via Kafka)**

```

1. **Message Reception**: Receives from k-kafka-message-receiver via `direct:kafka-message-processing`

```http2. **Type Detection**: `MessageTypeProcessor` analyzes message content

POST /business/api/direct/camt-053-transform3. **Header Setting**: Sets `MessageType`, `RouteTarget`, and `messageSource=KAFKA_TOPIC` headers

Content-Type: application/json4. **Dynamic Routing**: Routes to appropriate transformer based on headers

5. **Transformation**: Calls transformer kamelet (k-pacs-008-to-cdm or k-pan-001-to-cdm)

# Direct processing of CAMT.053 messages 6. **🆕 Conditional Routing**: Routes to Kafka output broker (based on messageSource header)

```7. **🆕 Kafka Output**: Sends to `cdm-processed-messages` topic

#### General Processing Endpoint**Real-time Processing (HTTP/MQ Messages via Ingestion)**

```http1. **Direct Reception**: Receives from ingestion module via `direct:kafka-message-processing`

POST /business/api/direct/kafka-message-processing2. **Type Detection**: `MessageTypeProcessor` analyzes message content

Content-Type: application/json3. **Header Setting**: Sets `MessageType`, `RouteTarget`, and `messageSource≠KAFKA_TOPIC` headers

4. **Dynamic Routing**: Routes to appropriate transformer based on headers

# General message processing with type detection5. **Transformation**: Calls transformer kamelet with real-time optimization

````6. **🆕 Conditional Routing**: Routes to business service (based on messageSource header)

7. **🆕 HTTP Delivery**: POST to business service at `localhost:8082/business/submit`

### Monitoring Endpoints

#### 🔀 Conditional Routing Logic

```http

GET /actuator/health```java

# Application health status// CDM Output Processing with Conditional Routing

from(cdmOutputEndpoint)

GET /actuator/metrics      .choice()

# Application metrics        .when(header("messageSource").isEqualTo("KAFKA_TOPIC"))

            .log("Routing Kafka-originated message to Kafka output broker")

GET /actuator/camel/routes            .to("kafka:cdm-processed-messages?brokers=localhost:9092")

# Camel route information        .otherwise()

```            .log("Routing non-Kafka message to business service")

            .to("http://localhost:8082/business/submit")

## 🔄 Message Processing Flow    .end();

````

### Real-Time Processing (HTTP/MQ Messages)

### 📋 Message Headers

`````yaml

# Real-Time FlowThe service enriches messages with comprehensive metadata headers:

Ingestion Module → HTTP Direct Call → Message-Type-Specific Route → Transformer Kamelet → CDM Output → Outbound Service

```| Header                   | Description                    | Example Values                             | Purpose                 |

| ------------------------ | ------------------------------ | ------------------------------------------ | ----------------------- |

**Example Flow for PACS.008**:| `MessageType`            | Detected message type          | `pacs.008`, `pan.001`, `unknown`           | Routing decisions       |

1. **Input**: Ingestion module calls `POST /business/api/direct/pacs-008-transform`| `RouteTarget`            | Target transformation endpoint | `direct:pacs-008-transform`                | Internal routing        |

2. **Routing**: `http-pacs-008-direct` route → `direct:pacs-008-transform`  | `ProcessingTimestamp`    | Processing start time          | `1673875200000`                            | Performance tracking    |

3. **Transformation**: `k-pacs-008-to-cdm` kamelet transforms message| `ProcessedBy`            | Processor identifier           | `MessageTypeProcessor`                     | Audit trail             |

4. **Output**: `direct:cdm-output` → `direct:route-to-outbound` → Outbound service| `messageSource`          | Message origin                 | `KAFKA_TOPIC`, `HTTP_ENDPOINT`, `MQ_QUEUE` | Conditional routing     |

5. **Result**: Transformed CDM message delivered to outbound service| `TransformationComplete` | CDM transformation status      | `true`, `false`                            | Processing state        |

| `OutputTimestamp`        | Output routing time            | `2024-01-16T10:30:00Z`                     | Performance metrics     |

### Batch Processing (CFT Messages)| `messageType`            | Business message type          | `CDM_PROCESSED`                            | Business classification |

| `processingStage`        | Current processing stage       | `CDM_TRANSFORMATION_COMPLETE`              | Pipeline tracking       |

```yaml

# Batch Flow## 🔌 API Reference

k-kafka-message-receiver → direct:kafka-message-processing → MessageTypeProcessor → Transformer Kamelet → CDM Output → Kafka Topic

```### 🏥 Health & Monitoring



**Example Flow for CFT File**:#### Application Health

1. **Input**: `k-kafka-message-receiver` consumes from Kafka topic

2. **Routing**: Routes to `direct:kafka-message-processing````http

3. **Detection**: `MessageTypeProcessor` analyzes message content  GET /actuator/health

4. **Transformation**: Routes to appropriate transformer kamelet```

5. **Output**: `direct:cdm-output` → Kafka output topic

6. **Result**: Transformed CDM message published to Kafka**Response:**



### Error Handling Flow```json

{

```yaml  "status": "UP",

# Error Flow    "components": {

Message Processing → Error Detection → direct:error-handling → Log/Dead Letter Queue    "camel": {

```      "status": "UP",

      "details": { "contextStatus": "Started", "version": "4.1.0" }

**Error Scenarios**:    },

- Unknown message type → `direct:unknown-message` → Error handler    "diskSpace": {

- Transformation failure → Exception handler → Error handler        "status": "UP",

- Outbound service failure → Retry logic → Error handler      "details": { "total": 250000000000, "free": 125000000000 }

    },

## 🧪 Testing    "kafka": { "status": "UP", "details": { "cluster": "localhost:9092" } }

  }

### Unit Tests}

`````

The module includes comprehensive unit tests for the MessageTypeProcessor:

#### Performance Metrics

````bash

# Run unit tests```http

mvn test -Dtest=MessageTypeProcessorTestGET /actuator/metrics

GET /actuator/camel/routes           # Camel-specific metrics

# Test methods included:GET /actuator/prometheus             # Prometheus format metrics

# - testPacs008XmlDetection()```

# - testPacs008JsonDetection()

# - testPacs009XmlDetection()### 🔗 Internal Endpoints

# - testPacs009JsonDetection()

# - testPain001XmlDetection()| Endpoint                          | Type               | Purpose                      | Source                    | Destination            |

# - testPain001JsonDetection()| --------------------------------- | ------------------ | ---------------------------- | ------------------------- | ---------------------- |

# - testCamt053XmlDetection()| `direct:kafka-message-processing` | **Primary Input**  | Dual-mode message processing | Kafka + Ingestion modules | Message Type Processor |

# - testCamt053JsonDetection()| `direct:pacs-008-transform`       | **Transform**      | PACS.008 processing          | Message router            | k-pacs-008-to-cdm      |

# - testUnknownMessageHandling()| `direct:pan-001-transform`        | **Transform**      | PAN.001 processing           | Message router            | k-pan-001-to-cdm       |

# - testEmptyMessageHandling()| `direct:unknown-message`          | **Error Handler**  | Unknown message types        | Message router            | Error logging          |

# - testNullMessageHandling()| `direct:cdm-output`               | **Output Router**  | CDM conditional routing      | Transformers              | Kafka/Distribution     |

# - testInvalidJsonHandling()| `direct:route-to-business`        | **HTTP Client**    | Business service calls       | CDM router                | Distribution service   |

```| `direct:error-handling`           | **Error Recovery** | System error processing      | All components            | k-log-tx               |



### Integration Tests  ## 🆕 CDM Persistence Integration



```bash### CDM Transformation and Persistence Flow

# Run integration tests

mvn test -Dtest=BusinessApplicationTestAfter successful transformation to CDM format, the processing module automatically handles persistence:



# Test scenarios:```yaml

# - Application startup verification# CDM Processing Pipeline

# - Health check endpoint testing- Transform: pacs.008/pan.001 → CDM JSON format

# - Metrics endpoint testing  - Validate: CDM schema validation

# - Camel context validation- Route: cdmOutputEndpoint → direct:cdm-persistence

```- Persist: k-db-tx → CdmMessage entity

- Audit: Link CDM record to source message

### Manual Testing- Response: Processing completion status

````

#### Test Real-Time Processing

### CDM Persistence Features

```bash

# Test PACS.008 direct processing- **Automatic Persistence**: No manual intervention required for CDM storage

curl -X POST http://localhost:8081/business/api/direct/pacs-008-transform \- **Transaction Management**: Ensures data consistency across operations

  -H "Content-Type: application/json" \- **Relationship Tracking**: CDM objects linked to original payment messages

  -d '{"messageType":"pacs.008","payload":"<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\"><FIToFICstmrCdtTrf><GrpHdr><MsgId>TEST-001</MsgId></GrpHdr></FIToFICstmrCdtTrf></Document>"}'- **Error Isolation**: CDM persistence failures don't affect message processing

- **Audit Trail**: Complete processing history maintained

# Test general processing with type detection

curl -X POST http://localhost:8081/business/api/direct/kafka-message-processing \### CDM Database Schema

  -H "Content-Type: application/json" \

  -d '{"payload":"<Document><pain.001>...</pain.001></Document>"}'The processing module integrates with the `CdmMessage` entity:

```

````java

#### Test Health and Metrics// CdmMessage entity fields automatically populated

- cdmType: PAYMENT_INSTRUCTION, PAYMENT_STATUS, etc.

```bash- instructionId: Extracted from CDM payload

# Check application health- endToEndId: Transaction identifier

curl http://localhost:8081/actuator/health- amount/currency: Payment amount information

- debtorInfo/creditorInfo: Party information

# Get processing metrics  - processingDate: Transformation timestamp

curl http://localhost:8081/actuator/metrics- sourceMessageId: Link to original ReceivedMessage

````

# Get Camel route information

curl http://localhost:8081/actuator/camel/routes### CDM Output Configuration

````

```properties

## 🚀 Running the Service# CDM Output Endpoint Configuration

cdmOutputEndpoint=direct:cdm-persistence

### Local Development

# CDM Persistence Settings

```bash  processing.cdm.persistence.enabled=true

# Start the business serviceprocessing.cdm.persistence.auto-link=true

mvn spring-boot:runprocessing.cdm.persistence.validation.enabled=true

````

# With specific profile

mvn spring-boot:run -Dspring-boot.run.profiles=dev## 📊 Monitoring & Observability

# Background mode### 🔍 Logging Configuration

mvn spring-boot:run &

````#### Log Levels



### Docker Deployment```properties

# Application Logging

```bashlogging.level.com.pixel.v2.business=INFO

# Build Docker imagelogging.level.org.apache.camel=INFO

mvn clean packagelogging.level.org.springframework.kafka=WARN

docker build -t pixel-v2/business .

# Performance Monitoring

# Run containerlogging.level.com.pixel.v2.business.MessageTypeProcessor=DEBUG

docker run -p 8081:8081 \logging.level.kamelet=INFO

  -e SPRING_PROFILES_ACTIVE=prod \

  -e KAFKA_BROKERS=kafka:9092 \# Error Tracking

  pixel-v2/businesslogging.level.com.pixel.v2.business.error=ERROR

````

### Production Deployment #### 📝 Structured Log Messages

````bash| Component          | Log Pattern                                      | Example                                                      |

# Using environment variables| ------------------ | ------------------------------------------------ | ------------------------------------------------------------ |

export KAFKA_BROKERS=kafka-cluster:9092| **Main Processor** | `[BUSINESS-MAIN] {action}: {details}`            | `[BUSINESS-MAIN] Received message from: KAFKA_TOPIC`         |

export OUTBOUND_SERVICE_URL=http://outbound-service:8082/submit| **Type Detection** | `[MSG-TYPE] Detected {type}, routing to {route}` | `[MSG-TYPE] Detected pacs.008, routing to k-pacs-008-to-cdm` |

java -jar business-1.0.1-SNAPSHOT.jar| **Transformation** | `[TRANSFORM] {kamelet}: {status}`                | `[TRANSFORM] k-pacs-008-to-cdm: SUCCESS`                     |

```| **CDM Output**     | `[CDM-OUTPUT] Routed to {destination}`           | `[CDM-OUTPUT] Routed to Distribution Service`                |

| **Error Handler**  | `[ERROR] {component}: {error}`                   | `[ERROR] Transformation: Invalid XML structure`              |

## 📊 Monitoring & Observability

### 📈 Performance Metrics

### Key Metrics

#### Available Metrics (Actuator)

- **Message Processing Rate**: Messages processed per second

- **Message Type Distribution**: Breakdown by PACS.008, PACS.009, PAIN.001, CAMT.053```http

- **Transformation Success Rate**: Successful CDM transformationsGET /actuator/metrics/business.messages.processed.total

- **Error Rate**: Processing and transformation errorsGET /actuator/metrics/business.transformation.duration

- **Route Performance**: Latency by processing route (HTTP vs Kafka)GET /actuator/metrics/business.routing.decisions

````

### Logging Strategy

| Metric Category | Metrics | Description |

````properties| ------------------ | ------------------------------------------------------ | ------------------------------ |

# Logging Configuration| **Processing**     | `messages.processed.total`, `processing.duration`      | Message throughput and latency |

logging.level.com.pixel.v2.business=INFO| **Transformation** | `transformation.success.rate`, `transformation.errors` | CDM conversion performance     |

logging.level.org.apache.camel=INFO| **Routing**        | `kafka.routing.count`, `distribution.routing.count`    | Conditional routing statistics |

logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n| **Kamelets**       | `kamelet.execution.time`, `kamelet.error.rate`         | Individual kamelet performance |

```| **System**         | `memory.usage`, `cpu.utilization`                      | Resource consumption           |



**Log Categories**:## 🛡️ Error Handling & Recovery

- `[PROCESSING-MAIN]`: Main processing flow

- `[PACS-008-TRANSFORM]`: PACS.008 transformation### 🚨 Error Classification

- `[PACS-009-TRANSFORM]`: PACS.009 transformation

- `[PAIN-001-TRANSFORM]`: PAIN.001 transformation| Error Type                  | Severity   | Handler                  | Recovery Action                     |

- `[CAMT-053-TRANSFORM]`: CAMT.053 transformation| --------------------------- | ---------- | ------------------------ | ----------------------------------- |

- `[CDM-OUTPUT]`: CDM output processing| **Unknown Message Type**    | `WARNING`  | `direct:unknown-message` | Route to DLQ, manual review         |

- `[HTTP-DIRECT]`: HTTP direct integration| **JSON Parsing Errors**     | `ERROR`    | Message validator        | Return error response, log details  |

- `[ERROR-HANDLER]`: Error handling| **Transformation Errors**   | `ERROR`    | Kamelet error handler    | Retry 3x, then DLQ                  |

| **Routing Errors**          | `CRITICAL` | Circuit breaker          | Fallback routing, alert ops team    |

### Health Checks| **CDM Validation Errors**   | `ERROR`    | Schema validator         | Log validation errors, DLQ routing  |

| **External Service Errors** | `WARNING`  | HTTP client              | Retry with backoff, circuit breaker |

```json| **System Errors**           | `CRITICAL` | Global error handler     | Stop processing, alert monitoring   |

// Health check response

{### 🔄 Recovery Strategies

  "status": "UP",

  "components": {#### Circuit Breaker Pattern

    "camel": {

      "status": "UP", ```properties

      "details": {# Circuit Breaker Configuration

        "contextStatus": "Started",circuit-breaker.failure-threshold=5

        "routesTotal": 12,circuit-breaker.timeout=60s

        "routesStarted": 12circuit-breaker.half-open-calls=3

      }```

    },

    "kafka": {#### Retry Logic

      "status": "UP",

      "details": {```properties

        "brokers": "localhost:9092"# Retry Configuration

      }retry.max-attempts=3

    }retry.delay=1000ms

  }retry.backoff-multiplier=2.0

}```

````

#### Dead Letter Queue

## 🔧 Dependencies

`````properties

### Maven Dependencies# DLQ Configuration

dlq.kafka.topic=business-processing-dlq

| Dependency | Purpose | Version |dlq.retention.hours=168  # 7 days

|------------|---------|---------|dlq.max-message-size=1MB

| **spring-boot-starter** | Spring Boot framework | 3.4.1 |```

| **camel-spring-boot-starter** | Apache Camel integration | 4.1.0 |

| **camel-kafka-starter** | Kafka consumer/producer | 4.1.0 |### 📊 Error Response Format

| **camel-direct-starter** | Direct endpoint routing | 4.1.0 |

| **camel-kamelet-starter** | Kamelet integration | 4.1.0 |```json

| **camel-platform-http-starter** | HTTP endpoint support | 4.1.0 |{

| **camel-yaml-dsl** | YAML route definitions | 4.1.0 |  "errorCode": "UNKNOWN_MESSAGE_TYPE",

| **jackson-databind** | JSON processing | Latest |  "errorDescription": "Message type could not be determined from content analysis",

  "errorHandled": true,

### Runtime Dependencies  "errorTimestamp": "2024-01-16T10:30:00Z",

  "processingId": "proc-12345",

| Component | Purpose | Configuration |  "sourceEndpoint": "direct:kafka-message-processing",

|-----------|---------|---------------|  "retryable": false,

| **k-kafka-message-receiver** | Kafka message consumption | Routes to `direct:kafka-message-processing` |  "dlqRouted": true

| **Transformer Kamelets** | Message transformation | `k-pacs-008-to-cdm`, `k-pacs009-to-cdm-transformer`, etc. |}

| **Kafka Cluster** | Batch processing support | Topics: `payment-events`, `cdm-processed-messages` |```

| **Outbound Service** | Real-time message delivery | HTTP endpoint: `http://localhost:8082/outbound/submit` |

## 🧪 Testing

## 🚨 Troubleshooting

### 🔬 Test Execution

### Common Issues

#### Unit Tests

1. **Kafka Connection Failures**

   - Check Kafka broker availability: `kafka.bootstrap-servers````bash

   - Verify consumer group configuration# Run all unit tests

   - Ensure topics exist: `payment-events`, `cdm-processed-messages`mvn test



2. **Kamelet Not Found Errors**  # Test specific components

   - Verify transformer kamelets are deployed in technical-frameworkmvn test -Dtest=MessageTypeProcessorTest

   - Check kamelet endpoint configuration in application.propertiesmvn test -Dtest=ConditionalRouterTest

   - Ensure Camel context can discover kameletsmvn test -Dtest=KameletIntegrationTest



3. **HTTP Direct Integration Issues**# Run with coverage

   - Verify business module is running on port 8081mvn test jacoco:report

   - Check ingestion module can reach HTTP endpoints```

   - Validate endpoint paths: `/business/api/direct/*`

#### Integration Tests

4. **Message Type Detection Issues**

   - Review MessageTypeProcessor logic for supported patterns```bash

   - Check message format (XML vs JSON)# Full integration test suite

   - Verify message contains expected ISO 20022 elementsmvn verify -P integration-tests



5. **CDM Transformation Failures**# Test with embedded Kafka

   - Check transformer kamelet logs for XSLT errorsmvn test -Dspring.profiles.active=test,embedded-kafka

   - Validate input message format matches kamelet expectations

   - Verify CDM output schema compliance# Performance testing

mvn test -Dspring.profiles.active=performance

### Debugging Commands```



```bash### 📝 Test Message Examples

# Check application logs

tail -f logs/business.log#### PACS.008 Credit Transfer (XML)



# Monitor specific route processing```xml

grep "PROCESSING-MAIN" logs/business.log<?xml version="1.0" encoding="UTF-8"?>

<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">

# Check message type detection  <FIToFICstmrCdtTrf>

grep "MESSAGE-TYPE-PROCESSOR" logs/business.log    <GrpHdr>

      <MsgId>PACS008-20240116-001</MsgId>

# Monitor CDM transformations        <CreDtTm>2024-01-16T10:30:00</CreDtTm>

grep "CDM-OUTPUT" logs/business.log      <NbOfTxs>1</NbOfTxs>

    </GrpHdr>

# Check HTTP direct integration    <CdtTrfTxInf>

grep "HTTP-DIRECT" logs/business.log      <InstrId>INSTR-001</InstrId>

```      <EndToEndId>E2E-001</EndToEndId>

      <TxId>TX-001</TxId>

### Performance Tuning      <IntrBkSttlmAmt Ccy="EUR">1000.00</IntrBkSttlmAmt>

    </CdtTrfTxInf>

```properties  </FIToFICstmrCdtTrf>

# Kafka Consumer Tuning</Document>

spring.kafka.consumer.max-poll-records=500```

spring.kafka.consumer.fetch-min-size=1024

spring.kafka.consumer.fetch-max-wait=500ms#### PAN.001 Payment Status (JSON)



# Camel Route Tuning  ```json

camel.springboot.threadpool.pool-size=25{

camel.springboot.threadpool.max-pool-size=50  "CustomerPaymentStatusReport": {

camel.springboot.threadpool.queue-size=1000    "GrpHdr": {

      "MsgId": "PAN001-20240116-001",

# JVM Tuning      "CreDtTm": "2024-01-16T10:30:00",

-Xms512m -Xmx2g      "InitgPty": {

-XX:+UseG1GC        "Nm": "Test Bank",

-XX:MaxGCPauseMillis=200        "Id": { "OrgId": { "Othr": [{ "Id": "TESTBANK" }] } }

```      }

    },

---    "OrgnlPmtInfAndSts": {

      "OrgnlPmtInfId": "PMT-INFO-001",

## 📋 Quick Reference      "PmtInfSts": "ACCP"

    }

### Key Routes  }

}

| Route ID | Purpose | Input | Output |```

|----------|---------|-------|--------|

| `payment-message-processing-main` | Main processing entry | `direct:kafka-message-processing` | Dynamic routing |### 🎯 Test Scenarios

| `pacs-008-transformation` | PACS.008 → CDM | `direct:pacs-008-transform` | `direct:cdm-output` |

| `pacs-009-transformation` | PACS.009 → CDM | `direct:pacs-009-transform` | `direct:cdm-output` || Test Type             | Coverage          | Purpose                                        |

| `pain-001-transformation` | PAIN.001 → CDM | `direct:pain-001-transform` | `direct:cdm-output` || --------------------- | ----------------- | ---------------------------------------------- |

| `camt-053-transformation` | CAMT.053 → CDM | `direct:camt-053-transform` | `direct:cdm-output` || **Unit Tests**        | Component logic   | Verify individual processors and routers       |

| `cdm-output-processing` | CDM routing | `direct:cdm-output` | Kafka/Outbound || **Integration Tests** | End-to-end flows  | Validate complete message processing pipelines |

| `http-*-direct` | HTTP direct endpoints | `platform-http:/business/api/direct/*` | Transform routes || **Performance Tests** | Load testing      | Measure throughput and latency under load      |

| **Error Tests**       | Failure scenarios | Verify error handling and recovery mechanisms  |

### Message Types Supported

`````

| ISO 20022 Type | Description | Detection Patterns |

|----------------|-------------|-------------------|## 🚀 Performance & Optimization

| **PACS.008** | Customer Credit Transfer | `pacs.008`, `FIToFICstmrCdtTrf` |

| **PACS.009** | Credit Transfer Return | `pacs.009`, `FIToFICstmrCdtTrfRtr` |### 📈 Throughput Optimization

| **PAIN.001** | Payment Initiation | `pain.001`, `CstmrCdtTrfInitn` |

| **CAMT.053** | Account Statement | `camt.053`, `BkToCstmrStmt` || Component | Optimization | Recommendation |

|-----------|--------------|----------------|

### Configuration Files| **Kafka Consumer** | Batch size configuration | `max.poll.records=500`, `fetch.min.bytes=50000` |

| **Camel Routes** | Thread pool tuning | `poolSize=10`, `maxPoolSize=20` |

| File | Purpose || **HTTP Client** | Connection pooling | `maxConnections=50`, `keepAlive=true` |

|------|---------|| **XSLT Processor** | Template caching | Enable Saxon template caching |

| `application.properties` | Main configuration |

| `camel/main-business-routes.yaml` | Route definitions |### 💾 Memory Management

| `pom.xml` | Maven dependencies |

````properties

### Development Commands# JVM Heap Configuration

-Xms2g -Xmx4g

```bash  -XX:+UseG1GC

# Build and run-XX:MaxGCPauseMillis=200

mvn clean install

mvn spring-boot:run# Message Processing Limits

camel.component.kafka.max-poll-records=500

# Run testscamel.streamCaching=true

mvn testcamel.streamCaching.spoolThreshold=128000

````

# Package for deployment

mvn clean package### 📊 Performance Benchmarks

````

| Processing Type | Throughput | Latency (p95) | Memory Usage |

---|-----------------|------------|---------------|--------------|

| **Batch (Kafka)** | 1000 msg/sec | 50ms | ~200MB |

**For ingestion integration**: [Ingestion Module README](../ingestion/README.md)  | **Real-time (HTTP)** | 500 msg/sec | 25ms | ~150MB |

**For distribution integration**: [Distribution Module README](../distribution/README.md)  | **CDM Transformation** | 800 msg/sec | 75ms | ~300MB |

**For technical framework**: [Technical Framework README](../technical-framework/README.md)
## 🔧 Troubleshooting Guide

### 🚨 Common Issues

#### 1. Kamelet Integration Problems
```bash
# Verify kamelet availability
kubectl get kamelet k-pacs-008-to-cdm
kubectl get kamelet k-pan-001-to-cdm
kubectl get kamelet k-log-tx

# Check Camel context registration
curl http://localhost:8080/actuator/camel/routes
````

#### 2. Message Routing Failures

```bash
# Check message headers
curl http://localhost:8080/actuator/camel/routes/kafka-message-processing/info

# Verify conditional routing logic
grep "messageSource" /var/log/business-service.log
```

#### 3. Performance Degradation

```bash
# Monitor memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check route performance
curl http://localhost:8080/actuator/camel/routes/statistics
```

### 🔍 Debug Configuration

#### Enable Detailed Logging

```properties
# Component-specific debugging
logging.level.com.pixel.v2.business=DEBUG
logging.level.org.apache.camel.component.kafka=DEBUG
logging.level.org.apache.camel.processor=DEBUG

# Message tracing
camel.main.tracing=true
camel.main.message-history=true
```

#### Health Check Commands

```bash
# Application health
curl http://localhost:8080/actuator/health

# Camel route status
curl http://localhost:8080/actuator/camel/routes

# Message processing metrics
curl http://localhost:8080/actuator/metrics/business.messages.processed
```

## 📦 Dependencies & Integration

### 🔗 Required Kamelets

| Kamelet                    | Purpose                   | Version | Status   |
| -------------------------- | ------------------------- | ------- | -------- |
| `k-kafka-message-receiver` | Kafka batch message input | 1.0.x   | Required |
| `k-pacs-008-to-cdm`        | PACS.008 transformation   | 1.0.x   | Required |
| `k-pan-001-to-cdm`         | PAN.001 transformation    | 1.0.x   | Required |
| `k-log-tx`                 | Transaction logging       | 1.0.x   | Required |

### 🔌 System Integration

| Component               | Purpose                 | Connection Type | Endpoint                          |
| ----------------------- | ----------------------- | --------------- | --------------------------------- |
| **Ingestion Module**    | Real-time message input | Direct Camel    | `direct:kafka-message-processing` |
| **Distribution Module** | CDM message output      | HTTP REST       | `http://localhost:8082/submit`    |
| **Kafka Cluster**       | Batch processing        | Kafka Protocol  | `localhost:9092`                  |

### 📚 Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.4.1</version>
    </dependency>

    <!-- Apache Camel -->
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-spring-boot-starter</artifactId>
        <version>4.1.0</version>
    </dependency>

    <!-- Kamelet Support -->
    <dependency>
        <groupId>org.apache.camel</groupId>
        <artifactId>camel-kamelet</artifactId>
        <version>4.1.0</version>
    </dependency>
</dependencies>
```

## 🤝 Contributing

### 🛠️ Development Guidelines

1. **Code Standards**

   - Follow Spring Boot and Camel best practices
   - Use structured logging with correlation IDs
   - Implement comprehensive error handling

2. **Testing Requirements**

   - Minimum 80% code coverage
   - Unit tests for all processors and routers
   - Integration tests for end-to-end flows

3. **Documentation Standards**
   - Update API documentation for endpoint changes
   - Document configuration property changes
   - Include architecture diagrams for new flows

### 🚀 Development Workflow

```bash
# 1. Clone and setup
git clone <repository>
cd business/
mvn clean install

# 2. Run tests
mvn test
mvn verify -P integration-tests

# 3. Local development
mvn spring-boot:run -Dspring.profiles.active=dev
```

## 📋 Version History

### Current: v1.0.1-SNAPSHOT

- ✅ **Dual Input Architecture**: Kafka batch + Direct real-time processing
- ✅ **Conditional Routing**: Origin-aware message distribution
- ✅ **Enhanced Error Handling**: Circuit breaker and DLQ routing
- ✅ **Performance Optimization**: Improved throughput and latency
- ✅ **Comprehensive Monitoring**: Structured logging and metrics

### Previous Versions

- **v1.0.0**: Initial release with basic message type detection
- **v0.9.0**: Beta version with transformer kamelet integration
