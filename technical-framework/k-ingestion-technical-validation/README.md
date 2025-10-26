# k-ingest-validation Kamelet

Validates payment messages for correctness, completeness, and adherence to standards. This kamelet ensures all required fields are present, data formats are correct, and references are valid according to payment message standards like PACS.008 and PAN.001.

## Features

- **Comprehensive Validation**: Validates message structure, format, and compliance
- **Multiple Message Types**: Supports PACS.008, PAN.001, and generic payment messages
- **Configurable Strictness**: Normal and strict validation modes
- **Detailed Error Reporting**: Provides specific error codes and messages
- **Warning Management**: Optional warnings that can be treated as errors
- **Routing Headers**: Sets headers for downstream routing decisions

## Properties

- `strictMode` (optional): Enable strict validation mode with enhanced compliance checks. Default: `false`.
- `messageType` (optional): Expected payment message type (pacs.008, pan.001, etc.). If not specified, will be auto-detected.
- `failOnWarnings` (optional): Treat validation warnings as errors and fail the validation. Default: `false`.
- `skipFields` (optional): Comma-separated list of field names to skip during validation.

## Validation Rules

### PACS.008 (Financial Institution to Financial Institution Customer Credit Transfer)

**Group Header Validation:**

- Message ID: Required, max 35 characters, alphanumeric with allowed special characters
- Creation Date Time: Required, ISO 8601 format
- Number of Transactions: Required, numeric, greater than 0
- Instructing/Instructed Agent BIC: Valid BIC format if present

**Transaction Information Validation:**

- Instruction ID: Required, max 35 characters
- End to End ID: Required, max 35 characters
- Amount: Required, numeric, greater than 0, valid currency code (3 chars)
- Debtor/Creditor: Name required (max 70 chars), valid address format
- Accounts: IBAN or other valid account identification required

### PAN.001 (Payment Initiation)

**Group Header Validation:**

- Similar to PACS.008 group header rules

**Payment Information Validation:**

- Payment Information ID: Required
- Payment Method: Required, valid payment method codes
- Requested Execution Date: Valid date format if present

### Generic Validation

- Basic XML structure validation
- Required field presence checks
- Format validation for common fields

## Headers Set by Kamelet

### Validation Results

- `ValidationResult`: Complete validation result object
- `IsValid`: Boolean indicating if message passed validation
- `ErrorCount`: Number of validation errors found
- `WarningCount`: Number of validation warnings found

### Routing Headers

- `ValidationFailed`: Boolean indicating validation failure
- `ValidationErrors`: List of validation errors (if any)
- `ValidationWarnings`: List of validation warnings (if any)
- `ProcessingDecision`: "PROCEED" or "REJECT" based on validation result
- `RouteToErrorQueue`: Boolean indicating message should go to error queue
- `RouteToProcessing`: Boolean indicating message can proceed to processing
- `ErrorReason`: Description of validation failure (if failed)

## Error Codes

### Field Validation Errors

- `REQUIRED_FIELD`: Required field is missing
- `INVALID_FORMAT`: Field format is incorrect
- `INVALID_LENGTH`: Field length exceeds maximum
- `INVALID_VALUE`: Field value is invalid

### System Errors

- `PARSE_ERROR`: XML parsing failed
- `XML_INVALID`: Invalid XML structure

### Warning Codes

- `HIGH_AMOUNT`: Amount is unusually high
- `HIGH_VOLUME`: High number of transactions
- `UNUSUAL_VALUE`: Unusual but valid value detected
- `UNKNOWN_CODE`: Unknown but potentially valid code
- `UNKNOWN_TYPE`: Unknown message type

## Example Usage

### Basic Validation

```yaml
- from:
    uri: "jms:queue:incoming-payments"
    steps:
      - to: "kamelet:k-ingest-validation"
      - choice:
          when:
            - simple: "${header.IsValid} == true"
              steps:
                - to: "jms:queue:validated-payments"
          otherwise:
            - to: "jms:queue:invalid-payments"
```

### Strict Mode with Custom Configuration

```yaml
- from:
    uri: "file:incoming?include=*.xml"
    steps:
      - to: "kamelet:k-ingest-validation?strictMode=true&failOnWarnings=true&messageType=pacs.008"
      - choice:
          when:
            - simple: "${header.ProcessingDecision} == 'PROCEED'"
              steps:
                - log: "Message validated successfully: ${header.ValidationResult.messageId}"
                - to: "kamelet:k-pacs-008-to-cdm"
          otherwise:
            - log: "Validation failed: ${header.ErrorReason}"
            - to: "jms:queue:validation-errors"
```

### Error Handling with Detailed Logging

```yaml
- from:
    uri: "direct:validate-payment"
    steps:
      - to: "kamelet:k-ingest-validation?strictMode=false"
      - choice:
          when:
            - simple: "${header.ErrorCount} > 0"
              steps:
                - log: "Validation errors found: ${header.ErrorCount}"
                - split:
                    simple: "${header.ValidationErrors}"
                    steps:
                      - log: "Error: ${body.errorCode} - ${body.message} (Field: ${body.field})"
                - to: "jms:queue:error-handling"
          when:
            - simple: "${header.WarningCount} > 0"
              steps:
                - log: "Validation warnings: ${header.WarningCount}"
                - to: "jms:queue:processing?priority=low"
          otherwise:
            - log: "Clean validation - proceeding normally"
            - to: "jms:queue:processing"
```

## Integration

This kamelet is part of the PIXEL-V2 project's payment processing pipeline and integrates with:

- **Receipt Kamelets**: k-mq-receipt, k-http-message-receiver, k-file-receipt (validation after receipt)
- **Transformation Kamelets**: k-pacs-008-to-cdm, k-pan-001-to-cdm (validation before transformation)
- **Configuration Kamelet**: k-ref-loader (validation configuration from external service)

## Validation Flow

1. **Pre-validation**: Check message format and basic structure
2. **Type Detection**: Auto-detect or use provided message type
3. **Structure Validation**: Validate XML structure and required elements
4. **Field Validation**: Validate individual field formats and values
5. **Business Rules**: Apply business logic and compliance checks
6. **Result Generation**: Create comprehensive validation result
7. **Header Setting**: Set routing and processing headers
8. **Decision Making**: Determine processing path based on validation outcome
