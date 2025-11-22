package com.pixel.v2.validation.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/**
 * XSD Validation Processor for validating XML messages against XSD schemas Supports schema caching
 * and detailed error reporting
 */
@Component("xsdValidationProcessor")
public class XsdValidationProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(XsdValidationProcessor.class);

    // Cache for compiled schemas to improve performance
    private final ConcurrentHashMap<String, Schema> schemaCache = new ConcurrentHashMap<>();

    private static final String XSD_FOLDER_PATH = "/xsd/";
    private static final String XSD_FILE_NAME = "XsdFileName";
    private static final String VALIDATION_MODE = "ValidationMode";
    private static final String VALIDATION_MODE_STRICT = "STRICT";
    private static final String VALIDATION_DURATION = "ValidationDuration";
    private static final String VALIDATION_SCOPE = "ValidationScope";
    private static final String VALIDATION_COUNT = "ValidationCount";
    private static final String VALIDATION_SUCCESS_COUNT = "ValidationSuccessCount";
    private static final String VALIDATION_ERROR_COUNT = "ValidationErrorCount";

    @Override
    public void process(Exchange exchange) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // Get XSD file name from header
            String xsdFileName = exchange.getIn().getHeader(XSD_FILE_NAME, String.class);
            if (xsdFileName == null || xsdFileName.trim().isEmpty()) {
                throw new XsdValidationException("XSD file name header is missing or empty");
            }

            // Get validation mode (STRICT or LENIENT)
            String validationMode = exchange.getIn().getHeader(VALIDATION_MODE,
                    VALIDATION_MODE_STRICT, String.class);

            Object body = exchange.getIn().getBody();
            if (body == null) {
                throw new XsdValidationException("Message body is null or empty");
            }

            // Load and cache schema
            Schema schema = loadSchema(xsdFileName);

            // Check if body is a collection or single message
            if (body instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) body;
                validateCollection(collection, schema, xsdFileName, validationMode, exchange);
            } else if (body instanceof String) {
                String xmlContent = (String) body;
                validateSingleMessage(xmlContent, schema, xsdFileName, validationMode, exchange);
            } else {
                throw new XsdValidationException("Unsupported message body type: "
                        + body.getClass().getName() + ". Expected String or Collection<String>");
            }

            // Calculate validation duration
            long duration = System.currentTimeMillis() - startTime;
            exchange.getIn().setHeader(VALIDATION_DURATION, duration);

            logger.debug("[XSD-VALIDATION] Validation completed successfully in {}ms", duration);

        } catch (XsdValidationException e) {
            handleValidationError(exchange, e, System.currentTimeMillis() - startTime);
            throw e; // Re-throw to stop the route
        } catch (Exception e) {
            XsdValidationException validationException = new XsdValidationException(
                    "Unexpected error during XSD validation: " + e.getMessage(), e);
            handleValidationError(exchange, validationException,
                    System.currentTimeMillis() - startTime);
            throw validationException;
        }
    }

    /**
     * Validates a single XML message
     */
    private void validateSingleMessage(String xmlContent, Schema schema, String xsdFileName,
            String validationMode, Exchange exchange) throws XsdValidationException {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new XsdValidationException("XML message content is null or empty");
        }

        logger.debug("[XSD-VALIDATION] Starting single message validation - XSD: {}, Mode: {}",
                xsdFileName, validationMode);

        // Set headers first so they're available even if validation fails
        exchange.getIn().setHeader(VALIDATION_SCOPE, "SINGLE");
        exchange.getIn().setHeader(VALIDATION_COUNT, 1);

        validateXmlContent(xmlContent, schema, xsdFileName, validationMode);

        // Set success headers only after successful validation
        exchange.getIn().setHeader("ValidationStatus", "SUCCESS");
        exchange.getIn().setHeader("ValidationTimestamp",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        logger.debug("[XSD-VALIDATION] Single message validation successful");
    }

    /**
     * Validates a collection of XML messages
     */
    private void validateCollection(Collection<?> collection, Schema schema, String xsdFileName,
            String validationMode, Exchange exchange) throws XsdValidationException {
        if (collection.isEmpty()) {
            logger.warn("[XSD-VALIDATION] Empty collection provided for validation");
            exchange.getIn().setHeader("ValidationStatus", "SUCCESS");
            exchange.getIn().setHeader(VALIDATION_SCOPE, "COLLECTION");
            exchange.getIn().setHeader(VALIDATION_COUNT, 0);
            exchange.getIn().setHeader(VALIDATION_SUCCESS_COUNT, 0);
            exchange.getIn().setHeader(VALIDATION_ERROR_COUNT, 0);
            exchange.getIn().setHeader("ValidationTimestamp",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return;
        }

        logger.debug(
                "[XSD-VALIDATION] Starting collection validation - XSD: {}, Mode: {}, Count: {}",
                xsdFileName, validationMode, collection.size());

        int messageIndex = 0;
        List<String> validationErrors = new ArrayList<>();
        int successCount = 0;

        for (Object item : collection) {
            messageIndex++;
            String error = validateSingleItemInCollection(item, messageIndex, schema, xsdFileName,
                    validationMode);

            if (error != null) {
                validationErrors.add(error);
                logger.warn("[XSD-VALIDATION] Message #{} validation failed: {}", messageIndex,
                        error);
            } else {
                successCount++;
                logger.debug("[XSD-VALIDATION] Message #{} validated successfully", messageIndex);
            }
        }

        // Set collection validation headers
        exchange.getIn().setHeader(VALIDATION_SCOPE, "COLLECTION");
        exchange.getIn().setHeader(VALIDATION_COUNT, collection.size());
        exchange.getIn().setHeader(VALIDATION_SUCCESS_COUNT, successCount);
        exchange.getIn().setHeader(VALIDATION_ERROR_COUNT, validationErrors.size());

        if (!validationErrors.isEmpty()) {
            exchange.getIn().setHeader("ValidationStatus", "ERROR");
            String errorSummary = String.format("%d out of %d messages failed validation: %s",
                    validationErrors.size(), collection.size(),
                    String.join("; ", validationErrors));
            exchange.getIn().setHeader("ValidationError", errorSummary);
            throw new XsdValidationException(errorSummary);
        }

        // Success case
        exchange.getIn().setHeader("ValidationStatus", "SUCCESS");
        exchange.getIn().setHeader("ValidationTimestamp",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        logger.debug("[XSD-VALIDATION] Collection validation successful - {} messages validated",
                successCount);
    }

    /**
     * Validates a single item in a collection and returns error message if validation fails
     * 
     * @return error message if validation fails, null if successful
     */
    private String validateSingleItemInCollection(Object item, int messageIndex, Schema schema,
            String xsdFileName, String validationMode) {
        if (!(item instanceof String)) {
            return String.format("Message #%d is not a String: %s", messageIndex,
                    item != null ? item.getClass().getName() : "null");
        }

        String xmlContent = (String) item;
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return String.format("Message #%d is null or empty", messageIndex);
        }

        try {
            validateXmlContent(xmlContent, schema, xsdFileName, VALIDATION_MODE_STRICT);
            return null; // Success
        } catch (XsdValidationException e) {
            return String.format("Message #%d validation failed: %s", messageIndex, e.getMessage());
        }
    }

    /**
     * Validates XML content against the provided schema
     */
    private void validateXmlContent(String xmlContent, Schema schema, String xsdFileName,
            String validationMode) throws XsdValidationException {

        // Skip validation for JSON content to prevent routing loops
        if (xmlContent != null
                && (xmlContent.trim().startsWith("{") || xmlContent.trim().startsWith("["))) {
            logger.warn("[XSD-VALIDATION] ⚠️ Skipping XSD validation for JSON content - XSD: {}",
                    xsdFileName);
            return; // Skip validation for JSON content
        }

        try {
            Validator validator = schema.newValidator();

            // Create custom error handler for detailed error reporting
            XsdValidationErrorHandler errorHandler = new XsdValidationErrorHandler(validationMode);
            validator.setErrorHandler(errorHandler);

            // Perform validation
            validator.validate(new StreamSource(new StringReader(xmlContent)));

            // Check if there were validation errors
            if (errorHandler.hasErrors() && VALIDATION_MODE_STRICT.equals(validationMode)) {
                throw new XsdValidationException(
                        String.format("XSD validation failed against schema '%s': %s", xsdFileName,
                                errorHandler.getErrorSummary()));
            }

            if (errorHandler.hasWarnings() && VALIDATION_MODE_STRICT.equals(validationMode)) {
                logger.warn("[XSD-VALIDATION] Validation warnings for XSD {}: {}", xsdFileName,
                        errorHandler.getWarningSummary());
            }

        } catch (SAXException e) {
            throw new XsdValidationException(
                    String.format("XSD validation error against schema '%s': %s", xsdFileName,
                            e.getMessage()),
                    e);
        } catch (IOException e) {
            throw new XsdValidationException(
                    String.format("IO error during validation against schema '%s': %s", xsdFileName,
                            e.getMessage()),
                    e);
        }
    }

    /**
     * Loads and caches XSD schema from classpath
     */
    private Schema loadSchema(String xsdFileName) throws XsdValidationException {
        // Check cache first
        Schema cachedSchema = schemaCache.get(xsdFileName);
        if (cachedSchema != null) {
            logger.debug("[XSD-VALIDATION] Using cached schema for: {}", xsdFileName);
            return cachedSchema;
        }

        try {
            // Load schema from classpath
            String resourcePath = XSD_FOLDER_PATH + xsdFileName;
            ClassPathResource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                throw new XsdValidationException(
                        String.format("XSD schema file not found: %s (looking in classpath: %s)",
                                xsdFileName, resourcePath));
            }

            // Create schema factory
            SchemaFactory schemaFactory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load and compile schema
            try (InputStream schemaStream = resource.getInputStream()) {
                Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));

                // Cache the compiled schema
                schemaCache.put(xsdFileName, schema);

                logger.debug("[XSD-VALIDATION] Loaded and cached schema: {}", xsdFileName);
                return schema;
            }

        } catch (SAXException e) {
            throw new XsdValidationException(String.format("Failed to parse XSD schema '%s': %s",
                    xsdFileName, e.getMessage()), e);
        } catch (IOException e) {
            throw new XsdValidationException(String.format("Failed to load XSD schema '%s': %s",
                    xsdFileName, e.getMessage()), e);
        }
    }

    /**
     * Handles validation errors by setting appropriate headers and logging
     */
    private void handleValidationError(Exchange exchange, XsdValidationException e, long duration) {
        exchange.getIn().setHeader("ValidationStatus", "ERROR");
        exchange.getIn().setHeader("ValidationError", e.getMessage());
        exchange.getIn().setHeader("ValidationTimestamp",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        exchange.getIn().setHeader(VALIDATION_DURATION, duration);

        String xsdFileName = exchange.getIn().getHeader(XSD_FILE_NAME, "unknown", String.class);
        logger.error("[XSD-VALIDATION] ❌ Validation failed - XSD: {}, Duration: {}ms, Error: {}",
                xsdFileName, duration, e.getMessage());
    }

    /**
     * Clears the schema cache (useful for testing or dynamic schema updates)
     */
    public void clearSchemaCache() {
        schemaCache.clear();
        logger.info("[XSD-VALIDATION] Schema cache cleared");
    }

    /**
     * Returns the current cache size
     */
    public int getCacheSize() {
        return schemaCache.size();
    }
}
