# K-Message-Split

The `k-message-split` module provides message splitting functionality for Apache Camel integration workflows. This kamelet performs the inverse operation of message concatenation, allowing you to split collections of messages based on various criteria.

## Overview

This kamelet takes a collection of messages and splits them into multiple groups based on configurable properties. It supports different splitting strategies:

- **By Correlation**: Group messages by a correlation property value
- **By Size**: Split into fixed-size chunks
- **Individual**: Split each message into separate groups (default when no properties specified)

## Features

- 🔧 **Multiple Split Strategies**: Correlation-based, size-based, and individual splitting
- 🛡️ **Error Handling**: Configurable error handling modes (STRICT, LENIENT, IGNORE)
- 📊 **Metadata Support**: Optional metadata about split operations
- 🔄 **Header Preservation**: Configurable header preservation
- 📈 **Collection Support**: Works with Lists, Arrays, and single objects
- ⚡ **Performance Optimized**: Efficient processing of large message collections

## Properties

### Required Properties

None - all properties are optional with sensible defaults.

### Optional Properties

| Property          | Type    | Default          | Description                                                     |
| ----------------- | ------- | ---------------- | --------------------------------------------------------------- |
| `correlationId`   | string  | -                | Name of the property to use for correlation-based splitting     |
| `size`            | integer | -                | Maximum number of messages per chunk (for size-based splitting) |
| `splitStrategy`   | enum    | `BY_CORRELATION` | Strategy: `BY_CORRELATION`, `BY_SIZE`, or `INDIVIDUAL`          |
| `errorHandling`   | enum    | `STRICT`         | Error handling: `STRICT`, `LENIENT`, or `IGNORE`                |
| `preserveHeaders` | boolean | `true`           | Whether to preserve original message headers                    |
| `addMetadata`     | boolean | `true`           | Whether to add split operation metadata                         |

## Usage Examples

### 1. Split by Correlation ID

Split messages by order ID:

```yaml
- from:
    uri: "direct:input"
    steps:
      - to:
          uri: "kamelet:k-message-split"
          parameters:
            correlationId: "orderId"
            errorHandling: "LENIENT"
```

### 2. Split by Size

Split into fixed-size chunks:

```yaml
- from:
    uri: "direct:input"
    steps:
      - to:
          uri: "kamelet:k-message-split"
          parameters:
            size: 10
            splitStrategy: "BY_SIZE"
```

### 3. Split Individually

Split each message separately:

```yaml
- from:
    uri: "direct:input"
    steps:
      - to:
          uri: "kamelet:k-message-split"
          parameters:
            splitStrategy: "INDIVIDUAL"
            addMetadata: false
```

## Input Format

The kamelet expects the message body to contain:

- **Collection&lt;Object&gt;**: List or Set of messages
- **Object[]**: Array of messages
- **Single Object**: Will be wrapped in a collection

### Example Input

```json
[
  {
    "orderId": "order1",
    "item": "product1",
    "quantity": 2
  },
  {
    "orderId": "order1",
    "item": "product2",
    "quantity": 1
  },
  {
    "orderId": "order2",
    "item": "product3",
    "quantity": 3
  }
]
```

## Output Format

The kamelet produces a collection of message groups:

```json
[
  [
    {
      "orderId": "order1",
      "item": "product1",
      "quantity": 2
    },
    {
      "orderId": "order1",
      "item": "product2",
      "quantity": 1
    }
  ],
  [
    {
      "orderId": "order2",
      "item": "product3",
      "quantity": 3
    }
  ]
]
```

## Headers Added

When `addMetadata` is `true`, the following headers are added:

- `splitCount`: Number of groups created
- `originalSize`: Original collection size
- `CamelSplitTimestamp`: Split operation timestamp
- `CamelSplitStrategy`: Strategy used for splitting

## Error Handling

### STRICT Mode (default)

Exceptions stop processing and are propagated up.

### LENIENT Mode

Exceptions are logged as warnings, empty result returned.

### IGNORE Mode

Exceptions are logged as debug, original body returned.

## Dependencies

- Apache Camel Core 4.1.0+
- Apache Camel Jackson
- Spring Boot Starter
- Apache Commons Lang3

## Integration

Add to your Camel routes:

```java
@Component
public class MessageSplitRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:split-messages")
            .to("kamelet:k-message-split?correlationId=orderId&errorHandling=LENIENT")
            .split(body())
                .to("direct:process-group")
            .end();
    }
}
```

## Configuration

### Spring Boot Configuration

```properties
# Enable kamelet auto-discovery
camel.component.kamelet.location=classpath:kamelets

# Configure Jackson for JSON processing
camel.jackson.object-mapper=jacksonObjectMapper
```

### Camel Context Configuration

```java
@Configuration
public class CamelConfig {

    @Bean
    public MessageSplitProcessor messageSplitProcessor() {
        return new MessageSplitProcessor();
    }
}
```

## Performance Considerations

- **Memory Usage**: Large collections are processed efficiently using streaming where possible
- **Correlation Extraction**: Uses reflection fallback - consider custom extractors for complex objects
- **Chunk Size**: Larger chunks reduce overhead but increase memory usage
- **Header Processing**: Disable metadata generation for better performance in high-throughput scenarios

## Monitoring

Use the following headers for monitoring:

```java
// Check split success
Integer splitCount = exchange.getIn().getHeader("splitCount", Integer.class);
Integer originalSize = exchange.getIn().getHeader("originalSize", Integer.class);

// Check for errors
String splitError = exchange.getIn().getHeader("splitError", String.class);
if (splitError != null) {
    log.warn("Split operation failed: {}", splitError);
}
```

## Examples

See the test classes for comprehensive usage examples:

- `MessageSplitProcessorTest.java`: Unit tests with various scenarios
- Integration examples in the `examples/` directory

## Support

For issues and questions:

- Check the test cases for usage patterns
- Review the processor implementation for detailed behavior
- Consult Apache Camel documentation for integration patterns
