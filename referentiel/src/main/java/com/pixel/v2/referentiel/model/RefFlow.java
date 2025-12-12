package com.pixel.v2.referentiel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing the TIB_AUDIT_TEC.REF_FLOW table
 * Contains flow configuration and metadata for payment processing flows
 */
public class RefFlow {

    @JsonProperty("flowId")
    private Long flowId;

    @JsonProperty("funcProcessId")
    private Long funcProcessId;

    @JsonProperty("flowTypId")
    private Long flowTypId;

    @JsonProperty("techProcessId")
    private Long techProcessId;

    @JsonProperty("flowName")
    private String flowName;

    @JsonProperty("flowDirection")
    private String flowDirection;

    @JsonProperty("flowCode")
    private String flowCode;

    @JsonProperty("enableFlag")
    private String enableFlag;

    @JsonProperty("creationDate")
    private LocalDateTime creationDate;

    @JsonProperty("updateDate")
    private LocalDateTime updateDate;

    @JsonProperty("applicationId")
    private Long applicationId;

    @JsonProperty("maxFileSize")
    private Long maxFileSize;

    // Default constructor
    public RefFlow() {}

    // Constructor with required fields
    public RefFlow(Long flowId, String flowName, String flowDirection, String flowCode) {
        this.flowId = flowId;
        this.flowName = flowName;
        this.flowDirection = flowDirection;
        this.flowCode = flowCode;
    }

    // Getters and Setters
    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getFuncProcessId() {
        return funcProcessId;
    }

    public void setFuncProcessId(Long funcProcessId) {
        this.funcProcessId = funcProcessId;
    }

    public Long getFlowTypId() {
        return flowTypId;
    }

    public void setFlowTypId(Long flowTypId) {
        this.flowTypId = flowTypId;
    }

    public Long getTechProcessId() {
        return techProcessId;
    }

    public void setTechProcessId(Long techProcessId) {
        this.techProcessId = techProcessId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowDirection() {
        return flowDirection;
    }

    public void setFlowDirection(String flowDirection) {
        this.flowDirection = flowDirection;
    }

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefFlow refFlow = (RefFlow) o;
        return Objects.equals(flowId, refFlow.flowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowId);
    }

    @Override
    public String toString() {
        return "RefFlow{" +
                "flowId=" + flowId +
                ", flowName='" + flowName + '\'' +
                ", flowDirection='" + flowDirection + '\'' +
                ", flowCode='" + flowCode + '\'' +
                ", enableFlag='" + enableFlag + '\'' +
                '}';
    }
}