package com.pixel.v2.ingestion.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Payment Ingestion Route Builder
 * 
 * Defines the main ingestion flow orchestrating multiple kamelets:
 * 1. Receipt from various channels (MQ, API, File)
 * 2. Reference data enrichment
 * 3. Validation
 * 4. Idempotence checking
 * 5. Publishing to Kafka
 */
@Component
public class PaymentIngestionRouteBuilder extends RouteBuilder {

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @Override
    public void configure() throws Exception {
        
        // Global error handler
        onException(Exception.class)
            .handled(true)
            .log("Error in payment ingestion: ${exception.message}")
            .setHeader("ErrorOccurred", constant(true))
            .setHeader("ErrorMessage", simple("${exception.message}"))
            .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .to("direct:error-handler");

        // Main ingestion orchestrator route
        from("direct:payment-ingestion")
            .routeId("payment-ingestion-orchestrator")
            .log("Starting payment ingestion orchestration for message: ${body}")
            .setHeader("IngestionStartTime", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .setHeader("ProcessingStage", constant("INGESTION_START"))
            
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

        // MQ Series receipt route (skip in test mode)
        if (activeProfiles == null || !activeProfiles.contains("test")) {
            from("jms:queue:{{ingestion.mq.input.queue:PAYMENT_INPUT}}")
                .routeId("mq-receipt-route")
                .log("Received message from MQ Series: ${body}")
                .setHeader("ReceiptChannel", constant("MQ_SERIES"))
                .setHeader("ReceiptTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
                .to("kamelet:k-mq-receipt")
                .to("direct:payment-ingestion");
        }

        // API receipt route
        from("platform-http:/api/v1/payments?httpMethodRestrict=POST")
            .routeId("api-receipt-route")
            .log("Received message from API: ${body}")
            .setHeader("ReceiptChannel", constant("REST_API"))
            .setHeader("ReceiptTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .to("kamelet:k-api-receipt")
            .to("direct:payment-ingestion");

        // File (CFT) receipt route
        from("file:{{ingestion.file.input.directory:/tmp/payments-in}}?include={{ingestion.file.pattern:*.xml}}&move={{ingestion.file.processed.directory:/tmp/payments-processed}}")
            .routeId("file-receipt-route")
            .log("Received message from File: ${body}")
            .setHeader("ReceiptChannel", constant("FILE_CFT"))
            .setHeader("ReceiptTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .setHeader("FileName", simple("${header.CamelFileName}"))
            .to("kamelet:k-file-receipt")
            .to("direct:payment-ingestion");

        // Reference data enrichment step
        from("direct:reference-enrichment")
            .routeId("reference-enrichment")
            .log("Enriching message with reference data")
            .setHeader("ProcessingStage", constant("REFERENCE_ENRICHMENT"))
            .to("kamelet:k-ref-loader")
            .log("Reference data enrichment completed");

        // Validation step
        from("direct:validation")
            .routeId("validation-step")
            .log("Starting message validation")
            .setHeader("ProcessingStage", constant("VALIDATION"))
            .to("kamelet:k-ingest-validation")
            .choice()
                .when(header("IsValid").isEqualTo(true))
                    .log("Message validation passed")
                .otherwise()
                    .log("Message validation failed: ${header.ValidationResult}")
                    .setHeader("RejectionReason", constant("VALIDATION_FAILED"))
            .end();

        // Idempotence check step
        from("direct:idempotence-check")
            .routeId("idempotence-check")
            .log("Starting idempotence check")
            .setHeader("ProcessingStage", constant("IDEMPOTENCE"))
            
            // Use k-idempotence kamelet
            .to("kamelet:k-idempotence?messageId=${header.MessageId}&checkMode=PROCESS")
            
            .choice()
                .when(header("IsDuplicate").isEqualTo(true))
                    .log("Duplicate message detected, checking action")
                    .choice()
                        .when(header("ShouldReject").isEqualTo(true))
                            .setHeader("RejectionReason", constant("DUPLICATE_MESSAGE"))
                            .setHeader("CanProcess", constant(false))
                        .when(header("ShouldIgnore").isEqualTo(true))
                            .log("Ignoring duplicate message as per configuration")
                            .setHeader("CanProcess", constant(false))
                        .otherwise()
                            .log("Processing duplicate with warning")
                    .endChoice()
                .otherwise()
                    .log("Message idempotence check passed")
            .endChoice();

        // Kafka publisher step
        from("direct:kafka-publisher")
            .routeId("kafka-publisher")
            .log("Publishing message to Kafka")
            .setHeader("ProcessingStage", constant("KAFKA_PUBLISH"))
            .setHeader("PublishTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            
            // Determine Kafka topic based on message type or channel
            .choice()
                .when(header("ExpectedMessageType").isEqualTo("pacs.008"))
                    .setHeader("KafkaTopic", simple("{{ingestion.kafka.topic.pacs008:payments-pacs008}}"))
                .when(header("ExpectedMessageType").isEqualTo("pan.001"))
                    .setHeader("KafkaTopic", simple("{{ingestion.kafka.topic.pan001:payments-pan001}}"))
                .otherwise()
                    .setHeader("KafkaTopic", simple("{{ingestion.kafka.topic.default:payments-processed}}"))
            .end()
            
            // Set Kafka message key (use primary identifier if available)
            .setHeader("kafka.KEY", simple("${header.PrimaryIdentifier:${exchangeId}}"))
            
            // Add processing metadata to the message
            .process(exchange -> {
                String originalBody = exchange.getIn().getBody(String.class);
                String enrichedMessage = String.format("""
                    {
                        "metadata": {
                            "receiptChannel": "%s",
                            "receiptTimestamp": "%s",
                            "ingestionStartTime": "%s",
                            "publishTimestamp": "%s",
                            "primaryIdentifier": "%s",
                            "messageType": "%s",
                            "validationPassed": %s,
                            "duplicateCheck": %s
                        },
                        "payload": %s
                    }
                    """,
                    exchange.getIn().getHeader("ReceiptChannel", "UNKNOWN"),
                    exchange.getIn().getHeader("ReceiptTimestamp", ""),
                    exchange.getIn().getHeader("IngestionStartTime", ""),
                    exchange.getIn().getHeader("PublishTimestamp", ""),
                    exchange.getIn().getHeader("PrimaryIdentifier", ""),
                    exchange.getIn().getHeader("ExpectedMessageType", "UNKNOWN"),
                    exchange.getIn().getHeader("IsValid", false),
                    exchange.getIn().getHeader("IdempotenceChecked", false),
                    originalBody != null ? originalBody : "null"
                );

                
                exchange.getIn().setBody(enrichedMessage);
            })
            
            .toD("kafka:${header.KafkaTopic}?brokers={{ingestion.kafka.brokers:localhost:9092}}")
            .log("Message successfully published to Kafka topic: ${header.KafkaTopic}");

        // Rejection handler
        from("direct:rejection-handler")
            .routeId("rejection-handler")
            .log("Processing rejected message: ${header.RejectionReason}")
            .setHeader("ProcessingStage", constant("REJECTION"))
            .setHeader("RejectionTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            
            // Create rejection message
            .process(exchange -> {
                String rejectionMessage = String.format("""
                    {
                        "rejectionInfo": {
                            "reason": "%s",
                            "timestamp": "%s",
                            "receiptChannel": "%s",
                            "originalMessageId": "%s",
                            "errorDetails": "%s"
                        },
                        "originalMessage": %s
                    }
                    """,
                    exchange.getIn().getHeader("RejectionReason", "UNKNOWN"),
                    exchange.getIn().getHeader("RejectionTimestamp", ""),
                    exchange.getIn().getHeader("ReceiptChannel", "UNKNOWN"),
                    exchange.getIn().getHeader("PrimaryIdentifier", ""),
                    exchange.getIn().getHeader("ValidationResult", ""),
                    exchange.getIn().getBody(String.class)
                );
                exchange.getIn().setBody(rejectionMessage);
            })
            
            .to("kafka:{{ingestion.kafka.topic.rejected:payments-rejected}}?brokers={{ingestion.kafka.brokers:localhost:9092}}")
            .log("Rejected message sent to dead letter topic");

        // Error handler
        from("direct:error-handler")
            .routeId("error-handler")
            .log("Processing system error")
            .setHeader("ProcessingStage", constant("ERROR"))
            
            // Create error message
            .process(exchange -> {
                String errorMessage = String.format("""
                    {
                        "errorInfo": {
                            "message": "%s",
                            "timestamp": "%s",
                            "receiptChannel": "%s",
                            "processingStage": "%s"
                        },
                        "originalMessage": %s
                    }
                    """,
                    exchange.getIn().getHeader("ErrorMessage", "Unknown error"),
                    exchange.getIn().getHeader("ErrorTimestamp", ""),
                    exchange.getIn().getHeader("ReceiptChannel", "UNKNOWN"),
                    exchange.getIn().getHeader("ProcessingStage", "UNKNOWN"),
                    exchange.getIn().getBody(String.class)
                );
                exchange.getIn().setBody(errorMessage);
            })
            
            .to("kafka:{{ingestion.kafka.topic.errors:payments-errors}}?brokers={{ingestion.kafka.brokers:localhost:9092}}")
            .log("Error message sent to error topic");

        // Health check endpoint
        from("platform-http:/health")
            .routeId("health-check")
            .setBody(constant("{\"status\":\"UP\",\"service\":\"payment-ingestion\"}"))
            .setHeader("Content-Type", constant("application/json"));

        // Metrics endpoint
        from("platform-http:/metrics")
            .routeId("metrics-endpoint")
            .setBody(constant("{\"ingestionRoutes\":{\"active\":true,\"channels\":[\"MQ_SERIES\",\"REST_API\",\"FILE_CFT\"]}}"))
            .setHeader("Content-Type", constant("application/json"));
    }
}