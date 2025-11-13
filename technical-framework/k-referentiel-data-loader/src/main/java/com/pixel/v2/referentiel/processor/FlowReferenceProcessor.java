package com.pixel.v2.referentiel.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pixel.v2.referentiel.model.FlowReferenceDto;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Processor for handling FlowReference data loading from external service Converts response to
 * FlowReferenceDto and sets relevant headers
 */
public class FlowReferenceProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(FlowReferenceProcessor.class);
    private static final String BOOLEAN_FALSE = "FALSE";
    private static final String BOOLEAN_TRUE = "TRUE";
    private final ObjectMapper objectMapper;

    public FlowReferenceProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String flowCode = exchange.getIn().getHeader("FlowCode", String.class);
        String serviceUrl = exchange.getIn().getHeader("ServiceUrl", String.class);
        Object responseBody = exchange.getIn().getBody();

        logger.info(
                "[REFERENTIEL-LOADER] Processing FlowReference data for flowCode: {} from service: {}",
                flowCode, serviceUrl);

        try {
            FlowReferenceDto flowReference;
            boolean loadedFromService = false;

            if (responseBody != null && !responseBody.toString().trim().isEmpty()) {
                // Parse the JSON response to FlowReferenceDto
                String jsonResponse = responseBody.toString();
                logger.debug("[REFERENTIEL-LOADER] Parsing JSON response: {}", jsonResponse);

                flowReference = objectMapper.readValue(jsonResponse, FlowReferenceDto.class);

                // Validate that the flowCode matches
                if (flowReference.getFlowCode() == null) {
                    flowReference.setFlowCode(flowCode);
                }

                loadedFromService = true;
                logger.info("[REFERENTIEL-LOADER] ✅ Successfully loaded FlowReference: {}",
                        flowReference);

            } else {
                // Create default FlowReferenceDto if no response
                logger.warn(
                        "[REFERENTIEL-LOADER] No response from service, creating default FlowReference for flowCode: {}",
                        flowCode);
                flowReference = createDefaultFlowReference(flowCode);
                loadedFromService = false;
            }

            // Set FlowReference as message body (as JSON)
            String flowReferenceJson = objectMapper.writeValueAsString(flowReference);
            exchange.getIn().setBody(flowReferenceJson);

            // Set individual headers for easy access in routes
            setFlowReferenceHeaders(exchange, flowReference);

            // Set processing metadata
            exchange.getIn().setHeader("FlowReferenceLoaded", loadedFromService);
            exchange.getIn().setHeader("FlowReferenceLoadTime", LocalDateTime.now().toString());

            logger.info(
                    "[REFERENTIEL-LOADER] ✅ FlowReference processing completed for flowCode: {}",
                    flowCode);

        } catch (Exception e) {
            logger.error(
                    "[REFERENTIEL-LOADER] ❌ Error processing FlowReference for flowCode: {} - {}",
                    flowCode, e.getMessage(), e);

            // Create fallback FlowReference and continue
            FlowReferenceDto fallbackFlow = createDefaultFlowReference(flowCode);
            String fallbackJson = objectMapper.writeValueAsString(fallbackFlow);
            exchange.getIn().setBody(fallbackJson);

            setFlowReferenceHeaders(exchange, fallbackFlow);
            exchange.getIn().setHeader("FlowReferenceLoaded", false);
            exchange.getIn().setHeader("FlowReferenceError", e.getMessage());

            // Don't throw exception, let the flow continue with defaults
        }
    }

    /**
     * Set FlowReference fields as message headers for easy access in routes
     */
    private void setFlowReferenceHeaders(Exchange exchange, FlowReferenceDto flowRef) {
        // Core flow information
        exchange.getIn().setHeader("FlowId", flowRef.getFlowId());
        exchange.getIn().setHeader("FlowCode", flowRef.getFlowCode());
        exchange.getIn().setHeader("FlowName", flowRef.getFlowName());
        exchange.getIn().setHeader("FlowStatus", flowRef.getStatus());
        exchange.getIn().setHeader("FlowType", flowRef.getFlowType());

        // Source and target information
        exchange.getIn().setHeader("SourceChannel", flowRef.getSourceChannel());
        exchange.getIn().setHeader("SourceSystem", flowRef.getSourceSystem());
        exchange.getIn().setHeader("SourceFormat", flowRef.getSourceFormat());
        exchange.getIn().setHeader("TargetSystem", flowRef.getTargetSystem());
        exchange.getIn().setHeader("TargetChannel", flowRef.getTargetChannel());

        // Processing configuration
        exchange.getIn().setHeader("RailMode", flowRef.getRailMode());
        exchange.getIn().setHeader("Priority", flowRef.getPriority());
        exchange.getIn().setHeader("SlaMaxLatencyMs", flowRef.getSlaMaxLatencyMs());

        // Split and concat configuration
        exchange.getIn().setHeader("SplitEnabled", flowRef.getSplitEnabled());
        exchange.getIn().setHeader("SplitChunkSize", flowRef.getSplitChunkSize());
        exchange.getIn().setHeader("ConcatEnabled", flowRef.getConcatEnabled());
        exchange.getIn().setHeader("ConcatCriteria", flowRef.getConcatCriteria());

        // Retention configuration
        exchange.getIn().setHeader("RetentionInEnabled", flowRef.getRetentionInEnabled());
        exchange.getIn().setHeader("RetentionInMode", flowRef.getRetentionInMode());
        exchange.getIn().setHeader("RetentionInDays", flowRef.getRetentionInDays());
        exchange.getIn().setHeader("RetentionOutEnabled", flowRef.getRetentionOutEnabled());
        exchange.getIn().setHeader("RetentionOutMode", flowRef.getRetentionOutMode());
        exchange.getIn().setHeader("RetentionOutDays", flowRef.getRetentionOutDays());

        // Shaping and security
        exchange.getIn().setHeader("ShapingEnabled", flowRef.getShapingEnabled());
        exchange.getIn().setHeader("ShapingMaxTrxPerMin", flowRef.getShapingMaxTrxPerMin());
        exchange.getIn().setHeader("ShapingStrategy", flowRef.getShapingStrategy());
        exchange.getIn().setHeader("PiiLevel", flowRef.getPiiLevel());
        exchange.getIn().setHeader("EncryptionRequired", flowRef.getEncryptionRequired());
        exchange.getIn().setHeader("DrStrategy", flowRef.getDrStrategy());

        // Metadata
        exchange.getIn().setHeader("FlowVersion", flowRef.getVersion());
        exchange.getIn().setHeader("FlowComments", flowRef.getComments());

        logger.debug("[REFERENTIEL-LOADER] Set {} FlowReference headers for flowCode: {}",
                exchange.getIn().getHeaders().size(), flowRef.getFlowCode());
    }

    /**
     * Create a default FlowReferenceDto when service is unavailable or returns empty response
     */
    private FlowReferenceDto createDefaultFlowReference(String flowCode) {
        FlowReferenceDto defaultFlow = new FlowReferenceDto();

        // Set basic required fields
        defaultFlow.setFlowId("DEFAULT_" + flowCode.toUpperCase());
        defaultFlow.setFlowCode(flowCode);
        defaultFlow.setFlowName("Default Flow Configuration for " + flowCode);
        defaultFlow.setStatus("ACTIVE");

        // Set default processing configuration
        defaultFlow.setFlowType("PAYMENT");
        defaultFlow.setRailMode("STANDARD");
        defaultFlow.setPriority(5);
        defaultFlow.setSlaMaxLatencyMs(30000);

        // Set default source/target
        defaultFlow.setSourceChannel("HTTP");
        defaultFlow.setSourceSystem("EXTERNAL");
        defaultFlow.setSourceFormat("XML");
        defaultFlow.setTargetSystem("INTERNAL");
        defaultFlow.setTargetChannel("KAFKA");

        // Set default split/concat configuration
        defaultFlow.setSplitEnabled(BOOLEAN_FALSE);
        defaultFlow.setSplitChunkSize(1000);
        defaultFlow.setConcatEnabled(BOOLEAN_FALSE);

        // Set default retention
        defaultFlow.setRetentionInEnabled(BOOLEAN_TRUE);
        defaultFlow.setRetentionInMode("ARCHIVE");
        defaultFlow.setRetentionInDays(90);
        defaultFlow.setRetentionOutEnabled(BOOLEAN_TRUE);
        defaultFlow.setRetentionOutMode("ARCHIVE");
        defaultFlow.setRetentionOutDays(30);

        // Set default shaping and security
        defaultFlow.setShapingEnabled(BOOLEAN_FALSE);
        defaultFlow.setShapingMaxTrxPerMin(1000);
        defaultFlow.setShapingStrategy("THROTTLE");
        defaultFlow.setPiiLevel("MEDIUM");
        defaultFlow.setEncryptionRequired(BOOLEAN_TRUE);
        defaultFlow.setDrStrategy("ACTIVE_PASSIVE");

        // Set metadata
        defaultFlow.setVersion("1.0.0");
        defaultFlow.setComments("Default configuration - service unavailable");

        logger.info("[REFERENTIEL-LOADER] Created default FlowReference for flowCode: {}",
                flowCode);
        return defaultFlow;
    }
}
