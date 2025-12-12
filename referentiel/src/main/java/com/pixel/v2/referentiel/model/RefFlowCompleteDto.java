package com.pixel.v2.referentiel.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Comprehensive DTO for RefFlow data with all related information Includes flow, country, partner,
 * rules, and charset encoding data
 */
public class RefFlowCompleteDto {

    // RefFlow main data
    @JsonProperty("flowId")
    private Integer flowId;

    @JsonProperty("funcProcessId")
    private Integer funcProcessId;

    @JsonProperty("flowTypId")
    private Integer flowTypId;

    @JsonProperty("techProcessId")
    private Integer techProcessId;

    @JsonProperty("flowName")
    private String flowName;

    @JsonProperty("flowDirection")
    private String flowDirection;

    @JsonProperty("flowCode")
    private String flowCode;

    @JsonProperty("enableFlg")
    private String enableFlg;

    @JsonProperty("creationDte")
    private LocalDateTime creationDte;

    @JsonProperty("updateDte")
    private LocalDateTime updateDte;

    @JsonProperty("applicationId")
    private Integer applicationId;

    @JsonProperty("maxFileSize")
    private Long maxFileSize;

    // Related country data
    @JsonProperty("countries")
    private List<RefFlowCountryDto> countries;

    // Related partner data
    @JsonProperty("partners")
    private List<RefFlowPartnerDto> partners;

    // Related rules data
    @JsonProperty("rules")
    private RefFlowRulesDto rules;

    // Related charset encoding data
    @JsonProperty("charsetEncodings")
    private List<RefCharsetEncodingDto> charsetEncodings;

    // Nested DTOs
    public static class RefFlowCountryDto {
        @JsonProperty("flowId")
        private Integer flowId;

        @JsonProperty("countryId")
        private Integer countryId;

        // Constructors, getters, setters
        public RefFlowCountryDto() {}

        public RefFlowCountryDto(Integer flowId, Integer countryId) {
            this.flowId = flowId;
            this.countryId = countryId;
        }

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
    }

    public static class RefFlowPartnerDto {
        @JsonProperty("partnerId")
        private Integer partnerId;

        @JsonProperty("flowId")
        private Integer flowId;

        @JsonProperty("transportId")
        private Integer transportId;

        @JsonProperty("partnerDirection")
        private String partnerDirection;

        @JsonProperty("creationDte")
        private LocalDateTime creationDte;

        @JsonProperty("updateDte")
        private LocalDateTime updateDte;

        @JsonProperty("ruleId")
        private Integer ruleId;

        @JsonProperty("charsetEncodingId")
        private Integer charsetEncodingId;

        @JsonProperty("enableOut")
        private String enableOut;

        @JsonProperty("enableBmsa")
        private String enableBmsa;

        // Constructors, getters, setters
        public RefFlowPartnerDto() {
            // Default constructor for JSON deserialization
        }

        // Getters and setters
        public Integer getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(Integer partnerId) {
            this.partnerId = partnerId;
        }

        public Integer getFlowId() {
            return flowId;
        }

        public void setFlowId(Integer flowId) {
            this.flowId = flowId;
        }

        public Integer getTransportId() {
            return transportId;
        }

        public void setTransportId(Integer transportId) {
            this.transportId = transportId;
        }

        public String getPartnerDirection() {
            return partnerDirection;
        }

        public void setPartnerDirection(String partnerDirection) {
            this.partnerDirection = partnerDirection;
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

        public Integer getRuleId() {
            return ruleId;
        }

        public void setRuleId(Integer ruleId) {
            this.ruleId = ruleId;
        }

        public Integer getCharsetEncodingId() {
            return charsetEncodingId;
        }

        public void setCharsetEncodingId(Integer charsetEncodingId) {
            this.charsetEncodingId = charsetEncodingId;
        }

        public String getEnableOut() {
            return enableOut;
        }

        public void setEnableOut(String enableOut) {
            this.enableOut = enableOut;
        }

        public String getEnableBmsa() {
            return enableBmsa;
        }

        public void setEnableBmsa(String enableBmsa) {
            this.enableBmsa = enableBmsa;
        }
    }

    public static class RefFlowRulesDto {
        @JsonProperty("flowCode")
        private String flowCode;

        @JsonProperty("transportType")
        private String transportType;

        @JsonProperty("isUnitary")
        private Boolean isUnitary;

        @JsonProperty("priority")
        private String priority;

        @JsonProperty("urgency")
        private String urgency;

        @JsonProperty("flowControlledEnabled")
        private Boolean flowControlledEnabled;

        @JsonProperty("flowMaximum")
        private Integer flowMaximum;

        @JsonProperty("flowRetentionEnabled")
        private Boolean flowRetentionEnabled;

        @JsonProperty("retentionCyclePeriod")
        private String retentionCyclePeriod;

        @JsonProperty("writeFile")
        private Boolean writeFile;

        @JsonProperty("minRequiredFileSize")
        private Integer minRequiredFileSize;

        @JsonProperty("ignoreOutputDupCheck")
        private Boolean ignoreOutputDupCheck;

        @JsonProperty("logAll")
        private Boolean logAll;

