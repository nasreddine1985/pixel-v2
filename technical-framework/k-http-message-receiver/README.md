# k-http-message-receiver Kamelet

Exposes REST API endpoint to receive JSON messages and persists them to database using JPA.

## Configuration

- Configure Spring Data JPA datasource properties for PostgreSQL in application properties.
- Ensure REST component is configured (camel-platform-http or other HTTP component).

## Usage

The kamelet exposes a REST endpoint that receives JSON messages via HTTP POST and persists them to the database. Ensure `messagePersistenceProcessor` is available as a bean in the runtime.

## Properties

- `path` (required): the REST API endpoint path (default: `/api/messages`)
- `port` (optional): HTTP port for the REST service (default: 8080)
- `consumes` (optional): content type consumed (default: `application/json`)
- `produces` (optional): content type produced (default: `application/json`)

## Example Usage

```yaml
- from:
    uri: "kamelet:k-http-message-receiver?path=/api/payments&port=8080"
    steps:
      - log: "JSON message received and persisted: ${body}"
```

## API Response

The kamelet returns a JSON response:

- Success: `{"status": "success", "message": "Message received and persisted"}`
- The actual persistence is handled by the `messagePersistenceProcessor` bean

## Integration

This kamelet is part of the PIXEL-V2 project's payment processing pipeline, handling message receipt from REST API clients and persistence to the database using JPA.

## Example Request

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"messageId": "12345", "payload": "sample data", "timestamp": "2025-10-19T10:00:00Z"}'
```
