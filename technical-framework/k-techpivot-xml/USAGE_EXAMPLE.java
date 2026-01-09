// Usage example of k-techpivot-xml kamelet in ChRoute.java
// The kamelet reads data from Camel headers and generates/updates a TechnicalPivot XML
// IMPORTANT: Use HeaderConstants for consistent header names

import static com.pixel.v2.common.headers.HeaderConstants.*;

// ==================================================================================
// GENERATE MODE: Create new XML from JSON data in headers
// ==================================================================================

// 1. Prepare headers with JSON data and processing variables using HeaderConstants
.setHeader(FLOW_DATA_JSON, simple("${file:content}")) // Complete referential JSON
// OR
.setHeader(REF_FLOW_DATA_JSON, simple("${file:content}")) // Alternative for referential

// Processing variables using constants
.setHeader(FLOW_OCCUR_ID, simple("FLOW-${date:now:yyyyMMdd-HHmmss}"))
.setHeader(RECEIVED_TIMESTAMP, simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
.setHeader(MESSAGE_ID, simple("MSG-${uuid}"))
.setHeader(CORRELATION_ID, simple("CORR-${exchangeId}"))
.setHeader(PROCESSING_MODE, constant(PROCESSING_MODE_BATCH))
.setHeader(BUSINESS_STATUS, constant(BUSINESS_STATUS_VALIDATED))
.setHeader(TECHNICAL_STATUS, constant(TECHNICAL_STATUS_SUCCESS))

// Generate TechnicalPivot XML
.to("kamelet:k-techpivot-xml?operation=" + OPERATION_GENERATE)
.log("Generated TechnicalPivot XML: ${header." + TECH_PIVOT_XML + "}")

// ==================================================================================
// UPDATE MODE: Update existing XML with ONLY header variables
// ==================================================================================

// 2. Prepare existing XML and update variables using constants
.setHeader(EXISTING_TECH_PIVOT_XML, simple("${body}")) // Existing XML to update

// Update variables using HeaderConstants (NO FlowDataJson/RefFlowDataJson in update mode)
.setHeader(FLOW_OCCUR_ID, simple("UPDATED-${date:now:yyyyMMddHHmmss}"))
.setHeader(RECEIVED_TIMESTAMP, simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
.setHeader(MESSAGE_ID, simple("UPD-MSG-${uuid}"))
.setHeader(PROCESSING_MODE, constant(PROCESSING_MODE_REALTIME))
.setHeader(BUSINESS_STATUS, constant(BUSINESS_STATUS_COMPLETED))
.setHeader(TECHNICAL_STATUS, constant(TECHNICAL_STATUS_FINALIZED))

// Update XML (preserves all existing data, updates only variables)
.to("kamelet:k-techpivot-xml?operation=" + OPERATION_UPDATE)
.log("Updated TechnicalPivot XML: ${header." + TECH_PIVOT_XML + "}")

// ==================================================================================
// ADVANCED CONFIGURATION WITH HEADER CONSTANTS
// ==================================================================================

// 3. Complete configuration with custom headers using constants
.to("kamelet:k-techpivot-xml?" +
    "operation=" + OPERATION_GENERATE + "&" +
    "xmlOutputHeader=myCustomXmlHeader&" +
    "existingXmlHeader=myExistingXmlHeader")

// 4. Example using HeaderUtils for automatic generation
from("file:data?include=*.json&noop=true")
    .log("Processing file: ${header.CamelFileName}")
    
    // Use HeaderUtils for standard setup
    .process(exchange -> {
        // Load JSON using HeaderConstants
        String jsonContent = exchange.getIn().getBody(String.class);
        exchange.getIn().setHeader(FLOW_DATA_JSON, jsonContent);
        
        // Set standard processing headers using HeaderUtils
        HeaderUtils.setStandardProcessingHeaders(exchange, 
            "FILE", "FILE-PROCESS", PROCESSING_MODE_FILE_BATCH);
        
        // Override specific values
        HeaderUtils.setBusinessStatus(exchange, BUSINESS_STATUS_INITIALIZED);
    })
    
    // Generate TechnicalPivot XML
    .to("kamelet:k-techpivot-xml?operation=" + OPERATION_GENERATE)
    
    // XML is now available in TECH_PIVOT_XML header AND in body
    .log("Generated XML for file ${header.CamelFileName}")
    .to("file:output?fileName=${header.CamelFileName}.xml");

// ==================================================================================
// ALTERNATIVE APPROACH WITH PROCESSOR
// ==================================================================================

// 5. Using a processor with HeaderUtils for cleaner code
.process(new HeaderSetupProcessor())
.to("kamelet:k-techpivot-xml?operation=" + OPERATION_GENERATE)

// Example processor class
public static class HeaderSetupProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        // Set JSON data
        HeaderUtils.setFlowDataJson(exchange, exchange.getIn().getBody(String.class));
        
        // Generate processing headers
        HeaderUtils.setFlowOccurId(exchange, 
            HeaderUtils.generateFlowOccurId("PROC"));
        HeaderUtils.setMessageId(exchange, 
            HeaderUtils.generateMessageId("PROC"));
        HeaderUtils.setReceivedTimestamp(exchange, 
            HeaderUtils.generateTimestamp());
        
        // Set processing mode and statuses
        HeaderUtils.setProcessingMode(exchange, PROCESSING_MODE_BATCH);
        HeaderUtils.setBusinessStatus(exchange, BUSINESS_STATUS_PROCESSING);
        HeaderUtils.setTechnicalStatus(exchange, TECHNICAL_STATUS_ACTIVE);
    }
}

// ==================================================================================
// SUPPORTED HEADERS (using HeaderConstants)
// ==================================================================================

/*
INPUT HEADERS (for operation=generate):
- FLOW_DATA_JSON / REF_FLOW_DATA_JSON : Complete referential JSON (required)
- FLOW_OCCUR_ID : Flow occurrence identifier
- RECEIVED_TIMESTAMP : Reception timestamp
- MESSAGE_ID : Message identifier
- CORRELATION_ID : Correlation identifier
- PROCESSING_MODE : Processing mode (default: PROCESSING_MODE_NORMAL)
- BUSINESS_STATUS : Business status (default: BUSINESS_STATUS_PENDING)
- TECHNICAL_STATUS : Technical status (default: TECHNICAL_STATUS_PROCESSING)

INPUT HEADERS (for operation=update):
- EXISTING_TECH_PIVOT_XML : Existing XML to update (required)
- FLOW_OCCUR_ID : New occurrence identifier
- RECEIVED_TIMESTAMP : New timestamp
- MESSAGE_ID : New message identifier
- CORRELATION_ID : New correlation identifier
- PROCESSING_MODE : New processing mode
- BUSINESS_STATUS : New business status
- TECHNICAL_STATUS : New technical status
- NOTE: FLOW_DATA_JSON/REF_FLOW_DATA_JSON are IGNORED in update mode

OUTPUT HEADERS:
- TECH_PIVOT_XML (or custom header) : Generated/updated TechnicalPivot XML

OUTPUT BODY:
- Generated/updated TechnicalPivot XML

OPERATIONS:
- OPERATION_GENERATE : Create new XML
- OPERATION_UPDATE : Update existing XML
- OPERATION_VALIDATE : Validate XML (if supported)
*/