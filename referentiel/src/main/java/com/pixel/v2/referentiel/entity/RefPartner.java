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
 * JPA Entity for ref_partner table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_PARTNER", schema = "TIB_AUDIT_TEC")
public class RefPartner {

    @Id
    @Column(name = "PARTNER_ID")
    private Integer partnerId;

    @Column(name = "PARTNER_TYP_ID")
    private Integer partnerTypId;

    @Column(name = "PARTNER_NAME")
    private String partnerName;

    @Column(name = "PARTNER_CODE")
    private String partnerCode;

    @Column(name = "BIC")
    private String bic;

    @Column(name = "PARTNER_DESCRIPTION")
    private String partnerDescription;

    @Column(name = "ENABLE_FLG")
    private String enableFlg;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Many-to-one relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTNER_TYP_ID", insertable = false, updatable = false)
    private RefPartnerTyp partnerTyp;

    // Default constructor
    public RefPartner() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefPartner(Integer partnerId, String partnerName, String partnerCode,
            Integer partnerTypId) {
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.partnerCode = partnerCode;
        this.partnerTypId = partnerTypId;
        this.enableFlg = "Y";
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Integer getPartnerTypId() {
        return partnerTypId;
    }

    public void setPartnerTypId(Integer partnerTypId) {
        this.partnerTypId = partnerTypId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getPartnerDescription() {
        return partnerDescription;
    }

    public void setPartnerDescription(String partnerDescription) {
        this.partnerDescription = partnerDescription;
    }

    public String getEnableFlg() {
        return enableFlg;
    }

    public void setEnableFlg(String enableFlg) {
        this.enableFlg = enableFlg;
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

    public RefPartnerTyp getPartnerTyp() {
        return partnerTyp;
    }

    public void setPartnerTyp(RefPartnerTyp partnerTyp) {
        this.partnerTyp = partnerTyp;
    }

    @Override
    public String toString() {
        return "RefPartner{" + "partnerId=" + partnerId + ", partnerTypId=" + partnerTypId
                + ", partnerName='" + partnerName + '\'' + ", partnerCode='" + partnerCode + '\''
                + ", bic='" + bic + '\'' + ", partnerDescription='" + partnerDescription + '\''
                + ", enableFlg='" + enableFlg + '\'' + ", creationDte=" + creationDte
                + ", updateDte=" + updateDte + '}';
    }
}
