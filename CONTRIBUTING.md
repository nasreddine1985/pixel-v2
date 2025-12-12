# Contributing to PIXEL-V2

Thank you for your interest in contributing to the PIXEL-V2 Payment Message Processing System! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Contribution Workflow](#contribution-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Security](#security)

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment. Please:

- Use welcoming and inclusive language
- Respect differing viewpoints and experiences
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other contributors

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- **Java Development Kit (JDK) 21** or higher
- **Apache Maven 3.9** or higher
- **Git** for version control
- **Oracle Database 19c+** or **PostgreSQL 13+** for database operations
- **IBM MQ** for message queue integration (for full testing)
- **Apache Kafka** for event streaming (for full testing)
- **Docker & Docker Compose** for local infrastructure setup

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/pixel-v2.git
   cd pixel-v2
   ```
3. Add the original repository as upstream:
   ```bash
   git remote add upstream https://github.com/nasreddine1985/pixel-v2.git
   ```

## Development Setup

### Initial Build

```bash
# Install all dependencies and build modules
mvn clean install

# Build without running tests (faster)
mvn clean install -DskipTests
```

### Environment Configuration

#### Database Setup

Choose Oracle Database or PostgreSQL for local development:

**For Oracle Database:**

```properties
# Oracle Database Configuration
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

**For PostgreSQL:**

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/pixel_v2_dev
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

#### Complete Application Configuration

Create or update `application-dev.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api/v1

# Database Configuration (choose Oracle or PostgreSQL)
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Camel Configuration
camel.springboot.name=pixel-v2-ingestion
camel.routes.yaml.enabled=true

# MQ Configuration (optional for development)
ingestion.mq.host=localhost
ingestion.mq.port=1414
ingestion.mq.queue.manager=QM1
ingestion.mq.channel=DEV.ADMIN.SVRCONN

# Kafka Configuration (optional for development)
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=pixel-v2-ingestion
spring.kafka.consumer.auto-offset-reset=earliest

# Logging Configuration
logging.level.com.pixel.v2=DEBUG
logging.level.org.apache.camel=INFO
logging.level.org.springframework=INFO
```

### Docker Infrastructure Setup

Start required infrastructure services using Docker Compose:

```bash
# Start all infrastructure services
docker-compose up -d

# Or start specific services
docker-compose up -d oracle-db kafka zookeeper
```

### Running in Development Mode

```bash
# Terminal 1: Start the ingestion service
cd ingestion
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2: Start the business service
cd business
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 3: Start the distribution service
cd distribution
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Project Structure

### Modules Overview

#### Main Application Modules

- **`ingestion/`** - Message ingestion and intelligent routing service
- **`business/`** - Core payment processing and business logic
- **`distribution/`** - Message distribution and delivery service
- **`technical-framework/`** - Reusable Kamelet components library

#### Technical Framework Kamelets

Located in `technical-framework/` directory:

- **`k-mq-message-receiver/`** - IBM MQ message integration
- **`k-http-starter/`** - REST API message reception
- **`k-kafka-starter/`** - Kafka message consumption
- **`k-cft-starter/`** - CFT file-based message processing
- **`k-db-tx/`** - Unified database transaction operations
- **`k-log-tx/`** - Centralized transaction logging
- **`k-referentiel-data-loader/`** - Reference data enrichment

#### Message Transformation Kamelets

- **`k-pacs008-to-cdm-transformer/`** - ISO 20022 PACS.008 to CDM
- **`k-pacs009-to-cdm-transformer/`** - ISO 20022 PACS.009 to CDM
- **`k-pain001-to-cdm-transformer/`** - ISO 20022 PAIN.001 to CDM
- **`k-camt053-to-cdm-transformer/`** - ISO 20022 CAMT.053 to CDM

### Key Technologies

- **Apache Camel 4.1.0** - Integration framework and message routing
- **Spring Boot 3.4.1** - Application framework and microservices
- **Spring Framework 6.2.1** - Core dependency injection and configuration
- **Jakarta Persistence API** - Data persistence and ORM
- **Hibernate 6.2.7.Final** - JPA implementation
- **Saxon XSLT Processor 12.3** - XML transformations
- **Oracle Database 19c+** or **PostgreSQL 13+** - Primary database
- **Apache Kafka** - Event streaming and asynchronous processing
- **IBM MQ** - Enterprise message queuing
- **Java 21** - Programming language and runtime

## Contribution Workflow

### 1. Choose an Issue

- Check the [Issues](https://github.com/nasreddine1985/pixel-v2/issues) page
- Look for issues labeled `good first issue` or `help wanted`
- Comment on the issue to express your interest

### 2. Create a Feature Branch

```bash
# Update your main branch
git checkout main
git pull upstream main

# Create a feature branch
git checkout -b feature/your-feature-name
```

### 3. Make Your Changes

Follow the coding standards and ensure your changes:

- Are well-tested
- Follow the existing code style
- Include appropriate documentation
- Don't break existing functionality

### 4. Commit Your Changes

Use conventional commit messages:

```bash
# Examples of good commit messages
git commit -m "feat(ingestion): add support for PAN002 message type"
git commit -m "fix(k-validation): resolve null pointer in schema validation"
git commit -m "docs(readme): update installation instructions"
git commit -m "test(k-mq-message-receiver): add integration tests for error scenarios"
```

**Commit Types:**

- `feat`: New features
- `fix`: Bug fixes
- `docs`: Documentation changes
- `test`: Test additions or modifications
- `refactor`: Code refactoring
- `perf`: Performance improvements

### 5. Push and Create Pull Request

```bash
# Push to your fork
git push origin feature/your-feature-name

# Create a Pull Request on GitHub
```

## Coding Standards

### Java Code Style

- **Indentation**: Use 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Naming Conventions**:
  - Classes: `PascalCase` (e.g., `PaymentIngestionRoute`)
  - Methods/Variables: `camelCase` (e.g., `processPaymentMessage`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT_MS`)
  - Packages: `lowercase.with.dots` (e.g., `com.pixel.v2.ingestion`)

