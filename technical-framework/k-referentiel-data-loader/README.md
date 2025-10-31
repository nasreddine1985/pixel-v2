# k-referentiel-data-loader Kamelet

A Camel Kamelet that loads flow-specific configuration metadata from the referentiel REST service and enriches message headers for payment processing pipelines. The kamelet retrieves comprehensive configuration information including transformation files, validation schemas, and Kafka settings, while preserving the original message payload.

## Overview

This kamelet is designed for the PIXEL-V2 payment processing system to dynamically load configuration based on specific flow types (PACS008, PACS009, PAIN001, CAMT053). It connects to the referentiel service to retrieve flow-specific processing parameters and enriches messages with configuration headers needed for downstream processing.

## Properties

| Property         | Required | Type   | Default                 | Description                                                                                 |
| ---------------- | -------- | ------ | ----------------------- | ------------------------------------------------------------------------------------------- |
| `serviceUrl`     | ✅       | string | `http://localhost:8099` | The HTTP base URL for the referentiel configuration service                                 |
| `flowId`         | ✅       | string | -                       | The flow identifier to retrieve specific configuration (pacs008, pacs009, pain001, camt053) |
| `configEndpoint` | ❌       | string | `/api/config`           | Path to append to serviceUrl for configuration retrieval                                    |

### Supported Flow IDs

- **pacs008**: Instant payment messages (FIToFICustomerCreditTransfer)
- **pacs009**: Instant payment response messages (FinancialInstitutionCreditTransfer)
- **pain001**: SEPA credit transfer initiation messages (CustomerCreditTransferInitiation)
- **camt053**: Bank account statement messages (BankToCustomerStatement)

## Configuration Headers Added

The kamelet adds the following headers to the message based on the service response:

**Core Configuration:**

- `CmdMapping`: Command mapping configuration
- `Rail`: Processing rail information
- `Mode`: Processing mode
- `NeedSplit`: Boolean indicating if message splitting is required
- `SplitExpr`: Expression used for splitting messages
- `ChunkSize`: Size of processing chunks
- `Outputs`: Array of output configurations

**Transformation & Validation:**

- `XsltFileToCdm`: Path to XSLT file for transforming flow format to CDM
- `XsltFileFromCdm`: Path to XSLT file for transforming CDM to flow format
- `XsdFlowFile`: Path to XSD schema file for flow validation
- `XsdCdmFile`: Path to XSD schema file for CDM validation

**Kafka Configuration:**

- `KafkaBroker`: Kafka broker configuration for message streaming
- `KafkaTopic`: Specific Kafka topic for the flow type

## Expected Service Response

The configuration service should return a JSON response like:

```json
{
  "cmdMapping": "pacs008_mapping",
  "rail": "instant_payments",
  "mode": "real_time",
  "needSplit": true,
  "splitExpr": "//Document",
  "chunkSize": 100,
  "outputs": ["queue1", "queue2"],
  "xsltFileToCdm": "xslt/pacs008-to-cdm.xslt",
  "xsltFileFromCdm": "xslt/cdm-to-pacs008.xslt",
  "xsdFlowFile": "xsd/pacs008.xsd",
  "xsdCdmFile": "xsd/cdm.xsd",
  "kafkaBroker": "localhost:9092",
  "kafkaTopic": "pacs008-topic"
}
```

## Example Usage

```yaml
- from:
    uri: "timer:tick?period=60000"
    steps:
      - set-body:
          constant: "<Document>...</Document>"
      - to: "kamelet:k-referentiel-data-loader?serviceUrl=http://localhost:8099&flowId=pacs008"
      - log: "Processing mode: ${header.Mode}, Rail: ${header.Rail}, XSLT: ${header.XsltFileToCdm}"
      - log: "Kafka Topic: ${header.KafkaTopic}, Broker: ${header.KafkaBroker}"
      - log: "Original payload preserved: ${body}"
```

### Flow-specific Examples

```yaml
# For PACS008 processing
- to: "kamelet:k-referentiel-data-loader?flowId=pacs008"

# For PAIN001 processing
- to: "kamelet:k-referentiel-data-loader?flowId=pain001"

# For CAMT053 processing
- to: "kamelet:k-referentiel-data-loader?flowId=camt053"

# Using custom service URL
- to: "kamelet:k-referentiel-data-loader?serviceUrl=http://config-service:8099&flowId=pacs009"
```

## Fallback Behavior

If the configuration service is unavailable or returns an error, default values are set:

**Core Configuration:**

- `CmdMapping`: "default_mapping"
- `Rail`: "standard"
- `Mode`: "normal"
- `NeedSplit`: "false"
- `SplitExpr`: ""
- `ChunkSize`: "1000"
- `Outputs`: "[]"

**Transformation & Validation:**

