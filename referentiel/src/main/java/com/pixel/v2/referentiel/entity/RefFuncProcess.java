package com.pixel.v2.referentiel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_func_process table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_FUNC_PROCESS", schema = "TIB_AUDIT_TEC")
public class RefFuncProcess {

    @Id
    @Column(name = "FUNC_PROCESS_ID")
    private Integer funcProcessId;

    @Column(name = "FUNC_PROCESS_NAME")
    private String funcProcessName;

    @Column(name = "FUNC_PROCESS_TYPE")
    private String funcProcessType;

    @Column(name = "FUNC_PROCESS_DESCRIPTION")
    private String funcProcessDescription;

    @Column(name = "ENABLE_FLG")
    private String enableFlg;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefFuncProcess() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefFuncProcess(Integer funcProcessId, String funcProcessName, String funcProcessType) {
        this.funcProcessId = funcProcessId;
        this.funcProcessName = funcProcessName;
        this.funcProcessType = funcProcessType;
        this.enableFlg = "Y";
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getFuncProcessId() {
        return funcProcessId;
    }

    public void setFuncProcessId(Integer funcProcessId) {
        this.funcProcessId = funcProcessId;
    }

    public String getFuncProcessName() {
        return funcProcessName;
    }

    public void setFuncProcessName(String funcProcessName) {
        this.funcProcessName = funcProcessName;
    }

    public String getFuncProcessType() {
        return funcProcessType;
    }

    public void setFuncProcessType(String funcProcessType) {
        this.funcProcessType = funcProcessType;
    }

    public String getFuncProcessDescription() {
        return funcProcessDescription;
    }

    public void setFuncProcessDescription(String funcProcessDescription) {
        this.funcProcessDescription = funcProcessDescription;
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
        return "RefFuncProcess{" + "funcProcessId=" + funcProcessId + ", funcProcessName='"
                + funcProcessName + '\'' + ", funcProcessType='" + funcProcessType + '\''
                + ", funcProcessDescription='" + funcProcessDescription + '\'' + ", enableFlg='"
                + enableFlg + '\'' + ", creationDte=" + creationDte + ", updateDte=" + updateDte
                + '}';
    }
}