### DTO, Request, and Response Object Naming

Follow these specific naming conventions for data transfer objects, request objects, and response objects:

#### DTO (Data Transfer Object) Naming

- **Format**: `{Entity}Dto` (PascalCase with `Dto` suffix)
- **Purpose**: Represent data structures for internal transfers between layers
- **Package**: Place in `dto` subpackage (e.g., `com.pixel.v2.ingestion.dto`)

```java
✅ Good Examples:
- PaymentDto              (payment data transfer)
- PaymentMessageDto       (payment message data)
- ValidationResultDto     (validation result data)
- EnrichmentDataDto       (enrichment data transfer)
- IdempotenceCheckDto     (idempotence check data)

❌ Bad Examples:
- Payment                 (missing Dto suffix)
- PaymentDataTransfer     (verbose, use Dto suffix)
- PaymentTO              (use Dto instead of TO)
- PaymentData            (ambiguous, could be entity)
```

#### Request Object Naming

- **Format**: `{Operation}{Entity}Request` (PascalCase with `Request` suffix)
- **Purpose**: Represent incoming API or service requests
- **Package**: Place in `request` subpackage (e.g., `com.pixel.v2.ingestion.api.request`)

```java
✅ Good Examples:
- ProcessPaymentRequest       (process payment operation)
- ValidateMessageRequest      (validate message operation)
- EnrichDataRequest          (enrich data operation)
- CheckIdempotenceRequest    (check idempotence operation)
- PublishEventRequest        (publish event operation)
- CreatePaymentRequest       (create payment operation)
- UpdatePaymentStatusRequest (update status operation)

❌ Bad Examples:
- PaymentRequest             (missing operation context)
- ProcessRequest             (missing entity context)
- PaymentProcessingReq       (abbreviated, use full Request)
- RequestPayment             (wrong order, should end with Request)
```

#### Response Object Naming

- **Format**: `{Operation}{Entity}Response` (PascalCase with `Response` suffix)
- **Purpose**: Represent outgoing API or service responses
- **Package**: Place in `response` subpackage (e.g., `com.pixel.v2.ingestion.api.response`)

```java
✅ Good Examples:
- ProcessPaymentResponse      (process payment result)
- ValidateMessageResponse     (validation result)
- EnrichDataResponse         (enrichment result)
- CheckIdempotenceResponse   (idempotence check result)
- PublishEventResponse       (publish event result)
- GetPaymentStatusResponse   (get status result)
- ListPaymentsResponse       (list payments result)

❌ Bad Examples:
- PaymentResponse            (missing operation context)
- ProcessResponse            (missing entity context)
- PaymentProcessingResp      (abbreviated, use full Response)
- ResponsePayment            (wrong order, should end with Response)
```

