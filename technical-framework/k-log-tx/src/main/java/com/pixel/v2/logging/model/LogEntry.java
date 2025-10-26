package com.pixel.v2.logging.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * LogEntry entity for persisting application logs
 * 
 * This entity stores centralized log information from various application components
 * including routing events, business operations, errors, and audit trails.
 */
@Entity
@Table(name = "LOG_ENTRIES", indexes = {
    @Index(name = "idx_log_timestamp", columnList = "logTimestamp"),
    @Index(name = "idx_log_level", columnList = "logLevel"),
    @Index(name = "idx_log_source", columnList = "logSource"),
    @Index(name = "idx_correlation_id", columnList = "correlationId")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_entry_seq")
    @SequenceGenerator(name = "log_entry_seq", sequenceName = "LOG_ENTRY_SEQ", allocationSize = 1)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id;

    @Column(name = "logTimestamp", nullable = false)
    @JsonProperty("logTimestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logTimestamp;

    @Column(name = "logLevel", nullable = false, length = 10)
    @JsonProperty("logLevel")
    private String logLevel; // INFO, WARN, ERROR, DEBUG, TRACE

    @Column(name = "logSource", nullable = false, length = 100)
    @JsonProperty("logSource")
    private String logSource; // ingestion, processing, k-db-tx, etc.

    @Column(name = "logCategory", length = 50)
    @JsonProperty("logCategory")
    private String logCategory; // ROUTE, BUSINESS, ERROR, AUDIT, PERFORMANCE

    @Column(name = "message", nullable = false, length = 4000)
    @JsonProperty("message")
    private String message;

    @Column(name = "correlationId", length = 100)
    @JsonProperty("correlationId")
    private String correlationId; // For tracing related log entries

    @Column(name = "exchangeId", length = 100)
    @JsonProperty("exchangeId")
    private String exchangeId; // Camel exchange ID

    @Column(name = "routeId", length = 100)
    @JsonProperty("routeId")
    private String routeId; // Camel route ID

    @Column(name = "breadcrumbId", length = 100)
    @JsonProperty("breadcrumbId")
    private String breadcrumbId; // Camel breadcrumb ID

    @Column(name = "exception", columnDefinition = "CLOB")
    @JsonProperty("exception")
    private String exception; // Exception stack trace if any

    @Column(name = "additionalData", columnDefinition = "CLOB")
    @JsonProperty("additionalData")
    private String additionalData; // Additional JSON data

    @Column(name = "processingTime")
    @JsonProperty("processingTime")
    private Long processingTime; // Processing time in milliseconds

    @Column(name = "messageSize")
    @JsonProperty("messageSize")
    private Long messageSize; // Message size in bytes

    @Column(name = "threadName", length = 100)
    @JsonProperty("threadName")
    private String threadName;

    @Column(name = "createdAt", nullable = false)
    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // Default constructor
    public LogEntry() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public LogEntry(String logLevel, String logSource, String message) {
        this();
        this.logTimestamp = LocalDateTime.now();
        this.logLevel = logLevel;
        this.logSource = logSource;
        this.message = message;
    }

    // Full constructor
    public LogEntry(String logLevel, String logSource, String logCategory, String message, 
                   String correlationId, String exchangeId, String routeId) {
        this(logLevel, logSource, message);
        this.logCategory = logCategory;
        this.correlationId = correlationId;
        this.exchangeId = exchangeId;
        this.routeId = routeId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getLogTimestamp() { return logTimestamp; }
    public void setLogTimestamp(LocalDateTime logTimestamp) { this.logTimestamp = logTimestamp; }

    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

    public String getLogSource() { return logSource; }
    public void setLogSource(String logSource) { this.logSource = logSource; }

    public String getLogCategory() { return logCategory; }
    public void setLogCategory(String logCategory) { this.logCategory = logCategory; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getExchangeId() { return exchangeId; }
    public void setExchangeId(String exchangeId) { this.exchangeId = exchangeId; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getBreadcrumbId() { return breadcrumbId; }
    public void setBreadcrumbId(String breadcrumbId) { this.breadcrumbId = breadcrumbId; }

    public String getException() { return exception; }
    public void setException(String exception) { this.exception = exception; }

    public String getAdditionalData() { return additionalData; }
    public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }

    public Long getProcessingTime() { return processingTime; }
    public void setProcessingTime(Long processingTime) { this.processingTime = processingTime; }

    public Long getMessageSize() { return messageSize; }
    public void setMessageSize(Long messageSize) { this.messageSize = messageSize; }

    public String getThreadName() { return threadName; }
    public void setThreadName(String threadName) { this.threadName = threadName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Utility methods for common log levels
    public static LogEntry info(String source, String message) {
        return new LogEntry("INFO", source, message);
    }

    public static LogEntry warn(String source, String message) {
        return new LogEntry("WARN", source, message);
    }

    public static LogEntry error(String source, String message) {
        return new LogEntry("ERROR", source, message);
    }

    public static LogEntry debug(String source, String message) {
        return new LogEntry("DEBUG", source, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogEntry logEntry = (LogEntry) o;
        return Objects.equals(id, logEntry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "id=" + id +
                ", logTimestamp=" + logTimestamp +
                ", logLevel='" + logLevel + '\'' +
                ", logSource='" + logSource + '\'' +
                ", logCategory='" + logCategory + '\'' +
                ", message='" + message + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", routeId='" + routeId + '\'' +
                '}';
    }
}