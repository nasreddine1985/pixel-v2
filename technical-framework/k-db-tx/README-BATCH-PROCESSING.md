# k-db-tx-messages Kamelet Usage Examples

## Single Message Processing

The kamelet automatically detects single messages and processes them individually:

```yaml
- to:
    uri: "kamelet:k-db-tx-messages?dataSource=#dataSource&tableName=pixel_v2.tb_messages"
```

## Batch Message Processing

When the message body contains a Collection, the kamelet automatically uses batch processing for better performance:

### Collection of Maps

```yaml
- setBody:
    simple: |
      [
        {
          "messageId": "MSG-001", 
          "correlationId": "CORR-001", 
          "messageType": "PACS008", 
          "source": "MQ", 
          "payload": "XML_CONTENT_1"
        },
        {
          "messageId": "MSG-002", 
          "correlationId": "CORR-002", 
          "messageType": "PACS008", 
          "source": "MQ", 
          "payload": "XML_CONTENT_2"
        }
      ]
- to:
    uri: "kamelet:k-db-tx-messages?dataSource=#dataSource&tableName=pixel_v2.tb_messages&enableBatchProcessing=true"
```

### Collection of Strings with Headers

```yaml
- setHeader:
    name: "messageType"
    constant: "PACS008"
- setHeader:
    name: "source"
    constant: "KAFKA"
- setHeader:
    name: "correlationId"
    constant: "BATCH-001"
- setBody:
    simple: |
      ["MESSAGE_PAYLOAD_1", "MESSAGE_PAYLOAD_2", "MESSAGE_PAYLOAD_3"]
- to:
    uri: "kamelet:k-db-tx-messages?dataSource=#dataSource&tableName=pixel_v2.tb_messages&enableBatchProcessing=true"
```

## Configuration Options

- `enableBatchProcessing`: Enable/disable batch processing (default: true)
- `dataSource`: Reference to PostgreSQL DataSource bean
- `tableName`: Database table name (default: pixel_v2.tb_messages)

## Response Headers

After processing, the kamelet sets these headers:

### Single Message

- `PersistenceStatus`: SUCCESS/FAILED
- `PersistenceOperation`: INSERT/INSERT_FAILED
- `PersistenceRecordId`: The message ID

### Batch Processing

- `PersistenceStatus`: SUCCESS/PARTIAL_SUCCESS/FAILED
- `PersistenceOperation`: BATCH_INSERT/BATCH_INSERT_FAILED
- `BatchSize`: Total number of messages in batch
- `SuccessCount`: Number of successfully persisted messages
- `FailureCount`: Number of failed messages

## Error Handling

The kamelet maintains the same error handling behavior:

- Catches database errors and logs them
- Throws RuntimeException to parent route for Error Handler Route processing
- Supports both single message and batch transaction rollback
