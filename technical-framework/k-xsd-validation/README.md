# K-XSD Validation Kamelet

## Overview

The `k-xsd-validation` kamelet provides XML Schema Definition (XSD) validation capabilities for payment messages in Apache Camel routes. It validates XML messages against XSD schemas stored in the kamelet's resource folder and provides comprehensive error reporting.

## Features

### Core Capabilities

- **XSD Schema Validation**: Validates XML messages against XSD schemas
- **Schema Caching**: Improves performance by caching compiled schemas
- **Flexible Validation Modes**: Support for STRICT and LENIENT validation modes
- **Detailed Error Reporting**: Provides detailed validation error messages with line/column numbers
- **Exception Handling**: Throws exceptions on validation failures to stop route processing

### Performance Features

- **Schema Caching**: Compiled schemas are cached for better performance
- **Resource Management**: Efficient loading of XSD files from classpath
- **Memory Optimization**: Uses streaming validation for large XML documents

### Error Handling

- **Validation Modes**: STRICT (stops on any error/warning) or LENIENT (logs warnings but continues)
- **Detailed Messages**: Line and column number information for validation errors
- **Structured Logging**: Comprehensive logging for debugging and monitoring

## Configuration Properties

| Property               | Type    | Required | Default  | Description                                               |
| ---------------------- | ------- | -------- | -------- | --------------------------------------------------------- |
| `xsdFileName`          | string  | Yes      | -        | Name of the XSD schema file (must exist in `/xsd` folder) |
| `validationMode`       | enum    | No       | `STRICT` | Validation mode: `STRICT` or `LENIENT`                    |
| `enableDetailedErrors` | boolean | No       | `true`   | Include detailed error messages in exceptions             |
| `logValidationResult`  | boolean | No       | `true`   | Log validation success/failure messages                   |
| `namespaceAware`       | boolean | No       | `true`   | Enable namespace-aware XML parsing                        |

## XSD Schema Management

### Schema Location

All XSD schema files must be placed in the `/src/main/resources/xsd/` folder of the kamelet module.

### Supported Schema Files

The module comes with sample schemas for common ISO 20022 message types:

- `pacs.008.001.08.xsd` - PACS.008 Credit Transfer schema
- `pacs.009.001.08.xsd` - PACS.009 Return Payment schema
- `pain.001.001.09.xsd` - PAIN.001 Customer Credit Transfer Initiation schema
- `camt.053.001.08.xsd` - CAMT.053 Bank to Customer Statement schema

### Adding New Schemas

1. Place the XSD file in `/src/main/resources/xsd/` folder
2. Ensure the file follows proper naming conventions
3. Update documentation as needed

## Usage Examples

### Basic Usage in Camel Route

```java
// Java DSL
from("direct:validateXml")
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .log("XML validation successful")
    .to("direct:processValidXml");
```

```yaml
# YAML DSL
- from:
    uri: "direct:validateXml"
    steps:
      - to:
          uri: "kamelet:k-xsd-validation"
          parameters:
            xsdFileName: "pacs.008.001.08.xsd"
            validationMode: "STRICT"
            logValidationResult: true
      - log:
          message: "XML validation successful"
      - to: "direct:processValidXml"
```

### Validation with Custom Configuration

```java
from("direct:lenientValidation")
    .to("kamelet:k-xsd-validation"
        + "?xsdFileName=pain.001.001.09.xsd"
        + "&validationMode=LENIENT"
        + "&enableDetailedErrors=true")
    .log("Validation completed (warnings may exist)")
    .to("direct:continueProcessing");
```

### Error Handling

```java
from("direct:validateWithErrorHandling")
    .onException(Exception.class)
        .log("Validation failed: ${exception.message}")
        .to("direct:handleValidationError")
        .handled(true)
    .end()
    .to("kamelet:k-xsd-validation?xsdFileName=camt.053.001.08.xsd")
    .log("Validation successful")
    .to("direct:processMessage");
```

### Multiple Schema Validation

