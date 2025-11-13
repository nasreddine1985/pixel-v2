# K-XSD Validation - Collection Support Examples

## Overview

The `k-xsd-validation` kamelet now supports both single XML messages and collections of XML messages. This enables batch validation scenarios where multiple messages need to be validated against the same XSD schema.

## Supported Input Types

### Single Message

- **Type**: `String`
- **Description**: Single XML message as a string
- **Headers Set**: `ValidationScope=SINGLE`, `ValidationCount=1`

### Collection of Messages

- **Type**: `Collection<String>` (List, Set, etc.)
- **Description**: Multiple XML messages in a collection
- **Headers Set**: `ValidationScope=COLLECTION`, `ValidationCount=<size>`, `ValidationSuccessCount=<successes>`, `ValidationErrorCount=<errors>`

## Collection Validation Behavior

### Success Criteria

- **All Valid**: All messages in the collection pass XSD validation
- **Result**: Processing continues to next step
- **Headers**: `ValidationStatus=SUCCESS`

### Failure Criteria

- **Any Invalid**: One or more messages fail XSD validation
- **Result**: Exception is thrown with detailed error summary
- **Headers**: `ValidationStatus=ERROR`, `ValidationError=<summary>`

### Error Reporting

- Detailed error messages for each failed message with index numbers
- Summary format: "X out of Y messages failed validation: [details...]"
- Individual message errors include line/column numbers where available

## Usage Examples

### Java DSL Examples

#### Basic Collection Validation

```java
from("direct:validateCollection")
    .log("[BATCH-VALIDATION] Starting collection validation")
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .log("[BATCH-VALIDATION] ✅ All messages validated successfully: ${header.ValidationCount} messages")
    .to("direct:processBatch");
```

#### Collection Validation with Error Handling

```java
from("direct:validateBatchWithErrorHandling")
    .routeId("batch-validation-with-errors")
    .onException(Exception.class)
        .log("[BATCH-ERROR] Collection validation failed: ${exception.message}")
        .log("[BATCH-ERROR] Success count: ${header.ValidationSuccessCount}, Error count: ${header.ValidationErrorCount}")
        .to("direct:handleBatchErrors")
        .handled(true)
    .end()

    .log("[BATCH-VALIDATION] Validating collection of ${body.size()} messages")
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd&validationMode=STRICT")
    .log("[BATCH-VALIDATION] ✅ Batch validation successful")
    .to("direct:processBatch");
```

#### Dynamic Collection Processing

```java
from("direct:processPaymentBatch")
    .routeId("payment-batch-processing")
    .log("[PAYMENT-BATCH] Received batch of ${body.size()} messages")

    // Split collection for individual processing if needed
    .choice()
        .when(simple("${body.size()} > {{batch.validation.threshold:100}}"))
            .log("[PAYMENT-BATCH] Large batch detected, processing in chunks")
            .to("direct:processLargeBatch")
        .otherwise()
            .log("[PAYMENT-BATCH] Standard batch processing")
            .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .end()

    .to("direct:persistBatch");
```

#### Collection with Message Type Detection

```java
from("direct:mixedMessageTypeValidation")
    .process(exchange -> {
        // Group messages by type for validation
        Collection<String> messages = exchange.getIn().getBody(Collection.class);
        Map<String, List<String>> messagesByType = groupMessagesByType(messages);

        exchange.setProperty("messagesByType", messagesByType);
    })

    // Validate each type separately
    .split(simple("${exchangeProperty.messagesByType.entrySet()}"))
        .setHeader("MessageType", simple("${body.key}"))
        .setBody(simple("${body.value}"))

        .choice()
            .when(header("MessageType").isEqualTo("PACS008"))
                .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
            .when(header("MessageType").isEqualTo("PAIN001"))
                .to("kamelet:k-xsd-validation?xsdFileName=pain.001.001.09.xsd")
        .end()
    .end();
```

### YAML DSL Examples

