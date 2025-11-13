package com.pixel.v2.referentiel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for FlowReference data - mirrors the FlowReference entity from k-db-tx Used for JSON
 * serialization/deserialization in referentiel data loading
 */
public class FlowReferenceDto {

    @JsonProperty("flowId")
    private String flowId;

    @JsonProperty("flowCode")
    private String flowCode;

    @JsonProperty("flowName")
    private String flowName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("sourceChannel")
    private String sourceChannel;

    @JsonProperty("sourceSystem")
    private String sourceSystem;

    @JsonProperty("sourceFormat")
    private String sourceFormat;

    @JsonProperty("flowType")
    private String flowType;

    @JsonProperty("railMode")
    private String railMode;

    @JsonProperty("slaMaxLatencyMs")
    private Integer slaMaxLatencyMs;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("splitEnabled")
    private String splitEnabled;

    @JsonProperty("splitChunkSize")
    private Integer splitChunkSize;

    @JsonProperty("concatEnabled")
    private String concatEnabled;

    @JsonProperty("concatCriteria")
    private String concatCriteria;

    @JsonProperty("targetSystem")
    private String targetSystem;

    @JsonProperty("targetChannel")
    private String targetChannel;

    @JsonProperty("retentionInEnabled")
    private String retentionInEnabled;

    @JsonProperty("retentionInMode")
    private String retentionInMode;

    @JsonProperty("retentionInDays")
    private Integer retentionInDays;

