package com.pixel.v2.referentiel.demo;

import com.pixel.v2.referentiel.processor.FlowReferenceProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;

/**
 * Demonstration of K-Referentiel Data Loader functionality Shows how to load FlowReference
 * configuration from external service
 */
public class ReferentielLoaderDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== K-REFERENTIEL DATA LOADER DEMO ===\n");

        FlowReferenceProcessor processor = new FlowReferenceProcessor();

        // Demo 1: Valid FlowReference Response
        System.out.println("--- Demo 1: Valid FlowReference Response ---");
        demonstrateValidResponse(processor);

        // Demo 2: Empty Service Response (Default FlowReference)
        System.out.println("\n--- Demo 2: Service Unavailable (Default FlowReference) ---");
        demonstrateServiceUnavailable(processor);

        // Demo 3: Invalid JSON Response (Fallback FlowReference)
        System.out.println("\n--- Demo 3: Invalid JSON Response (Fallback FlowReference) ---");
        demonstrateInvalidResponse(processor);

        System.out.println("\n=== DEMO COMPLETED ===");
    }

    private static void demonstrateValidResponse(FlowReferenceProcessor processor)
            throws Exception {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());

        // Simulate valid FlowReference JSON response
        String validFlowReferenceJson = """
                {
                    "flowId": "FLOW_PACS008_PROD_001",
                    "flowCode": "PACS008",
                    "flowName": "PACS.008 Credit Transfer Production",
                    "status": "ACTIVE",
                    "flowType": "PAYMENT",
                    "railMode": "INSTANT",
                    "priority": 1,
                    "slaMaxLatencyMs": 3000,
                    "sourceChannel": "HTTP",
                    "sourceSystem": "BANK_GATEWAY",
                    "sourceFormat": "XML",
                    "targetSystem": "CORE_BANKING",
                    "targetChannel": "KAFKA",
                    "splitEnabled": "TRUE",
                    "splitChunkSize": 100,
                    "concatEnabled": "FALSE",
                    "retentionInEnabled": "TRUE",
                    "retentionInMode": "ARCHIVE",
                    "retentionInDays": 365,
                    "retentionOutEnabled": "TRUE",
                    "retentionOutDays": 90,
                    "shapingEnabled": "TRUE",
                    "shapingMaxTrxPerMin": 5000,
                    "shapingStrategy": "THROTTLE",
                    "piiLevel": "HIGH",
                    "encryptionRequired": "TRUE",
                    "drStrategy": "ACTIVE_PASSIVE",
                    "version": "2.1.0",
                    "comments": "Production PACS.008 configuration with high throughput"
                }
                """;

        exchange.getIn().setHeader("FlowCode", "PACS008");
        exchange.getIn().setHeader("ServiceUrl", "http://localhost:8099");
        exchange.getIn().setBody(validFlowReferenceJson);

        processor.process(exchange);

        System.out.println(
                "FlowReference Loaded: " + exchange.getIn().getHeader("FlowReferenceLoaded"));
        System.out.println("Flow ID: " + exchange.getIn().getHeader("FlowId"));
        System.out.println("Flow Code: " + exchange.getIn().getHeader("FlowCode"));
        System.out.println("Flow Name: " + exchange.getIn().getHeader("FlowName"));
        System.out.println("Status: " + exchange.getIn().getHeader("FlowStatus"));
        System.out.println("Rail Mode: " + exchange.getIn().getHeader("RailMode"));
        System.out.println("Priority: " + exchange.getIn().getHeader("Priority"));
        System.out
                .println("SLA Max Latency (ms): " + exchange.getIn().getHeader("SlaMaxLatencyMs"));
        System.out.println("Split Enabled: " + exchange.getIn().getHeader("SplitEnabled"));
        System.out.println("Split Chunk Size: " + exchange.getIn().getHeader("SplitChunkSize"));
        System.out.println(
                "Shaping Max TRX/min: " + exchange.getIn().getHeader("ShapingMaxTrxPerMin"));
        System.out.println("PII Level: " + exchange.getIn().getHeader("PiiLevel"));
        System.out.println(
                "Encryption Required: " + exchange.getIn().getHeader("EncryptionRequired"));
        System.out.println("Version: " + exchange.getIn().getHeader("FlowVersion"));

        // Show first 200 characters of JSON body
        String jsonBody = exchange.getIn().getBody(String.class);
        System.out.println("JSON Body (first 200 chars): "
                + jsonBody.substring(0, Math.min(200, jsonBody.length())) + "...");
    }

    private static void demonstrateServiceUnavailable(FlowReferenceProcessor processor)
            throws Exception {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());

        exchange.getIn().setHeader("FlowCode", "PAIN001");
        exchange.getIn().setHeader("ServiceUrl", "http://localhost:8099");
        exchange.getIn().setBody(""); // Empty response simulating service unavailable

        processor.process(exchange);

        System.out.println(
                "FlowReference Loaded: " + exchange.getIn().getHeader("FlowReferenceLoaded"));
        System.out.println("Flow ID (Default): " + exchange.getIn().getHeader("FlowId"));
        System.out.println("Flow Code: " + exchange.getIn().getHeader("FlowCode"));
        System.out.println("Flow Name (Default): " + exchange.getIn().getHeader("FlowName"));
        System.out.println("Status (Default): " + exchange.getIn().getHeader("FlowStatus"));
        System.out.println("Rail Mode (Default): " + exchange.getIn().getHeader("RailMode"));
        System.out.println("Priority (Default): " + exchange.getIn().getHeader("Priority"));
        System.out
                .println("Split Enabled (Default): " + exchange.getIn().getHeader("SplitEnabled"));
        System.out.println(
                "Retention In Days (Default): " + exchange.getIn().getHeader("RetentionInDays"));

        String jsonBody = exchange.getIn().getBody(String.class);
        System.out.println("Default Configuration Generated: "
                + (jsonBody.contains("Default configuration - service unavailable") ? "YES"
                        : "NO"));
    }

    private static void demonstrateInvalidResponse(FlowReferenceProcessor processor)
            throws Exception {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());

        exchange.getIn().setHeader("FlowCode", "CAMT053");
        exchange.getIn().setHeader("ServiceUrl", "http://localhost:8099");
        exchange.getIn().setBody("{ invalid json response from service"); // Invalid JSON

        processor.process(exchange);

        System.out.println(
                "FlowReference Loaded: " + exchange.getIn().getHeader("FlowReferenceLoaded"));
        System.out.println("Error Occurred: "
                + (exchange.getIn().getHeader("FlowReferenceError") != null ? "YES" : "NO"));
        System.out.println("Error Message: " + exchange.getIn().getHeader("FlowReferenceError"));
        System.out.println("Fallback Flow ID: " + exchange.getIn().getHeader("FlowId"));
        System.out.println("Fallback Flow Code: " + exchange.getIn().getHeader("FlowCode"));
        System.out.println("Fallback Flow Name: " + exchange.getIn().getHeader("FlowName"));
        System.out.println("Graceful Degradation: "
                + (exchange.getIn().getHeader("FlowId") != null ? "SUCCESS" : "FAILED"));

        String jsonBody = exchange.getIn().getBody(String.class);
        System.out.println("Fallback JSON Generated: "
                + (jsonBody != null && jsonBody.length() > 50 ? "YES" : "NO"));
    }
}
