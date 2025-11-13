# K-XSD Validation Usage Examples

## Basic Usage from Another Route

### Java DSL Example

```java
package com.pixel.v2.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidationExampleRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Example 1: Basic XML validation
        from("direct:validatePacs008")
            .routeId("pacs008-validation-example")
            .log("[EXAMPLE] Starting PACS.008 validation")

            // Validate XML against PACS.008 schema
            .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")

            .log("[EXAMPLE] ✅ PACS.008 validation successful")
            .setBody(constant("XML validation passed"))
            .to("mock:validationSuccess");

        // Example 2: Validation with error handling
        from("direct:validateWithErrorHandling")
            .routeId("validation-with-error-handling")
            .onException(Exception.class)
                .log("[EXAMPLE] ❌ Validation failed: ${exception.message}")
                .setHeader("ErrorReason", simple("${exception.message}"))
                .to("mock:validationError")
                .handled(true)
            .end()

            .log("[EXAMPLE] Starting validation with error handling")
            .to("kamelet:k-xsd-validation?xsdFileName=pain.001.001.09.xsd&validationMode=STRICT")
            .log("[EXAMPLE] Validation completed successfully")
            .to("mock:success");

        // Example 3: Dynamic schema selection based on message type
        from("direct:dynamicSchemaValidation")
            .routeId("dynamic-schema-validation")
            .log("[EXAMPLE] Dynamic schema validation for message type: ${header.MessageType}")

            .choice()
                .when(header("MessageType").isEqualTo("PACS008"))
                    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
                .when(header("MessageType").isEqualTo("PAIN001"))
                    .to("kamelet:k-xsd-validation?xsdFileName=pain.001.001.09.xsd")
                .when(header("MessageType").isEqualTo("CAMT053"))
                    .to("kamelet:k-xsd-validation?xsdFileName=camt.053.001.08.xsd")
                .otherwise()
                    .log("[EXAMPLE] Unknown message type: ${header.MessageType}")
                    .throwException(new IllegalArgumentException("Unsupported message type"))
            .end()

            .log("[EXAMPLE] ✅ Dynamic validation successful")
            .to("mock:dynamicSuccess");

        // Example 4: Lenient validation mode
        from("direct:lenientValidation")
            .routeId("lenient-validation-example")
            .log("[EXAMPLE] Starting lenient validation (warnings allowed)")

            .to("kamelet:k-xsd-validation"
                + "?xsdFileName=pacs.008.001.08.xsd"
                + "&validationMode=LENIENT"
                + "&logValidationResult=true")

            .log("[EXAMPLE] Lenient validation completed (check logs for warnings)")
            .to("mock:lenientSuccess");

        // Example 5: Batch validation
        from("direct:batchValidation")
            .routeId("batch-validation-example")
            .log("[EXAMPLE] Processing batch of XML messages")

            .split(body())
                .log("[EXAMPLE] Validating message ${property.CamelSplitIndex}")
                .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
                .log("[EXAMPLE] Message ${property.CamelSplitIndex} validated successfully")
            .end()

            .log("[EXAMPLE] ✅ All messages in batch validated")
            .to("mock:batchSuccess");
    }
}
```

### YAML DSL Example

```yaml
# Example YAML routes using k-xsd-validation kamelet
- route:
    id: "yaml-validation-example"
    from:
      uri: "direct:validateXmlMessage"
      steps:
        - log:
            message: "[YAML-EXAMPLE] Starting XML validation"

        - to:
            uri: "kamelet:k-xsd-validation"
            parameters:
              xsdFileName: "pacs.008.001.08.xsd"
              validationMode: "STRICT"
              logValidationResult: true

        - log:
            message: "[YAML-EXAMPLE] ✅ Validation successful"

        - to: "mock:yamlSuccess"

- route:
    id: "conditional-validation"
    from:
      uri: "direct:conditionalValidation"
      steps:
        - choice:
            when:
              - simple: "${header.MessageType} == 'PACS008'"
                steps:
                  - to:
                      uri: "kamelet:k-xsd-validation"
                      parameters:
                        xsdFileName: "pacs.008.001.08.xsd"
            when:
              - simple: "${header.MessageType} == 'PAIN001'"
                steps:
                  - to:
                      uri: "kamelet:k-xsd-validation"
                      parameters:
                        xsdFileName: "pain.001.001.09.xsd"
            otherwise:
              steps:
                - log:
                    message: "No validation needed for ${header.MessageType}"

        - to: "mock:conditionalSuccess"
```

## Integration with Existing Flow Module

### Adding to Pacs008RouteBuilder

