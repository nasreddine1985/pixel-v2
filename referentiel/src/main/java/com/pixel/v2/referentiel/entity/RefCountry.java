package com.pixel.v2.referentiel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_country table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_COUNTRY", schema = "TIB_AUDIT_TEC")
public class RefCountry {

    @Id
    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "ISO_2_CODE")
    private String iso2Code;

    @Column(name = "ISO_3_CODE")
    private String iso3Code;

    @Column(name = "ENABLE_FLG")
    private String enableFlg;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefCountry() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefCountry(Integer countryId, String countryName, String iso2Code, String iso3Code) {
        this.countryId = countryId;
        this.countryName = countryName;
        this.iso2Code = iso2Code;
        this.iso3Code = iso3Code;
        this.enableFlg = "Y";
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getIso2Code() {
        return iso2Code;
    }

    public void setIso2Code(String iso2Code) {
        this.iso2Code = iso2Code;
    }

    public String getIso3Code() {
        return iso3Code;
    }

    public void setIso3Code(String iso3Code) {
        this.iso3Code = iso3Code;
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

    @Override
    public String toString() {
        return "RefCountry{" + "countryId=" + countryId + ", countryName='" + countryName + '\''
                + ", iso2Code='" + iso2Code + '\'' + ", iso3Code='" + iso3Code + '\''
                + ", enableFlg='" + enableFlg + '\'' + ", creationDte=" + creationDte
                + ", updateDte=" + updateDte + '}';
    }
}
