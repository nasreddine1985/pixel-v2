# K-Referentiel Data Loader Usage Examples

## Basic FlowReference Loading

```yaml
# Simple usage - load PACS008 flow configuration
from:
  uri: "direct:load-pacs008-config"
  steps:
    - kamelet:
        name: k-referentiel-data-loader
        properties:
          serviceUrl: "http://config-service:8099"
          flowCode: "PACS008"

    - log: "FlowReference loaded for ${header.FlowCode}: ${header.FlowName}"

    - choice:
        when:
          - simple: "${header.FlowReferenceLoaded} == true"
            steps:
              - log: "✅ Using service configuration - Priority: ${header.Priority}, Rail: ${header.RailMode}"
              - to: "direct:process-with-service-config"
        otherwise:
          steps:
            - log: "⚠️ Using default configuration for ${header.FlowCode}"
            - to: "direct:process-with-default-config"
```

## Advanced Integration with Processing Logic

```yaml
# Enhanced usage with flow-specific processing
from:
  uri: "direct:process-payment"
  steps:
    - setHeader:
        name: "PaymentFlowCode"
        simple: "${header.MessageType}" # e.g., PACS008, PAIN001, CAMT053

    - kamelet:
        name: k-referentiel-data-loader
        properties:
          serviceUrl: "{{config.service.url}}"
          flowCode: "${header.PaymentFlowCode}"
          configEndpoint: "/api/v2/flows"

    - log: "Configuration loaded: Rail=${header.RailMode}, SLA=${header.SlaMaxLatencyMs}ms"

    # Use FlowReference data for processing decisions
    - choice:
        when:
          - simple: "${header.SplitEnabled} == 'TRUE'"
            steps:
              - log: "Splitting enabled with chunk size: ${header.SplitChunkSize}"
              - split:
                  tokenize: "${header.SplitExpr}"
                  parallelProcessing: true
                  streaming: true
                steps:
                  - to: "direct:process-chunk"

          - simple: "${header.RailMode} == 'INSTANT'"
            steps:
              - log: "Processing in instant mode with priority ${header.Priority}"
              - setHeader:
                  name: "ProcessingTimeout"
                  simple: "${header.SlaMaxLatencyMs}"
              - to: "direct:instant-processing"

        otherwise:
          steps:
            - log: "Standard processing mode"
            - to: "direct:standard-processing"
```

## Error Handling and Fallbacks

```yaml
# Robust configuration loading with error handling
from:
  uri: "timer:config-loader?period=60000"
  steps:
    - setBody:
        constant: null

    - loop:
        copy: true
        expression:
          constant: 3 # Try 3 different flow codes
        steps:
          - setHeader:
              name: "CurrentFlowCode"
              expression:
                method:
                  beanType: "com.example.FlowCodeProvider"
                  method: "getFlowCode(${exchangeProperty.CamelLoopIndex})"

          - kamelet:
              name: k-referentiel-data-loader
              properties:
                serviceUrl: "http://config-service:8099"
                flowCode: "${header.CurrentFlowCode}"

          - choice:
              when:
                - simple: "${header.FlowReferenceLoaded} == true"
                  steps:
                    - log: "✅ Successfully loaded config for ${header.FlowCode}"
                    - marshal:
                        json: {}
                    - to: "kafka:flow-configs?brokers=localhost:9092"

              otherwise:
                steps:
                  - log: "⚠️ Using fallback config for ${header.FlowCode}: ${header.FlowReferenceError}"
                  - to: "direct:handle-config-fallback"
```

## Integration with Transformation Services

