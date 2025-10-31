# PACS008 Referentiel Integration Summary

## Overview

Successfully integrated the referentiel service into the PACS008 message processing flow using the k-referentiel-data-loader kamelet. The integration occurs after successful database persistence and before JMS acknowledgment.

## Changes Made

### ✅ 1. Application Properties Configuration

Added new configuration properties in `/flow/src/main/resources/application.properties`:

```properties
# Message Processing Configuration (enhanced)
flow.pacs008.referentiel.flowId=pacs008

# Referentiel Service Configuration
referentiel.service.url=http://localhost:8099
```

### ✅ 2. Pacs008RouteBuilder Enhancement

Enhanced the `Pacs008RouteBuilder.java` with referentiel integration:

#### **New Route Added: pacs008-referentiel-loader**

```java
// Route 3: Referentiel Configuration Loader
from("direct:pacs008-referentiel-loader").routeId("pacs008-referentiel-loader")
    .log("[PACS008-REFERENTIEL] Loading configuration from referentiel service for flowId: {{flow.pacs008.referentiel.flowId}}")

    // Store original body (collection of messages) in a header to preserve it
    .setHeader("OriginalMessageCollection", simple("${body}"))

    // Call k-referentiel-data-loader kamelet to get flow configuration
    .to("kamelet:k-referentiel-data-loader"
        + "?serviceUrl={{referentiel.service.url}}"
        + "&flowId={{flow.pacs008.referentiel.flowId}}")

    .log("[PACS008-REFERENTIEL] Configuration loaded successfully")
    .log("[PACS008-REFERENTIEL] CmdMapping: ${header.CmdMapping}, Rail: ${header.Rail}, Mode: ${header.Mode}")
    .log("[PACS008-REFERENTIEL] XsltFileToCdm: ${header.XsltFileToCdm}, KafkaTopic: ${header.KafkaTopic}")
    .log("[PACS008-REFERENTIEL] NeedSplit: ${header.NeedSplit}, ChunkSize: ${header.ChunkSize}")

    // Restore original message collection body
    .setBody(simple("${header.OriginalMessageCollection}"))
    .removeHeader("OriginalMessageCollection")

    .log("[PACS008-REFERENTIEL] Configuration headers added, original message collection preserved (${body.size()} messages)");
```

#### **Integration Point in Main Route**

Added referentiel call in the persistence success branch:

```java
.choice().when(header("persistenceStatus").isEqualTo("SUCCESS"))
    .log("[PACS008-PERSIST] Database persistence successful - JMS messages will be acknowledged")
    .log("[PACS008-JMS] Transaction will commit - ${header.persistedCount} messages will be ACKNOWLEDGED to MQ")

    // Call referentiel service to load flow configuration
    .to("direct:pacs008-referentiel-loader")

    .log("[PACS008-JMS] ✅ Route completion - JMS acknowledgment will occur automatically on transaction commit")
```

## Integration Flow

### **Complete PACS008 Processing Flow**

1. **Message Consumption**: Messages received from `PACS008_QUEUE` via `k-mq-message-receiver`
2. **Batch Aggregation**: Messages aggregated using Apache Camel aggregator
3. **Database Persistence**: Batch persisted via `pacs008BatchPersistenceProcessor`
4. **✅ NEW: Referentiel Configuration Loading**:
   - Calls referentiel service with flowId `pacs008`
   - Loads comprehensive configuration headers
   - Preserves original message collection body
5. **JMS Acknowledgment**: Messages acknowledged on successful completion

### **Configuration Headers Added**

The referentiel integration adds the following headers to the message:

**Core Configuration:**

- `CmdMapping`: "pacs008_mapping"
- `Rail`: "instant_payments"
- `Mode`: "real_time"
- `NeedSplit`: true
- `SplitExpr`: "//Document"
- `ChunkSize`: 100
- `Outputs`: ["queue1", "queue2"]

**Transformation & Validation:**

- `XsltFileToCdm`: "pacs008-to-cdm.xslt"
- `XsltFileFromCdm`: "cdm-to-pacs008.xslt"
- `XsdFlowFile`: "pacs008.xsd"
- `XsdCdmFile`: "cdm.xsd"

**Kafka Configuration:**

- `KafkaBroker`: "localhost:9092"
- `KafkaTopic`: "pacs008-topic"

## Key Design Decisions

### ✅ **Body Preservation Strategy**

