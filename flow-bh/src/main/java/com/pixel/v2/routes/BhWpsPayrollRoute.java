package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * BhWpsPayrollRoute - Bahrain WPS Payroll Processing Route
 * 
 * This route handles payroll file processing from WPS Benefit system:
 * 1. Receives payroll files via HTTP from WPS Benefit
 * 2. Validates and processes payroll data
 * 3. Performs duplicate check
 * 4. Routes payments to DOME for processing
 * 5. Sends status reports back to WPS Benefit
 * 6. Settles payments via EFTS Bahrain (FAWRI)
 * 
 * SLA Requirements:
 * - Request #3: Confirm receiving payroll file - 1 minute
 * - Request #4: EFTS payroll validation status - by 12:55 of salary value date
 * - Request #5: EFTS payroll validation status - by 14:00 of salary value date
 * - Request #6: Employer approval status - 12:55 to 13:55 same business day
 * - Request #7: Payment processing failure - by 15:30 salary value date
 */
@Component
public class BhWpsPayrollRoute extends RouteBuilder {

    // HTTP endpoint for receiving WPS payroll files
    private static final String WPS_PAYROLL_RECEIVE_ENDPOINT = 
        "jetty:http://0.0.0.0:{{pixel.wps.port}}/wps/payroll?httpMethodRestrict=POST";

    // Kamelet endpoint for identification and caching
    private static final String K_IDENTIFICATION_ENDPOINT =
        "kamelet:k-identification?flowCode={{pixel.flow.code}}&referentialServiceUrl={{pixel.referential.service.url}}&kafkaBrokers={{pixel.kafka.brokers}}&cacheTtl={{pixel.cache.ttl}}";

    // Kamelet endpoint for duplicate check
    private static final String K_DUPLICATE_CHECK_ENDPOINT =
        "kamelet:k-duplicate-check?dataSource={{pixel.datasource.name}}&disableCheckDB={{pixel.duplicate.check.disable:false}}&disableCheckMaxFileSize={{pixel.duplicate.check.max.file.size.disable:false}}&maxRetryCount={{pixel.duplicate.check.max.retry:3}}&retrySleepPeriod={{pixel.duplicate.check.retry.sleep:1000}}";

    // Kamelet endpoint for XSD validation (if WPS provides XML)
    private static final String K_XSD_VALIDATION_ENDPOINT =
        "kamelet:k-xsd-validation?xsdFileName=wps-payroll.xsd&validationMode=LENIENT";

    // Kamelet endpoint for dynamic publisher to DOME
    private static final String K_DYNAMIC_PUBLISHER_ENDPOINT =
        "kamelet:k-dynamic-publisher?header-name=RefFlowData";

    // Kamelet endpoint for flow summary logging
    private static final String K_LOG_FLOW_SUMMARY_ENDPOINT =
        "kamelet:k-log-flow-summary?step=COMPLETED&kafkaTopicName=${header.kafkaFlowSummaryTopicName}&brokers={{pixel.kafka.brokers}}";

    // HTTP endpoint to send status back to WPS Benefit
    private static final String WPS_STATUS_CALLBACK_ENDPOINT =
        "http://{{pixel.wps.benefit.host}}:{{pixel.wps.benefit.port}}/api/payroll/status?httpMethod=POST&bridgeEndpoint=true";

    @Override
    public void configure() throws Exception {

        // Main payroll processing route
        // Note: Exception handling is done globally by GlobalExceptionHandler
        from(WPS_PAYROLL_RECEIVE_ENDPOINT)
            .routeId("bh-wps-payroll-processing")
            .log("[BH-WPS-PAYROLL] Received payroll file from WPS Benefit")
            
            // Set flow metadata
            .setHeader("flowCode", constant("BHWPSPRL"))
            .setHeader("flowDirection", constant("INBOUND"))
            .setHeader("kafkaFlowSummaryTopicName", constant("{{pixel.kafka.flow-summary.topic-name}}"))
            
            // Step 1: Send immediate confirmation (within 1 minute SLA)
            .wireTap("direct:wps-send-receipt-confirmation")
            
            // Step 2: Fetch reference data (flow configuration, partner settings)
            .to(K_IDENTIFICATION_ENDPOINT)
            
            // Step 3: Duplicate check
            .to(K_DUPLICATE_CHECK_ENDPOINT)
            
            // Step 4: Validate payroll file structure
            //.to(K_XSD_VALIDATION_ENDPOINT)
            
            // Step 5: Archive payroll file to NAS
            .to("file://{{pixel.nas.bh.archive.path}}?fileName=${header.flowOccurId}_payroll_${date:now:yyyyMMddHHmmss}.xml")
            
            // Step 6: Route to DOME for payment processing
            .log("[BH-WPS-PAYROLL] Routing payroll to DOME for processing")
            .to(K_DYNAMIC_PUBLISHER_ENDPOINT)
            
            // Step 7: Log flow summary
            .wireTap(K_LOG_FLOW_SUMMARY_ENDPOINT)
            
            // Step 8: Return success response to WPS Benefit
            .setHeader("Content-Type", constant("application/json"))
            .setHeader("CamelHttpResponseCode", constant(200))
            .setBody(simple("{\"status\":\"SUCCESS\",\"flowOccurId\":\"${header.flowOccurId}\",\"timestamp\":\"${date:now:yyyy-MM-dd'T'HH:mm:ss}\"}"))
            .log("[BH-WPS-PAYROLL] Payroll processing completed successfully");

        // Sub-route: Send receipt confirmation to WPS Benefit (1 minute SLA)
        from("direct:wps-send-receipt-confirmation")
            .routeId("bh-wps-send-receipt-confirmation")
            .log("[BH-WPS-PAYROLL] Sending receipt confirmation to WPS Benefit")
            .setBody(simple("{\"status\":\"RECEIVED\",\"flowOccurId\":\"${header.flowOccurId}\",\"timestamp\":\"${date:now:yyyy-MM-dd'T'HH:mm:ss}\"}"))
            .setHeader("Content-Type", constant("application/json"))
            .to(WPS_STATUS_CALLBACK_ENDPOINT)
            .log("[BH-WPS-PAYROLL] Receipt confirmation sent to WPS Benefit");

        // Sub-route: Send error status to WPS Benefit
        from("direct:wps-send-error-status")
            .routeId("bh-wps-send-error-status")
            .log("[BH-WPS-PAYROLL] Sending error status to WPS Benefit")
            .setBody(simple("{\"status\":\"ERROR\",\"flowOccurId\":\"${header.flowOccurId}\",\"message\":\"${exception.message}\",\"timestamp\":\"${date:now:yyyy-MM-dd'T'HH:mm:ss}\"}"))
            .setHeader("Content-Type", constant("application/json"))
            .to(WPS_STATUS_CALLBACK_ENDPOINT)
            .log("[BH-WPS-PAYROLL] Error status sent to WPS Benefit");

        // Route: Payment status callback from DOME
        from("direct:dome-payment-status")
            .routeId("bh-dome-payment-status-callback")
            .log("[BH-WPS-PAYROLL] Received payment status from DOME: ${body}")
            .to(WPS_STATUS_CALLBACK_ENDPOINT)
            .log("[BH-WPS-PAYROLL] Payment status forwarded to WPS Benefit");
    }
}
