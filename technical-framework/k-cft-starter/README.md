# k-cft-starter Kamelet

Monitors NAS folder for new files, reads XML payment messages line by line and persists them to database using JPA.

## Configuration

- Configure Spring Data JPA datasource properties for Oracle/PostgreSQL in application properties.
- Ensure the NAS directory paths are accessible by the application.
- Configure file system permissions for reading from source directory and writing to processed/error directories.

## Usage

The kamelet monitors a specified directory for new files matching a pattern, reads each file line by line (each line should contain an XML payment message), and persists each message to the database. Ensure `messagePersistenceProcessor` is available as a bean in the runtime.

## Properties

- `directoryPath` (required): the NAS directory path to monitor (default: `/nas/incoming`)
- `filePattern` (optional): file pattern to match (default: `*.xml`)
- `moveProcessed` (optional): whether to move processed files (default: `true`)
- `processedDirectory` (optional): directory for processed files (default: `/nas/processed`)
- `errorDirectory` (optional): directory for files with errors (default: `/nas/error`)
- `delay` (optional): polling delay in milliseconds (default: `5000`)

## Example Usage

```yaml
- from:
    uri: "kamelet:k-file-receipt?directoryPath=/nas/payments&filePattern=payment_*.xml&delay=10000"
    steps:
      - log: "File processing completed: ${header.FileName}"
```

## File Processing

1. **File Detection**: Monitors directory for new files matching the pattern
2. **Line-by-Line Processing**: Splits file content by newlines and processes each line
3. **XML Message Persistence**: Each non-empty line is treated as an XML payment message and persisted
4. **File Management**: Moves processed files to processed directory, failed files to error directory

## File Format

Each file should contain XML payment messages, one per line:

```
<?xml version="1.0"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">...</Document>
<?xml version="1.0"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">...</Document>
<?xml version="1.0"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.03">...</Document>
```

## Integration

This kamelet is part of the PIXEL-V2 project's payment processing pipeline, handling message receipt from NAS file system and persistence to the database using JPA.

## Directory Structure

```
/nas/
├── incoming/       # Source directory (directoryPath)
├── processed/      # Successfully processed files
└── error/          # Files that failed processing
```

## Error Handling

- Files that cannot be processed are moved to the error directory
- Individual line processing errors are logged but don't stop file processing
- Database persistence failures are logged with file and line information
