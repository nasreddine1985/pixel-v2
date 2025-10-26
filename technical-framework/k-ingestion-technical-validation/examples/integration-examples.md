# k-ingest-validation Integration Examples

This directory contains integration examples showing how to use the k-ingest-validation kamelet with other kamelets in the PIXEL-V2 ecosystem.

## Example 1: Basic PACS.008 Processing Pipeline

```yaml
# Complete PACS.008 processing pipeline with validation
- from:
    uri: "jms:queue:incoming-pacs008"
    steps:
      # Step 1: Load configuration metadata
      - to: "kamelet:k-ref-loader?serviceUrl=http://config-service:8080"

      # Step 2: Validate message structure and compliance
      - to: "kamelet:k-ingest-validation?strictMode=true&messageType=pacs.008"

      # Step 3: Route based on validation result
      - choice:
          when:
            - simple: "${header.IsValid} == true"
              steps:
                - log: "Message validated successfully: ${header.ValidationResult.messageId}"
                - to: "kamelet:k-pacs-008-to-cdm"
                - to: "jms:queue:processed-payments"
          otherwise:
            - log: "Validation failed: ${header.ErrorReason}"
            - to: "jms:queue:invalid-payments"
```

## Example 2: Multi-Channel Receipt with Validation

```yaml
# API receipt with validation
- from:
    uri: "platform-http:/api/payments"
    steps:
      - to: "kamelet:k-http-message-receiver"
      - to: "kamelet:k-ingest-validation?failOnWarnings=false"
      - choice:
          when:
            - simple: "${header.ProcessingDecision} == 'PROCEED'"
              steps:
                - to: "jms:queue:api-validated-payments"
          otherwise:
            - to: "jms:queue:api-rejected-payments"

# File receipt with validation
- from:
    uri: "file:input?include=*.xml"
    steps:
      - to: "kamelet:k-file-receipt"
      - to: "kamelet:k-ingest-validation?strictMode=false"
      - choice:
          when:
            - simple: "${header.ValidationFailed} != true"
              steps:
                - to: "jms:queue:file-validated-payments"
          otherwise:
            - to: "jms:queue:file-rejected-payments"
```

## Example 3: Error Handling and Monitoring

```yaml
- from:
    uri: "direct:validate-and-process"
    steps:
      # Enhanced validation with monitoring
      - to: "kamelet:k-ingest-validation?strictMode={{validation.strict:false}}&failOnWarnings={{validation.fail.warnings:false}}"

      # Detailed error processing
      - choice:
          when:
            - simple: "${header.ErrorCount} > 0"
              steps:
                - log: "Validation errors detected: ${header.ErrorCount}"
                - split:
                    simple: "${header.ValidationErrors}"
                    steps:
                      - log: "Error ${exchangeProperty.CamelSplitIndex}: [${body.errorCode}] ${body.message} (Field: ${body.field})"
                      - to: "direct:error-notification"
                - to: "jms:queue:error-handling"

          when:
            - simple: "${header.WarningCount} > 0"
              steps:
                - log: "Validation warnings detected: ${header.WarningCount}"
                - set-header:
                    name: "ProcessingPriority"
                    constant: "LOW"
                - to: "jms:queue:processing-with-warnings"

          otherwise:
            - log: "Clean validation - processing normally"
            - set-header:
                name: "ProcessingPriority"
                constant: "NORMAL"
            - to: "jms:queue:clean-processing"

# Error notification sub-route
- from:
    uri: "direct:error-notification"
    steps:
      - transform:
          simple: |
            {
              "errorCode": "${body.errorCode}",
              "field": "${body.field}",
              "message": "${body.message}",
              "messageId": "${header.ValidationResult.messageId}",
              "timestamp": "${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"
            }
      - to: "jms:topic:validation-errors"
```

## Example 4: Configuration-Driven Validation

```yaml
# Dynamic validation based on configuration
- from:
    uri: "timer:validation-demo?period=30000"
    steps:
      - set-body:
          constant: |
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
              <FIToFICstmrCdtTrf>
                <GrpHdr>
                  <MsgId>DEMO123456789</MsgId>
                  <CreDtTm>2023-10-19T10:30:00.000Z</CreDtTm>
                  <NbOfTxs>1</NbOfTxs>
                </GrpHdr>
                <CdtTrfTxInf>
                  <PmtId>
                    <InstrId>INSTR123</InstrId>
                    <EndToEndId>E2E123</EndToEndId>
                  </PmtId>
                  <Amt>
                    <InstdAmt Ccy="EUR">1000.00</InstdAmt>
                  </Amt>
                  <Dbtr><Nm>John Doe</Nm></Dbtr>
                  <Cdtr><Nm>Jane Smith</Nm></Cdtr>
                </CdtTrfTxInf>
              </FIToFICstmrCdtTrf>
            </Document>

      # Load configuration to determine validation settings
      - to: "kamelet:k-ref-loader"

      # Use configuration headers to customize validation
      - to: "kamelet:k-ingest-validation?strictMode=${header.ValidationStrict:false}&failOnWarnings=${header.ValidationFailOnWarnings:false}"

      - log: "Demo validation result: ${header.ProcessingDecision} (Errors: ${header.ErrorCount}, Warnings: ${header.WarningCount})"
```

## Example 5: Batch Processing with Validation

```yaml
# Batch file processing with individual message validation
- from:
    uri: "file:batch-input?include=*.xml"
    steps:
      - split:
          xpath: "//PaymentMessage"
          steps:
            - to: "kamelet:k-ingest-validation?messageType=auto-detect"
            - choice:
                when:
                  - simple: "${header.IsValid} == true"
                    steps:
                      - set-header:
                          name: "BatchStatus"
                          constant: "VALID"
                      - to: "jms:queue:batch-valid-messages"
                otherwise:
                  - set-header:
                      name: "BatchStatus"
                      constant: "INVALID"
                  - set-header:
                      name: "InvalidReason"
                      simple: "${header.ErrorReason}"
                  - to: "jms:queue:batch-invalid-messages"
          aggregationStrategy: "batchAggregator"

      # Process batch results
      - choice:
          when:
            - simple: "${header.CamelSplitComplete} == true"
              steps:
                - log: "Batch processing complete - Valid: ${header.ValidCount}, Invalid: ${header.InvalidCount}"
                - to: "jms:queue:batch-results"
```

## Integration Patterns

### Pattern 1: Validation Gateway

Use k-ingest-validation as a gateway for all incoming messages before any processing.

### Pattern 2: Configuration-Driven Processing

Combine k-ref-loader with k-ingest-validation for dynamic validation rules.

### Pattern 3: Multi-Stage Validation

Use multiple validation points with different strictness levels.

### Pattern 4: Error Classification

Route messages to different queues based on validation error types and severity.

### Pattern 5: Performance Monitoring

Track validation metrics and performance for system monitoring.
