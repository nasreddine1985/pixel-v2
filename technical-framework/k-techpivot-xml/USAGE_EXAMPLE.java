// Usage example of k-techpivot-xml kamelet in ChRoute.java
// The kamelet reads data from Camel headers and generates/updates a TechnicalPivot XML

// ==================================================================================
// GENERATE MODE: Create new XML from JSON data in headers
// ==================================================================================

// 1. Prepare headers with JSON data and processing variables
.setHeader("FlowDataJson", simple("${file:content}")) // Complete referential JSON
// OR
.setHeader("RefFlowDataJson", simple("${file:content}")) // Alternative for referential

// Processing variables
.setHeader("flowOccurId", simple("FLOW-${date:now:yyyyMMdd-HHmmss}"))
.setHeader("ReceivedTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
.setHeader("MessageId", simple("MSG-${uuid}"))
.setHeader("CorrelationId", simple("CORR-${exchangeId}"))
.setHeader("ProcessingMode", constant("BATCH"))
.setHeader("BusinessStatus", constant("VALIDATED"))
.setHeader("TechnicalStatus", constant("SUCCESS"))

// Generate TechnicalPivot XML
.to("kamelet:k-techpivot-xml?operation=generate")
.log("Generated TechnicalPivot XML: ${header.techPivotXml}")

// ==================================================================================
// UPDATE MODE: Update existing XML with ONLY header variables
// ==================================================================================

// 2. Prepare existing XML and update variables
.setHeader("existingTechPivotXml", simple("${body}")) // Existing XML to update

// Update variables (NO FlowDataJson/RefFlowDataJson in update mode)
.setHeader("flowOccurId", simple("UPDATED-${date:now:yyyyMMddHHmmss}"))
.setHeader("ReceivedTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
.setHeader("MessageId", simple("UPD-MSG-${uuid}"))
.setHeader("ProcessingMode", constant("REALTIME"))
.setHeader("BusinessStatus", constant("COMPLETED"))
.setHeader("TechnicalStatus", constant("FINALIZED"))

// Update XML (preserves all existing data, updates only variables)
.to("kamelet:k-techpivot-xml?operation=update")
.log("Updated TechnicalPivot XML: ${header.techPivotXml}")

// ==================================================================================
// ADVANCED CONFIGURATION
// ==================================================================================

// 3. Complete configuration with custom headers
.to("kamelet:k-techpivot-xml?" +
    "operation=generate&" +
    "xmlOutputHeader=myCustomXmlHeader&" +
    "existingXmlHeader=myExistingXmlHeader")

// 4. Example of complete route for JSON file processing
from("file:data?include=*.json&noop=true")
    .log("Processing file: ${header.CamelFileName}")
    
    // Load JSON into FlowDataJson header
    .setHeader("FlowDataJson", simple("${body}"))
    
    // Prepare processing variables
    .setHeader("flowOccurId", simple("${header.CamelFileName}-${date:now:yyyyMMddHHmmss}"))
    .setHeader("ReceivedTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
    .setHeader("MessageId", simple("FILE-${header.CamelFileName}"))
    .setHeader("CorrelationId", simple("${exchangeId}"))
    .setHeader("ProcessingMode", constant("FILE_BATCH"))
    .setHeader("BusinessStatus", constant("PROCESSING"))
    .setHeader("TechnicalStatus", constant("ACTIVE"))
    
    // Generate TechnicalPivot XML
    .to("kamelet:k-techpivot-xml?operation=generate")
    
    // XML is now available in techPivotXml header AND in body
    .log("Generated XML for file ${header.CamelFileName}")
    .to("file:output?fileName=${header.CamelFileName}.xml");

// ==================================================================================
// SUPPORTED HEADERS
// ==================================================================================

/*
INPUT HEADERS (for operation=generate):
- FlowDataJson / RefFlowDataJson : Complete referential JSON (required)
- flowOccurId / FlowOccurId : Flow occurrence identifier
- ReceivedTimestamp / receivedTimestamp : Reception timestamp
- MessageId / messageId : Message identifier
- CorrelationId / correlationId : Correlation identifier
- ProcessingMode / processingMode : Processing mode (default: NORMAL)
- BusinessStatus / businessStatus : Business status (default: PENDING)
- TechnicalStatus / technicalStatus : Technical status (default: PROCESSING)

INPUT HEADERS (for operation=update):
- existingTechPivotXml : Existing XML to update (required)
- flowOccurId / FlowOccurId : New occurrence identifier
- ReceivedTimestamp / receivedTimestamp : New timestamp
- MessageId / messageId : New message identifier
- CorrelationId / correlationId : New correlation identifier
- ProcessingMode / processingMode : New processing mode
- BusinessStatus / businessStatus : New business status
- TechnicalStatus / technicalStatus : New technical status
- NOTE: FlowDataJson/RefFlowDataJson are IGNORED in update mode

OUTPUT HEADERS:
- techPivotXml (or custom header) : Generated/updated TechnicalPivot XML

OUTPUT BODY:
- Generated/updated TechnicalPivot XML
*/