#### Nested Object Naming

For complex objects with nested structures:

```java
// Request with nested objects
public class ProcessPaymentRequest {
    private PaymentDetailsDto paymentDetails;
    private ValidationOptionsDto validationOptions;
    private ProcessingConfigDto processingConfig;
}

// Response with nested objects
public class ProcessPaymentResponse {
    private PaymentResultDto paymentResult;
    private List<ValidationErrorDto> validationErrors;
    private ProcessingStatusDto processingStatus;
}
```

#### Package Structure Best Practices

```
com.pixel.v2.ingestion/
├── dto/
│   ├── PaymentDto.java
│   ├── ValidationResultDto.java
│   └── EnrichmentDataDto.java
├── api/
│   ├── request/
│   │   ├── ProcessPaymentRequest.java
│   │   ├── ValidateMessageRequest.java
│   │   └── EnrichDataRequest.java
│   └── response/
│       ├── ProcessPaymentResponse.java
│       ├── ValidateMessageResponse.java
│       └── EnrichDataResponse.java
└── model/
    ├── Payment.java           (entity/domain model)
    ├── PaymentMessage.java    (domain model)
    └── ValidationRule.java    (domain model)
```

#### Annotation Best Practices

Use appropriate annotations for validation and documentation:

```java
// Request Object Example
@Valid
public class ProcessPaymentRequest {

    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentDetailsDto paymentDetails;

    @Valid
    private ValidationOptionsDto validationOptions;

    // Constructors, getters, setters
}

// Response Object Example
public class ProcessPaymentResponse {

    @NotNull
    private PaymentResultDto result;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationErrorDto> errors;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    // Constructors, getters, setters
}

// DTO Object Example
public class PaymentDto {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3-letter ISO code")
    private String currency;

    // Constructors, getters, setters
}
```

#### Builder Pattern for Complex Objects

For complex request/response objects, consider using the Builder pattern:

```java
// Request with Builder
public class ProcessPaymentRequest {
    private final PaymentDetailsDto paymentDetails;
    private final ValidationOptionsDto validationOptions;
    private final ProcessingConfigDto processingConfig;

    private ProcessPaymentRequest(Builder builder) {
        this.paymentDetails = builder.paymentDetails;
        this.validationOptions = builder.validationOptions;
        this.processingConfig = builder.processingConfig;
    }

    public static class Builder {
        private PaymentDetailsDto paymentDetails;
        private ValidationOptionsDto validationOptions;
        private ProcessingConfigDto processingConfig;

        public Builder paymentDetails(PaymentDetailsDto paymentDetails) {
            this.paymentDetails = paymentDetails;
            return this;
        }

        public Builder validationOptions(ValidationOptionsDto options) {
            this.validationOptions = options;
            return this;
        }

        public Builder processingConfig(ProcessingConfigDto config) {
            this.processingConfig = config;
            return this;
        }

        public ProcessPaymentRequest build() {
            return new ProcessPaymentRequest(this);
        }
    }

    // Getters only (immutable)
}
```

### Code Organization

```java
// Example class structure
@Component
public class PaymentProcessor {

    // Constants first
    private static final String DEFAULT_ROUTE_ID = "payment-processor";

    // Fields
    private final PaymentValidator validator;
    private final MessagePublisher publisher;

    // Constructor
    public PaymentProcessor(PaymentValidator validator, MessagePublisher publisher) {
        this.validator = validator;
        this.publisher = publisher;
    }

    // Public methods
    public void processPayment(PaymentMessage message) {
        // Implementation
    }

    // Private methods
    private void validateMessage(PaymentMessage message) {
        // Implementation
    }
}
```

### Camel Route Guidelines

- **Route IDs**: Use descriptive, kebab-case identifiers
- **Error Handling**: Always implement comprehensive error handling
- **Logging**: Include meaningful log messages at appropriate levels
- **Configuration**: Externalize configuration properties

