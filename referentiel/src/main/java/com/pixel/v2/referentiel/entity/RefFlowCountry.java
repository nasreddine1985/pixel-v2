package com.pixel.v2.referentiel.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_flow_country table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_FLOW_COUNTRY", schema = "TIB_AUDIT_TEC")
@IdClass(RefFlowCountry.RefFlowCountryId.class)
public class RefFlowCountry {

    @Id
    @Column(name = "FLOW_ID")
    private Integer flowId;

    @Id
    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    // Many-to-one relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FLOW_ID", insertable = false, updatable = false)
    private RefFlow refFlow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRY_ID", insertable = false, updatable = false)
    private RefCountry country;

    // Default constructor
    public RefFlowCountry() {
        // Required for JPA
    }

    /**
     * Composite key class for RefFlowCountry
     */
    public static class RefFlowCountryId implements Serializable {
        private Integer flowId;
        private Integer countryId;

        public RefFlowCountryId() {
            // Default constructor for JPA
        }

        public RefFlowCountryId(Integer flowId, Integer countryId) {
            this.flowId = flowId;
            this.countryId = countryId;
        }

        // Getters and setters
        public Integer getFlowId() {
            return flowId;
        }

        public void setFlowId(Integer flowId) {
            this.flowId = flowId;
        }

        public Integer getCountryId() {
            return countryId;
        }

        public void setCountryId(Integer countryId) {
            this.countryId = countryId;
        }

        // equals and hashCode required for composite keys
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            RefFlowCountryId that = (RefFlowCountryId) obj;
            return flowId.equals(that.flowId) && countryId.equals(that.countryId);
        }

        @Override
        public int hashCode() {
            return flowId.hashCode() + countryId.hashCode();
        }
    }

    // Getters and Setters

    public Integer getFlowId() {
        return flowId;
    }

    public void setFlowId(Integer flowId) {
        this.flowId = flowId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public RefFlow getRefFlow() {
        return refFlow;
    }

    public void setRefFlow(RefFlow refFlow) {
        this.refFlow = refFlow;
    }

    public RefCountry getCountry() {
        return country;
    }

    public void setCountry(RefCountry country) {
        this.country = country;
    }
}