```java
// Add this to your existing Pacs008RouteBuilder.java
from("direct:validatePacs008Message")
    .routeId("pacs008-xsd-validation")
    .log("[PACS008-VALIDATION] Starting XSD validation for received message")

    // Validate against PACS.008 schema
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd&validationMode=STRICT")

    .log("[PACS008-VALIDATION] ✅ XSD validation successful")
    .setHeader("XsdValidationStatus", constant("PASSED"))

    // Continue to existing processing
    .to("direct:pacs008-persistence");
```

### Integrating with MQ Message Reception

```java
// Modify your existing MQ route to include validation
from("kamelet:k-mq-message-receiver" + "?destination={{flow.pacs008.queue.name}}"
    + "&brokerUrl={{mq.broker-url}}" + "&user={{mq.user}}"
    + "&password={{mq.password}}"
    + "&acknowledgmentModeName=CLIENT_ACKNOWLEDGE" + "&transacted=true"
    + "&concurrentConsumers={{flow.pacs008.concurrent-consumers}}"
    + "&maxConcurrentConsumers={{flow.pacs008.max-concurrent-consumers}}")
    .routeId("pacs008-message-consumer-with-validation")

    .log("[PACS008-CONSUMER] Message received, starting XSD validation")

    // Add XSD validation step before processing
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")

    .log("[PACS008-CONSUMER] XSD validation passed, continuing processing")

    // Continue with existing logic
    .setHeader("ProcessingRoute", constant("PACS008"))
    // ... rest of existing route
```

## Sample XML Messages for Testing

### Valid PACS.008 Message

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>MSG123456789</MsgId>
            <CreDtTm>2024-11-12T10:30:00.000Z</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <EndToEndId>E2E123456789</EndToEndId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="EUR">1000.00</IntrBkSttlmAmt>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
```

### Invalid PACS.008 Message (Missing Required Elements)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <!-- Missing GrpHdr - will cause validation error -->
        <CdtTrfTxInf>
            <PmtId>
                <EndToEndId>E2E123456789</EndToEndId>
            </PmtId>
            <!-- Missing IntrBkSttlmAmt - will cause validation error -->
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
```

## Configuration Examples

### Application Properties

```properties
# XSD Validation Configuration
xsd.validation.default.mode=STRICT
xsd.validation.logging.enabled=true
xsd.validation.detailed.errors=true

# Schema file mappings
xsd.schema.pacs008=pacs.008.001.08.xsd
xsd.schema.pain001=pain.001.001.09.xsd
xsd.schema.camt053=camt.053.001.08.xsd
```

### Using Properties in Routes

```java
from("direct:configBasedValidation")
    .to("kamelet:k-xsd-validation"
        + "?xsdFileName={{xsd.schema.pacs008}}"
        + "&validationMode={{xsd.validation.default.mode}}")
    .to("mock:success");
```

## Error Handling Patterns

### Global Exception Handling

```java
// Add to your RouteBuilder
onException(Exception.class)
    .when(simple("${exception} is 'com.pixel.v2.validation.processor.XsdValidationException'"))
    .log("[XSD-ERROR] Validation failed: ${exception.message}")
    .setHeader("ValidationError", simple("${exception.message}"))
    .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
    .to("direct:handleValidationError")
    .handled(false); // Let transaction rollback
```

### Retry on Validation Failure

```java
from("direct:validateWithRetry")
    .onException(Exception.class)
        .maximumRedeliveries(2)
        .redeliveryDelay(500)
        .log("Retrying validation attempt ${header.CamelRedeliveryCounter}")
    .end()
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .to("mock:success");
```

## Testing Your Integration

### Unit Test Example

```java
@Test
public void testXsdValidation() {
    // Send valid XML message
    String validXml = loadTestXml("valid-pacs008.xml");

    template.sendBody("direct:validatePacs008Message", validXml);

    // Verify success
    MockEndpoint mock = getMockEndpoint("mock:validationSuccess");
    mock.expectedMessageCount(1);
    mock.expectedHeaderReceived("ValidationStatus", "SUCCESS");

    assertMockEndpointsSatisfied();
}

@Test
public void testInvalidXmlValidation() {
    String invalidXml = loadTestXml("invalid-pacs008.xml");

    // Expect exception
    assertThrows(CamelExecutionException.class, () -> {
        template.sendBody("direct:validatePacs008Message", invalidXml);
    });
}
```

## Performance Monitoring

### JMX Metrics

Monitor these metrics via JMX:

- Validation success/failure rates
- Validation duration
- Schema cache hits/misses

### Custom Metrics

```java
// Add to your processor or route
.process(exchange -> {
    long duration = exchange.getIn().getHeader("ValidationDuration", Long.class);
    // Record metrics using your monitoring system
    meterRegistry.timer("xsd.validation.duration").record(duration, TimeUnit.MILLISECONDS);
});
```

This kamelet provides a robust, production-ready solution for XSD validation in your Camel routes with comprehensive error handling and monitoring capabilities.
