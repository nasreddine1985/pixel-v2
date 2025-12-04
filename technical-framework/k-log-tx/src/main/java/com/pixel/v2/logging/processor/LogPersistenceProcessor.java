package com.pixel.v2.logging.processor;

import java.time.LocalDateTime;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * LogPersistenceProcessor handles simple log processing without database persistence
 * 
 * This processor logs messages to the standard logging framework
 */
@Component("logPersistenceProcessor")
public class LogPersistenceProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(LogPersistenceProcessor.class);

    private final ObjectMapper objectMapper;

    public LogPersistenceProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            // Extract log information from exchange
            String logLevel = exchange.getIn().getHeader("LogLevel", "INFO", String.class);
            String logSource = exchange.getIn().getHeader("LogSource", "unknown", String.class);
            String logCategory = exchange.getIn().getHeader("LogCategory", String.class);
            String correlationId = exchange.getIn().getHeader("CorrelationId", String.class);

            Object body = exchange.getIn().getBody();
            String message = body != null ? body.toString() : "Empty message";

            // Create structured log message
            StringBuilder logMsg = new StringBuilder();
            logMsg.append("[").append(logSource).append("]");
            if (logCategory != null) {
                logMsg.append("[").append(logCategory).append("]");
            }
            if (correlationId != null) {
                logMsg.append("[").append(correlationId).append("]");
            }
            logMsg.append(" ").append(message);

            // Log based on level
            String finalMessage = logMsg.toString();
            switch (logLevel.toUpperCase()) {
                case "TRACE":
                    logger.trace(finalMessage);
                    break;
                case "DEBUG":
                    logger.debug(finalMessage);
                    break;
                case "WARN":
                    logger.warn(finalMessage);
                    break;
                case "ERROR":
                    logger.error(finalMessage);
                    break;
                default:
                    logger.info(finalMessage);
                    break;
            }

            // Set success headers
            exchange.getIn().setHeader("LogPersistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("LogPersistenceTimestamp", LocalDateTime.now());

        } catch (Exception e) {
            // Set error headers
            exchange.getIn().setHeader("LogPersistenceStatus", "FAILED");
            exchange.getIn().setHeader("LogPersistenceError", e.getMessage());

            // Log error
            logger.error("Failed to process log entry: {}", e.getMessage());

            throw e;
        }
    }
}
