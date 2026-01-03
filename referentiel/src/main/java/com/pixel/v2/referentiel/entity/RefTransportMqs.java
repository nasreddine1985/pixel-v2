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
 * JPA Entity for ref_transport_mqs table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_TRANSPORT_MQS", schema = "TIB_AUDIT_TEC")
public class RefTransportMqs {

    @Id
    @Column(name = "TRANSPORT_ID")
    private Integer transportId;

    @Column(name = "MQS_Q_NAME")
    private String mqsQName;

    @Column(name = "MQS_Q_MANAGER")
    private String mqsQManager;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Many-to-one relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSPORT_ID", insertable = false, updatable = false)
    private RefTransport transport;

    // Default constructor
    public RefTransportMqs() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefTransportMqs(Integer transportId, String mqsQName, String mqsQManager) {
        this.transportId = transportId;
        this.mqsQName = mqsQName;
        this.mqsQManager = mqsQManager;
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

    public String getMqsQName() {
        return mqsQName;
    }

    public void setMqsQName(String mqsQName) {
        this.mqsQName = mqsQName;
    }

    public String getMqsQManager() {
        return mqsQManager;
    }

    public void setMqsQManager(String mqsQManager) {
        this.mqsQManager = mqsQManager;
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
        return "RefTransportMqs{" + "transportId=" + transportId + ", mqsQName='" + mqsQName + '\''
                + ", mqsQManager='" + mqsQManager + '\'' + ", creationDte=" + creationDte
                + ", updateDte=" + updateDte + '}';
    }
}