#### Simple Collection Validation

```yaml
- route:
    id: "yaml-collection-validation"
    from:
      uri: "direct:validateYamlCollection"
      steps:
        - log:
            message: "[YAML-BATCH] Starting validation of ${body.size()} messages"

        - to:
            uri: "kamelet:k-xsd-validation"
            parameters:
              xsdFileName: "pacs.008.001.08.xsd"
              validationMode: "STRICT"
              logValidationResult: true

        - log:
            message: "[YAML-BATCH] ✅ Validation successful - ${header.ValidationSuccessCount} messages validated"

        - to: "mock:batchSuccess"
```

#### Collection Validation with Conditional Processing

```yaml
- route:
    id: "conditional-batch-validation"
    from:
      uri: "direct:conditionalBatch"
      steps:
        - choice:
            when:
              - simple: "${body.size()} > 0"
                steps:
                  - log:
                      message: "[CONDITIONAL-BATCH] Validating ${body.size()} messages"
                  - to:
                      uri: "kamelet:k-xsd-validation"
                      parameters:
                        xsdFileName: "pacs.008.001.08.xsd"
                        validationMode: "LENIENT"
                  - to: "direct:processBatch"
            otherwise:
              steps:
                - log:
                    message: "[CONDITIONAL-BATCH] Empty collection, skipping validation"
                - to: "direct:emptyBatchHandler"
```

### Integration with Existing Routes

#### Adding Collection Validation to Pacs008RouteBuilder

```java
// Add this route to your existing Pacs008RouteBuilder
from("direct:validatePacs008Batch")
    .routeId("pacs008-batch-validation")
    .log("[PACS008-BATCH] Starting XSD validation for batch of ${body.size()} messages")

    // Validate entire collection at once
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd&validationMode=STRICT")

    .log("[PACS008-BATCH] ✅ Batch XSD validation successful - ${header.ValidationSuccessCount} messages validated")
    .setHeader("BatchXsdValidationStatus", constant("PASSED"))

    // Continue to batch persistence
    .to("direct:pacs008-batch-persistence");
```

#### Collection Processing with MQ Integration

```java
// Modify existing MQ route to handle collections
from("kamelet:k-mq-message-receiver" + "?destination={{flow.pacs008.batch.queue.name}}"
    + "&brokerUrl={{mq.broker-url}}" + "&user={{mq.user}}"
    + "&password={{mq.password}}")
    .routeId("pacs008-batch-consumer")

    .log("[PACS008-BATCH] Batch message received from queue")

    // Convert message body to collection if needed
    .process(exchange -> {
        String batchContent = exchange.getIn().getBody(String.class);
        List<String> messages = parseBatchMessage(batchContent);
        exchange.getIn().setBody(messages);
    })

    .log("[PACS008-BATCH] Parsed ${body.size()} individual messages from batch")

    // Validate collection
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")

    .log("[PACS008-BATCH] Batch validation completed successfully")
    // Continue with existing batch processing logic...
```

## Sample Data Structures

### Java Collection Examples

```java
// List of XML messages
List<String> xmlMessages = Arrays.asList(
    "<?xml version=\"1.0\"?><Document>...</Document>",
    "<?xml version=\"1.0\"?><Document>...</Document>",
    "<?xml version=\"1.0\"?><Document>...</Document>"
);

// Set of unique XML messages
Set<String> uniqueMessages = new HashSet<>(xmlMessages);

// ArrayList from external source
ArrayList<String> paymentMessages = paymentService.getBatchMessages();
```

### Collection Creation from Aggregated Messages

```java
from("direct:aggregateToCollection")
    .aggregate(constant("batch"))
    .aggregationStrategy((oldExchange, newExchange) -> {
        List<String> messages;
        if (oldExchange == null) {
            messages = new ArrayList<>();
        } else {
            messages = oldExchange.getIn().getBody(List.class);
        }
        messages.add(newExchange.getIn().getBody(String.class));

        if (oldExchange == null) {
            oldExchange = newExchange;
        }
        oldExchange.getIn().setBody(messages);
        return oldExchange;
    })
    .completionSize(10)
    .completionTimeout(5000)

    .log("Aggregated ${body.size()} messages for batch validation")
    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .to("direct:processBatch");
```

