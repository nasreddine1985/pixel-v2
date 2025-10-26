# k-pain001-to-cdm-transformer Kamelet

This kamelet takes incoming PAIN.001 (Payment Initiation) XML and applies an XSLT transform to produce a Common Data Model (CDM) XML.

## Properties

- `xsltResource` (required): path to the XSLT resource inside the kamelet. Default: `classpath:xslt/k-pain-001-to-cdm.xslt`

## Example Usage

```yaml
- from:
    uri: "direct:transform"
    steps:
      - to: "kamelet:k-pain001-to-cdm-transformer"
      - log: "${body}"
```

## XSLT Transformation

The kamelet uses Saxon XSLT processor to transform PAIN.001 payment initiation messages to a standardized CDM format. The transformation logic is defined in `k-pain-001-to-cdm.xslt`.

## Integration

This kamelet is part of the PIXEL-V2 project's PAIN.001 processing pipeline, converting payment initiation messages to the internal CDM format for downstream processing.

## PAIN.001 Message Type

PAIN.001 (Payment Initiation) is used for:

- Customer payment initiation requests
- Direct debit instructions
- Credit transfer instructions from customer to bank
- Bulk payment processing
