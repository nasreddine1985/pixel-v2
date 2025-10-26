# k-mq-message-receiver Kamelet

Listens to IBM MQ and persists messages to database using JPA.

## Configuration

- Provide a JMS ConnectionFactory bean (e.g. `jmsConnectionFactory`) in the application context, or configure your runtime to bind IBM MQ connection.
- Configure Spring Data JPA datasource properties for PostgreSQL in application properties.

## Usage

Bind the kamelet source to an actual MQ endpoint in your runtime (the kamelet uses a placeholder internally). Ensure `messagePersistenceProcessor` is available as a bean in the runtime.

## Properties

- `destination` (required): the MQ queue name to listen to
- `jmsConnectionFactoryRef` (required): reference to the JMS connection factory bean

## Example Usage

```yaml
- from:
    uri: "kamelet:k-mq-receipt?destination=INPUT.QUEUE&jmsConnectionFactoryRef=mqConnectionFactory"
    steps:
      - log: "Message received and persisted: ${body}"
```

## Integration

This kamelet is part of the PIXEL-V2 project's PACS.008 processing pipeline, handling message receipt from IBM MQ and persistence to the database using JPA.
