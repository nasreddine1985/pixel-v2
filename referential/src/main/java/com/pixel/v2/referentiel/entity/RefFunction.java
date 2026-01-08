package com.pixel.v2.referential.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_function table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_FUNCTION", schema = "TIB_AUDIT_TEC")
public class RefFunction {

    @Id
    @Column(name = "FUNCTION_ID")
    private Integer functionId;

    @Column(name = "FUNCTION_NAME")
    private String functionName;

    @Column(name = "FUNCTION_TYPE")
    private String functionType;

    @Column(name = "FUNCTION_DESCRIPTION")
    private String functionDescription;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Default constructor
    public RefFunction() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefFunction(Integer functionId, String functionName, String functionType) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.functionType = functionType;
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Integer functionId) {
        this.functionId = functionId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    public String getFunctionDescription() {
        return functionDescription;
    }

    public void setFunctionDescription(String functionDescription) {
        this.functionDescription = functionDescription;
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
        return "RefFunction{" + "functionId=" + functionId + ", functionName='" + functionName
                + '\'' + ", functionType='" + functionType + '\'' + ", functionDescription='"
                + functionDescription + '\'' + ", creationDte=" + creationDte + ", updateDte="
                + updateDte + '}';
    }
}