```java
// Example Camel route
@Component
public class PaymentIngestionRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:k-mq-message-receiver")
            .routeId("payment-mq-ingestion")
            .log(INFO, "Processing MQ payment message: ${header.JMSMessageID}")
            .onException(ValidationException.class)
                .handled(true)
                .log(ERROR, "Validation failed: ${exception.message}")
                .to("kamelet:error-handler")
            .end()
            .to("kamelet:k-referentiel-data-loader")
            .choice()
                .when(header("validation.passed").isEqualTo(true))
                    .to("kafka:{{kafka.topic.processed}}")
                .otherwise()
                    .to("kafka:{{kafka.topic.rejected}}")
            .end();
    }
}
```

### Kamelet Naming Best Practices

Kamelets are reusable integration components in Apache Camel. Follow these naming conventions and best practices for consistency across the PIXEL-V2 ecosystem:

#### Kamelet Module Naming

- **Format**: `k-{function}-{target}` (lowercase with hyphens)
- **Prefix**: Always start with `k-` to identify kamelet modules
- **Function**: Describe the primary operation (e.g., `receipt`, `validation`, `loader`)
- **Target**: Optional suffix for specificity (e.g., `to-cdm`, `from-mq`)

**Examples:**

```
✅ Good Examples:
- k-mq-message-receiver (receives messages from MQ)
- k-http-starter (receives messages from HTTP API)
- k-referentiel-data-loader      (loads reference data)
- k-pacs008-to-cdm-transformer   (transforms PACS.008 to CDM)

❌ Bad Examples:
- MQReceiptKamelet      (not following naming pattern - use k-mq-message-receiver)
- k_validation          (underscores instead of hyphens)
- kamelet-validation    (redundant kamelet prefix)
- k-val                 (too abbreviated)
```

#### Kamelet YAML Definition Naming

- **File Name**: Should match the module name (e.g., `k-mq-message-receiver.yaml`)
- **Kamelet Name**: Use the same name as the file without extension
- **Metadata Name**: Must match the kamelet name exactly

```yaml
# File: k-mq-message-receiver/src/main/resources/kamelets/k-mq-message-receiver.yaml
apiVersion: camel.apache.org/v1alpha1
kind: Kamelet
metadata:
  name: k-mq-message-receiver # Must match file name
  labels:
    camel.apache.org/kamelet.type: source
spec:
  definition:
    title: "MQ Message Receiver Kamelet"
    description: "Receives and persists messages from IBM MQ queues"
```

#### Route ID Conventions in Kamelets

- **Format**: `{kamelet-name}-{operation}` (kebab-case)
- **Consistency**: Align with the kamelet name
- **Descriptive**: Clearly indicate the route's purpose

```yaml
# Good route IDs
routes:
  - id: k-mq-message-receiver-main # Main processing route
  - id: k-mq-message-receiver-error-handler # Error handling route
  - id: k-validation-schema-check # Schema validation route
  - id: k-referentiel-data-loader-enrich-data # Data enrichment route
```

#### Property Naming in Kamelets

- **Prefix**: Use the kamelet name as prefix for unique identification
- **Case**: Use camelCase for property names
- **Clarity**: Be explicit about the property's purpose

```yaml
# Property naming examples
properties:
  # MQ Message Receiver Kamelet
  mqMessageReceiverQueue: # Clear and specific
    type: string
    title: MQ Queue Name
  mqMessageReceiverConnectionFactory:
    type: string
    title: "Connection Factory Reference"

  # Validation Kamelet
  validationStrictMode:
    type: boolean
    title: "Enable Strict Validation"
  validationSchemaPath:
    type: string
    title: "XSD Schema File Path"
```

#### Kamelet Categories and Labels

Use consistent labels to categorize kamelets:

```yaml
metadata:
  labels:
    camel.apache.org/kamelet.type: "source|sink|action"
    pixel.v2/category: "receipt|transformation|validation|persistence"
    pixel.v2/message-type: "pacs008|pan001|cdm|generic"
    pixel.v2/channel: "mq|api|file|kafka"

# Examples:
# Receipt Kamelets
camel.apache.org/kamelet.type: "source"
pixel.v2/category: "receipt"
pixel.v2/channel: "mq"

# Transformation Kamelets
camel.apache.org/kamelet.type: "action"
pixel.v2/category: "transformation"
pixel.v2/message-type: "pacs008"

# Validation Kamelets
camel.apache.org/kamelet.type: "action"
pixel.v2/category: "validation"
```