## Error Handling Patterns

### Partial Failure Handling

```java
from("direct:handlePartialFailures")
    .doTry()
        .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
        .log("All messages validated successfully")
        .to("direct:processAllMessages")
    .doCatch(Exception.class)
        .log("Batch validation failed: ${exception.message}")
        .log("Successful validations: ${header.ValidationSuccessCount}")
        .log("Failed validations: ${header.ValidationErrorCount}")

        // Process successful messages separately if needed
        .choice()
            .when(simple("${header.ValidationSuccessCount} > 0"))
                .to("direct:processPartialBatch")
            .otherwise()
                .to("direct:handleAllFailed")
        .end()
    .end();
```

### Retry with Reduced Batch Size

```java
from("direct:retryWithSmallerBatch")
    .onException(Exception.class)
        .maximumRedeliveries(2)
        .process(exchange -> {
            // Reduce batch size on retry
            List<String> originalBatch = exchange.getIn().getBody(List.class);
            int retryAttempt = exchange.getIn().getHeader("CamelRedeliveryCounter", Integer.class);
            int newBatchSize = Math.max(1, originalBatch.size() / (retryAttempt + 1));

            List<String> reducedBatch = originalBatch.subList(0, newBatchSize);
            exchange.getIn().setBody(reducedBatch);
        })
        .log("Retrying with reduced batch size: ${body.size()}")
    .end()

    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .to("direct:success");
```

## Performance Considerations

### Batch Size Guidelines

- **Small batches (1-10 messages)**: Minimal overhead
- **Medium batches (10-100 messages)**: Good performance balance
- **Large batches (100+ messages)**: Consider memory usage and timeout settings

### Memory Management

```java
// For very large collections, consider streaming validation
from("direct:largeCollectionValidation")
    .choice()
        .when(simple("${body.size()} > {{validation.batch.size.limit:1000}}"))
            .log("Large collection detected, using streaming validation")
            .split(body())
                .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
            .end()
        .otherwise()
            .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")
    .end();
```

### Monitoring and Metrics

```java
// Add metrics for collection validation
from("direct:monitoredBatchValidation")
    .process(exchange -> {
        int batchSize = exchange.getIn().getBody(Collection.class).size();
        // Record batch size metric
        meterRegistry.counter("validation.batch.size").increment(batchSize);
    })

    .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.08.xsd")

    .process(exchange -> {
        // Record success metrics
        int successCount = exchange.getIn().getHeader("ValidationSuccessCount", Integer.class);
        meterRegistry.counter("validation.batch.success").increment(successCount);
    });
```

## Headers Reference

### Input Headers (Optional)

- `ValidationMode`: STRICT or LENIENT (overrides kamelet parameter)
- `XsdFileName`: Schema file name (overrides kamelet parameter)

### Output Headers (Set by Kamelet)

#### Common Headers

- `ValidationStatus`: SUCCESS or ERROR
- `ValidationTimestamp`: ISO timestamp of validation completion
- `ValidationDuration`: Validation duration in milliseconds
- `ValidationScope`: SINGLE or COLLECTION

#### Single Message Headers

- `ValidationCount`: Always 1

#### Collection Headers

- `ValidationCount`: Total number of messages in collection
- `ValidationSuccessCount`: Number of successfully validated messages
- `ValidationErrorCount`: Number of failed validations

#### Error Headers (on failure)

- `ValidationError`: Detailed error message with all failures

This collection support makes the `k-xsd-validation` kamelet suitable for high-throughput batch processing scenarios while maintaining detailed error reporting and monitoring capabilities.
