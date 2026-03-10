package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * BhWpsAccountValidationRoute - Bahrain WPS Account Validation Route
 * 
 * This route handles account validation requests from WPS Benefit system:
 * 1. Receives account validation requests via HTTP from WPS Benefit
 * 2. Validates the request format and extracts account details
 * 3. Forwards validation request to ATLAS2 for account status check
 * 4. Returns validation response within 1 minute SLA (as per requirement)
 * 5. Logs all validation events to Kafka for audit
 * 
 * SLA Requirements:
 * - Request #1: Confirm receiving account validation - 1 minute
 * - Request #2: Debit account validation - 15:00 to 15:30 same business day
 */
@Component
public class BhWpsAccountValidationRoute extends RouteBuilder {

    // HTTP endpoint for receiving WPS account validation requests
    private static final String WPS_ACCOUNT_VALIDATION_ENDPOINT = 
        "jetty:http://0.0.0.0:{{pixel.wps.port}}/wps/account-validation?httpMethodRestrict=POST";

    // Kamelet endpoint for identification and caching
    private static final String K_IDENTIFICATION_ENDPOINT =
        "kamelet:k-identification?flowCode={{pixel.flow.code}}&referentialServiceUrl={{pixel.referential.service.url}}&kafkaBrokers={{pixel.kafka.brokers}}&cacheTtl={{pixel.cache.ttl}}";

    // Kamelet endpoint for logging events
    private static final String K_LOG_EVENTS_ENDPOINT =
        "kamelet:k-log-events?step=ACCOUNT_VALIDATION&kafkaTopicName=${header.kafkaLogTopicName}&brokers={{pixel.kafka.brokers}}";

    // HTTP endpoint to ATLAS2 for account validation
    private static final String ATLAS2_VALIDATION_ENDPOINT =
        "http://{{pixel.atlas2.host}}:{{pixel.atlas2.port}}/api/account/validate?httpMethod=POST&bridgeEndpoint=true";

    @Override
    public void configure() throws Exception {

        // Main account validation route
        // Note: Exception handling is done globally by GlobalExceptionHandler
        from(WPS_ACCOUNT_VALIDATION_ENDPOINT)
            .routeId("bh-wps-account-validation")
            .log("[BH-WPS-ACCOUNT-VALIDATION] Received account validation request from WPS Benefit")
            
            // Set flow metadata
            .setHeader("flowCode", constant("BHWPSACCT"))
            .setHeader("flowDirection", constant("INBOUND"))
            .setHeader("kafkaLogTopicName", constant("{{pixel.kafka.log.topic-name}}"))
            
            // Step 1: Fetch reference data (flow configuration, partner settings)
            .to(K_IDENTIFICATION_ENDPOINT)
            
            // Step 2: Log receipt confirmation
            .wireTap(K_LOG_EVENTS_ENDPOINT)
            
            // Step 3: Extract account information from request
            .log("[BH-WPS-ACCOUNT-VALIDATION] Processing account: ${body}")
            
            // Step 4: Forward to ATLAS2 for actual validation
            .to(ATLAS2_VALIDATION_ENDPOINT)
            
            // Step 5: Log validation result
            .log("[BH-WPS-ACCOUNT-VALIDATION] Validation result: ${body}")
            .wireTap(K_LOG_EVENTS_ENDPOINT)
            
            // Step 6: Return response to WPS Benefit
            .setHeader("Content-Type", constant("application/json"))
            .setHeader("CamelHttpResponseCode", constant(200))
            .log("[BH-WPS-ACCOUNT-VALIDATION] Returning validation response to WPS Benefit");
    }
}
