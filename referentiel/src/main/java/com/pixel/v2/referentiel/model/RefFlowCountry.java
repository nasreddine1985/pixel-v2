package com.pixel.v2.referentiel.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model representing the TIB_AUDIT_TEC.REF_FLOW_COUNTRY table Junction table linking flows to
 * countries
 */
public class RefFlowCountry {

    @JsonProperty("flowId")
    private Long flowId;

    @JsonProperty("countryId")
    private Long countryId;

    // Default constructor
    public RefFlowCountry() {}

    // Constructor with all fields
    public RefFlowCountry(Long flowId, Long countryId) {
        this.flowId = flowId;
        this.countryId = countryId;
    }

    // Getters and Setters
    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RefFlowCountry that = (RefFlowCountry) o;
        return Objects.equals(flowId, that.flowId) && Objects.equals(countryId, that.countryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowId, countryId);
    }

    @Override
    public String toString() {
        return "RefFlowCountry{" + "flowId=" + flowId + ", countryId=" + countryId + '}';
    }
}
