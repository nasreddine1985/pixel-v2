package com.pixel.v2.common.headers;

/**
 * Constants for header names used in Pixel V2 kamelets.
 * This class centralizes all header names to avoid typos
 * and ensure consistency across all kamelets.
 */
public final class HeaderConstants {

    private HeaderConstants() {
        // Utility class - private constructor
    }

    // ==================================================================================
    // JSON DATA HEADERS
    // ==================================================================================

    /** Header containing main flow JSON data */
    public static final String FLOW_DATA_JSON = "FlowDataJson";
    
    /** Header containing main flow JSON data (lowercase alternative) */
    public static final String FLOW_DATA_JSON_LC = "flowDataJson";
    
    /** Header containing reference JSON data */
    public static final String REF_FLOW_DATA_JSON = "RefFlowDataJson";
    
    /** Header containing reference JSON data (lowercase alternative) */
    public static final String REF_FLOW_DATA_JSON_LC = "refFlowDataJson";

    // ==================================================================================
    // IDENTIFICATION AND TRACEABILITY HEADERS
    // ==================================================================================

    /** Unique flow occurrence identifier */
    public static final String FLOW_OCCUR_ID = "flowOccurId";
    
    /** Unique flow occurrence identifier (CamelCase alternative) */
    public static final String FLOW_OCCUR_ID_CC = "FlowOccurId";
    
    /** Unique message identifier */
    public static final String MESSAGE_ID = "MessageId";
    
    /** Unique message identifier (lowercase alternative) */
    public static final String MESSAGE_ID_LC = "messageId";
    
    /** Correlation identifier for tracking */
    public static final String CORRELATION_ID = "CorrelationId";
    
    /** Correlation identifier for tracking (lowercase alternative) */
    public static final String CORRELATION_ID_LC = "correlationId";

    // ==================================================================================
    // TIMESTAMP AND TEMPORAL HEADERS
    // ==================================================================================

    /** Message reception timestamp */
    public static final String RECEIVED_TIMESTAMP = "ReceivedTimestamp";
    
    /** Message reception timestamp (lowercase alternative) */
    public static final String RECEIVED_TIMESTAMP_LC = "receivedTimestamp";
    
    /** Processing timestamp */
    public static final String PROCESSING_TIMESTAMP = "ProcessingTimestamp";
    
    /** Processing timestamp (lowercase alternative) */
    public static final String PROCESSING_TIMESTAMP_LC = "processingTimestamp";

    // ==================================================================================
    // PROCESSING MODE AND STATUS HEADERS
    // ==================================================================================

    /** Processing mode (NORMAL, BATCH, REALTIME, etc.) */
    public static final String PROCESSING_MODE = "ProcessingMode";
    
    /** Processing mode (lowercase alternative) */
    public static final String PROCESSING_MODE_LC = "processingMode";
    
    /** Business processing status */
    public static final String BUSINESS_STATUS = "BusinessStatus";
    
    /** Business processing status (lowercase alternative) */
    public static final String BUSINESS_STATUS_LC = "businessStatus";
    
    /** Technical processing status */
    public static final String TECHNICAL_STATUS = "TechnicalStatus";
    
    /** Technical processing status (lowercase alternative) */
    public static final String TECHNICAL_STATUS_LC = "technicalStatus";

    // ==================================================================================
    // KAMELET-SPECIFIC HEADERS
    // ==================================================================================

    /** Header containing generated TechnicalPivot XML (standard output) */
    public static final String TECH_PIVOT_XML = "techPivotXml";
    
    /** Header containing existing TechnicalPivot XML (for update) */
    public static final String EXISTING_TECH_PIVOT_XML = "existingTechPivotXml";
    
    /** Header containing source file name */
    public static final String SOURCE_FILE_NAME = "sourceFileName";
    
    /** Header containing output file name */
    public static final String OUTPUT_FILE_NAME = "outputFileName";

    // ==================================================================================
    // KAMELET CONFIGURATION HEADERS
    // ==================================================================================

    /** Header specifying operation to perform (generate, update, validate, etc.) */
    public static final String OPERATION = "operation";
    
    /** Header specifying custom XML output header */
    public static final String XML_OUTPUT_HEADER = "xmlOutputHeader";
    
    /** Header specifying header containing existing XML */
    public static final String EXISTING_XML_HEADER = "existingXmlHeader";

    // ==================================================================================
    // DEFAULT VALUES FOR PROCESSING MODES
    // ==================================================================================

    /** Normal processing mode (default value) */
    public static final String PROCESSING_MODE_NORMAL = "NORMAL";
    
    /** Batch processing mode */
    public static final String PROCESSING_MODE_BATCH = "BATCH";
    
    /** Real-time processing mode */
    public static final String PROCESSING_MODE_REALTIME = "REALTIME";
    
    /** File processing mode */
    public static final String PROCESSING_MODE_FILE_BATCH = "FILE_BATCH";

    // ==================================================================================
    // DEFAULT VALUES FOR BUSINESS STATUSES
    // ==================================================================================

    /** Pending business status */
    public static final String BUSINESS_STATUS_PENDING = "PENDING";
    
    /** Processing business status */
    public static final String BUSINESS_STATUS_PROCESSING = "PROCESSING";
    
    /** Validated business status */
    public static final String BUSINESS_STATUS_VALIDATED = "VALIDATED";
    
    /** Completed business status */
    public static final String BUSINESS_STATUS_COMPLETED = "COMPLETED";
    
    /** Initialized business status */
    public static final String BUSINESS_STATUS_INITIALIZED = "INITIALIZED";

    // ==================================================================================
    // DEFAULT VALUES FOR TECHNICAL STATUSES
    // ==================================================================================

    /** Processing technical status */
    public static final String TECHNICAL_STATUS_PROCESSING = "PROCESSING";
    
    /** Active technical status */
    public static final String TECHNICAL_STATUS_ACTIVE = "ACTIVE";
    
    /** Success technical status */
    public static final String TECHNICAL_STATUS_SUCCESS = "SUCCESS";
    
    /** Finalized technical status */
    public static final String TECHNICAL_STATUS_FINALIZED = "FINALIZED";
    
    /** Error technical status */
    public static final String TECHNICAL_STATUS_ERROR = "ERROR";

    // ==================================================================================
    // OPERATIONS SUPPORTED BY KAMELETS
    // ==================================================================================

    /** Generation operation (create new) */
    public static final String OPERATION_GENERATE = "generate";
    
    /** Update operation */
    public static final String OPERATION_UPDATE = "update";
    
    /** Validation operation */
    public static final String OPERATION_VALIDATE = "validate";

}