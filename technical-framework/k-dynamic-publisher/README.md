# K-Multi Publisher Kamelet

The K-Multi Publisher kamelet provides multi-transport message routing capabilities. It reads a collection of partner configurations from message headers and routes the same message body to different publisher kamelets based on transport type and enabled flags.

## Features

- **Multi-transport Support**: Routes to MQ, CFT, HTTP, and Kafka publishers
- **Conditional Routing**: Only routes to enabled partners (enabled = "Y")
- **Character Encoding**: Converts message encoding based on partner charset configuration
- **Parameter Extraction**: Automatically extracts transport-specific parameters from partner configuration
- **Parallel Processing**: Supports sending to multiple destinations simultaneously

## Configuration Properties

| Property         | Type   | Default      | Description                                      |
| ---------------- | ------ | ------------ | ------------------------------------------------ |
| partnerOutHeader | string | "partnerOut" | Header name containing the partnerOut collection |
| defaultCharset   | string | "UTF-8"      | Default charset encoding if not specified        |
| enabledFlag      | string | "Y"          | Value to check for enabled flag                  |

## Input Message Format

The kamelet expects a `partnerOut` collection in the message headers with the following structure:

```json
[
  {
    "partnerCode": "DOME",
    "partnerName": "Domestic payment engine",
    "partnerTypeName": "Internal Partner",
    "charsetCode": "UTF-8",
    "ruleName": null,
    "enabled": "Y",
    "bmsaEnabled": "N",
    "transport": {
      "type": "CFT",
      "mqs": null,
      "cft": {
        "idf": "DOME_CFT_IDF",
        "partnerCode": "DOME"
      },
      "jms": null,
      "http": null,
      "email": null,
      "sftp": null
    }
  },
  {
    "partnerCode": "MQPARTNER",
    "partnerName": "MQ Partner",
    "partnerTypeName": "Internal Partner",
    "charsetCode": "ISO-8859-1",
    "ruleName": null,
    "enabled": "Y",
    "bmsaEnabled": "N",
    "transport": {
      "type": "MQ",
      "mqs": {
        "qname": "OITARTMI01",
        "qmanager": "FRITL01Z"
      },
      "cft": null,
      "jms": null,
      "http": null,
      "email": null,
      "sftp": null
    }
  }
]
```

## Transport Type Routing

### MQ Transport

- **Condition**: `transport.type = "MQ"` and `enabled = "Y"`
- **Target**: `k-mq-publisher` kamelet
- **Parameters**: Extracts `qname` and `qmanager` from `transport.mqs`

### CFT Transport

- **Condition**: `transport.type = "CFT"` and `enabled = "Y"`
- **Target**: `k-cft-publisher` kamelet
- **Parameters**: Extracts `idf` and `partnerCode` from `transport.cft`

### HTTP Transport

- **Condition**: `transport.type = "HTTP"` and `enabled = "Y"`
- **Target**: `k-http-publisher` kamelet
- **Parameters**: Extracts `uri` and `method` from `transport.http`

### KAFKA Transport

- **Condition**: `transport.type = "KAFKA"` and `enabled = "Y"`
- **Target**: `k-kafka-publisher` kamelet
- **Parameters**: Extracts `topic` and `brokers` from `transport.kafka`

## Message Processing Flow

1. **Input Validation**: Checks if `partnerOut` header exists
2. **Filtering**: Filters partners where `enabled = "Y"`
3. **Charset Conversion**: Converts message encoding if `charsetCode` differs from default
4. **Parameter Extraction**: Extracts transport-specific parameters from partner configuration
5. **Routing**: Routes to appropriate publisher kamelet based on transport type
6. **Logging**: Provides detailed logging for debugging and monitoring

## Usage Example

```yaml
# Camel route using the k-multi-publisher kamelet
- from:
    uri: "direct:multicast-message"
    steps:
      - setHeader:
          name: "partnerOut"
          constant: |
            [
              {
                "partnerCode": "CFT_PARTNER",
                "enabled": "Y",
                "charsetCode": "UTF-8",
                "transport": {
                  "type": "CFT",
                  "cft": {
                    "idf": "CFT_IDF_001",
                    "partnerCode": "CFT_PARTNER"
                  }
                }
              }
            ]
      - to:
          uri: "kamelet:k-multi-publisher"
```

## Dependencies

This kamelet depends on the following publisher kamelets:

- `k-mq-publisher` - for MQ transport
- `k-cft-publisher` - for CFT transport
- `k-http-publisher` - for HTTP transport
- `k-kafka-publisher` - for Kafka transport

## Error Handling

- Partners with `enabled != "Y"` are skipped silently
- Unsupported transport types are logged as warnings
- Missing transport parameters fallback to header values if available
- Character encoding errors are logged but don't stop processing
