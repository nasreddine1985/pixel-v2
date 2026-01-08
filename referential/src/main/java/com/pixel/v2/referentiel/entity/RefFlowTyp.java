package com.pixel.v2.referential.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_flow_typ table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_FLOW_TYP", schema = "TIB_AUDIT_TEC")
public class RefFlowTyp {

    @Id
    @Column(name = "FLOW_TYP_ID")
    private Integer flowTypId;

    @Column(name = "FLOW_TYP_NAME")
    private String flowTypName;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefFlowTyp() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefFlowTyp(Integer flowTypId, String flowTypName) {
        this.flowTypId = flowTypId;
        this.flowTypName = flowTypName;
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getFlowTypId() {
        return flowTypId;
    }

    public void setFlowTypId(Integer flowTypId) {
        this.flowTypId = flowTypId;
    }

    public String getFlowTypName() {
        return flowTypName;
    }

    public void setFlowTypName(String flowTypName) {
        this.flowTypName = flowTypName;
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
        return "RefFlowTyp{" + "flowTypId=" + flowTypId + ", flowTypName='" + flowTypName + '\''
                + ", creationDte=" + creationDte + ", updateDte=" + updateDte + '}';
    }
}
