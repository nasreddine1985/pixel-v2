# Referentiel Configuration Service

A simple REST service mock for configuration management in the PIXEL-V2 payment processing system.

## Overview

The Referentiel service provides comprehensive configuration metadata for different payment flow types in the PIXEL-V2 ecosystem. Given a flow ID, it returns a complete configuration object containing processing parameters, transformation file paths, validation schemas, and Kafka integration settings. This service acts as a centralized configuration repository that supports dynamic configuration loading for payment message processing pipelines.

## Features

- REST API for configuration retrieval
- Predefined configurations for common payment flows (PACS008, PACS009, PAIN001, CAMT053)
- Enhanced configuration model with 13 comprehensive properties
- Transformation file paths (XSLT for CDM conversion)
- Schema validation files (XSD for flow and CDM validation)
- Kafka integration configuration (broker and topic settings)
- Default fallback configuration for unknown flow IDs
- Health and info endpoints for monitoring
- CORS enabled for cross-origin requests

## API Endpoints

### Get Configuration by Flow ID

```
GET /api/config/{flowId}
```

Returns configuration for the specified flow ID.

**Example:**

```bash
curl http://localhost:8099/api/config/pacs008
```

**Response:**

```json
{
  "cmdMapping": "pacs008_mapping",
  "rail": "instant_payments",
  "mode": "real_time",
  "needSplit": true,
  "splitExpr": "//Document",
  "chunkSize": 100,
  "outputs": ["queue1", "queue2"],
  "xsltFileToCdm": "pacs008-to-cdm.xslt",
  "xsltFileFromCdm": "cdm-to-pacs008.xslt",
  "xsdFlowFile": "pacs008.xsd",
  "xsdCdmFile": "cdm.xsd",
  "kafkaBroker": "localhost:9092",
  "kafkaTopic": "pacs008-topic"
}
```

### Get Default Configuration

```
GET /api/config?flowId={flowId}
```

Alternative endpoint for backward compatibility with k-referentiel-data-loader kamelet.

### Get All Configurations

```
GET /api/configs
```

Returns all available configurations.

### Health Check

```
GET /api/health
```

Returns service health status.

### Service Information

```
GET /api/info
```

Returns service metadata and available flows.

## Configuration Properties

Each flow configuration includes the following properties:

### Core Properties

- **cmdMapping**: Command mapping identifier for the flow
- **rail**: Payment rail type (e.g., instant_payments, sepa_credit_transfer)
- **mode**: Processing mode (real_time, batch, daily)
- **needSplit**: Boolean indicating if message splitting is required
- **splitExpr**: XPath expression for message splitting
- **chunkSize**: Maximum number of items per processing chunk
- **outputs**: List of output queue names

### Transformation Properties

- **xsltFileToCdm**: XSLT file path for transforming flow format to CDM
- **xsltFileFromCdm**: XSLT file path for transforming CDM to flow format
- **xsdFlowFile**: XSD schema file for flow format validation
- **xsdCdmFile**: XSD schema file for CDM format validation

### Integration Properties

- **kafkaBroker**: Kafka broker address for message publishing
- **kafkaTopic**: Kafka topic name for this flow type

## Predefined Configurations

### PACS008 (Instant Payments)

- Command Mapping: `pacs008_mapping`
- Rail: `instant_payments`
- Mode: `real_time`
- Split Required: `true`
- Split Expression: `//Document`
- Chunk Size: `100`
- Outputs: `["queue1", "queue2"]`
- XSLT To CDM: `pacs008-to-cdm.xslt`
- XSLT From CDM: `cdm-to-pacs008.xslt`
- XSD Flow File: `pacs008.xsd`
- XSD CDM File: `cdm.xsd`
- Kafka Broker: `localhost:9092`
- Kafka Topic: `pacs008-topic`

### PACS009 (Instant Payments Response)

- Command Mapping: `pacs009_mapping`
- Rail: `instant_payments`
- Mode: `real_time`
- Split Required: `true`
- Split Expression: `//Document`
- Chunk Size: `50`
- Outputs: `["queue3", "queue4"]`
- XSLT To CDM: `pacs009-to-cdm.xslt`
- XSLT From CDM: `cdm-to-pacs009.xslt`
- XSD Flow File: `pacs009.xsd`
- XSD CDM File: `cdm.xsd`
- Kafka Broker: `localhost:9092`
- Kafka Topic: `pacs009-topic`

