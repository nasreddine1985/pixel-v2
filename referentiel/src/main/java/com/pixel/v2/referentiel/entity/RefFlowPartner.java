package com.pixel.v2.referentiel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for ref_flow_partner table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "ref_flow_partner", schema = "tib_audit_tec")
public class RefFlowPartner {

    @Id
    @Column(name = "partner_id")
    private Integer partnerId;

    @Column(name = "flow_id")
    private Integer flowId;

    @Column(name = "transport_id")
    private Integer transportId;

    @Column(name = "partner_direction")
    private String partnerDirection;

    @Column(name = "creation_dte")
    private LocalDateTime creationDte;

    @Column(name = "update_dte")
    private LocalDateTime updateDte;

    @Column(name = "rule_id")
    private Integer ruleId;

    @Column(name = "charset_encoding_id")
    private Integer charsetEncodingId;

    @Column(name = "enable_out")
    private String enableOut;

    @Column(name = "enable_bmsa")
    private String enableBmsa;

    // Many-to-one relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", insertable = false, updatable = false)
    private RefFlow refFlow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charset_encoding_id", insertable = false, updatable = false)
    private RefCharsetEncoding charsetEncoding;

    // Default constructor
    public RefFlowPartner() {
        // Required for JPA
    }

    // Getters and Setters
    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Integer getFlowId() {
        return flowId;
    }

    public void setFlowId(Integer flowId) {
        this.flowId = flowId;
    }

    public Integer getTransportId() {
        return transportId;
    }

    public void setTransportId(Integer transportId) {
        this.transportId = transportId;
    }

    public String getPartnerDirection() {
        return partnerDirection;
    }

    public void setPartnerDirection(String partnerDirection) {
        this.partnerDirection = partnerDirection;
    }

    public LocalDateTime getCreationDte() {
        return creationDte;
    }

    public void setCreationDte(LocalDateTime creationDte) {
        this.creationDte = creationDte;
    }

    public LocalDateTime getUpdateDte() {
        return updateDte;
    }

    public void setUpdateDte(LocalDateTime updateDte) {
        this.updateDte = updateDte;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getCharsetEncodingId() {
        return charsetEncodingId;
    }

    public void setCharsetEncodingId(Integer charsetEncodingId) {
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

    public RefFlow getRefFlow() {
        return refFlow;
    }

    public void setRefFlow(RefFlow refFlow) {
        this.refFlow = refFlow;
    }

    public RefCharsetEncoding getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(RefCharsetEncoding charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }
}
