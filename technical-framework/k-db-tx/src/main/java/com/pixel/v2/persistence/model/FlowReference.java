package com.pixel.v2.persistence.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing FLOW_REFERENCE table Contains flow configuration and metadata
 */
@Entity
@Table(name = "FLOW_REFERENCE",
        indexes = {@Index(name = "idx_flow_code", columnList = "flowCode"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_source_channel", columnList = "sourceChannel"),
                @Index(name = "idx_target_system", columnList = "targetSystem")})
public class FlowReference {

    @Id
    @Column(name = "FLOW_ID", length = 36)
    private String flowId;

    @Column(name = "FLOW_CODE", length = 50, nullable = false)
    private String flowCode;

    @Column(name = "FLOW_NAME", length = 100, nullable = false)
    private String flowName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10)
    private FlowStatus status;

    @Column(name = "SOURCE_CHANNEL", length = 20)
    private String sourceChannel;

    @Column(name = "SOURCE_SYSTEM", length = 50)
    private String sourceSystem;

    @Column(name = "SOURCE_FORMAT", length = 20)
    private String sourceFormat;

    @Column(name = "FLOW_TYPE", length = 20)
    private String flowType;

    @Column(name = "RAIL_MODE", length = 10)
    private String railMode;

    @Column(name = "SLA_MAX_LATENCY_MS")
    private Integer slaMaxLatencyMs;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "SPLIT_ENABLED", length = 5)
    private BooleanFlag splitEnabled;

    @Column(name = "SPLIT_CHUNK_SIZE")
    private Integer splitChunkSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "CONCAT_ENABLED", length = 5)
    private BooleanFlag concatEnabled;

    @Column(name = "CONCAT_CRITERIA", length = 20)
    private String concatCriteria;

    @Column(name = "TARGET_SYSTEM", length = 20)
    private String targetSystem;

    @Column(name = "TARGET_CHANNEL", length = 20)
    private String targetChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "RETENTION_IN_ENABLED", length = 5)
    private BooleanFlag retentionInEnabled;

    @Column(name = "RETENTION_IN_MODE", length = 10)
    private String retentionInMode;

    @Column(name = "RETENTION_IN_DAYS")
    private Integer retentionInDays;

    @Column(name = "RETENTION_IN_RELEASE_DATE")
    private LocalDate retentionInReleaseDate;

    @Column(name = "RETENTION_IN_STORAGE_PATH", length = 100)
    private String retentionInStoragePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "RETENTION_OUT_ENABLED", length = 5)
    private BooleanFlag retentionOutEnabled;

    @Column(name = "RETENTION_OUT_MODE", length = 10)
    private String retentionOutMode;

    @Column(name = "RETENTION_OUT_DAYS")
    private Integer retentionOutDays;

    @Column(name = "RETENTION_OUT_RELEASE_DATE")
    private LocalDate retentionOutReleaseDate;

    @Column(name = "RETENTION_OUT_STORAGE_PATH", length = 100)
    private String retentionOutStoragePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "SHAPING_ENABLED", length = 5)
    private BooleanFlag shapingEnabled;

    @Column(name = "SHAPING_MAX_TRX_PER_MIN")
    private Integer shapingMaxTrxPerMin;

    @Column(name = "SHAPING_STRATEGY", length = 10)
    private String shapingStrategy;

    @Column(name = "PII_LEVEL", length = 10)
    private String piiLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "ENCRYPTION_REQUIRED", length = 5)
    private BooleanFlag encryptionRequired;

    @Column(name = "DR_STRATEGY", length = 20)
    private String drStrategy;

    @Column(name = "LAST_UPDATE", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdate;

    @Column(name = "VERSION", length = 10)
    private String version;

    @Column(name = "COMMENTS", length = 200)
    private String comments;

    // Enums
    public enum FlowStatus {
        ACTIVE, DISABLED
    }

    public enum BooleanFlag {
        TRUE, FALSE
    }

    // Default constructor
    public FlowReference() {
        this.lastUpdate = LocalDateTime.now();
    }

    // Constructor with required fields
    public FlowReference(String flowId, String flowCode, String flowName) {
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

    public FlowStatus getStatus() {
        return status;
    }

    public void setStatus(FlowStatus status) {
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

    public BooleanFlag getSplitEnabled() {
        return splitEnabled;
    }

    public void setSplitEnabled(BooleanFlag splitEnabled) {
        this.splitEnabled = splitEnabled;
    }

    public Integer getSplitChunkSize() {
        return splitChunkSize;
    }

    public void setSplitChunkSize(Integer splitChunkSize) {
        this.splitChunkSize = splitChunkSize;
    }

    public BooleanFlag getConcatEnabled() {
        return concatEnabled;
    }

    public void setConcatEnabled(BooleanFlag concatEnabled) {
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

    public BooleanFlag getRetentionInEnabled() {
        return retentionInEnabled;
    }

    public void setRetentionInEnabled(BooleanFlag retentionInEnabled) {
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

    public BooleanFlag getRetentionOutEnabled() {
        return retentionOutEnabled;
    }

    public void setRetentionOutEnabled(BooleanFlag retentionOutEnabled) {
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

    public BooleanFlag getShapingEnabled() {
        return shapingEnabled;
    }

    public void setShapingEnabled(BooleanFlag shapingEnabled) {
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

    public BooleanFlag getEncryptionRequired() {
        return encryptionRequired;
    }

    public void setEncryptionRequired(BooleanFlag encryptionRequired) {
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
        return "FlowReference{" + "flowId='" + flowId + '\'' + ", flowCode='" + flowCode + '\''
                + ", flowName='" + flowName + '\'' + ", status=" + status + ", sourceChannel='"
                + sourceChannel + '\'' + ", targetSystem='" + targetSystem + '\'' + '}';
    }
}