```java
// Validate different message types with different schemas
from("direct:routeByMessageType")
    .choice()
        .when(header("MessageType").isEqualTo("PACS008"))
            .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
        .when(header("MessageType").isEqualTo("PAIN001"))
            .to("kamelet:k-xsd-validation?xsdFileName=pain.001.001.09.xsd")
        .otherwise()
            .log("Unknown message type, skipping validation")
    .end()
    .to("direct:continueProcessing");
```

### Dynamic Schema Selection

```java
// Select schema based on header value
from("direct:dynamicValidation")
    .setHeader("xsdFileName", simple("${header.MessageType}.xsd"))
    .to("kamelet:k-xsd-validation")
    .log("Validation successful for ${header.MessageType}")
    .to("direct:processMessage");
```

## Integration Examples

### With Flow Processing Module

```java
@Component
public class PaymentValidationRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Global exception handler for validation failures
        onException(Exception.class)
            .log("[VALIDATION-ERROR] XML validation failed: ${exception.message}")
            .setHeader("ValidationStatus", constant("FAILED"))
            .to("direct:handleValidationError")
            .handled(false); // Let transaction rollback

        from("direct:validatePaymentMessage")
            .routeId("payment-validation-route")
            .log("[VALIDATION] Starting XML validation for message type: ${header.MessageType}")

            // Set XSD file name based on message type
            .choice()
                .when(header("MessageType").isEqualTo("pacs.008.001.08"))
                    .setProperty("xsdFile", constant("pacs.008.001.08.xsd"))
                .when(header("MessageType").isEqualTo("pain.001.001.09"))
                    .setProperty("xsdFile", constant("pain.001.001.09.xsd"))
                .otherwise()
                    .throwException(new IllegalArgumentException("Unsupported message type"))
            .end()

            // Perform XSD validation
            .to("kamelet:k-xsd-validation?xsdFileName=${exchangeProperty.xsdFile}")

            .log("[VALIDATION] ✅ XML validation successful")
            .setHeader("ValidationStatus", constant("SUCCESS"))
            .to("direct:continuePaymentProcessing");
    }
}
```

### With Ingestion Module

```yaml
# YAML route for ingestion validation
- route:
    id: "ingestion-validation-route"
    from:
      uri: "direct:validateIncomingMessage"
      steps:
        - log:
            message: "[INGESTION-VALIDATION] Validating incoming XML message"

        - setHeader:
            name: "ValidationStartTime"
            simple: "${date:now:yyyy-MM-dd HH:mm:ss.SSS}"

        # XSD validation step
        - to:
            uri: "kamelet:k-xsd-validation"
            parameters:
              xsdFileName: "{{ingestion.validation.xsd.filename}}"
              validationMode: "{{ingestion.validation.mode:STRICT}}"
              logValidationResult: true

        - log:
            message: "[INGESTION-VALIDATION] ✅ Validation successful, processing duration: ${header.ValidationDuration}ms"

        - to: "direct:processValidatedMessage"
```

## Error Handling Patterns

### Simple Error Handling

```java
from("direct:simpleValidation")
    .doTry()
        .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
        .log("Validation successful")
        .to("direct:success")
    .doCatch(Exception.class)
        .log("Validation failed: ${exception.message}")
        .to("direct:validationError")
    .end();
```

### Advanced Error Handling with Retry

```java
from("direct:validationWithRetry")
    .onException(Exception.class)
        .maximumRedeliveries(3)
        .redeliveryDelay(1000)
        .retryAttemptedLogLevel(LoggingLevel.WARN)
        .log("Validation retry attempt ${header.CamelRedeliveryCounter}")
        .handled(true)
        .to("direct:validationFailed")
    .end()
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .to("direct:validationSuccess");
```

## Configuration Properties

### Application Properties

```properties
# XSD Validation Configuration
validation.xsd.default.schema=pacs.008.001.08.xsd
validation.mode.default=STRICT
validation.logging.enabled=true
validation.detailed.errors=true

# Performance Configuration
validation.cache.enabled=true
validation.cache.max.size=100
```

