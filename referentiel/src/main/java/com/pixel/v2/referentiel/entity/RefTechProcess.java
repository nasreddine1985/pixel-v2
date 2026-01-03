package com.pixel.v2.referentiel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_tech_process table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_TECH_PROCESS", schema = "TIB_AUDIT_TEC")
public class RefTechProcess {

    @Id
    @Column(name = "TECH_PROCESS_ID")
    private Integer techProcessId;

    @Column(name = "TECH_PROCESS_NAME")
    private String techProcessName;

    @Column(name = "TECH_PROCESS_TYPE")
    private String techProcessType;

    @Column(name = "INPUT_TYPE")
    private String inputType;

    @Column(name = "OUTPUT_TYPE")
    private String outputType;

    @Column(name = "TECH_PROCESS_DESCRIPTION")
    private String techProcessDescription;

    @Column(name = "ENABLE_FLG")
    private String enableFlg;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefTechProcess() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefTechProcess(Integer techProcessId, String techProcessName, String techProcessType) {
        this.techProcessId = techProcessId;
        this.techProcessName = techProcessName;
        this.techProcessType = techProcessType;
        this.enableFlg = "Y";
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getTechProcessId() {
        return techProcessId;
    }

    public void setTechProcessId(Integer techProcessId) {
        this.techProcessId = techProcessId;
    }

    public String getTechProcessName() {
        return techProcessName;
    }

    public void setTechProcessName(String techProcessName) {
        this.techProcessName = techProcessName;
    }

    public String getTechProcessType() {
        return techProcessType;
    }

    public void setTechProcessType(String techProcessType) {
        this.techProcessType = techProcessType;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getTechProcessDescription() {
        return techProcessDescription;
    }

    public void setTechProcessDescription(String techProcessDescription) {
        this.techProcessDescription = techProcessDescription;
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
        return "RefTechProcess{" + "techProcessId=" + techProcessId + ", techProcessName='"
                + techProcessName + '\'' + ", techProcessType='" + techProcessType + '\''
                + ", inputType='" + inputType + '\'' + ", outputType='" + outputType + '\''
                + ", techProcessDescription='" + techProcessDescription + '\'' + ", enableFlg='"
                + enableFlg + '\'' + ", creationDte=" + creationDte + ", updateDte=" + updateDte
                + '}';
    }
}