#### Documentation Requirements

Each kamelet must include:

1. **Clear Description**: Explain the kamelet's purpose and use case
2. **Property Documentation**: Document all configurable properties
3. **Usage Examples**: Provide practical implementation examples
4. **Dependencies**: List required external systems or libraries

```yaml
spec:
  definition:
    title: "MQ Message Receiver Kamelet"
    description: |
      Receives payment messages from IBM MQ queues and persists them 
      to the database using JPA. Supports transactional processing 
      and automatic retry on connection failures.

    required:
      - mqMessageReceiverQueue
      - mqMessageReceiverConnectionFactory

    properties:
      mqMessageReceiverQueue:
        type: string
        title: "MQ Queue Name"
        description: "The IBM MQ queue name to receive messages from"
        example: "PAYMENT.INPUT.QUEUE"
```

#### Integration Patterns

Follow these patterns when creating kamelets:

**Source Kamelets (Receipt):**

- Always include persistence logic
- Implement proper error handling
- Add correlation ID generation
- Include message metadata enrichment

**Action Kamelets (Processing):**

- Maintain message immutability where possible
- Implement validation with clear error messages
- Support both sync and async processing modes
- Include comprehensive logging

**Sink Kamelets (Publishing):**

- Handle connection failures gracefully
- Implement retry mechanisms
- Support message transformation
- Include delivery confirmation

#### Testing Kamelet Names

Test files should follow the kamelet naming:

```
src/test/java/
└── com/pixel/v2/kamelets/
    ├── KMqMessageReceiverTest.java           # Unit tests
    ├── KMqMessageReceiverIntegrationTest.java # Integration tests
    └── KMqMessageReceiverRouteTest.java      # Route-specific tests
```

### Maven Configuration

- **Dependencies**: Add new dependencies to the appropriate module's `pom.xml`
- **Versions**: Use properties for version management in the parent POM
- **Scope**: Use appropriate dependency scopes (`compile`, `test`, `provided`)

## Testing Guidelines

### Test Structure

Each module should have comprehensive tests:

```
src/
├── main/java/
└── test/java/
    ├── integration/     # Integration tests
    ├── unit/           # Unit tests
    └── fixtures/       # Test data and fixtures
```

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @Mock
    private PaymentValidator validator;

    @Mock
    private MessagePublisher publisher;

    @InjectMocks
    private PaymentProcessor processor;

    @Test
    void shouldProcessValidPayment() {
        // Given
        PaymentMessage message = createTestPayment();
        when(validator.validate(message)).thenReturn(ValidationResult.success());

        // When
        processor.processPayment(message);

        // Then
        verify(publisher).publish(message);
    }
}
```

### Integration Tests

```java
@CamelSpringBootTest
@SpringBootTest
@UseAdviceWith
@ActiveProfiles("integration-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MainIngestionRoutesIntegrationTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @BeforeEach
    void setUp() throws Exception {
        // Configure route interceptions for testing
        AdviceWith.adviceWith(camelContext, "payment-ingestion-orchestrator", a -> {
            a.interceptSendToEndpoint("direct:processing-module")
                .skipSendToOriginalEndpoint()
                .to("mock:processing-module-result");
            a.interceptSendToEndpoint("direct:kafka-publisher")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-publisher-result");
        });

        // CRITICAL: Start routes explicitly when using @UseAdviceWith
        camelContext.getRouteController().startAllRoutes();
    }

    @Test
    @DisplayName("Test CFT message routing to Kafka for asynchronous processing")
    void testCftMessageRouting() throws Exception {
        // Test implementation with proper route startup
    }
}
```

### Test Configuration

#### Test Profiles

Use appropriate test profiles for different testing scenarios:

- **`integration-test`** - Full integration testing with mocked external dependencies
- **`test`** - Unit testing with minimal Spring context
- **`dev`** - Development testing with local infrastructure

#### Test Properties

Create `application-integration-test.properties`:

```properties
# Camel Configuration for Testing
camel.springboot.name=payment-ingestion-test
camel.routes.yaml.enabled=true

# Disable problematic auto-configurations
camel.kamelet.configuration.enabled=false
camel.yaml.dsl.enabled=false

# Test Logging
logging.level.com.pixel.v2=DEBUG
logging.level.org.apache.camel=INFO
logging.level.ROOT=WARN

