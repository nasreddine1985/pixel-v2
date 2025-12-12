package com.pixel.v2.referentiel.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model representing the TIB_AUDIT_TEC.REF_FLOW_PARTNER table Contains partner configuration for
 * flows
 */
public class RefFlowPartner {

    @JsonProperty("partnerId")
    private Long partnerId;

    @JsonProperty("flowId")
    private Long flowId;

    @JsonProperty("transportId")
    private Long transportId;

    @JsonProperty("partnerDirection")
    private String partnerDirection;

    @JsonProperty("creationDate")
    private LocalDateTime creationDate;

    @JsonProperty("updateDate")
    private LocalDateTime updateDate;

    @JsonProperty("ruleId")
    private Long ruleId;

    @JsonProperty("charsetEncodingId")
    private Long charsetEncodingId;

    @JsonProperty("enableOut")
    private String enableOut;

    @JsonProperty("enableBmsa")
    private String enableBmsa;

    // Default constructor
    public RefFlowPartner() {}

    // Constructor with required fields
    public RefFlowPartner(Long partnerId, Long flowId, Long transportId, String partnerDirection) {
        this.partnerId = partnerId;
        this.flowId = flowId;
        this.transportId = transportId;
        this.partnerDirection = partnerDirection;
    }

    // Getters and Setters
    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public String getPartnerDirection() {
        return partnerDirection;
    }

    public void setPartnerDirection(String partnerDirection) {
        this.partnerDirection = partnerDirection;
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

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getCharsetEncodingId() {
        return charsetEncodingId;
    }

    public void setCharsetEncodingId(Long charsetEncodingId) {
        this.charsetEncodingId = charsetEncodingId;
    }

    public String getEnableOut() {
        return enableOut;
    }

    public void setEnableOut(String enableOut) {
        this.enableOut = enableOut;
    }

    public String getEnableBmsa() {
        return enableBmsa;
    }

    public void setEnableBmsa(String enableBmsa) {
        this.enableBmsa = enableBmsa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RefFlowPartner that = (RefFlowPartner) o;
        return Objects.equals(partnerId, that.partnerId) && Objects.equals(flowId, that.flowId)
                && Objects.equals(transportId, that.transportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partnerId, flowId, transportId);
    }

    @Override
    public String toString() {
        return "RefFlowPartner{" + "partnerId=" + partnerId + ", flowId=" + flowId
                + ", transportId=" + transportId + ", partnerDirection='" + partnerDirection + '\''
                + '}';
    }
}
