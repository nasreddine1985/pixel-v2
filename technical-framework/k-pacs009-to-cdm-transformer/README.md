# k-pacs009-to-cdm-transformer Kamelet

This kamelet takes incoming PACS.009.001.02 XML and applies an XSLT transform to produce a Common Data Model (CDM) XML.

## Properties

- `xsltResource` (required): path to the XSLT resource inside the kamelet. Default: `classpath:xslt/k-pacs-009-to-cdm.xslt`

## Example Usage

```yaml
- from:
    uri: "direct:transform"
    steps:
      - to: "kamelet:k-pacs009-to-cdm-transformer"
      - log: "${body}"
```

## XSLT Transformation

The kamelet uses Saxon XSLT processor to transform PACS.009.001.02 payment messages to a standardized CDM format. The transformation logic is defined in `k-pacs-009-to-cdm.xslt`.

## Integration

This kamelet is part of the PIXEL-V2 project's PACS.009 processing pipeline, converting payment reversal messages to the internal CDM format for downstream processing.

## PACS.009 Message Type

PACS.009 (Financial Institution Credit Transfer - Reversal) is used for:

- Credit transfer reversals
- Payment cancellation requests
- Return of funds to originator
- Error correction in payment processing
