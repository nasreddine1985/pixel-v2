# PIXEL-V2 Payment Message Processing System

## Presentation Overview

---

## 📋 Table of Contents

1. [Executive Summary](#executive-summary)
2. [Project Overview](#project-overview)
3. [System Architecture](#system-architecture)
4. [Key Components](#key-components)
5. [Technical Stack](#technical-stack)
6. [Core Features](#core-features)
7. [Payment Flow Processing](#payment-flow-processing)
8. [Infrastructure & Deployment](#infrastructure--deployment)
9. [Security & Compliance](#security--compliance)
10. [Future Roadmap](#future-roadmap)

---

## 🎯 Executive Summary

**PIXEL-V2** is an enterprise-grade payment message processing system designed to handle Swiss (CH) PACS.008 payment messages with high reliability, scalability, and compliance.

### Key Highlights

- ✅ **Processing Capability**: Swiss PACS.008 payment message processing
- ✅ **Architecture**: Microservices-based with Apache Camel integration
- ✅ **Technology**: Spring Boot 3.4.1, Java 21, Apache Camel 4.1.0
- ✅ **Infrastructure**: Containerized deployment with Docker
- ✅ **Integration**: Kafka, IBM MQ, Redis, PostgreSQL, CFT (File Transfer)
- ✅ **Compliance**: ISO 20022 standards compliant

---

## 🔍 Project Overview

### What is PIXEL-V2?

PIXEL-V2 is a comprehensive payment message processing platform that:

- **Receives** payment messages from multiple sources (JMS queues)
- **Validates** messages against XSD schemas (ISO 20022 standards)
- **Transforms** messages using XSLT templates
- **Enriches** data with referential information
- **Archives** messages to NAS file systems
- **Logs** processing events to Kafka for monitoring and audit

### Business Value

- 🚀 **High Performance**: Spring caching for sub-millisecond referential lookups
- 🔒 **Reliability**: Comprehensive validation and error handling
- 📊 **Observability**: Complete audit trail via Kafka event streaming
- 🔄 **Flexibility**: Configurable flows and transformation rules
- 💾 **Compliance**: Automatic message archiving for regulatory requirements

---

## 🏗️ System Architecture

### Multi-Module Maven Project

```
PIXEL-V2 (Parent)
│
├── technical-framework/    # Reusable Camel Kamelets
├── referential/            # Configuration Service API
└── flow-ch/                # Swiss Payment Processing Flow
```

### High-Level Architecture

```
┌─────────────┐
│   JMS/MQ    │ ─────► Message Ingestion
└─────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│     Apache Camel Flow Processing        │
│  (K-MQ-Starter → K-Identification →     │
│   K-XSD-Validation → K-XSL-Transform)   │
└─────────────────────────────────────────┘
       │
       ├──────────► Spring Cache (Referential Data)
       ├──────────► PostgreSQL (Audit & Config)
       ├──────────► Kafka (Event Streaming)
       └──────────► NAS (Message Archive)
```

---

## 🧩 Key Components

### 1. Technical Framework

**Custom Apache Camel Kamelets** - Reusable integration components:

| Kamelet                  | Purpose                | Key Features                          |
| ------------------------ | ---------------------- | ------------------------------------- |
| **k-mq-starter**         | JMS queue listener     | Flow ID generation, message archiving |
| **k-identification**     | Payment identification | Spring caching, referential lookup    |
| **k-xsd-validation**     | Schema validation      | Strict ISO 20022 compliance           |
| **k-xsl-transformation** | Message transformation | XSLT-based mapping                    |
| **k-log-flow-summary**   | Audit logging          | Kafka event publishing                |
| **k-kafka-publisher**    | Event publishing       | Asynchronous event streaming          |
| **k-http-publisher**     | HTTP integration       | RESTful service calls                 |
| **k-db-tx**              | Database operations    | Transaction management                |
| **k-duplicate-check**    | Deduplication          | Duplicate message detection           |

### 2. Flow-CH (Swiss Payment Processing)

**Spring Boot Camel Application** for Swiss PACS.008 messages:

- **Input**: JMS queues (ActiveMQ)
- **Validation**: `pacs.008.001.02.ch.02.xsd`
- **Transformation**: `overall-xslt-ch-pacs008-001-02.xsl`
- **Output**: Kafka topics, NAS archive
- **Configuration**: Dynamic via referential service

### 3. Referential Service

**Configuration Management API** - Central configuration repository:

- Flow definitions (ICHSIC, SEPA, etc.)
- Country and partner mappings
- Business rules and validation parameters
- Character encoding configurations
- REST API for real-time configuration retrieval

---

## 💻 Technical Stack

### Core Technologies

| Component            | Technology   | Version |
| -------------------- | ------------ | ------- |
| **Language**         | Java         | 21      |
| **Framework**        | Spring Boot  | 3.4.1   |
| **Integration**      | Apache Camel | 4.1.0   |
| **Build Tool**       | Maven        | 3.9+    |
| **Containerization** | Docker       | Latest  |

### Infrastructure Components

| Component            | Technology           | Purpose                          |
| -------------------- | -------------------- | -------------------------------- |
| **Message Broker**   | IBM MQ               | Enterprise JMS queue management  |
| **Event Streaming**  | Apache Kafka         | Event logs & audit trail         |
| **File Transfer**    | Axway CFT            | Secure file exchange             |
| **Cache**            | Spring Cache (Redis) | High-performance caching         |
| **Database**         | PostgreSQL           | Configuration & persistence      |
| **Schema Migration** | Liquibase            | Database versioning              |
| **XML Processing**   | Saxon                | XSLT 3.0 transformations         |
| **Monitoring**       | Hawtio               | Real-time Camel route monitoring |
| **Visual Design**    | Kaoto                | Low-code Camel route designer    |

---

## ⚡ Core Features

### 1. Message Processing Pipeline

```
Input → Archive → Identify → Validate → Transform → Log → Output/CFT
```

**Each step includes:**

- Error handling and recovery
- Performance metrics collection
- Comprehensive logging
- Event publishing
- CFT file transfer integration

### 2. Caching Strategy

**Spring caching** for referential data:

- Configurable TTL (default: 3600 seconds)
- Cache-aside pattern implementation
- Automatic cache invalidation
- Fallback to service calls
- Abstraction supporting Redis, Caffeine, EhCache

### 3. Message Archiving

**NAS file system archiving**:

- Archive path: `/opt/nas/CH/IN/{flowOccurId}/`
- File pattern: `{flowCode}_{flowOccurId}_{timestamp}.txt`
- Automatic directory creation
- Audit-compliant retention

### 4. Validation & Transformation

**Strict validation**:

- ISO 20022 XSD schema validation
- Swiss PACS.008 format compliance
- Detailed error reporting

**XSLT transformation**:

- XSLT 3.0 support via Saxon
- Multiple transformation templates
- Configurable transformation modes

### 5. CFT File Transfer Integration

**Axway CFT integration**:

- Secure file exchange with external partners
- Automatic file routing and transformation
- Integration with IBM MQ and Kafka
- Support for multiple protocols (SFTP, HTTPS, PeSIT)
- Acknowledgment and tracking mechanisms
- High availability and fault tolerance

**Use cases**:

- Partner file exchange (banks, financial institutions)
- Batch payment file transmission
- Regulatory report submission
- Archive and backup file transfers

### 6. Event-Driven Architecture

**Kafka integration**:

- Processing event streaming
- Audit log publishing
- Real-time monitoring
- Error notification

---

## 🔄 Payment Flow Processing

### ICHSIC Flow Example (Switzerland IN SEPA SIC)

#### Step 1: Message Reception

```
IBM MQ Queue → K-MQ-Starter
- Generate flow occurrence ID from DB sequence
- Archive to /opt/nas/CH/IN/{flowOccurId}/
- Publish START event to Kafka
```

#### Step 2: Identification & Enrichment

```
K-Identification → Spring Cache → Referential Service
- Retrieve flow configuration (ICHSIC)
- Get country mappings (CHE - 756)
- Load partner settings
- Apply business rules
- Cache results (TTL: 1 hour)
```

#### Step 3: Validation

```
K-XSD-Validation → pacs.008.001.02.ch.02.xsd
- Validate XML structure
- Check ISO 20022 compliance
- Verify Swiss extensions
- Report validation errors
```

#### Step 4: Transformation

```
K-XSL-Transformation → overall-xslt-ch-pacs008-001-02.xsl
- Transform to target format
- Apply business rules
- Generate output message
```

#### Step 5: Logging & Output

```
K-Log-Flow-Summary → Kafka
- Publish processing summary
- Record metrics
- Archive final state
```

#### Step 6: File Transfer (Optional)

```
K-CFT-Publisher → Axway CFT
- Transfer processed messages to partners
- Automatic protocol selection
- Delivery confirmation tracking
- Integration with external systems
```

---

## 🛠️ Apache Camel & Spring Boot Deep Dive

### Apache Camel 4.1.0 - Enterprise Integration Patterns

**Why Apache Camel?**

Apache Camel is the backbone of PIXEL-V2, providing:

- **300+ Components**: Out-of-the-box connectors for JMS, HTTP, Kafka, FTP, etc.
- **Enterprise Integration Patterns**: Proven patterns for messaging, routing, and transformation
- **DSL Flexibility**: Java, XML, and YAML route definitions
- **Kamelet Support**: Reusable, configurable route templates
- **Cloud-Native**: Lightweight, optimized for containers and Kubernetes

**Key Camel Features in PIXEL-V2**:

| Feature                    | Usage in PIXEL-V2                                      |
| -------------------------- | ------------------------------------------------------ |
| **Content-Based Routing**  | Dynamic message routing based on flow configuration    |
| **Message Transformation** | XSLT, JSON, XML transformations                        |
| **Error Handling**         | Dead letter channels, retry policies, circuit breakers |
| **Parallel Processing**    | Concurrent message processing with thread pools        |
| **Aggregation**            | Message splitting and aggregation for batch processing |
| **Idempotency**            | Duplicate detection with k-duplicate-check             |

**Camel Route Example**:

```java
from("jms:queue:payment.in")
    .routeId("payment-processing")
    .to("kamelet:k-identification")
    .to("kamelet:k-xsd-validation")
    .to("kamelet:k-xsl-transformation")
    .choice()
        .when(simple("${header.requiresCFT}"))
            .to("kamelet:k-cft-publisher")
        .otherwise()
            .to("kamelet:k-kafka-publisher")
    .end()
    .to("kamelet:k-log-flow-summary");
```

### Spring Boot 3.4.1 - Modern Application Framework

**Why Spring Boot?**

Spring Boot provides the foundation for PIXEL-V2:

- **Auto-Configuration**: Zero-config setup for common use cases
- **Production-Ready**: Built-in health checks, metrics, and monitoring
- **Dependency Management**: Simplified Maven/Gradle configurations
- **Cloud-Native**: Native support for containers and cloud platforms
- **Security**: Spring Security integration for authentication and authorization

**Spring Boot Features in PIXEL-V2**:

| Feature                      | Implementation                                     |
| ---------------------------- | -------------------------------------------------- |
| **Spring Boot Starter**      | Camel Spring Boot starter for seamless integration |
| **Configuration Management** | application.properties with profiles (dev, prod)   |
| **Health Checks**            | /actuator/health endpoint for readiness/liveness   |
| **Metrics**                  | Micrometer metrics exposed for Prometheus          |
| **Database Integration**     | Spring Data JPA with PostgreSQL                    |
| **Redis Integration**        | Spring Data Redis for caching                      |
| **REST APIs**                | Spring MVC for referential service                 |

**application.properties Example**:

```properties
spring.application.name=flow-ch
server.port=8080

# Camel Configuration
camel.springboot.name=pixel-v2-flow-ch
camel.component.kafka.brokers=kafka:29092
camel.component.jms.connection-factory=#ibmMQConnectionFactory

# Redis Cache
spring.data.redis.host=redis
spring.data.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000

# PostgreSQL
spring.datasource.url=jdbc:postgresql://postgresql:5432/pixeldb
spring.datasource.username=pixel
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true
```

### Hawtio - Real-Time Camel Monitoring

**What is Hawtio?**

Hawtio is a lightweight, modular web console for managing and monitoring Java applications, with first-class support for Apache Camel.

**Hawtio in PIXEL-V2**:

- **Route Visualization**: Real-time display of all Camel routes with message flow
- **Message Tracing**: Step-by-step message journey through routes
- **Metrics Dashboard**: Performance statistics (throughput, latency, errors)
- **JMX Management**: Access to JMX beans for runtime control
- **Thread Management**: Monitor and manage thread pools
- **Log Viewing**: Centralized log access

**Key Monitoring Capabilities**:

| Feature                 | Description                             |
| ----------------------- | --------------------------------------- |
| **Route Diagram**       | Visual representation of route topology |
| **Message Browser**     | Inspect messages in queues and topics   |
| **Route Control**       | Start/stop/suspend routes dynamically   |
| **Performance Metrics** | Message rate, processing time, failures |
| **Debug Mode**          | Breakpoint-style message inspection     |
| **Plugin Architecture** | Extensible with custom plugins          |

**Access**: http://localhost:8090/hawtio (in PIXEL-V2 Docker environment)

### Kaoto - Visual Camel Route Designer

**What is Kaoto?**

Kaoto is a visual, low-code integration designer for Apache Camel routes, enabling developers and non-developers to create integration flows with drag-and-drop simplicity.

**Why Kaoto for PIXEL-V2?**

- **Visual Design**: No-code route creation with graphical interface
- **Component Catalog**: Browse and search 300+ Camel components
- **Kamelet Designer**: Create and edit custom Kamelets visually
- **Code Generation**: Automatic YAML/Java DSL generation
- **Version Control**: Export routes to Git-friendly formats
- **Learning Tool**: Understand Camel patterns through visualization

**Kaoto Features**:

| Feature                  | Benefit                                        |
| ------------------------ | ---------------------------------------------- |
| **Drag-and-Drop Canvas** | Build routes without coding                    |
| **Smart Suggestions**    | Context-aware component recommendations        |
| **Property Editor**      | Configure component properties with validation |
| **Live Preview**         | See generated code in real-time                |
| **Template Library**     | Pre-built patterns for common scenarios        |
| **Kamelet Authoring**    | Design reusable integration components         |

**Use Cases in PIXEL-V2**:

- Design new payment flow routes visually
- Create custom Kamelets for new integration requirements
- Document existing routes with visual diagrams
- Onboard new developers with visual route exploration
- Rapid prototyping of integration patterns

**Integration with Development Workflow**:

1. **Design**: Create route visually in Kaoto
2. **Export**: Generate YAML/Java DSL code
3. **Version Control**: Commit to Git
4. **Deploy**: Build with Maven and deploy to Docker
5. **Monitor**: Observe in Hawtio

---

## 📋 Technology Stack Summary

### Docker Compose Stack

**Complete containerized infrastructure**:

```yaml
Services:
  - Zookeeper # Kafka coordination
  - Kafka # Event streaming (ports 9092, 29092)
  - PostgreSQL # Database (port 5432)
  - Redis # Cache (port 6379)
  - IBM MQ # Enterprise message broker (ports 1414, 9443)
  - Referential API # Config service (port 8099)
  - Flow-CH App # Payment processor (port 8080)
  - Hawtio # Camel monitoring (port 8090)
  - Axway CFT # File transfer (port 1761)
```

### Network Architecture

```
pixel-v2-network (Bridge)
│
├─ kafka:29092 (internal)
├─ postgresql:5432
├─ redis:6379
├─ ibm-mq:1414
├─ cft:1761
├─ referential:8099
└─ flow-ch:8080
```

---

## 🔒 Security & Compliance

### Security Features

- **Authentication**: Spring Security integration
- **Encryption**: Password encryption via simple-crypto
- **Network**: Docker network isolation
- **Access Control**: Service-level permissions

### Compliance

- **ISO 20022**: Full standards compliance
- **Audit Trail**: Complete Kafka event logs
- **Data Retention**: NAS archiving for regulatory requirements
- **Validation**: Strict XSD validation

### Data Protection

- PostgreSQL for secure configuration storage
- Redis with password authentication
- IBM MQ with secure channels and credentials
- CFT with encryption and authentication
- NAS file permissions

---

## 🚀 Future Roadmap

### Short-term Enhancements

- [ ] **Performance**: Optimize Spring caching strategies
- [ ] **Monitoring**: Enhanced Prometheus metrics
- [ ] **Testing**: Increased test coverage
- [ ] **Kaoto Integration**: Visual route design environment

### Medium-term Goals

- [ ] **Multi-country**: Support for additional payment schemes
- [ ] **CFT Expansion**: Enhanced file transfer scenarios
- [ ] **Resilience**: Circuit breaker patterns with Camel
- [ ] **API Gateway**: Centralized API management

### Long-term Vision

- [ ] **AI/ML**: Fraud detection integration
- [ ] **Real-time**: Stream processing optimization
- [ ] **Cloud-Native**: Enhanced cloud deployment
- [ ] **Microservices**: Further decomposition with Camel Quarkus

---

## 📊 Project Statistics

| Metric              | Value                               |
| ------------------- | ----------------------------------- |
| **Modules**         | 3 (framework, referential, flow-ch) |
| **Custom Kamelets** | 15+ reusable components             |
| **Technologies**    | 10+ infrastructure components       |
| **Java Version**    | 21 (Latest LTS)                     |
| **Spring Boot**     | 3.4.1                               |
| **Apache Camel**    | 4.1.0                               |
| **Docker Services** | 9 containerized services (with CFT) |

---

## 🎓 Key Takeaways

1. **Modern Architecture**: Microservices + Event-driven design with Apache Camel
2. **Enterprise-grade**: Spring Boot 3.4.1 + Camel 4.1.0 production stack
3. **Visual Development**: Kaoto for low-code integration design
4. **Real-Time Monitoring**: Hawtio for comprehensive Camel route observability
5. **Enterprise Integration**: IBM MQ + Kafka + CFT for reliable messaging
6. **Compliant**: ISO 20022 standards adherence
7. **Maintainable**: Modular Kamelet-based architecture
8. **Flexible**: Configuration-driven flows with Spring Boot profiles

---

## 📞 Contact & Resources

### Project Information

- **Project Name**: PIXEL-V2
- **Version**: 1.0.1-SNAPSHOT
- **Group ID**: com.pixel.v2
- **Artifact ID**: PIXEL-V2

### Key Documentation

- [Flow-CH README](flow-ch/README.md) - Swiss payment processing details
- [Referential README](referential/README.md) - Configuration service API
- [Docker README](docker/README.md) - Infrastructure setup
- [Scripts](scripts/) - Deployment & testing scripts

---

## 🙏 Thank You!

**PIXEL-V2** - Enterprise Payment Processing, Simplified.

_Questions?_
