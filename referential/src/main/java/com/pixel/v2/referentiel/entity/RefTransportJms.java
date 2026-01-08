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
 * JPA Entity for ref_transport_jms table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_TRANSPORT_JMS", schema = "TIB_AUDIT_TEC")
public class RefTransportJms {

    @Id
    @Column(name = "TRANSPORT_ID")
    private Integer transportId;

    @Column(name = "JMS_Q_NAME")
    private String jmsQName;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Many-to-one relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSPORT_ID", insertable = false, updatable = false)
    private RefTransport transport;

    // Default constructor
    public RefTransportJms() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefTransportJms(Integer transportId, String jmsQName) {
        this.transportId = transportId;
        this.jmsQName = jmsQName;
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

    public String getJmsQName() {
        return jmsQName;
    }

    public void setJmsQName(String jmsQName) {
        this.jmsQName = jmsQName;
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
        return "RefTransportJms{" + "transportId=" + transportId + ", jmsQName='" + jmsQName + '\''
                + ", creationDte=" + creationDte + ", updateDte=" + updateDte + '}';
    }
}
