# XSD Schema Files

This folder contains XML Schema Definition (XSD) files used for validating payment messages.

## Available Schema Files

- `pacs.008.001.08.xsd` - PACS.008 Credit Transfer schema
- `pacs.009.001.08.xsd` - PACS.009 Return Payment schema
- `pain.001.001.09.xsd` - PAIN.001 Customer Credit Transfer Initiation schema
- `camt.053.001.08.xsd` - CAMT.053 Bank to Customer Statement schema

## Usage

The kamelet will automatically look for XSD files in this directory when validating XML messages.
Provide the XSD file name (with extension) as the `xsdFileName` parameter when using the kamelet.

## Adding New Schemas

To add new XSD schemas:

1. Place the XSD file in this directory
2. Ensure the file name follows the ISO 20022 naming convention
3. Update the README if needed to document the new schema