    @JsonProperty("retentionInReleaseDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate retentionInReleaseDate;

    @JsonProperty("retentionInStoragePath")
    private String retentionInStoragePath;

    @JsonProperty("retentionOutEnabled")
    private String retentionOutEnabled;

    @JsonProperty("retentionOutMode")
    private String retentionOutMode;

    @JsonProperty("retentionOutDays")
    private Integer retentionOutDays;

    @JsonProperty("retentionOutReleaseDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate retentionOutReleaseDate;

    @JsonProperty("retentionOutStoragePath")
    private String retentionOutStoragePath;

    @JsonProperty("shapingEnabled")
    private String shapingEnabled;

    @JsonProperty("shapingMaxTrxPerMin")
    private Integer shapingMaxTrxPerMin;

    @JsonProperty("shapingStrategy")
    private String shapingStrategy;

    @JsonProperty("piiLevel")
    private String piiLevel;

    @JsonProperty("encryptionRequired")
    private String encryptionRequired;

    @JsonProperty("drStrategy")
    private String drStrategy;

    @JsonProperty("lastUpdate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdate;

    @JsonProperty("version")
    private String version;

    @JsonProperty("comments")
    private String comments;

    // Default constructor
    public FlowReferenceDto() {
        this.lastUpdate = LocalDateTime.now();
    }

    // Constructor with required fields
    public FlowReferenceDto(String flowId, String flowCode, String flowName) {
        this();
        this.flowId = flowId;
        this.flowCode = flowCode;
        this.flowName = flowName;
    }

    // Getters and Setters
    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(String sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public String getRailMode() {
        return railMode;
    }

    public void setRailMode(String railMode) {
        this.railMode = railMode;
    }

    public Integer getSlaMaxLatencyMs() {
        return slaMaxLatencyMs;
    }

    public void setSlaMaxLatencyMs(Integer slaMaxLatencyMs) {
        this.slaMaxLatencyMs = slaMaxLatencyMs;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getSplitEnabled() {
        return splitEnabled;
    }

    public void setSplitEnabled(String splitEnabled) {
        this.splitEnabled = splitEnabled;
    }

    public Integer getSplitChunkSize() {
        return splitChunkSize;
    }

    public void setSplitChunkSize(Integer splitChunkSize) {
        this.splitChunkSize = splitChunkSize;
    }

    public String getConcatEnabled() {
        return concatEnabled;
    }

    public void setConcatEnabled(String concatEnabled) {
        this.concatEnabled = concatEnabled;
    }

    public String getConcatCriteria() {
        return concatCriteria;
    }

    public void setConcatCriteria(String concatCriteria) {
        this.concatCriteria = concatCriteria;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    public String getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(String targetChannel) {
        this.targetChannel = targetChannel;
    }

    public String getRetentionInEnabled() {
        return retentionInEnabled;
    }

    public void setRetentionInEnabled(String retentionInEnabled) {
        this.retentionInEnabled = retentionInEnabled;
    }

    public String getRetentionInMode() {
        return retentionInMode;
    }

    public void setRetentionInMode(String retentionInMode) {
        this.retentionInMode = retentionInMode;
    }

    public Integer getRetentionInDays() {
        return retentionInDays;
    }

    public void setRetentionInDays(Integer retentionInDays) {
        this.retentionInDays = retentionInDays;
    }

    public LocalDate getRetentionInReleaseDate() {
        return retentionInReleaseDate;
    }

    public void setRetentionInReleaseDate(LocalDate retentionInReleaseDate) {
        this.retentionInReleaseDate = retentionInReleaseDate;
    }

    public String getRetentionInStoragePath() {
        return retentionInStoragePath;
    }

    public void setRetentionInStoragePath(String retentionInStoragePath) {
        this.retentionInStoragePath = retentionInStoragePath;
    }

    public String getRetentionOutEnabled() {
        return retentionOutEnabled;
    }

    public void setRetentionOutEnabled(String retentionOutEnabled) {
        this.retentionOutEnabled = retentionOutEnabled;
    }

    public String getRetentionOutMode() {
        return retentionOutMode;
    }

    public void setRetentionOutMode(String retentionOutMode) {
        this.retentionOutMode = retentionOutMode;
    }

    public Integer getRetentionOutDays() {
        return retentionOutDays;
    }

    public void setRetentionOutDays(Integer retentionOutDays) {
        this.retentionOutDays = retentionOutDays;
    }

    public LocalDate getRetentionOutReleaseDate() {
        return retentionOutReleaseDate;
    }

    public void setRetentionOutReleaseDate(LocalDate retentionOutReleaseDate) {
        this.retentionOutReleaseDate = retentionOutReleaseDate;
    }

    public String getRetentionOutStoragePath() {
        return retentionOutStoragePath;
    }

    public void setRetentionOutStoragePath(String retentionOutStoragePath) {
        this.retentionOutStoragePath = retentionOutStoragePath;
    }

    public String getShapingEnabled() {
        return shapingEnabled;
    }

    public void setShapingEnabled(String shapingEnabled) {
        this.shapingEnabled = shapingEnabled;
    }

    public Integer getShapingMaxTrxPerMin() {
        return shapingMaxTrxPerMin;
    }

    public void setShapingMaxTrxPerMin(Integer shapingMaxTrxPerMin) {
        this.shapingMaxTrxPerMin = shapingMaxTrxPerMin;
    }

    public String getShapingStrategy() {
        return shapingStrategy;
    }

    public void setShapingStrategy(String shapingStrategy) {
        this.shapingStrategy = shapingStrategy;
    }

    public String getPiiLevel() {
        return piiLevel;
    }

    public void setPiiLevel(String piiLevel) {
        this.piiLevel = piiLevel;
    }

    public String getEncryptionRequired() {
        return encryptionRequired;
    }

    public void setEncryptionRequired(String encryptionRequired) {
        this.encryptionRequired = encryptionRequired;
    }

    public String getDrStrategy() {
        return drStrategy;
    }

    public void setDrStrategy(String drStrategy) {
        this.drStrategy = drStrategy;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "FlowReferenceDto{" + "flowId='" + flowId + '\'' + ", flowCode='" + flowCode + '\''
                + ", flowName='" + flowName + '\'' + ", status='" + status + '\''
                + ", sourceChannel='" + sourceChannel + '\'' + ", targetSystem='" + targetSystem
                + '\'' + '}';
    }
}
