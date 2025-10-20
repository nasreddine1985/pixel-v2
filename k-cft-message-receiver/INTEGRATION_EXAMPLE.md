# Example usage of k-file-receipt kamelet

This example shows how to integrate the `k-file-receipt` kamelet into a Camel route for file-based payment processing.

## Integration Example

```java
@Component
public class FileReceiptFlowRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // File Receipt Flow - Monitor NAS for payment files
        from("kamelet:k-file-receipt?"
            + "directoryPath=/nas/payments/incoming"
            + "&filePattern=pacs_*.xml"
            + "&processedDirectory=/nas/payments/processed"
            + "&errorDirectory=/nas/payments/error"
            + "&delay=10000")
            .routeId("file-pacs008-receipt-flow")
            .log("Step 1: Processing payment file from NAS")
            .log("XML message processed from file: ${header.CamelFileName}, Line: ${header.CamelSplitIndex}")
            .to("direct:enrichPacs008Message");

        // Alternative configuration for different message types
        from("kamelet:k-file-receipt?"
            + "directoryPath=/nas/pain001/incoming"
            + "&filePattern=pain_*.xml"
            + "&delay=5000")
            .routeId("file-pain001-receipt-flow")
            .log("PAIN.001 message received from file: ${header.CamelFileName}")
            .to("direct:processPain001Message");

        // Error handling route
        from("direct:handleFileProcessingError")
            .routeId("file-error-handler")
            .log("Error processing file: ${header.CamelFileName}, Line: ${header.CamelSplitIndex}")
            .setBody(constant("File processing error"))
            .to("jms:queue:FILE.ERROR");
    }
}
```

## Configuration Properties

Add these properties to `application.properties`:

```properties
# File Receipt Configuration
file.nas.incoming.path=/nas/payments/incoming
file.nas.processed.path=/nas/payments/processed
file.nas.error.path=/nas/payments/error
file.polling.interval=10000
file.pattern=*.xml

# Ensure directories exist and are writable
camel.component.file.auto-create=true

# Database configuration for message persistence
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/ORCLCDB
spring.datasource.username=app
spring.datasource.password=pass
spring.jpa.hibernate.ddl-auto=validate
```

## Testing the File Processing

### Create test files:

```bash
# Create test directory structure
mkdir -p /nas/payments/{incoming,processed,error}

# Create a test file with XML payment messages (one per line)
cat > /nas/payments/incoming/pacs_test_001.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02"><FIToFICstmrCdtTrf><GrpHdr><MsgId>MSG001</MsgId></GrpHdr></FIToFICstmrCdtTrf></Document>
<?xml version="1.0" encoding="UTF-8"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02"><FIToFICstmrCdtTrf><GrpHdr><MsgId>MSG002</MsgId></GrpHdr></FIToFICstmrCdtTrf></Document>
<?xml version="1.0" encoding="UTF-8"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02"><FIToFICstmrCdtTrf><GrpHdr><MsgId>MSG003</MsgId></GrpHdr></FIToFICstmrCdtTrf></Document>
EOF
```

### Monitor processing:

```bash
# Watch the directories
watch -n 1 'echo "=== Incoming ==="; ls -la /nas/payments/incoming/; echo "=== Processed ==="; ls -la /nas/payments/processed/; echo "=== Error ==="; ls -la /nas/payments/error/'
```

## Spring Boot Configuration

```java
@Configuration
public class FileReceiptConfig {

    @Bean
    public MessagePersistenceProcessor messagePersistenceProcessor() {
        return new MessagePersistenceProcessor();
    }

    // Optional: Custom file processing configuration
    @Bean
    public RouteBuilder fileReceiptCustomization() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Add custom error handling for file processing
                onException(Exception.class)
                    .handled(true)
                    .log("File processing error: ${exception.message}")
                    .to("direct:handleFileProcessingError");
            }
        };
    }
}
```

## File Format Requirements

Each file should contain XML payment messages, one per line:

```xml
<?xml version="1.0" encoding="UTF-8"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">...</Document>
<?xml version="1.0" encoding="UTF-8"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">...</Document>
```

## Database Schema

The kamelet will persist each XML message line with additional metadata:

```sql
-- Example table structure (adjust for your database)
CREATE TABLE RECEIVED_MESSAGE (
    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    RECEIVED_AT TIMESTAMP WITH TIME ZONE NOT NULL,
    SOURCE VARCHAR2(50) NOT NULL,
    FILE_NAME VARCHAR2(255),
    LINE_NUMBER NUMBER,
    PAYLOAD CLOB NOT NULL
);
```

## Monitoring and Operations

- Monitor `/nas/payments/incoming` for new files
- Check `/nas/payments/processed` for successfully processed files
- Check `/nas/payments/error` for files that failed processing
- Monitor application logs for line-by-line processing status
- Query database for persisted messages with file and line metadata
