package com.pixel.v2.referential.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_transport table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_TRANSPORT", schema = "TIB_AUDIT_TEC")
public class RefTransport {

    @Id
    @Column(name = "TRANSPORT_ID")
    private Integer transportId;

    @Column(name = "TRANSPORT_TYPE")
    private String transportType;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefTransport() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefTransport(Integer transportId, String transportType) {
        this.transportId = transportId;
        this.transportType = transportType;
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

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
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
        return "RefTransport{" + "transportId=" + transportId + ", transportType='" + transportType
                + '\'' + ", creationDte=" + creationDte + ", updateDte=" + updateDte + '}';
    }
}
