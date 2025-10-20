# k-referentiel-data-loader Kamelet

Loads configuration metadata from an external REST service and adds it to message headers for processing enrichment. The kamelet retrieves configuration information (cmdMapping, rail, mode, needSplit, splitExpr, chunkSize, outputs) and preserves the original message payload.

## Properties

- `serviceUrl` (required): the HTTP base URL for the configuration service. Default: read from `application.properties` property `refloader.serviceUrl` (defaults to `http://localhost:8080`).
- `configEndpoint` (optional): path to append to serviceUrl for configuration retrieval. Default: `/config`.

## Configuration Headers Added

The kamelet adds the following headers to the message based on the service response:

- `CmdMapping`: Command mapping configuration
- `Rail`: Processing rail information
- `Mode`: Processing mode
- `NeedSplit`: Boolean indicating if message splitting is required
- `SplitExpr`: Expression used for splitting messages
- `ChunkSize`: Size of processing chunks
- `Outputs`: Array of output configurations

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
  "outputs": ["queue1", "queue2"]
}
```

## Example Usage

```yaml
- from:
    uri: "timer:tick?period=60000"
    steps:
      - set-body:
          constant: "<Document>...</Document>"
      - to: "kamelet:k-ref-loader?serviceUrl=http://config-service:8080&configEndpoint=/api/config"
      - log: "Processing mode: ${header.Mode}, Rail: ${header.Rail}"
      - log: "Original payload preserved: ${body}"
```

## Fallback Behavior

If the configuration service is unavailable or returns an error, default values are set:

- `CmdMapping`: "default"
- `Rail`: "standard"
- `Mode`: "normal"
- `NeedSplit`: "false"
- `SplitExpr`: ""
- `ChunkSize`: "1000"
- `Outputs`: "[]"

## Integration

This kamelet is part of the PIXEL-V2 project's payment processing pipeline, used for loading processing configuration metadata while preserving the original XML message payload for subsequent processing steps.