```java
// Store original body before kamelet call
.setHeader("OriginalMessageCollection", simple("${body}"))

// Call kamelet (which may modify body for HTTP request)
.to("kamelet:k-referentiel-data-loader?...")

// Restore original message collection
.setBody(simple("${header.OriginalMessageCollection}"))
.removeHeader("OriginalMessageCollection")
```

**Why this approach:**

- The `k-referentiel-data-loader` kamelet needs to make an HTTP call which may modify the body
- The original body is a collection of messages that must be preserved
- Headers are enriched with configuration while body remains unchanged

### ✅ **Integration Timing**

**Placed after database persistence, before JMS acknowledgment:**

- Ensures messages are safely persisted before configuration loading
- Configuration is available for any downstream processing
- JMS acknowledgment only occurs if both persistence AND configuration loading succeed

### ✅ **Error Handling**

- If referentiel service is unavailable, the kamelet provides default fallback values
- Transaction rollback still occurs if there are any failures
- Original error handling patterns remain intact

## Configuration Properties

### **Required Properties**

```properties
# Flow-specific configuration
flow.pacs008.referentiel.flowId=pacs008

# Service endpoint
referentiel.service.url=http://localhost:8099

# Existing batch configuration (unchanged)
flow.pacs008.batch.completion-size=1000
flow.pacs008.batch.completion-timeout=5000
```

### **Environment-Specific Overrides**

```properties
# Development
referentiel.service.url=http://localhost:8099

# Staging
referentiel.service.url=http://referentiel-service-staging:8099

# Production
referentiel.service.url=http://referentiel-service:8099
```

## Testing Verification

### ✅ **Build Status**

- **Compilation**: ✅ Successful
- **No Syntax Errors**: ✅ Confirmed
- **Dependencies**: ✅ All kamelet dependencies available

### **Expected Log Output**

```
[PACS008-PERSIST] Batch persistence completed: status=SUCCESS, count=1000
[PACS008-PERSIST] Database persistence successful - JMS messages will be acknowledged
[PACS008-JMS] Transaction will commit - 1000 messages will be ACKNOWLEDGED to MQ
[PACS008-REFERENTIEL] Loading configuration from referentiel service for flowId: pacs008
[PACS008-REFERENTIEL] Configuration loaded successfully
[PACS008-REFERENTIEL] CmdMapping: pacs008_mapping, Rail: instant_payments, Mode: real_time
[PACS008-REFERENTIEL] XsltFileToCdm: pacs008-to-cdm.xslt, KafkaTopic: pacs008-topic
[PACS008-REFERENTIEL] NeedSplit: true, ChunkSize: 100
[PACS008-REFERENTIEL] Configuration headers added, original message collection preserved (1000 messages)
[PACS008-JMS] ✅ Route completion - JMS acknowledgment will occur automatically on transaction commit
```

## Integration Benefits

### ✅ **Dynamic Configuration**

- Flow-specific processing parameters loaded at runtime
- No hardcoded configuration values in the route
- Easy configuration updates via referentiel service

### ✅ **Downstream Processing Support**

- XSLT transformation file paths available for message transformation
- XSD schema paths available for validation
- Kafka topic and broker configuration for message routing

### ✅ **Operational Flexibility**

- Configuration changes without code deployment
- Environment-specific settings via properties
- Centralized configuration management

## Next Steps

### **Recommended Enhancements**

1. **Use Configuration Headers**: Implement downstream processing using the loaded configuration headers
2. **Caching**: Consider caching configuration to reduce service calls
3. **Monitoring**: Add metrics for referentiel service call success/failure rates
4. **Similar Integration**: Apply same pattern to PACS009, PAIN001, CAMT053 routes

### **Example Usage of Configuration Headers**

```java
// Example: Use loaded configuration for conditional processing
.choice()
    .when(header("NeedSplit").isEqualTo("true"))
        .split(xpath("${header.SplitExpr}"))
        .to("xslt:${header.XsltFileToCdm}")
        .to("kafka:${header.KafkaTopic}?brokers=${header.KafkaBroker}")
    .otherwise()
        .to("xslt:${header.XsltFileToCdm}")
        .to("kafka:${header.KafkaTopic}?brokers=${header.KafkaBroker}")
```

## Summary

✅ **Referentiel integration successfully added** to PACS008 route  
✅ **Configuration headers loaded** after database persistence  
✅ **Message body preserved** throughout the process  
✅ **Error handling maintained** with fallback configuration  
✅ **Build verification completed** - ready for testing

The PACS008 route now dynamically loads flow-specific configuration from the referentiel service, providing the foundation for flexible, configuration-driven message processing in the PIXEL-V2 system.
