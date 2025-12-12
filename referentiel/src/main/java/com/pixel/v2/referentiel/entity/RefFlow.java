package com.pixel.v2.referentiel.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_flow table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "ref_flow", schema = "tib_audit_tec")
public class RefFlow {

    @Id
    @Column(name = "flow_id")
    private Integer flowId;

    @Column(name = "func_process_id")
    private Integer funcProcessId;

    @Column(name = "flow_typ_id")
    private Integer flowTypId;

    @Column(name = "tech_process_id")
    private Integer techProcessId;

    @Column(name = "flow_name")
    private String flowName;

    @Column(name = "flow_direction")
    private String flowDirection;

    @Column(name = "flow_code")
    private String flowCode;

    @Column(name = "enable_flg")
    private String enableFlg;

    @Column(name = "creation_dte")
    private LocalDateTime creationDte;

    @Column(name = "update_dte")
    private LocalDateTime updateDte;

    @Column(name = "application_id")
    private Integer applicationId;

    @Column(name = "max_file_size")
    private Long maxFileSize;

    // One-to-many relationships
    @OneToMany(mappedBy = "refFlow", fetch = FetchType.LAZY)
    private List<RefFlowCountry> countries;

    @OneToMany(mappedBy = "refFlow", fetch = FetchType.LAZY)
    private List<RefFlowPartner> partners;

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
}
