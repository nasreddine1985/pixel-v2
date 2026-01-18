package com.pixel.v2.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entity representing EXCEPTION table Contains error and exception logging information for flow
 * processing
 */
@Entity
@Table(name = "EXCEPTION",
        indexes = {@Index(name = "idx_exception_datats", columnList = "datats"),
                @Index(name = "idx_exception_component", columnList = "component"),
                @Index(name = "idx_exception_severity", columnList = "severity"),
                @Index(name = "idx_exception_type", columnList = "type")})
public class ErrorLogEvent {

    @Id
    @Column(name = "LOGID", length = 64, nullable = false)
    private String logId;

    @Column(name = "DATATS", nullable = false, precision = 6)
    private LocalDateTime datats;

    @Column(name = "ERRTIMESTAMP", nullable = false, precision = 6)
    private LocalDateTime errTimestamp;

    @Column(name = "COMPONENT", length = 64, nullable = false)
    private String component;

    @Column(name = "INSTANCEID", length = 128, nullable = false)
    private String instanceId;

    @Column(name = "PROCESSSTACK", length = 1024)
    private String processStack;

    @Column(name = "ROOTPROCESSPATH", length = 256)
    private String rootProcessPath;

    @Column(name = "CODE", length = 32)
    private String code;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "SHORTDESC", length = 512)
    private String shortDesc;

    @Column(name = "TYPE", length = 1, nullable = false)
    private String type;

    @Column(name = "SEVERITY", nullable = false)
    private Integer severity;

    @Column(name = "STACK", columnDefinition = "TEXT")
    private String stack;

    @Column(name = "INPUT", columnDefinition = "TEXT")
    private String input;

    @Column(name = "DEBUGDATA", columnDefinition = "TEXT")
    private String debugData;

    // Default constructor
    public ErrorLogEvent() {
        this.datats = LocalDateTime.now();
        this.type = "E";
    }

    // Constructor with essential fields
    public ErrorLogEvent(String logId, String component, String shortDesc, Integer severity) {
        this();
        this.logId = logId;
        this.component = component;
        this.shortDesc = shortDesc;
        this.severity = severity;
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public LocalDateTime getDatats() {
        return datats;
    }

    public void setDatats(LocalDateTime datats) {
        this.datats = datats;
    }

    public LocalDateTime getErrTimestamp() {
        return errTimestamp;
    }

    public void setErrTimestamp(LocalDateTime errTimestamp) {
        this.errTimestamp = errTimestamp;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getProcessStack() {
        return processStack;
    }

    public void setProcessStack(String processStack) {
        this.processStack = processStack;
    }

    public String getRootProcessPath() {
        return rootProcessPath;
    }

    public void setRootProcessPath(String rootProcessPath) {
        this.rootProcessPath = rootProcessPath;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getDebugData() {
        return debugData;
    }

    public void setDebugData(String debugData) {
        this.debugData = debugData;
    }

    @Override
    public String toString() {
        return "ErrorLogEvent{" + "logId='" + logId + '\'' + ", component='" + component + '\''
                + ", shortDesc='" + shortDesc + '\'' + ", severity=" + severity + ", type='" + type
                + '\'' + ", datats=" + datats + '}';
    }
}
