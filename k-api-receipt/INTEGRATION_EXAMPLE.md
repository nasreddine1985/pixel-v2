# Example usage of k-api-receipt kamelet

This example shows how to integrate the `k-api-receipt` kamelet into a Camel route.

## Integration Example

```java
@Component
public class ApiReceiptFlowRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // REST API Receipt Flow - Alternative to MQ receipt
        from("kamelet:k-api-receipt?"
            + "path=/api/pacs008"
            + "&port=8080"
            + "&consumes=application/json"
            + "&produces=application/json")
            .routeId("api-pacs008-receipt-flow")
            .log("Step 1: Receiving PACS.008 message from REST API")
            .log("API message received: ${body}")
            .to("direct:enrichPacs008Message");

        // Alternative endpoint for different message types
        from("kamelet:k-api-receipt?"
            + "path=/api/payments"
            + "&port=8080")
            .routeId("api-payments-receipt-flow")
            .log("Payment message received via API: ${body}")
            .to("direct:processPaymentMessage");
    }
}
```

## Configuration Properties

Add these properties to `application.properties`:

```properties
# API Receipt Configuration
api.receipt.path=/api/messages
api.receipt.port=8080
api.receipt.host=0.0.0.0

# Enable REST component
camel.component.rest.component=platform-http
camel.component.platform-http.enabled=true
```

## Testing the API

```bash
# Test the endpoint
curl -X POST http://localhost:8080/api/pacs008 \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "MSG001",
    "messageType": "pacs.008.001.02",
    "payload": "<?xml version=\"1.0\"?><Document>...</Document>",
    "timestamp": "2025-10-19T10:00:00Z"
  }'

# Expected response
{"status": "success", "message": "Message received and persisted"}
```

## Spring Boot Configuration

To use this kamelet in a Spring Boot application, ensure you have:

1. `MessagePersistenceProcessor` bean configured
2. JPA EntityManager properly configured
3. REST component available (camel-platform-http)

```java
@Configuration
public class ApiReceiptConfig {

    @Bean
    public MessagePersistenceProcessor messagePersistenceProcessor() {
        return new MessagePersistenceProcessor();
    }
}
```
