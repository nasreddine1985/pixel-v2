# k-pan-001-to-cdm Kamelet

This kamelet takes incoming PAN001 XML and applies an XSLT transform to produce a Common Data Model (CDM) XML.

## Properties

- `xsltResource` (required): path to the XSLT resource inside the kamelet. Default: `classpath:xslt/k-pan-001-to-cdm.xslt`

## Example Usage

```yaml
- from:
    uri: "direct:transform"
    steps:
      - to: "kamelet:k-pan-001-to-cdm"
      - log: "${body}"
```

## XSLT Transformation

The kamelet uses Saxon XSLT processor to transform PAN001 messages to a standardized CDM format. The transformation logic is defined in `k-pan-001-to-cdm.xslt`.

## Integration

This kamelet is part of the PIXEL-V2 project's message processing pipeline, converting PAN001 messages to the internal CDM format for downstream processing.
