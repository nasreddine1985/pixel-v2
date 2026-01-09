package com.pixel.v2.kamelets.examples;

import java.util.Map;
import java.util.UUID;

import com.pixel.v2.common.headers.HeaderConstants;
import com.pixel.v2.common.headers.HeaderUtils;

import static com.pixel.v2.common.headers.HeaderConstants.*;

/**
 * Example showing how to use HeaderConstants and HeaderUtils in kamelets
 * 
 * This example demonstrates:
 * 1. Using HeaderConstants instead of string literals for header names
 * 2. Using HeaderUtils for reading/writing headers with automatic fallback
 * 3. Using predefined values for consistent status/mode values
 * 
 * @author Pixel V2 Framework
 */
public class USAGE_EXAMPLE {

    /**
     * Example of how to use HeaderConstants in Java code
     */
    public void demonstrateHeaderConstantsUsage() {
        
        // ===== BEFORE: Using string literals (prone to errors) =====
        /*
        .setHeader("FlowDataJson", "{\"flowId\":\"123\"}")
        .setHeader("flowOccurId", "FLOW-123-456")
        .setHeader("ProcessingMode", "BATCH")
        .setHeader("businessStatus", "PROCESSING")
        .setHeader("TechnicalStatus", "ACTIVE")
        */
        
        // ===== AFTER: Using HeaderConstants (type-safe) =====
        Map<String, Object> headers = Map.of(
            FLOW_DATA_JSON, "{\"flowId\":\"123\",\"amount\":1000}",
            FLOW_OCCUR_ID, HeaderUtils.generateFlowOccurId("DEMO"),
            MESSAGE_ID, HeaderUtils.generateMessageId("MSG"),
            PROCESSING_MODE, PROCESSING_MODE_BATCH,
            BUSINESS_STATUS, BUSINESS_STATUS_PROCESSING,
            TECHNICAL_STATUS, TECHNICAL_STATUS_ACTIVE,
            RECEIVED_TIMESTAMP, HeaderUtils.generateTimestamp()
        );
        
        System.out.println("Headers created with HeaderConstants:");
        headers.forEach((key, value) -> System.out.println("  " + key + ": " + value));
    }

    /**
     * Example of using HeaderUtils for reading headers with automatic fallback
     */
    public void demonstrateHeaderUtilsUsage() {
        
        // Simulation of an exchange for demonstration
        // In real code, this would be a Camel Exchange object
        
        /*
        // Old way - manual fallback checking
        String flowData = exchange.getIn().getHeader("FlowDataJson", String.class);
        if (flowData == null) {
            flowData = exchange.getIn().getHeader("flowDataJson", String.class);
        }
        if (flowData == null) {
            flowData = exchange.getIn().getHeader("RefFlowDataJson", String.class);
        }
        */
        
        // New way - automatic fallback with HeaderUtils
        /*
        String flowData = HeaderUtils.getFlowDataJson(exchange);
        String processingMode = HeaderUtils.getProcessingMode(exchange);
        String businessStatus = HeaderUtils.getBusinessStatus(exchange);
        
        // Set status using constants
        HeaderUtils.setBusinessStatus(exchange, BUSINESS_STATUS_VALIDATED);
        HeaderUtils.setTechnicalStatus(exchange, TECHNICAL_STATUS_SUCCESS);
        
        // Set all standard headers at once
        HeaderUtils.setStandardProcessingHeaders(exchange, 
            "PROC", "MSG", PROCESSING_MODE_BATCH);
        */
    }

    /**
     * Camel Route configuration example using HeaderConstants
     */
    public static final String CAMEL_ROUTE_EXAMPLE = """
        from("direct:start")
            .routeId("example-with-header-constants")
            .log("Processing message with FlowDataJson: ${header.%s}")
            
            // Reading headers with constants in expressions
            .choice()
                .when(header("%s").isEqualTo("%s"))
                    .log("Processing in BATCH mode")
                    .setHeader("%s", constant("%s"))
                .when(header("%s").isEqualTo("%s"))
                    .log("Processing in REALTIME mode")
                    .setHeader("%s", constant("%s"))
                .otherwise()
                    .log("Processing in NORMAL mode")
                    .setHeader("%s", constant("%s"))
            .end()
            
            .to("mock:result");
        """.formatted(
            FLOW_DATA_JSON,
            PROCESSING_MODE, PROCESSING_MODE_BATCH,
            TECHNICAL_STATUS, TECHNICAL_STATUS_PROCESSING,
            PROCESSING_MODE, PROCESSING_MODE_REALTIME,
            TECHNICAL_STATUS, TECHNICAL_STATUS_ACTIVE,
            TECHNICAL_STATUS, TECHNICAL_STATUS_SUCCESS
        );

    /**
     * Kamelet YAML configuration example using HeaderConstants
     */
    public static final String KAMELET_YAML_EXAMPLE = """
        apiVersion: camel.apache.org/v1alpha1
        kind: Kamelet
        metadata:
          name: example-header-constants-kamelet
          title: "Example Header Constants Kamelet"
        spec:
          definition:
            title: Example using HeaderConstants
            properties:
              defaultMode:
                title: Default Processing Mode
                type: string
                default: NORMAL
                enum: ["NORMAL", "BATCH", "REALTIME"]
          template:
            from:
              uri: "kamelet:source"
              steps:
                - script:
                    groovy: |
                      // Import HeaderConstants in Groovy
                      import static com.pixel.v2.common.headers.HeaderConstants.*
                      
                      // Use constants instead of strings
                      request.headers[PROCESSING_MODE] = exchange.properties.defaultMode ?: PROCESSING_MODE_NORMAL
                      request.headers[BUSINESS_STATUS] = BUSINESS_STATUS_INITIALIZED
                      request.headers[TECHNICAL_STATUS] = TECHNICAL_STATUS_PROCESSING
                      request.headers[FLOW_OCCUR_ID] = "FLOW-${System.currentTimeMillis()}"
                      request.headers[MESSAGE_ID] = UUID.randomUUID().toString()
                      request.headers[RECEIVED_TIMESTAMP] = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS")
                      
                      // Log using constants
                      log.info("Set processing mode to: ${request.headers[PROCESSING_MODE]}")
                - to: "kamelet:sink"
        """;

    /**
     * Benefits of using HeaderConstants:
     * 
     * 1. TYPE SAFETY: Compile-time checking prevents typos
     * 2. CONSISTENCY: Same header names across all kamelets  
     * 3. MAINTAINABILITY: Single place to change header names
     * 4. AUTO-COMPLETION: IDE support for header names
     * 5. READABILITY: Clear intent with meaningful constant names
     * 6. VALIDATION: Easier to validate header usage across codebase
     * 7. DOCUMENTATION: Constants serve as self-documenting code
     * 8. TESTING: Easier to mock and test with constants
     * 9. REFACTORING: Safe renaming with IDE refactoring tools
     * 10. FALLBACK: HeaderUtils provides automatic CamelCase/lowercase fallback
     */
}