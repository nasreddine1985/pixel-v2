package com.pixel.v2.referential.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_transport_cft table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_TRANSPORT_CFT", schema = "TIB_AUDIT_TEC")
public class RefTransportCft {

    @Id
    @Column(name = "TRANSPORT_ID")
    private Integer transportId;

    @Column(name = "CFT_IDF")
    private String cftIdf;

    @Column(name = "CFT_PARTNER_CODE")
    private String cftPartnerCode;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Many-to-one relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSPORT_ID", insertable = false, updatable = false)
    private RefTransport transport;

    // Default constructor
    public RefTransportCft() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefTransportCft(Integer transportId, String cftIdf, String cftPartnerCode) {
        this.transportId = transportId;
        this.cftIdf = cftIdf;
        this.cftPartnerCode = cftPartnerCode;
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getTransportId() {
        return transportId;
    }

    public void setTransportId(Integer transportId) {
        this.transportId = transportId;
    }

    public String getCftIdf() {
        return cftIdf;
    }

    public void setCftIdf(String cftIdf) {
        this.cftIdf = cftIdf;
    }

    public String getCftPartnerCode() {
        return cftPartnerCode;
    }

    public void setCftPartnerCode(String cftPartnerCode) {
        this.cftPartnerCode = cftPartnerCode;
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

    public RefTransport getTransport() {
        return transport;
    }

    public void setTransport(RefTransport transport) {
        this.transport = transport;
    }

    @Override
    public String toString() {
        return "RefTransportCft{" + "transportId=" + transportId + ", cftIdf='" + cftIdf + '\''
                + ", cftPartnerCode='" + cftPartnerCode + '\'' + ", creationDte=" + creationDte
                + ", updateDte=" + updateDte + '}';
    }
}
