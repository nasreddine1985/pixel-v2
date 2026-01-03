package com.pixel.v2.referentiel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_partner_typ table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_PARTNER_TYP", schema = "TIB_AUDIT_TEC")
public class RefPartnerTyp {

    @Id
    @Column(name = "PARTNER_TYP_ID")
    private Integer partnerTypId;

    @Column(name = "PARTNER_TYP_NAME")
    private String partnerTypName;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefPartnerTyp() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefPartnerTyp(Integer partnerTypId, String partnerTypName) {
        this.partnerTypId = partnerTypId;
        this.partnerTypName = partnerTypName;
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getPartnerTypId() {
        return partnerTypId;
    }

    public void setPartnerTypId(Integer partnerTypId) {
        this.partnerTypId = partnerTypId;
    }

    public String getPartnerTypName() {
        return partnerTypName;
    }

    public void setPartnerTypName(String partnerTypName) {
        this.partnerTypName = partnerTypName;
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

    @Override
    public String toString() {
        return "RefPartnerTyp{" + "partnerTypId=" + partnerTypId + ", partnerTypName='"
                + partnerTypName + '\'' + ", creationDte=" + creationDte + ", updateDte="
                + updateDte + '}';
    }
}