# Database Configuration for Testing
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

### Test Data Management

- Use **test fixtures** for consistent test data
- **Mock external dependencies** (databases, web services, queues) using @MockBean
- Create **realistic test scenarios** based on actual payment message types (PACS.008, PAIN.001, etc.)
- Use **@UseAdviceWith** for Camel route testing with explicit route startup

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl ingestion

# Run integration tests only
mvn test -Dtest=*IntegrationTest

# Run with coverage report
mvn test jacoco:report
```

## Documentation

### Code Documentation

- **Javadoc**: Document all public classes and methods
- **Inline Comments**: Explain complex business logic
- **README Files**: Each module should have its own README

```java
/**
 * Processes payment messages through the ingestion pipeline.
 *
 * This processor handles validation, enrichment, and publishing
 * of payment messages from various sources (MQ, API, File).
 *
 * @author Your Name
 * @since 1.0.0
 */
@Component
public class PaymentProcessor {

    /**
     * Processes a payment message through the complete pipeline.
     *
     * @param message the payment message to process
     * @throws ValidationException if the message fails validation
     * @throws PublishingException if publishing to Kafka fails
     */
    public void processPayment(PaymentMessage message) {
        // Implementation
    }
}
```

### Configuration Documentation

Document all configuration properties:

```properties
# Payment processing configuration
payment.validation.strict=true
# Whether to enforce strict validation rules (true/false)

payment.kafka.topic.processed=payments-processed
# Kafka topic for successfully processed payments

payment.idempotence.window.hours=24
# Time window in hours for duplicate detection (1-168)
```

## Pull Request Process

### Before Submitting

1. **Rebase** your branch on the latest main:

   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run the full test suite**:

   ```bash
   mvn clean verify
   ```

3. **Check code quality**:
   ```bash
   mvn checkstyle:check spotbugs:check
   ```

### Pull Request Template

When creating a PR, include:

```markdown
## Description

Brief description of changes

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Refactoring

## Testing

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist

- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes (or properly documented)
```

### Review Process

1. **Automated Checks**: CI/CD pipeline runs tests and quality checks
2. **Peer Review**: At least one maintainer reviews the code
3. **Discussion**: Address any feedback or requested changes
4. **Approval**: Maintainer approves and merges the PR

## Issue Reporting

### Bug Reports

Use the bug report template and include:

- **Environment**: OS, Java version, Maven version
- **Steps to Reproduce**: Clear, numbered steps
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Logs**: Relevant log excerpts or stack traces
- **Configuration**: Relevant configuration files (sanitized)

### Feature Requests

- **Use Case**: Describe the business need
- **Proposed Solution**: Your suggested approach
- **Alternatives**: Other solutions you considered
- **Impact**: Who would benefit from this feature

### Security Issues

**Do not create public issues for security vulnerabilities.** Instead:

1. Email security concerns to the project maintainers
2. Include detailed description and reproduction steps
3. Allow time for assessment before public disclosure

## Recent Architecture Improvements

### Intelligent Message Routing

The platform now features sophisticated routing logic based on message source and content:

- **CFT Messages**: Automatically routed to Kafka for asynchronous processing
- **HTTP/MQ Messages**: Routed to synchronous processing module
- **Smart Decision Engine**: Conditional routing based on `ReceiptChannel` header

### Enhanced Testing Framework

Recent improvements include:

- **Integration Test Infrastructure**: Complete test setup with `@UseAdviceWith` and proper route startup
- **Mock Endpoint Strategy**: Comprehensive mocking of external dependencies
- **Test Profiles**: Dedicated `integration-test` profile for consistent testing
- **Route Interception**: Advanced route interception for isolated testing

### Modular Kamelet Architecture

The technical framework has been restructured into focused, reusable components:

- **Source Kamelets**: Message reception from various channels
- **Action Kamelets**: Message transformation and processing
- **Sink Kamelets**: Message delivery and persistence

## Development Best Practices

### Performance Considerations

- **Asynchronous Processing**: Use Camel's asynchronous processing features for high-volume scenarios
- **Memory Management**: Be mindful of memory usage in message processing
- **Connection Pooling**: Use connection pools for database and MQ connections
- **Monitoring**: Add appropriate metrics and health checks

### Error Handling

- **Comprehensive Coverage**: Handle all expected error scenarios
- **Meaningful Messages**: Provide clear error messages and codes
- **Dead Letter Queues**: Use DLQs for unprocessable messages
- **Circuit Breakers**: Implement circuit breakers for external service calls

### Security

- **Input Validation**: Validate all external inputs
- **Credential Management**: Never hardcode credentials
- **Audit Logging**: Log security-relevant events
- **Data Privacy**: Handle PII appropriately

## Common Development Issues & Solutions

### Camel Route Testing Issues

**Problem**: `IllegalArgumentException: Cannot advice route as there are no routes`

**Solution**:

```java
@BeforeEach
void setUp() throws Exception {
    // Configure AdviceWith first
    AdviceWith.adviceWith(camelContext, "route-id", a -> {
        // Your advice configuration
    });

    // CRITICAL: Explicitly start routes
    camelContext.getRouteController().startAllRoutes();
}
```

### Integration Test Configuration

**Problem**: Kamelet auto-configuration conflicts in tests

**Solution**: Use dedicated test properties:

```properties
# application-integration-test.properties
camel.kamelet.configuration.enabled=false
camel.yaml.dsl.enabled=false
```

### Database Connection Issues

**Problem**: Oracle driver not found or connection failures

**Solutions**:

1. Ensure Oracle JDBC driver is properly configured:

   ```xml
   <dependency>
       <groupId>com.oracle.database.jdbc</groupId>
       <artifactId>ojdbc11</artifactId>
       <version>23.2.0.0</version>
   </dependency>
   ```

2. For local development, use Docker:
   ```bash
   docker run -d -p 1521:1521 -e ORACLE_PASSWORD=mypassword gvenzl/oracle-xe
   ```

### Maven Build Issues

**Problem**: Module dependency resolution failures

**Solution**: Build in correct order:

```bash
# Build technical framework first
mvn clean install -pl technical-framework

