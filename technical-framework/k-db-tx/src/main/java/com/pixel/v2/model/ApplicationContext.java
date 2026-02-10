package com.pixel.v2.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entity representing APPLICATION_CONTEXT table Contains application context data associated with
 * log events for flow processing
 */
@Entity
@Table(name = "APPLICATION_CONTEXT",
        uniqueConstraints = {@UniqueConstraint(name = "uq_app_context_logid_name",
                columnNames = {"logId", "name"})},
        indexes = {@Index(name = "idx_app_context_logid", columnList = "logId"),
                @Index(name = "idx_app_context_name", columnList = "name"),
                @Index(name = "idx_app_context_flowid", columnList = "flowid"),
                @Index(name = "idx_app_context_datats", columnList = "datats")})
public class ApplicationContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "LOGID", length = 64, nullable = false)
    private String logId;

    @Column(name = "NAME", length = 255, nullable = false)
    private String name;

    @Column(name = "DATATS", nullable = false, precision = 6)
    private LocalDateTime datats;

    @Column(name = "FLOWID", length = 64, nullable = false)
    private String flowId;

    @Column(name = "VALUE", columnDefinition = "TEXT")
    private String value;

    @ManyToOne
    @JoinColumn(name = "LOGID", referencedColumnName = "LOGID", insertable = false,
            updatable = false, foreignKey = @ForeignKey(name = "fk_app_context_log_event"))
    private LogEvent logEvent;

    // Default constructor
    public ApplicationContext() {
        this.datats = LocalDateTime.now();
    }

    // Constructor with essential fields
    public ApplicationContext(String logId, String name, String flowId, String value) {
        this();
        this.logId = logId;
        this.name = name;
        this.flowId = flowId;
        this.value = value;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDatats() {
        return datats;
    }

    public void setDatats(LocalDateTime datats) {
        this.datats = datats;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    @Override
    public String toString() {
        return "ApplicationContext{" + "logId='" + logId + '\'' + ", name='" + name + '\''
                + ", flowId='" + flowId + '\'' + ", datats=" + datats + ", value='"
                + (value != null ? value.substring(0, Math.min(50, value.length())) : "null")
                + "...'" + '}';
    }
}
