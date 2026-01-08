package com.pixel.v2.referential.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_flow_rules table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_FLOW_RULES", schema = "TIB_AUDIT_TEC")
public class RefFlowRules {

    @Id
    @Column(name = "FLOWCODE")
    private String flowCode;

    @Column(name = "TRANSPORTTYPE")
    private String transportType;

    @Column(name = "ISUNITARY")
    private Boolean isUnitary;

    @Column(name = "PRIORITY")
    private String priority;

    @Column(name = "URGENCY")
    private String urgency;

    @Column(name = "FLOWCONTROLLEDENABLED")
    private Boolean flowControlledEnabled;

    @Column(name = "FLOWMAXIMUM")
    private Integer flowMaximum;

    @Column(name = "FLOWRETENTIONENABLED")
    private Boolean flowRetentionEnabled;

    @Column(name = "RETENTIONCYCLEPERIOD")
    private String retentionCyclePeriod;

    @Column(name = "WRITE_FILE")
    private Boolean writeFile;

    @Column(name = "MINREQUIREDFILESIZE")
    private Integer minRequiredFileSize;

    @Column(name = "IGNOREOUTPUTDUPCHECK")
    private Boolean ignoreOutputDupCheck;

    @Column(name = "LOGALL")
    private Boolean logAll;

    // Default constructor
    public RefFlowRules() {
        // Required for JPA
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

    public Integer getFlowMaximum() {
        return flowMaximum;
    }

    public void setFlowMaximum(Integer flowMaximum) {
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

    public Integer getMinRequiredFileSize() {
        return minRequiredFileSize;
    }

    public void setMinRequiredFileSize(Integer minRequiredFileSize) {
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
}
