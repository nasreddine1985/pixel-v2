package com.pixel.v2.logging.processor;

import com.pixel.v2.logging.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * LogPersistenceProcessor handles the persistence of log entries to the database
 * 
 * This processor can handle: - Log entries as JSON strings - Log entries as LogEntry objects - Log
 * entries created from Camel exchange headers and body
 */
@Component("logPersistenceProcessor")
public class LogPersistenceProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(LogPersistenceProcessor.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    public LogPersistenceProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional
    public void process(Exchange exchange) throws Exception {
        try {
            LogEntry logEntry = extractLogEntry(exchange);

            // Enrich with Camel exchange information if not already set
            enrichWithExchangeInfo(logEntry, exchange);

            // Validate and set defaults
            validateAndSetDefaults(logEntry);

            // Persist to database
            entityManager.persist(logEntry);
            entityManager.flush();

            // Set success headers
            exchange.getIn().setHeader("LogPersistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("LogEntryId", logEntry.getId());
            exchange.getIn().setHeader("LogPersistenceTimestamp", LocalDateTime.now());

        } catch (Exception e) {
            // Set error headers
            exchange.getIn().setHeader("LogPersistenceStatus", "FAILED");
            exchange.getIn().setHeader("LogPersistenceError", e.getMessage());

            // Create error log entry
            LogEntry errorEntry =
                    LogEntry.error("k-log-tx", "Failed to persist log entry: " + e.getMessage());
            errorEntry.setException(getStackTrace(e));
            errorEntry.setCorrelationId(exchange.getIn().getHeader("CorrelationId", String.class));

            try {
                entityManager.persist(errorEntry);
                entityManager.flush();
            } catch (Exception persistError) {
                // Log to system logger as last resort
                logger.error("Critical: Failed to persist error log entry: {}",
                        persistError.getMessage());
            }

            throw e;
        }
    }

    private LogEntry extractLogEntry(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();

        if (body instanceof LogEntry) {
            return (LogEntry) body;
        } else if (body instanceof String) {
            String bodyStr = (String) body;
            if (bodyStr.trim().startsWith("{")) {
                // Try to parse as JSON
                try {
                    return objectMapper.readValue(bodyStr, LogEntry.class);
                } catch (Exception e) {
                    // If JSON parsing fails, create log entry from string message
                    return createLogEntryFromMessage(bodyStr, exchange);
                }
            } else {
                return createLogEntryFromMessage(bodyStr, exchange);
            }
        } else {
            // Create log entry from exchange headers and convert body to string
            String message = body != null ? body.toString() : "Empty message";
            return createLogEntryFromMessage(message, exchange);
        }
    }

    private LogEntry createLogEntryFromMessage(String message, Exchange exchange) {
        // Extract information from headers
        String logLevel = exchange.getIn().getHeader("LogLevel", "INFO", String.class);
        String logSource = exchange.getIn().getHeader("LogSource", "unknown", String.class);
        String logCategory = exchange.getIn().getHeader("LogCategory", String.class);
        String correlationId = exchange.getIn().getHeader("CorrelationId", String.class);
        Long processingTime = exchange.getIn().getHeader("ProcessingTime", Long.class);

        LogEntry logEntry = new LogEntry(logLevel, logSource, message);
        logEntry.setLogCategory(logCategory);
        logEntry.setCorrelationId(correlationId);
        logEntry.setProcessingTime(processingTime);

        return logEntry;
    }

    private void enrichWithExchangeInfo(LogEntry logEntry, Exchange exchange) {
        // Set Camel-specific information if not already set
        if (logEntry.getExchangeId() == null) {
            logEntry.setExchangeId(exchange.getExchangeId());
        }

        if (logEntry.getBreadcrumbId() == null
                && exchange.getIn().getHeader("breadcrumbId") != null) {
            logEntry.setBreadcrumbId(exchange.getIn().getHeader("breadcrumbId", String.class));
        }

        if (logEntry.getRouteId() == null) {
            logEntry.setRouteId(exchange.getFromRouteId());
        }

        if (logEntry.getThreadName() == null) {
            logEntry.setThreadName(Thread.currentThread().getName());
        }

        // Calculate message size if not set
        if (logEntry.getMessageSize() == null) {
            Object body = exchange.getIn().getBody();
            if (body instanceof String) {
                logEntry.setMessageSize((long) ((String) body).length());
            } else if (body != null) {
                logEntry.setMessageSize((long) body.toString().length());
            }
        }

        // Extract additional data from headers
        if (logEntry.getAdditionalData() == null) {
            try {
                Map<String, Object> headers = exchange.getIn().getHeaders();
                // Filter out standard Camel headers and only include custom ones
                Map<String, Object> customHeaders = headers.entrySet().stream()
                        .filter(entry -> !entry.getKey().startsWith("Camel")
                                && !entry.getKey().equals("breadcrumbId")
                                && !entry.getKey().equals("LogLevel")
                                && !entry.getKey().equals("LogSource")
                                && !entry.getKey().equals("LogCategory"))
                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                                Map.Entry::getValue));

                if (!customHeaders.isEmpty()) {
                    logEntry.setAdditionalData(objectMapper.writeValueAsString(customHeaders));
                }
            } catch (Exception e) {
                // Ignore JSON serialization errors for additional data
            }
        }
    }

    private void validateAndSetDefaults(LogEntry logEntry) {
        if (logEntry.getLogTimestamp() == null) {
            logEntry.setLogTimestamp(LocalDateTime.now());
        }

        if (logEntry.getLogLevel() == null || logEntry.getLogLevel().trim().isEmpty()) {
            logEntry.setLogLevel("INFO");
        }

        if (logEntry.getLogSource() == null || logEntry.getLogSource().trim().isEmpty()) {
            logEntry.setLogSource("unknown");
        }

        if (logEntry.getMessage() == null) {
            logEntry.setMessage("Empty log message");
        }

        // Ensure message doesn't exceed database column limit
        if (logEntry.getMessage().length() > 4000) {
            logEntry.setMessage(logEntry.getMessage().substring(0, 3997) + "...");
        }

        // Validate log level
        String level = logEntry.getLogLevel().toUpperCase();
        if (!level.matches("^(TRACE|DEBUG|INFO|WARN|ERROR)$")) {
            logEntry.setLogLevel("INFO");
        }
    }

    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
