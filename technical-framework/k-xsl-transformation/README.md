# K-XSL Transformation Kamelet

This Kamelet provides XSL transformation capabilities for XML messages. It supports both single XML messages and collections of XML messages, applying XSLT stylesheets to transform them from one format to another.

## Features

- **Single Message Transformation**: Transform individual XML messages
- **Collection Transformation**: Transform collections of XML messages in batch
- **Flexible Error Handling**: STRICT mode (fail fast) or LENIENT mode (continue on errors)
- **Performance Tracking**: Measures and reports transformation duration
- **Detailed Error Reporting**: Comprehensive error messages for debugging
- **Multi-Schema Support**: Works with any XML format (PACS.008, ITL Pivot, etc.)
- **Namespace Aware**: Supports namespace-aware XML processing

## Configuration Properties

| Property                  | Type    | Required | Default  | Description                                                   |
| ------------------------- | ------- | -------- | -------- | ------------------------------------------------------------- |
| `xslFileName`             | string  | Yes      | -        | Name of the XSL stylesheet file (must exist in `/xsl` folder) |
| `transformationMode`      | string  | No       | "STRICT" | Transformation mode: STRICT or LENIENT                        |
| `enableDetailedErrors`    | boolean | No       | true     | Include detailed error messages in exceptions                 |
| `logTransformationResult` | boolean | No       | true     | Log transformation success/failure messages                   |
| `namespaceAware`          | boolean | No       | true     | Enable namespace-aware XML processing                         |

## XSL Stylesheets

Place your XSL transformation files in the `src/main/resources/xsl/` directory. The Kamelet includes sample transformations:

- `pacs-008-to-simplified.xsl` - Transforms PACS.008 messages to simplified format
- `itl-pivot-to-cdm.xsl` - Transforms ITL Pivot messages to CDM format

## Input/Output

### Input

- **Single Message**: `String` containing XML content
- **Collection**: `Collection<String>` containing multiple XML messages

### Output

- **Single Message**: `String` containing transformed XML
- **Collection**: `List<String>` containing transformed XML messages (nulls for failed transformations in LENIENT mode)

### Headers Set by Kamelet

| Header                       | Type    | Description                          |
| ---------------------------- | ------- | ------------------------------------ |
| `TransformationStatus`       | String  | SUCCESS, ERROR, or PARTIAL           |
| `TransformationScope`        | String  | SINGLE or COLLECTION                 |
| `TransformationCount`        | Integer | Total number of messages processed   |
| `TransformationSuccessCount` | Integer | Number of successful transformations |
| `TransformationErrorCount`   | Integer | Number of failed transformations     |
| `TransformationDuration`     | Long    | Processing duration in milliseconds  |
| `TransformationTimestamp`    | String  | ISO timestamp of completion          |
| `TransformationError`        | String  | Error details (if any)               |

## Usage Examples

### 1. Single Message Transformation

```yaml
apiVersion: camel.apache.org/v1alpha1
kind: Integration
metadata:
  name: single-transformation
spec:
  flows:
    - from:
        uri: "timer:trigger?period=30000"
        steps:
          - setBody:
              constant: |
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                  <!-- PACS.008 content -->
                </Document>
          - to: "kamelet:k-xsl-transformation?xslFileName=pacs-008-to-simplified.xsl"
          - log: "Transformed: ${body}"
```

### 2. Collection Transformation

```yaml
apiVersion: camel.apache.org/v1alpha1
kind: Integration
metadata:
  name: collection-transformation
spec:
  flows:
    - from:
        uri: "timer:trigger?period=60000"
        steps:
          - setBody:
              constant:
                - |
                  <?xml version="1.0"?>
                  <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <!-- First message -->
                  </Document>
                - |
                  <?xml version="1.0"?>
                  <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <!-- Second message -->
                  </Document>
          - to: "kamelet:k-xsl-transformation?xslFileName=pacs-008-to-simplified.xsl&transformationMode=LENIENT"
          - log: "Transformed ${header.TransformationSuccessCount} messages"
```

### 3. Java Processor Usage

```java
@Component
public class MyProcessor implements Processor {

    @Autowired
    private XslTransformationProcessor xslProcessor;

    public void process(Exchange exchange) throws Exception {
        // Set transformation parameters
        exchange.getIn().setHeader("XslFileName", "pacs-008-to-simplified.xsl");
        exchange.getIn().setHeader("TransformationMode", "STRICT");

        // Your XML messages
        List<String> xmlMessages = Arrays.asList(xml1, xml2, xml3);
        exchange.getIn().setBody(xmlMessages);

        // Transform
        xslProcessor.process(exchange);

        // Get results
        List<String> transformedMessages = exchange.getIn().getBody(List.class);
        String status = exchange.getIn().getHeader("TransformationStatus", String.class);
    }
}
```

## Error Handling

### STRICT Mode (Default)

- Stops processing on first error
- Throws `XslTransformationException`
- Sets `TransformationStatus` to "ERROR"

### LENIENT Mode

- Continues processing despite errors
- Failed transformations result in `null` values in output collection
- Sets `TransformationStatus` to "SUCCESS", "PARTIAL", or "ERROR"
- Error details available in `TransformationError` header

## Sample XSL Transformations

### PACS.008 to Simplified Format

```xsl
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pacs="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:template match="/pacs:Document">
        <TransformedPayment>
            <Header>
                <MessageId><xsl:value-of select="pacs:FIToFICstmrCdtTrf/pacs:GrpHdr/pacs:MsgId"/></MessageId>
                <CreationDateTime><xsl:value-of select="pacs:FIToFICstmrCdtTrf/pacs:GrpHdr/pacs:CreDtTm"/></CreationDateTime>
            </Header>
            <!-- Additional transformations -->
        </TransformedPayment>
    </xsl:template>

</xsl:stylesheet>
```

## Testing

The module includes comprehensive unit tests:

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=XslTransformationProcessorTest#testSingleValidMessage

# Run with debug output
mvn test -Dtest=XslTransformationProcessorTest -X
```

## Performance

- **Single Message**: Typically 5-50ms depending on XML size and XSL complexity
- **Collection**: Parallel processing support for large collections
- **Memory**: Efficient streaming for large XML documents
- **Caching**: XSL templates are cached for improved performance

## Dependencies

- Apache Camel Core
- Apache Camel Spring Boot
- Saxon XSLT Processor (for XSLT 2.0/3.0 support)
- Spring Boot Framework

## Building

```bash
# Compile
mvn compile

# Package
mvn package

# Install to local repository
mvn install
```
