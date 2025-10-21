package com.pixel.v2.processing.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.LoggingLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pixel.v2.processing.processors.MessageTypeProcessor;

/**
 * Payment Message Processing Route Builder
 * 
 * This route builder defines Apache Camel routes for processing payment messages
 * received from k-kafka-message-receiver and routing them to appropriate
 * CDM transformer kamelets based on message type.
 * 
 * Route Flow:
 * 1. Listen for messages from Kafka (via k-kafka-message-receiver)
 * 2. Detect message type (pacs.008 or pan.001)
 * 3. Route to appropriate transformer kamelet
 * 4. Handle transformation results
 * 5. Forward to CDM output endpoint
 */
@Component
public class PaymentMessageRoutes extends RouteBuilder {

    @Autowired
    private MessageTypeProcessor messageTypeProcessor;

    @Value("${processing.kafka.input.endpoint:direct:kafka-message-processing}")
    private String kafkaInputEndpoint;

    @Value("${processing.cdm.output.endpoint:direct:cdm-output}")
    private String cdmOutputEndpoint;

    @Value("${processing.error.endpoint:direct:error-handling}")
    private String errorEndpoint;

    @Override
    public void configure() throws Exception {
        
        // Global error handling
        onException(Exception.class)
            .log(LoggingLevel.ERROR, "[PROCESSING-ERROR] Error processing message: ${exception.message}")
            .to(errorEndpoint)
            .handled(true);

        // Main processing route - receives messages from k-kafka-message-receiver
        from(kafkaInputEndpoint)
            .routeId("payment-message-processing-main")
            .log(LoggingLevel.INFO, "[PROCESSING-MAIN] Received message for processing")
            .process(messageTypeProcessor)
            .log(LoggingLevel.INFO, "[PROCESSING-MAIN] Message type: ${header.MessageType}, routing to: ${header.RouteTarget}")
            .recipientList(simple("${header.RouteTarget}"))
            .end();

        // Route for pacs.008 messages - calls k-pacs-008-to-cdm kamelet
        from("direct:pacs-008-transform")
            .routeId("pacs-008-transformation")
            .log(LoggingLevel.INFO, "[PACS-008-TRANSFORM] Processing pacs.008 message")
            .to("kamelet:k-pacs-008-to-cdm")
            .log(LoggingLevel.INFO, "[PACS-008-TRANSFORM] Successfully transformed pacs.008 to CDM")
            .to(cdmOutputEndpoint);

        // Route for pan.001 messages - calls k-pan-001-to-cdm kamelet
        from("direct:pan-001-transform")
            .routeId("pan-001-transformation")
            .log(LoggingLevel.INFO, "[PAN-001-TRANSFORM] Processing pan.001 message")
            .to("kamelet:k-pan-001-to-cdm")
            .log(LoggingLevel.INFO, "[PAN-001-TRANSFORM] Successfully transformed pan.001 to CDM")
            .to(cdmOutputEndpoint);

        // Route for unknown message types
        from("direct:unknown-message")
            .routeId("unknown-message-handling")
            .log(LoggingLevel.WARN, "[UNKNOWN-MESSAGE] Received message with unknown type")
            .setHeader("ErrorCode", constant("UNKNOWN_MESSAGE_TYPE"))
            .setHeader("ErrorDescription", constant("Message type could not be determined"))
            .to(errorEndpoint);

        // CDM output route - processes transformed CDM messages
        from(cdmOutputEndpoint)
            .routeId("cdm-output-processing")
            .log(LoggingLevel.INFO, "[CDM-OUTPUT] Processing transformed CDM message")
            .setHeader("TransformationComplete", constant(true))
            .setHeader("OutputTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
            .log(LoggingLevel.INFO, "[CDM-OUTPUT] CDM message processing complete");

        // Error handling route
        from(errorEndpoint)
            .routeId("error-handling")
            .log(LoggingLevel.ERROR, "[ERROR-HANDLER] Processing error: ${header.ErrorCode} - ${header.ErrorDescription}")
            .setHeader("ErrorHandled", constant(true))
            .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"));

        // Health check route
        from("direct:health-check")
            .routeId("health-check")
            .log(LoggingLevel.DEBUG, "[HEALTH-CHECK] Processing health check request")
            .setBody(constant("{'status': 'UP', 'service': 'payment-message-processing'}"))
            .setHeader("Content-Type", constant("application/json"));

        // Metrics route for monitoring
        from("direct:metrics")
            .routeId("metrics-collection")
            .log(LoggingLevel.DEBUG, "[METRICS] Collecting processing metrics")
            .setBody(simple("{'processedMessages': '${exchangeProperty.CamelMessageHistory}', 'timestamp': '${date:now:yyyy-MM-dd HH:mm:ss}'}"))
            .setHeader("Content-Type", constant("application/json"));
    }
}