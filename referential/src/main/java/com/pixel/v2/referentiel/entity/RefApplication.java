package com.pixel.v2.referential.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_application table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_APPLICATION", schema = "TIB_AUDIT_TEC")
public class RefApplication {

    @Id
    @Column(name = "APPLICATION_ID")
    private Integer applicationId;

    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    @Column(name = "APPLICATION_DESCRIPTION")
    private String applicationDescription;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefApplication() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefApplication(Integer applicationId, String applicationName,
            String applicationDescription) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.applicationDescription = applicationDescription;
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
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
        return "RefApplication{" + "applicationId=" + applicationId + ", applicationName='"
                + applicationName + '\'' + ", applicationDescription='" + applicationDescription
                + '\'' + ", creationDte=" + creationDte + ", updateDte=" + updateDte + '}';
    }
}
