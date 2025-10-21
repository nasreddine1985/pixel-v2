package com.pixel.v2.ingestion.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Test-specific route builder that replaces the main route builder for testing
 */
@Component
@Profile("test")
public class TestPaymentIngestionRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        // Global error handler
        onException(Exception.class)
            .handled(true)
            .log("Error in payment ingestion test: ${exception.message}")
            .setHeader("ErrorOccurred", constant(true))
            .setHeader("ErrorMessage", simple("${exception.message}"))
            .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .to("direct:error-handler");

        // Main ingestion orchestrator route (same as production)
        from("direct:payment-ingestion")
            .routeId("payment-ingestion-orchestrator")
            .log("Starting payment ingestion orchestration for message: ${body}")
            .setHeader("IngestionStartTime", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .setHeader("ProcessingStage", constant("INGESTION_START"))
            
            // Step 1: Save message to database first
            .to("direct:database-persistence")
            
            // Step 2: Enrich with reference data
            .to("direct:reference-enrichment")
            
            // Step 3: Apply validation
            .to("direct:validation")
            
            // Step 4: Apply idempotence checking
            .to("direct:idempotence-check")
            
            // Step 5: Publish to Kafka (only if validation and idempotence passed)
            .choice()
                .when(header("CanProcess").isEqualTo(true))
                    .to("direct:kafka-publisher")
                .otherwise()
                    .to("direct:rejection-handler")
            .end();

        // Test HTTP route (simplified, no kamelet)
        from("platform-http:/api/v1/test-payments")
            .routeId("http-receipt-route")
            .log("Test message received via HTTP")
            .setHeader("ReceiptChannel", constant("HTTP"))
            .setHeader("ReceiptTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .to("direct:payment-ingestion");

        // Test file route (simplified, no kamelet)  
        from("file:/tmp/test-payments-in?delay=5000&move=/tmp/test-payments-processed&moveFailed=/tmp/test-payments-error")
            .routeId("file-receipt-route")
            .log("Test message received via file")
            .setHeader("ReceiptChannel", constant("FILE"))
            .setHeader("ReceiptTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .to("direct:payment-ingestion");

        // Database persistence step (mock for testing)
        from("direct:database-persistence")
            .routeId("database-persistence")
            .log("Mock persisting message to database")
            .setHeader("ProcessingStage", constant("DATABASE_PERSISTENCE"))
            .setHeader("persistenceStatus", constant("SUCCESS"))
            .setHeader("persistedMessageId", simple("${uuid}"))
            .setHeader("MessagePersisted", constant(true))
            .log("Mock message successfully persisted to database");

        // Reference data enrichment step (mock for testing)
        from("direct:reference-enrichment")
            .routeId("reference-enrichment")
            .log("Mock enriching message with reference data")
            .setHeader("ProcessingStage", constant("REFERENCE_ENRICHMENT"))
            .setHeader("ReferencesLoaded", constant(true))
            .log("Mock reference data enrichment completed");

        // Validation step (mock for testing)
        from("direct:validation")
            .routeId("validation-step")
            .log("Mock message validation")
            .setHeader("ProcessingStage", constant("VALIDATION"))
            .setHeader("IsValid", constant(true))
            .setHeader("ValidationResult", constant("PASSED"))
            .log("Mock message validation passed");

        // Idempotence check step (mock for testing)
        from("direct:idempotence-check")
            .routeId("idempotence-check")
            .log("Mock idempotence check")
            .setHeader("ProcessingStage", constant("IDEMPOTENCE"))
            .setHeader("IsDuplicate", constant(false))
            .setHeader("IdempotenceChecked", constant(true))
            .setHeader("CanProcess", constant(true))
            .log("Mock message idempotence check passed");

        // Kafka publisher step (mock for testing)
        from("direct:kafka-publisher")
            .routeId("kafka-publisher")
            .log("Mock publishing message to Kafka")
            .setHeader("ProcessingStage", constant("KAFKA_PUBLISH"))
            .setHeader("PublishTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .setHeader("KafkaTopic", constant("test-topic"))
            .log("Mock message successfully published to Kafka topic: ${header.KafkaTopic}");

        // Rejection handler (same as production)
        from("direct:rejection-handler")
            .routeId("rejection-handler")
            .log("Processing rejected message: ${header.RejectionReason}")
            .setHeader("ProcessingStage", constant("REJECTION"))
            .setHeader("RejectionTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .log("Rejected message would be sent to dead letter topic");

        // Error handler (same as production)
        from("direct:error-handler")
            .routeId("error-handler")
            .log("Processing system error")
            .setHeader("ProcessingStage", constant("ERROR"))
            .log("Error message would be sent to error topic");

        // Health check endpoint
        from("platform-http:/health")
            .routeId("health-check")
            .setBody(constant("{\"status\":\"UP\",\"service\":\"payment-ingestion-test\"}"))
            .setHeader("Content-Type", constant("application/json"));

        // Metrics endpoint
        from("platform-http:/metrics")
            .routeId("metrics-endpoint")
            .setBody(constant("{\"ingestionRoutes\":{\"active\":true,\"channels\":[\"HTTP_TEST\",\"FILE_TEST\"]}}"))
            .setHeader("Content-Type", constant("application/json"));
    }
}