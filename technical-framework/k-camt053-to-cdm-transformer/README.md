# k-camt053-to-cdm-transformer Kamelet

This kamelet takes incoming CAMT.053.001.02 XML and applies an XSLT transform to produce a Common Data Model (CDM) XML.

## Properties

- `xsltResource` (required): path to the XSLT resource inside the kamelet. Default: `classpath:xslt/k-camt-053-to-cdm.xslt`

## Example Usage

```yaml
- from:
    uri: "direct:transform"
    steps:
      - to: "kamelet:k-camt053-to-cdm-transformer"
      - log: "${body}"
```

## XSLT Transformation

The kamelet uses Saxon XSLT processor to transform CAMT.053.001.02 bank statement messages to a standardized CDM format. The transformation logic is defined in `k-camt-053-to-cdm.xslt`.

## Integration

This kamelet is part of the PIXEL-V2 project's CAMT.053 processing pipeline, converting bank statement messages to the internal CDM format for downstream processing.

## CAMT.053 Message Type

CAMT.053 (Bank to Customer Statement) is used for:

- Bank account statements
- Transaction reporting from bank to customer
- Balance and movement information
- Account reconciliation data
- Daily/periodic statement delivery
