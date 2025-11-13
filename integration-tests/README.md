# Integration Tests Module

Global integration testing module for all PIXEL-V2 components and flows.

## Overview

This module provides comprehensive integration tests for:

- **PACS-008 Flow** - Complete message processing pipeline testing
- **Technical Framework** - Kamelet integration testing
- **Cross-Module Integration** - End-to-end workflow validation

## Test Structure

```
integration-tests/
├── src/test/java/com/pixel/v2/integration/
│   ├── Pacs008FlowIntegrationTest.java     # PACS-008 flow tests
│   └── [Future test classes]
├── src/test/resources/
│   ├── test-application.properties         # Test configuration
│   └── [Test data files]
└── pom.xml                                # Test dependencies
```

## Running Tests

### Unit Tests (Mock-based)

```bash
mvn test
```

### Integration Tests (Full pipeline)

```bash
mvn verify
```

### Specific Test Class

```bash
mvn test -Dtest=Pacs008FlowIntegrationTest
```

## PACS-008 Flow Tests

The `Pacs008FlowIntegrationTest` validates:

✅ **Complete Message Flow** - End-to-end processing pipeline  
✅ **Message Correlation** - Tracking throughout all steps  
✅ **Error Handling** - Dead letter channel and retry logic  
✅ **Multiple Messages** - Batch processing validation  
✅ **Performance** - Timing and throughput verification

### Test Scenarios

1. **Single Message Processing** - Validates complete 10-step pipeline
2. **Multiple Message Processing** - Tests batch handling (3 messages)
3. **Error Handling** - Verifies error routing and logging
4. **Correlation Tracking** - Ensures message traceability

## Test Configuration

- **Database**: H2 in-memory for isolation
- **Message Broker**: Embedded Artemis for fast testing
- **Timeouts**: Reduced for quick feedback (30s max)
- **Logging**: DEBUG level for detailed verification

## Dependencies

- **JUnit 5** - Test framework
- **Apache Camel Test** - Camel route testing support
- **Testcontainers** - Container-based integration testing
- **Awaitility** - Asynchronous testing utilities
- **Artemis JUnit** - Embedded message broker

## Adding New Tests

1. Create test class in `com.pixel.v2.integration` package
2. Extend `CamelTestSupport` for Camel route testing
3. Use `@DisplayName` for clear test descriptions
4. Add `@Timeout` for test execution limits
5. Follow naming convention: `*IntegrationTest.java`

## Best Practices

- **Isolation** - Each test is independent
- **Fast Execution** - Tests complete within 30 seconds
- **Clear Assertions** - Specific validation with meaningful messages
- **Proper Cleanup** - No state leakage between tests
- **Mock External Services** - Avoid external dependencies
