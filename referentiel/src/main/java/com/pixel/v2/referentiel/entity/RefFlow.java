package com.pixel.v2.referentiel.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_flow table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_FLOW", schema = "TIB_AUDIT_TEC")
public class RefFlow {

    @Id
    @Column(name = "FLOW_ID")
    private Integer flowId;

    @Column(name = "FUNC_PROCESS_ID")
    private Integer funcProcessId;

    @Column(name = "FLOW_TYP_ID")
    private Integer flowTypId;

    @Column(name = "TECH_PROCESS_ID")
    private Integer techProcessId;

    @Column(name = "FLOW_NAME")
    private String flowName;

    @Column(name = "FLOW_DIRECTION")
    private String flowDirection;

    @Column(name = "FLOW_CODE")
    private String flowCode;

    @Column(name = "ENABLE_FLG")
    private String enableFlg;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    @Column(name = "APPLICATION_ID")
    private Integer applicationId;

    @Column(name = "MAX_FILE_SIZE")
    private Long maxFileSize;

    // One-to-many relationships
    @OneToMany(mappedBy = "refFlow", fetch = FetchType.LAZY)
    private List<RefFlowCountry> countries;

    @OneToMany(mappedBy = "refFlow", fetch = FetchType.LAZY)
    private List<RefFlowPartner> partners;

    // Many-to-one relationships with reference tables
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FUNC_PROCESS_ID", insertable = false, updatable = false)
    private RefFuncProcess funcProcess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FLOW_TYP_ID", insertable = false, updatable = false)
    private RefFlowTyp flowTyp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TECH_PROCESS_ID", insertable = false, updatable = false)
    private RefTechProcess techProcess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", insertable = false, updatable = false)
    private RefApplication application;

    // Default constructor
    public RefFlow() {
        // Required for JPA
    }

    // Getters and Setters
    public Integer getFlowId() {
        return flowId;
    }

    public void setFlowId(Integer flowId) {
        this.flowId = flowId;
    }

    public Integer getFuncProcessId() {
        return funcProcessId;
    }

    public void setFuncProcessId(Integer funcProcessId) {
        this.funcProcessId = funcProcessId;
    }

    public Integer getFlowTypId() {
        return flowTypId;
    }

    public void setFlowTypId(Integer flowTypId) {
        this.flowTypId = flowTypId;
    }

    public Integer getTechProcessId() {
        return techProcessId;
    }

    public void setTechProcessId(Integer techProcessId) {
        this.techProcessId = techProcessId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowDirection() {
        return flowDirection;
    }

    public void setFlowDirection(String flowDirection) {
        this.flowDirection = flowDirection;
    }

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
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

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<RefFlowCountry> getCountries() {
        return countries;
    }

    public void setCountries(List<RefFlowCountry> countries) {
        this.countries = countries;
    }

    public List<RefFlowPartner> getPartners() {
        return partners;
    }

    public void setPartners(List<RefFlowPartner> partners) {
        this.partners = partners;
    }

    public RefFuncProcess getFuncProcess() {
        return funcProcess;
    }

    public void setFuncProcess(RefFuncProcess funcProcess) {
        this.funcProcess = funcProcess;
    }

    public RefFlowTyp getFlowTyp() {
        return flowTyp;
    }

    public void setFlowTyp(RefFlowTyp flowTyp) {
        this.flowTyp = flowTyp;
    }

    public RefTechProcess getTechProcess() {
        return techProcess;
    }

    public void setTechProcess(RefTechProcess techProcess) {
        this.techProcess = techProcess;
    }

    public RefApplication getApplication() {
        return application;
    }

    public void setApplication(RefApplication application) {
        this.application = application;
    }
}
