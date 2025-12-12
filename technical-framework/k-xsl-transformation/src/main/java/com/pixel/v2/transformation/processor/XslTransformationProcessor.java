package com.pixel.v2.transformation.processor;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Processor for XSL transformation of XML messages (single or collection). Supports both single
 * String messages and Collection<String> for batch transformation.
 * 
 * Configuration via headers: - XslFileName: Name of the XSL file (required) - TransformationMode:
 * STRICT (fail on error) or LENIENT (continue on error)
 * 
 * Output headers: - TransformationStatus: SUCCESS, ERROR, or PARTIAL (for collections with some
 * failures) - TransformationScope: SINGLE or COLLECTION - TransformationCount: Number of messages
 * processed - TransformationSuccessCount: Number of successful transformations -
 * TransformationErrorCount: Number of failed transformations - TransformationDuration: Processing
 * duration in milliseconds - TransformationTimestamp: ISO timestamp of completion -
 * TransformationError: Error details (if any)
 */
@Component("xslTransformationProcessor")
public class XslTransformationProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(XslTransformationProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // Get configuration from headers
            String xslFileName = exchange.getIn().getHeader("XslFileName", String.class);
            String transformationMode =
                    exchange.getIn().getHeader("TransformationMode", "STRICT", String.class);

            if (xslFileName == null || xslFileName.trim().isEmpty()) {
                throw new XslTransformationException("XslFileName header is required");
            }

            // Load XSL stylesheet
            Templates templates = loadXslTemplate(xslFileName);

            // Get message body
            Object body = exchange.getIn().getBody();

            if (body == null) {
                throw new XslTransformationException("Message body is null");
            }

            // Process based on message type
            if (body instanceof Collection) {
                processCollection(exchange, templates, (Collection<?>) body, transformationMode,
                        startTime);
            } else if (body instanceof String) {
                processSingleMessage(exchange, templates, (String) body, startTime);
            } else {
                throw new XslTransformationException("Unsupported body type: "
                        + body.getClass().getName() + ". Expected String or Collection<String>");
            }

        } catch (Exception e) {
            handleError(exchange, e, startTime);
            throw e;
        }
    }

    /**
     * Process a single XML message
     */
    private void processSingleMessage(Exchange exchange, Templates templates, String xmlContent,
            long startTime) throws XslTransformationException {

        logger.info("[XSL-TRANSFORMATION] Processing single message");

        try {
            String transformedXml = transformXml(xmlContent, templates);

            logger.debug("[XSL-TRANSFORMATION] Transformed XML: {}", transformedXml);

            // Set transformed content as body
            exchange.getIn().setBody(transformedXml);

            // Set success headers
            setHeaders(exchange, "SUCCESS", "SINGLE", 1, 1, 0, null, startTime);

            logger.info("[XSL-TRANSFORMATION] ✅ Single message transformed successfully");

        } catch (Exception e) {
            throw new XslTransformationException("XSL transformation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Process a collection of XML messages
     */
    private void processCollection(Exchange exchange, Templates templates, Collection<?> messages,
            String transformationMode, long startTime) throws XslTransformationException {

        logger.debug("[XSL-TRANSFORMATION] Processing collection of {} messages", messages.size());

        if (messages.isEmpty()) {
            logger.warn("[XSL-TRANSFORMATION] Empty collection provided for transformation");
            exchange.getIn().setBody(new ArrayList<>());
            setHeaders(exchange, "SUCCESS", "COLLECTION", 0, 0, 0, null, startTime);
            return;
        }

        List<String> transformedMessages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        int messageIndex = 1;
        for (Object message : messages) {
            try {
                if (!(message instanceof String)) {
                    throw new XslTransformationException(
                            "Message #" + messageIndex + " is not a String: "
                                    + (message != null ? message.getClass().getName() : "null"));
                }

                String transformedXml = transformXml((String) message, templates);
                transformedMessages.add(transformedXml);
                successCount++;

                logger.debug("[XSL-TRANSFORMATION] Message #{} transformed successfully",
                        messageIndex);

            } catch (Exception e) {
                errorCount++;
                String errorMsg =
                        "Message #" + messageIndex + " transformation failed: " + e.getMessage();
                errors.add(errorMsg);

                logger.warn("[XSL-TRANSFORMATION] {}", errorMsg);

                if ("STRICT".equals(transformationMode)) {
                    throw new XslTransformationException(errorCount + " out of " + messages.size()
                            + " messages failed transformation: " + String.join("; ", errors));
                } else {
                    // In LENIENT mode, add null or original message for failed transformations
                    transformedMessages.add(null);
                }
            }
            messageIndex++;
        }

        // Set transformed collection as body
        exchange.getIn().setBody(transformedMessages);

        // Determine overall status
        String status;
        String errorDetails = null;
        if (errorCount == 0) {
            status = "SUCCESS";
        } else if (successCount == 0) {
            status = "ERROR";
            errorDetails = errorCount + " out of " + messages.size()
                    + " messages failed transformation: " + String.join("; ", errors);
        } else {
            status = "PARTIAL";
            errorDetails = errorCount + " out of " + messages.size()
                    + " messages failed transformation: " + String.join("; ", errors);
        }

        setHeaders(exchange, status, "COLLECTION", messages.size(), successCount, errorCount,
                errorDetails, startTime);

        if ("ERROR".equals(status) && "STRICT".equals(transformationMode)) {
            throw new XslTransformationException(errorDetails);
        }

        logger.info(
                "[XSL-TRANSFORMATION] ✅ Collection transformation completed - Success: {}, Errors: {}, Status: {}",
                successCount, errorCount, status);
    }

    /**
     * Transform XML using XSL template
     */
    private String transformXml(String xmlContent, Templates templates)
            throws TransformerException {
        Transformer transformer = templates.newTransformer();

        StringReader xmlReader = new StringReader(xmlContent);
        StringWriter resultWriter = new StringWriter();

        Source xmlSource = new StreamSource(xmlReader);
        Result result = new StreamResult(resultWriter);

        transformer.transform(xmlSource, result);

        return resultWriter.toString();
    }

    /**
     * Load XSL template from classpath
     */
    private Templates loadXslTemplate(String xslFileName) throws XslTransformationException {
        try {
            String xslPath = "/xsl/" + xslFileName;

            try (InputStream xslStream = getClass().getResourceAsStream(xslPath)) {
                if (xslStream == null) {
                    throw new XslTransformationException("XSL stylesheet file not found: "
                            + xslFileName + " (looking in classpath: " + xslPath + ")");
                }

                TransformerFactory factory = TransformerFactory.newInstance();
                Source xslSource = new StreamSource(xslStream);

                return factory.newTemplates(xslSource);
            }

        } catch (TransformerConfigurationException e) {
            throw new XslTransformationException(
                    "Failed to parse XSL stylesheet '" + xslFileName + "': " + e.getMessage(), e);
        } catch (Exception e) {
            throw new XslTransformationException(
                    "Failed to load XSL stylesheet '" + xslFileName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Set transformation result headers
     */
    private void setHeaders(Exchange exchange, String status, String scope, int totalCount,
            int successCount, int errorCount, String errorDetails, long startTime) {

        long duration = System.currentTimeMillis() - startTime;

        exchange.getIn().setHeader("TransformationStatus", status);
        exchange.getIn().setHeader("TransformationScope", scope);
        exchange.getIn().setHeader("TransformationCount", totalCount);
        exchange.getIn().setHeader("TransformationSuccessCount", successCount);
        exchange.getIn().setHeader("TransformationErrorCount", errorCount);
        exchange.getIn().setHeader("TransformationDuration", duration);
        exchange.getIn().setHeader("TransformationTimestamp", java.time.Instant.now().toString());

        if (errorDetails != null) {
            exchange.getIn().setHeader("TransformationError", errorDetails);
        }
    }

    /**
     * Handle transformation errors
     */
    private void handleError(Exchange exchange, Exception e, long startTime) {
        logger.error(
                "[XSL-TRANSFORMATION] ❌ Transformation failed - XSL: {}, Duration: {}ms, Error: {}",
                exchange.getIn().getHeader("XslFileName"), System.currentTimeMillis() - startTime,
                e.getMessage());

        exchange.getIn().setHeader("TransformationStatus", "ERROR");
        exchange.getIn().setHeader("TransformationError", e.getMessage());

        // For single message errors, set count based on context
        Object body = exchange.getIn().getBody();
        if (body instanceof Collection) {
            exchange.getIn().setHeader("TransformationCount", ((Collection<?>) body).size());
        } else {
            exchange.getIn().setHeader("TransformationCount", 1);
        }
        exchange.getIn().setHeader("TransformationDuration",
                System.currentTimeMillis() - startTime);
        exchange.getIn().setHeader("TransformationTimestamp", java.time.Instant.now().toString());
    }
}