```yaml
# Use FlowReference data to drive XSL transformations
from:
  uri: "direct:transform-payment"
  steps:
    # Load flow configuration
    - kamelet:
        name: k-referentiel-data-loader
        properties:
          serviceUrl: "{{config.service.url}}"
          flowCode: "${header.PaymentType}"

    # Extract transformation file from FlowReference data
    - setHeader:
        name: "TransformationFile"
        jsonpath: "$.xsltFileToCdm" # Extract from FlowReference JSON

    # Apply XSL transformation using the configured file
    - kamelet:
        name: k-xsl-transformation
        properties:
          xslFileName: "${header.TransformationFile}"
          transformationMode: "${header.RailMode == 'INSTANT' ? 'STRICT' : 'LENIENT'}"

    # Validate using configured XSD
    - setHeader:
        name: "ValidationSchema"
        jsonpath: "$.xsdCdmFile" # Extract from FlowReference JSON

    - kamelet:
        name: k-xsd-validation
        properties:
          xsdFileName: "${header.ValidationSchema}"
          validationMode: "${header.EncryptionRequired == 'TRUE' ? 'STRICT' : 'LENIENT'}"
```

## Caching and Performance Optimization

```yaml
# Cache FlowReference data to reduce service calls
from:
  uri: "direct:get-cached-flow-config"
  steps:
    - setHeader:
        name: "CacheKey"
        simple: "flow-config-${header.FlowCode}"

    # Try to get from cache first
    - to: "caffeine-cache:flow-configs?action=GET&key=${header.CacheKey}"

    - choice:
        when:
          - simple: "${body} != null"
            steps:
              - log: "🚀 Using cached FlowReference for ${header.FlowCode}"
              - unmarshal:
                  json: {}
              # Set headers from cached data
              - process: "flowReferenceProcessor"

        otherwise:
          steps:
            - log: "📡 Loading FlowReference from service for ${header.FlowCode}"
            - kamelet:
                name: k-referentiel-data-loader
                properties:
                  serviceUrl: "{{config.service.url}}"
                  flowCode: "${header.FlowCode}"

            # Cache the loaded configuration
            - choice:
                when:
                  - simple: "${header.FlowReferenceLoaded} == true"
                    steps:
                      - setHeader:
                          name: "FlowReferenceJson"
                          simple: "${header.FlowReferenceData}"
                      - to: "caffeine-cache:flow-configs?action=PUT&key=${header.CacheKey}&ttl=300"
```

## Monitoring and Metrics

```yaml
# Add monitoring and metrics collection
from:
  uri: "direct:monitored-config-loading"
  steps:
    - setHeader:
        name: "LoadStartTime"
        simple: "${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"

    - kamelet:
        name: k-referentiel-data-loader
        properties:
          serviceUrl: "{{config.service.url}}"
          flowCode: "${header.FlowCode}"

    # Record metrics
    - choice:
        when:
          - simple: "${header.FlowReferenceLoaded} == true"
            steps:
              - to: "micrometer:counter:flow.config.loaded.success?tags=flowCode=${header.FlowCode}"
              - log: "📊 Config loaded successfully in ${header.FlowReferenceLoadTime}"

        otherwise:
          steps:
            - to: "micrometer:counter:flow.config.loaded.fallback?tags=flowCode=${header.FlowCode}"
            - log: "📊 Using fallback config for ${header.FlowCode}: ${header.FlowReferenceError}"

    # Always record load duration
    - setHeader:
        name: "LoadDuration"
        simple: "${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ} - ${header.LoadStartTime}"
    - to: "micrometer:timer:flow.config.load.duration?tags=flowCode=${header.FlowCode}"
```

## Expected Service API Response

Your service should return JSON at `GET /api/flows/{flowCode}`:

```json
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
  "targetSystem": "CORE_BANKING",
  "splitEnabled": "TRUE",
  "splitChunkSize": 100,
  "xsltFileToCdm": "xslt/pacs008-to-cdm.xsl",
  "xsdCdmFile": "xsd/cdm-payment.xsd",
  "encryptionRequired": "TRUE"
}
```

## Available Headers After Loading

All FlowReference fields are available as headers:

- `FlowId`, `FlowCode`, `FlowName`, `FlowStatus`
- `SourceChannel`, `TargetSystem`, `RailMode`, `Priority`
- `SplitEnabled`, `SplitChunkSize`, `EncryptionRequired`
- `FlowReferenceLoaded` (boolean), `FlowReferenceData` (JSON)
- Plus 20+ more fields from the FlowReference entity