### Environment-Specific Configuration

```properties
# Development
validation.mode.default=LENIENT
validation.logging.enabled=true

# Production
validation.mode.default=STRICT
validation.logging.enabled=false
validation.detailed.errors=false
```

## Monitoring and Observability

### Headers Set by Kamelet

The kamelet sets the following headers on the exchange:

- `ValidationStatus`: SUCCESS or ERROR
- `ValidationTimestamp`: ISO timestamp when validation completed
- `ValidationDuration`: Validation duration in milliseconds
- `ValidationError`: Error message (only set on failure)

### Logging

The kamelet provides structured logging at different levels:

```
[XSD-VALIDATION] Starting validation - XSD: pacs.008.001.08.xsd, Mode: STRICT
[XSD-VALIDATION] ✅ Validation successful - XSD: pacs.008.001.08.xsd, Duration: 45ms
[XSD-VALIDATION] ❌ Validation failed - XSD: pacs.008.001.08.xsd, Duration: 12ms, Error: Element 'InvalidTag' is not allowed
```

### Metrics

Monitor these key metrics:

- Validation success/failure rates
- Validation duration
- Schema cache hit rates
- Memory usage of schema cache

## Performance Considerations

### Schema Caching

- Schemas are compiled once and cached in memory
- Cache is thread-safe using ConcurrentHashMap
- Clear cache programmatically if schemas are updated

### Memory Usage

- Each compiled schema consumes memory
- Monitor cache size in production environments
- Consider cache size limits for large numbers of schemas

### Validation Performance

- XSD validation can be CPU intensive for large documents
- Consider using LENIENT mode for performance-critical paths
- Use streaming validation for very large XML documents

## Troubleshooting

### Common Issues

**Schema Not Found Error**

```
XSD schema file not found: missing-schema.xsd (looking in classpath: /xsd/missing-schema.xsd)
```

**Solution**: Ensure the XSD file exists in `src/main/resources/xsd/` folder

**Validation Error**

```
XML validation failed against schema 'pacs.008.001.08.xsd': ERROR: Element 'InvalidTag' is not allowed (Line: 15, Column: 23)
```

**Solution**: Fix the XML content to match the XSD schema requirements

**Performance Issues**

- Enable schema caching
- Use appropriate validation mode
- Monitor memory usage
- Consider schema complexity

### Debug Configuration

Enable debug logging for detailed validation information:

```properties
logging.level.com.pixel.v2.validation=DEBUG
```

## Dependencies

### Required Maven Dependencies

The kamelet requires these dependencies (included in pom.xml):

- Apache Camel Core
- Apache Camel Spring Boot Starter
- Apache Camel XML DSL
- Spring Framework
- SLF4J Logging

### Optional Dependencies

For advanced XML processing:

- Apache Camel Saxon (for XSLT processing)
- Xerces XML Parser (for advanced XML features)

## Testing

### Unit Testing

```java
@Test
public void testValidXmlValidation() {
    // Send valid XML to validation route
    template.sendBody("direct:validate", validXmlContent);

    // Verify validation success
    Exchange result = consumer.receive("mock:result", 5000);
    assertEquals("SUCCESS", result.getIn().getHeader("ValidationStatus"));
}

@Test
public void testInvalidXmlValidation() {
    // Send invalid XML to validation route
    try {
        template.sendBody("direct:validate", invalidXmlContent);
        fail("Expected validation exception");
    } catch (CamelExecutionException e) {
        assertTrue(e.getCause() instanceof XsdValidationException);
    }
}
```

### Integration Testing

Create test routes to validate the kamelet behavior in realistic scenarios with actual XSD schemas and XML messages.

## Best Practices

1. **Schema Management**: Keep XSD schemas organized and versioned
2. **Error Handling**: Always handle validation exceptions appropriately
3. **Performance**: Use schema caching and appropriate validation modes
4. **Monitoring**: Log validation results and monitor failure rates
5. **Testing**: Test with both valid and invalid XML samples
6. **Configuration**: Use environment-specific validation settings
