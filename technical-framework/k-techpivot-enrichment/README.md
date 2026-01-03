# K-Technical Pivot Enrichment Kamelet

## Overview

The K-Technical Pivot Enrichment Kamelet is designed to build TechnicalPivot.json data structures from flow properties. This kamelet takes various parameters related to payment flows and constructs a comprehensive technical pivot JSON structure.

## Purpose

- **Technical Pivot Data Building**: Creates structured TechnicalPivot.json from input parameters
- **Flow Configuration**: Transforms flow metadata into standardized technical pivot format
- **Payment Processing Support**: Specifically designed for payment flow processing systems

## Parameters

### Required Parameters

- `flowId` (integer): Unique identifier for the flow
- `flowCode` (string): The flow code identifier (e.g., "ICHSIC")
- `flowName` (string): Human-readable flow name

### Optional Parameters

- `flowTypeName` (string): Type of the flow (default: "Payment Processing")
- `flowDirection` (string): Direction of the flow - IN, OUT, BID (default: "BID")
- `flowEnabled` (string): Whether the flow is enabled (default: "Y")
- `fileName` (string): Name of the file being processed
- `fileContent` (string): Content of the file being processed
- `inputPartner` (string): Input partner information as JSON string
- `outputPartners` (string): Array of output partners as JSON string
- `countries` (string): Comma-separated list of countries
- `functionalProperties` (string): Functional properties as JSON array string

## Usage Example

```yaml
- to: 'kamelet:k-techpivot-enrichment?flowId=1&flowCode=ICHSIC&flowName=ICHSIC Payment Flow&countries=CH,FR&functionalProperties=[{"Key":"BIC","Value":"BPPBCHGGXXX"}]'
```

## Input/Output

- **Input**: Original message body (preserved)
- **Output**: TechnicalPivot JSON structure as message body
- **Headers**:
  - `TechnicalPivotJson`: The generated JSON structure
  - `ContentType`: Set to "application/json"

## Features

- JSON structure validation and error handling
- Flexible parameter parsing for complex objects
- Preserves original message body in headers
- Comprehensive logging for debugging
- Compatible with Kaoto 2.9

## Dependencies

- camel:kamelet
- camel:jackson
- camel:log
- camel:groovy
- camel:spring-boot-starter