### PAIN001 (SEPA Credit Transfer)

- Command Mapping: `pain001_mapping`
- Rail: `sepa_credit_transfer`
- Mode: `batch`
- Split Required: `true`
- Split Expression: `//CstmrCdtTrfInitn`
- Chunk Size: `200`
- Outputs: `["queue5", "queue6"]`
- XSLT To CDM: `pain001-to-cdm.xslt`
- XSLT From CDM: `cdm-to-pain001.xslt`
- XSD Flow File: `pain001.xsd`
- XSD CDM File: `cdm.xsd`
- Kafka Broker: `localhost:9092`
- Kafka Topic: `pain001-topic`

### CAMT053 (Bank Statement)

- Command Mapping: `camt053_mapping`
- Rail: `bank_statement`
- Mode: `daily`
- Split Required: `false`
- Split Expression: ``
- Chunk Size: `1000`
- Outputs: `["queue7"]`
- XSLT To CDM: `camt053-to-cdm.xslt`
- XSLT From CDM: `cdm-to-camt053.xslt`
- XSD Flow File: `camt053.xsd`
- XSD CDM File: `cdm.xsd`
- Kafka Broker: `localhost:9092`
- Kafka Topic: `camt053-topic`

## Running the Service

### Prerequisites

- Java 21+
- Maven 3.9+

### Build and Run

```bash
# Build the service
mvn clean package

# Run the service
java -jar target/referentiel-1.0.1-SNAPSHOT.jar

# Or use Maven Spring Boot plugin
mvn spring-boot:run
```

The service will start on port 8099 by default.

### Configuration

Service configuration can be customized in `application.properties`:

- `server.port`: Service port (default: 8099)
- `logging.level.*`: Adjust logging levels
- `management.endpoints.*`: Configure actuator endpoints
- `spring.application.name`: Service name for discovery

Current configuration runs on port **8099** to avoid conflicts with other PIXEL-V2 services.

## Integration

This service is designed to work seamlessly with the `k-referentiel-data-loader` kamelet in the PIXEL-V2 system. The kamelet loads configuration dynamically and sets message headers for downstream processing.

### Kamelet Integration

```yaml
- to: "kamelet:k-referentiel-data-loader?serviceUrl=http://localhost:8099&configEndpoint=/api/config&flowId=pacs008"
```

### PACS008 Route Integration

The service integrates with the PACS008 processing pipeline through the `pacs008-referentiel-loader` route:

```java
from("direct:pacs008-referentiel-loader")
    .to("kamelet:k-referentiel-data-loader?serviceUrl=http://localhost:8099&configEndpoint=/api/config&flowId=#{header.flowId}")
    .log("Configuration loaded for flow: ${header.flowId}")
```

### Configuration Headers

The kamelet sets the following headers from the configuration response:

- `cmdMapping`, `rail`, `mode`, `needSplit`, `splitExpr`, `chunkSize`, `outputs`
- `xsltFileToCdm`, `xsltFileFromCdm`, `xsdFlowFile`, `xsdCdmFile`
- `kafkaBroker`, `kafkaTopic`

## Testing

Run the unit tests:

```bash
mvn test
```

Test the service manually:

```bash
# Test health endpoint
curl http://localhost:8099/api/health

# Test configuration retrieval
curl http://localhost:8099/api/config/pacs008

# Test all configurations
curl http://localhost:8099/api/configs

# Test with query parameter (kamelet compatibility)
curl "http://localhost:8099/api/config?flowId=pacs008"
```

## Docker Support

Build and run with Docker:

```bash
# Build Docker image
docker build -t pixel-v2/referentiel:latest .

# Run container
docker run -p 8099:8099 pixel-v2/referentiel:latest
```

## Development

The service follows Spring Boot conventions:

- Controllers in `controller` package
- Services in `service` package
- Models in `model` package
- Configuration in `application.properties`
- Tests mirror the source structure