# Then build application modules
mvn clean install -pl ingestion,business,distribution
```

## Getting Help

### Communication Channels

- **GitHub Issues**: Technical questions and bug reports
- **GitHub Discussions**: General questions and community discussion
- **Documentation**: Check the project wiki and module READMEs

### Maintainers

Current project maintainers:

- **Nasreddine Abassi** ([@nasreddine1985](https://github.com/nasreddine1985)) - Project Lead

### Resources

- [Apache Camel Documentation](https://camel.apache.org/manual/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/guides/)

## Recent Changes (v1.0.1-SNAPSHOT)

### Architecture Enhancements

- ✅ **Modular Structure**: Separated concerns into `ingestion`, `business`, `distribution`, and `technical-framework` modules
- ✅ **Intelligent Routing**: Implemented smart message routing based on source channel
- ✅ **Kamelet Framework**: Comprehensive library of reusable integration components

### Testing Improvements

- ✅ **Integration Testing**: Complete test infrastructure with proper route startup management
- ✅ **Mock Strategies**: Advanced endpoint mocking for isolated testing
- ✅ **Test Profiles**: Dedicated configuration for consistent test environments

### Technology Updates

- ✅ **Spring Boot 3.4.1**: Latest framework version with enhanced features
- ✅ **Apache Camel 4.1.0**: Modern integration patterns and improved performance
- ✅ **Java 21**: Latest LTS version with performance improvements
- ✅ **Oracle Database Support**: Enterprise-grade database integration

### Developer Experience

- ✅ **Enhanced Documentation**: Comprehensive setup and troubleshooting guides
- ✅ **Docker Integration**: Streamlined local development environment
- ✅ **Code Quality**: Improved coding standards and best practices

---

**Thank you for contributing to PIXEL-V2!** Your contributions help make payment message processing more reliable and efficient for everyone.

### Quick Reference

| Component               | Purpose                     | Technology Stack                          |
| ----------------------- | --------------------------- | ----------------------------------------- |
| **Ingestion**           | Message reception & routing | Spring Boot + Camel + Oracle/PostgreSQL   |
| **Business**            | Payment processing logic    | Spring Boot + JPA + Message Validation    |
| **Distribution**        | Message delivery            | Camel + Kafka + External API Integration  |
| **Technical Framework** | Reusable Kamelets           | Apache Camel + XSLT + Database Operations |