- `XsltFileToCdm`: "xslt/default-to-cdm.xslt"
- `XsltFileFromCdm`: "xslt/cdm-to-default.xslt"
- `XsdFlowFile`: "xsd/default.xsd"
- `XsdCdmFile`: "xsd/cdm.xsd"

**Kafka Configuration:**

- `KafkaBroker`: "localhost:9092"
- `KafkaTopic`: "default-topic"

## Integration

This kamelet is part of the PIXEL-V2 project's payment processing pipeline, used for loading comprehensive processing configuration metadata including:

- **Transformation Settings**: XSLT file paths for CDM conversion
- **Validation Schemas**: XSD file paths for message validation
- **Kafka Configuration**: Broker and topic settings for message streaming
- **Processing Parameters**: Flow-specific processing rules and chunking

The kamelet preserves the original XML message payload for subsequent processing steps while enriching the message with all necessary configuration headers.

## Referentiel Service Integration

The kamelet integrates with the referentiel REST service running on port 8099, which provides predefined configurations for:

- **pacs008**: Instant payment messages
- **pacs009**: Instant payment response messages
- **pain001**: SEPA credit transfer messages
- **camt053**: Bank statement messages

Each flow ID returns a complete configuration set with transformation files, validation schemas, and Kafka settings tailored for that specific payment message type.

## API Endpoints Used

The kamelet makes HTTP GET requests to the referentiel service:

- **Endpoint Pattern**: `{{serviceUrl}}{{configEndpoint}}/{{flowId}}`
- **Example**: `http://localhost:8099/api/config/pacs008`
- **HTTP Method**: GET
- **Content-Type**: application/json

## Error Handling

The kamelet implements robust error handling:

1. **Service Unavailable**: Falls back to default configuration values
2. **Invalid Flow ID**: Returns default configuration with warning log
3. **Network Timeouts**: Uses default configuration and continues processing
4. **Invalid JSON Response**: Logs error and applies default values

## Troubleshooting

### Common Issues

**Issue: "No configuration found for flowId"**

- **Cause**: Invalid or unsupported flow ID provided
- **Solution**: Use one of the supported flow IDs: pacs008, pacs009, pain001, camt053

**Issue: "Service connection failed"**

- **Cause**: Referentiel service not running or network issues
- **Solution**: Verify service is running on port 8099 with `curl http://localhost:8099/api/health`

**Issue: "Headers not set correctly"**

- **Cause**: JSON parsing error or service returning unexpected format
- **Solution**: Check service response format matches expected JSON structure

### Debugging

Enable debug logging to troubleshoot issues:

```yaml
- log:
    message: "Loading config for flow: {{flowId}} from {{serviceUrl}}"
    level: DEBUG
- to: "kamelet:k-referentiel-data-loader?flowId={{flowId}}"
- log:
    message: "Config loaded - CmdMapping: ${header.CmdMapping}, Topic: ${header.KafkaTopic}"
    level: DEBUG
```

## Performance Considerations

- **Caching**: Configuration is loaded per message. Consider implementing caching for high-volume scenarios
- **Network Latency**: HTTP call adds ~10-50ms per message depending on network conditions
- **Fallback Speed**: Default configuration is applied immediately if service call fails
- **Memory Usage**: Minimal - only stores headers, preserves original message body

## Best Practices

### Configuration Management

```yaml
# Use environment-specific service URLs
- to: "kamelet:k-referentiel-data-loader?serviceUrl={{env:REFERENTIEL_URL}}&flowId=pacs008"

# Group similar flows in route configurations
- from: "kafka:pacs-input"
  steps:
    - choice:
        when:
          - xpath: "//Document[contains(@xsi:schemaLocation, 'pacs.008')]"
            steps:
              - to: "kamelet:k-referentiel-data-loader?flowId=pacs008"
          - xpath: "//Document[contains(@xsi:schemaLocation, 'pacs.009')]"
            steps:
              - to: "kamelet:k-referentiel-data-loader?flowId=pacs009"
```

### Integration Patterns

```yaml
# Pattern 1: Configuration-driven routing
- to: "kamelet:k-referentiel-data-loader?flowId=${header.MessageType}"
- choice:
    when:
      - simple: "${header.NeedSplit} == 'true'"
        steps:
          - split:
              xpath: "${header.SplitExpr}"

# Pattern 2: Transformation pipeline
- to: "kamelet:k-referentiel-data-loader?flowId=pacs008"
- to: "xslt:${header.XsltFileToCdm}"
- marshal:
    json: {}
- to: "kafka:${header.KafkaTopic}?brokers=${header.KafkaBroker}"
```

## Version History

- **v1.1.0**: Added flow-specific configuration support, enhanced properties, updated for referentiel service integration
- **v1.0.0**: Initial implementation with generic configuration loading