        // Constructors, getters, setters
        public RefFlowRulesDto() {
            // Default constructor for JSON deserialization
        }

        // Getters and setters
        public String getFlowCode() {
            return flowCode;
        }

        public void setFlowCode(String flowCode) {
            this.flowCode = flowCode;
        }

        public String getTransportType() {
            return transportType;
        }

        public void setTransportType(String transportType) {
            this.transportType = transportType;
        }

        public Boolean getIsUnitary() {
            return isUnitary;
        }

        public void setIsUnitary(Boolean isUnitary) {
            this.isUnitary = isUnitary;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getUrgency() {
            return urgency;
        }

        public void setUrgency(String urgency) {
            this.urgency = urgency;
        }

        public Boolean getFlowControlledEnabled() {
            return flowControlledEnabled;
        }

        public void setFlowControlledEnabled(Boolean flowControlledEnabled) {
            this.flowControlledEnabled = flowControlledEnabled;
        }

        public Integer getFlowMaximum() {
            return flowMaximum;
        }

        public void setFlowMaximum(Integer flowMaximum) {
            this.flowMaximum = flowMaximum;
        }

        public Boolean getFlowRetentionEnabled() {
            return flowRetentionEnabled;
        }

        public void setFlowRetentionEnabled(Boolean flowRetentionEnabled) {
            this.flowRetentionEnabled = flowRetentionEnabled;
        }

        public String getRetentionCyclePeriod() {
            return retentionCyclePeriod;
        }

        public void setRetentionCyclePeriod(String retentionCyclePeriod) {
            this.retentionCyclePeriod = retentionCyclePeriod;
        }

        public Boolean getWriteFile() {
            return writeFile;
        }

        public void setWriteFile(Boolean writeFile) {
            this.writeFile = writeFile;
        }

        public Integer getMinRequiredFileSize() {
            return minRequiredFileSize;
        }

        public void setMinRequiredFileSize(Integer minRequiredFileSize) {
            this.minRequiredFileSize = minRequiredFileSize;
        }

        public Boolean getIgnoreOutputDupCheck() {
            return ignoreOutputDupCheck;
        }

        public void setIgnoreOutputDupCheck(Boolean ignoreOutputDupCheck) {
            this.ignoreOutputDupCheck = ignoreOutputDupCheck;
        }

        public Boolean getLogAll() {
            return logAll;
        }

        public void setLogAll(Boolean logAll) {
            this.logAll = logAll;
        }
    }

    public static class RefCharsetEncodingDto {
        @JsonProperty("charsetEncodingId")
        private Integer charsetEncodingId;

        @JsonProperty("charsetCode")
        private String charsetCode;

        @JsonProperty("charsetDesc")
        private String charsetDesc;

        // Constructors, getters, setters
        public RefCharsetEncodingDto() {}

        public RefCharsetEncodingDto(Integer charsetEncodingId, String charsetCode,
                String charsetDesc) {
            this.charsetEncodingId = charsetEncodingId;
            this.charsetCode = charsetCode;
            this.charsetDesc = charsetDesc;
        }

        public Integer getCharsetEncodingId() {
            return charsetEncodingId;
        }

        public void setCharsetEncodingId(Integer charsetEncodingId) {
            this.charsetEncodingId = charsetEncodingId;
        }

        public String getCharsetCode() {
            return charsetCode;
        }

        public void setCharsetCode(String charsetCode) {
            this.charsetCode = charsetCode;
        }

        public String getCharsetDesc() {
            return charsetDesc;
        }

        public void setCharsetDesc(String charsetDesc) {
            this.charsetDesc = charsetDesc;
        }
    }

    // Main class constructors, getters, setters
    public RefFlowCompleteDto() {
        // Default constructor for JSON deserialization
    }

    // Getters and setters for main class
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

    public List<RefFlowCountryDto> getCountries() {
        return countries;
    }

    public void setCountries(List<RefFlowCountryDto> countries) {
        this.countries = countries;
    }

    public List<RefFlowPartnerDto> getPartners() {
        return partners;
    }

    public void setPartners(List<RefFlowPartnerDto> partners) {
        this.partners = partners;
    }

    public RefFlowRulesDto getRules() {
        return rules;
    }

    public void setRules(RefFlowRulesDto rules) {
        this.rules = rules;
    }

    public List<RefCharsetEncodingDto> getCharsetEncodings() {
        return charsetEncodings;
    }

    public void setCharsetEncodings(List<RefCharsetEncodingDto> charsetEncodings) {
        this.charsetEncodings = charsetEncodings;
    }

    @Override
    public String toString() {
        return "RefFlowCompleteDto{" + "flowId=" + flowId + ", flowCode='" + flowCode + '\''
                + ", flowName='" + flowName + '\'' + ", flowDirection='" + flowDirection + '\''
                + ", countries=" + (countries != null ? countries.size() : 0) + " items"
                + ", partners=" + (partners != null ? partners.size() : 0) + " items" + ", rules="
                + (rules != null ? "present" : "null") + ", charsetEncodings="
                + (charsetEncodings != null ? charsetEncodings.size() : 0) + " items" + '}';
    }
}
