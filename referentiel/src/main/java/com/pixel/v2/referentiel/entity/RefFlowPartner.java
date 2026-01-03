package com.pixel.v2.referentiel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_flow_partner table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_FLOW_PARTNER", schema = "TIB_AUDIT_TEC")
public class RefFlowPartner {

    @Id
    @Column(name = "PARTNER_ID")
    private Integer partnerId;

    @Column(name = "FLOW_ID")
    private Integer flowId;

    @Column(name = "TRANSPORT_ID")
    private Integer transportId;

    @Column(name = "PARTNER_DIRECTION")
    private String partnerDirection;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    @Column(name = "RULE_ID")
    private Integer ruleId;

    @Column(name = "CHARSET_ENCODING_ID")
    private Integer charsetEncodingId;

    @Column(name = "ENABLE_OUT")
    private String enableOut;

    @Column(name = "ENABLE_BMSA")
    private String enableBmsa;

    // Many-to-one relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FLOW_ID", insertable = false, updatable = false)
    private RefFlow refFlow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHARSET_ENCODING_ID", insertable = false, updatable = false)
    private RefCharsetEncoding charsetEncoding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSPORT_ID", insertable = false, updatable = false)
    private RefTransport transport;

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

    public RefTransport getTransport() {
        return transport;
    }

    public void setTransport(RefTransport transport) {
        this.transport = transport;
    }
}
