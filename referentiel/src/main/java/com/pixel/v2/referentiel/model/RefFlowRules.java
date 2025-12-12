package com.pixel.v2.referentiel.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model representing the TIB_AUDIT_TEC.REF_FLOW_RULES table Contains flow processing rules and
 * configuration parameters
 */
public class RefFlowRules {

    @JsonProperty("flowCode")
    private String flowCode;

    @JsonProperty("transportType")
    private String transportType;

    @JsonProperty("isUnitary")
    private Boolean isUnitary;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("urgency")
    private String urgency;

    @JsonProperty("flowControlledEnabled")
    private Boolean flowControlledEnabled;

    @JsonProperty("flowMaximum")
    private Long flowMaximum;

    @JsonProperty("flowRetentionEnabled")
    private Boolean flowRetentionEnabled;

    @JsonProperty("retentionCyclePeriod")
    private String retentionCyclePeriod;

    @JsonProperty("writeFile")
    private Boolean writeFile;

    @JsonProperty("minRequiredFileSize")
    private Long minRequiredFileSize;

    @JsonProperty("ignoreOutputDupCheck")
    private Boolean ignoreOutputDupCheck;

    @JsonProperty("logAll")
    private Boolean logAll;

    // Default constructor
    public RefFlowRules() {}

    // Constructor with required fields
    public RefFlowRules(String flowCode, String transportType) {
        this.flowCode = flowCode;
        this.transportType = transportType;
    }

    // Getters and Setters
    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public Boolean getIsUnitary() {
        return isUnitary;
    }

    public void setIsUnitary(Boolean isUnitary) {
        this.isUnitary = isUnitary;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public Boolean getFlowControlledEnabled() {
        return flowControlledEnabled;
    }

    public void setFlowControlledEnabled(Boolean flowControlledEnabled) {
        this.flowControlledEnabled = flowControlledEnabled;
    }

    public Long getFlowMaximum() {
        return flowMaximum;
    }

    public void setFlowMaximum(Long flowMaximum) {
        this.flowMaximum = flowMaximum;
    }

    public Boolean getFlowRetentionEnabled() {
        return flowRetentionEnabled;
    }

    public void setFlowRetentionEnabled(Boolean flowRetentionEnabled) {
        this.flowRetentionEnabled = flowRetentionEnabled;
    }

    public String getRetentionCyclePeriod() {
        return retentionCyclePeriod;
    }

    public void setRetentionCyclePeriod(String retentionCyclePeriod) {
        this.retentionCyclePeriod = retentionCyclePeriod;
    }

    public Boolean getWriteFile() {
        return writeFile;
    }

    public void setWriteFile(Boolean writeFile) {
        this.writeFile = writeFile;
    }

    public Long getMinRequiredFileSize() {
        return minRequiredFileSize;
    }

    public void setMinRequiredFileSize(Long minRequiredFileSize) {
        this.minRequiredFileSize = minRequiredFileSize;
    }

    public Boolean getIgnoreOutputDupCheck() {
        return ignoreOutputDupCheck;
    }

    public void setIgnoreOutputDupCheck(Boolean ignoreOutputDupCheck) {
        this.ignoreOutputDupCheck = ignoreOutputDupCheck;
    }

    public Boolean getLogAll() {
        return logAll;
    }

    public void setLogAll(Boolean logAll) {
        this.logAll = logAll;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RefFlowRules that = (RefFlowRules) o;
        return Objects.equals(flowCode, that.flowCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowCode);
    }

    @Override
    public String toString() {
        return "RefFlowRules{" + "flowCode='" + flowCode + '\'' + ", transportType='"
                + transportType + '\'' + ", priority='" + priority + '\'' + ", urgency='" + urgency
                + '\'' + '}';
    }
}
