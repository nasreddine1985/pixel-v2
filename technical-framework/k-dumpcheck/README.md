# K-Dump Check Kamelet

## Overview

The K-Dump Check kamelet provides message dumping and content validation capabilities for debugging and monitoring purposes in the PIXEL-V2 technical framework.

## Features

- **Message Dumping**: Optionally dump messages to file system for debugging
- **Content Validation**: Basic content validation checks (XML/JSON format validation, empty content detection)
- **Size Limits**: Configurable maximum dump size to prevent large file creation
- **Flexible Logging**: Configurable log levels for different environments

## Configuration Parameters

| Parameter            | Type    | Required | Default          | Description                          |
| -------------------- | ------- | -------- | ---------------- | ------------------------------------ |
| `dumpEnabled`        | boolean | Yes      | `false`          | Enable or disable message dumping    |
| `dumpLocation`       | string  | No       | `/opt/out/dumps` | File system location for dumps       |
| `dumpPrefix`         | string  | No       | `msg_dump`       | Prefix for dump file names           |
| `enableContentCheck` | boolean | No       | `true`           | Enable content validation            |
| `logLevel`           | string  | No       | `INFO`           | Log level (DEBUG, INFO, WARN, ERROR) |
| `maxDumpSize`        | integer | No       | `1048576`        | Maximum dump size in bytes (1MB)     |

## Usage Examples

### Basic Usage (No Dumping)

```yaml
- kamelet:
    ref: k-dumpcheck
    properties:
      dumpEnabled: false
      enableContentCheck: true
```

### Development/Debug Usage (With Dumping)

```yaml
- kamelet:
    ref: k-dumpcheck
    properties:
      dumpEnabled: true
      dumpLocation: "/tmp/message-dumps"
      dumpPrefix: "debug_msg"
      logLevel: "DEBUG"
      maxDumpSize: 2097152
```

### Production Usage (Validation Only)

```yaml
- kamelet:
    ref: k-dumpcheck
    properties:
      dumpEnabled: false
      enableContentCheck: true
      logLevel: "WARN"
```

## Output Headers

The kamelet adds the following headers to the exchange:

- `DumpCheckStatus`: Status of the dump operation (`DUMPED`, `NOT_DUMPED`, `SKIPPED_TOO_LARGE`)
- `ContentValid`: Boolean indicating if content validation passed
- `ValidationMessages`: String containing validation error messages (if any)
- `MessageSize`: Size of the message body in bytes
- `OriginalMessageBody`: Copy of the original message body

## File Dump Format

When dumping is enabled, files are created with the following naming pattern:

```
{dumpPrefix}_{yyyyMMdd_HHmmss_SSS}_{flowOccurId}.txt
```

The dump file contains:

- Timestamp and metadata
- Flow occurrence ID
- Content validation results
- Message headers
- Original message body

## Dependencies

- `camel:kamelet`
- `camel:log`
- `camel:file`
- `camel:jackson`

## Notes

- Messages larger than `maxDumpSize` will be skipped from dumping but still processed
- Content validation performs basic format checks for XML and JSON based on Content-Type header
- The kamelet preserves the original message body and passes it through unchanged
- Dump files are created with detailed metadata for debugging purposes
