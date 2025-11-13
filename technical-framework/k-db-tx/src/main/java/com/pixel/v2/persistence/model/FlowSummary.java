package com.pixel.v2.persistence.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing FLOW_SUMMARY table Contains flow execution summary information
 */
@Entity
@Table(name = "FLOW_SUMMARY",
        indexes = {@Index(name = "idx_flow_occur_id", columnList = "flowOccurId"),
                @Index(name = "idx_flow_code_summary", columnList = "flowCode"),
                @Index(name = "idx_flow_status", columnList = "flowStatusCode"),
                @Index(name = "idx_begin_flow_date", columnList = "beginFlowDate"),
                @Index(name = "idx_last_update_date", columnList = "lastUpdateDate"),
                @Index(name = "idx_issuing_partner", columnList = "issuingPartnerCode")})
public class FlowSummary {

    @Id
    @Column(name = "FLOW_OCCUR_ID", length = 64)
    private String flowOccurId;

    @Column(name = "FLOW_CODE", length = 64)
    private String flowCode;

    @Column(name = "FLOW_STATUS_CODE", length = 20)
    private String flowStatusCode;

    @Column(name = "FLOW_COUNTRY_CODE", length = 59)
    private String flowCountryCode;

    @Column(name = "FLOW_COUNTRY_ID")
    private Integer flowCountryId;

    @Column(name = "FLOW_TYPE_ID")
    private Integer flowTypeId;

    @Column(name = "FLOW_COMMENT", length = 255)
    private String flowComment;

    @Column(name = "NB_OUT_EXPECTED")
    private Integer nbOutExpected;

    @Column(name = "NB_OUT_COMPLETED")
    private Integer nbOutCompleted;

    @Column(name = "NB_ERROR")
    private Integer nbError;

    @Column(name = "NB_REMITANCE")
    private Integer nbRemitance;

    @Column(name = "NB_TRANSACTION")
    private Integer nbTransaction;

    @Column(name = "NB_REPLAY")
    private Integer nbReplay;

    @Column(name = "ISSUING_PARTNER_CODE", length = 64)
    private String issuingPartnerCode;

    @Column(name = "ISSUING_PARTNER_LINK", length = 128)
    private String issuingPartnerLink;

    @Column(name = "RECIPIENT_PARTNER_CODE", length = 255)
    private String recipientPartnerCode;

    @Column(name = "RECIPIENT_PARTNER_LINK", length = 510)
    private String recipientPartnerLink;

    @Column(name = "LAST_LOG_ID", length = 64)
    private String lastLogId;

    @Column(name = "LAST_LOG_COMPONENT", length = 255)
    private String lastLogComponent;

    @Column(name = "LAST_LOG_DATETIME", precision = 6)
    private LocalDateTime lastLogDatetime;

    @Column(name = "LAST_LOG_STATUS_CODE", length = 20)
    private String lastLogStatusCode;

    @Column(name = "LAST_UPDATE_DATETIME", precision = 6)
    private LocalDateTime lastUpdateDatetime;

    @Column(name = "LAST_UPDATE_USER", length = 50)
    private String lastUpdateUser;

    @Column(name = "ROOT_ERROR_CODE", length = 50)
    private String rootErrorCode;

    @Column(name = "ROOT_ERROR_LOG_ID", length = 50)
    private String rootErrorLogId;

    @Column(name = "ROOT_ERROR_DATETIME", precision = 6)
    private LocalDateTime rootErrorDatetime;

    @Column(name = "INPUT_FILE_PATH", length = 500)
    private String inputFilePath;

    @Column(name = "INPUT_FILE_SIZE", length = 50)
    private String inputFileSize;

    @Column(name = "REF_FLOW_ID", precision = 10, scale = 0)
    private BigDecimal refFlowId;

    @Column(name = "BEGIN_FLOW_DATETIME", precision = 6)
    private LocalDateTime beginFlowDatetime;

    @Column(name = "END_FLOW_DATETIME", precision = 6)
    private LocalDateTime endFlowDatetime;

    @Column(name = "CURRENT_CLIENT_DATETIME", precision = 6)
    private LocalDateTime currentClientDatetime;

    @Column(name = "BEGIN_FLOW_DATE", insertable = false, updatable = false)
    private LocalDate beginFlowDate;

    @Column(name = "END_FLOW_DATE", insertable = false, updatable = false)
    private LocalDate endFlowDate;

    @Column(name = "LAST_UPDATE_DATE", insertable = false, updatable = false)
    private LocalDate lastUpdateDate;

    @Column(name = "REPLAY_ID", length = 20)
    private String replayId;

    @Column(name = "REGION", length = 5)
    private String region;

    // Default constructor
    public FlowSummary() {
        this.lastUpdateDatetime = LocalDateTime.now();
        this.currentClientDatetime = LocalDateTime.now();
    }

    // Constructor with essential fields
    public FlowSummary(String flowOccurId, String flowCode) {
        this();
        this.flowOccurId = flowOccurId;
        this.flowCode = flowCode;
    }

    // Getters and Setters
    public String getFlowOccurId() {
        return flowOccurId;
    }

    public void setFlowOccurId(String flowOccurId) {
        this.flowOccurId = flowOccurId;
    }

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getFlowStatusCode() {
        return flowStatusCode;
    }

    public void setFlowStatusCode(String flowStatusCode) {
        this.flowStatusCode = flowStatusCode;
    }

    public String getFlowCountryCode() {
        return flowCountryCode;
    }

    public void setFlowCountryCode(String flowCountryCode) {
        this.flowCountryCode = flowCountryCode;
    }

    public Integer getFlowCountryId() {
        return flowCountryId;
    }

    public void setFlowCountryId(Integer flowCountryId) {
        this.flowCountryId = flowCountryId;
    }

    public Integer getFlowTypeId() {
        return flowTypeId;
    }

    public void setFlowTypeId(Integer flowTypeId) {
        this.flowTypeId = flowTypeId;
    }

    public String getFlowComment() {
        return flowComment;
    }

    public void setFlowComment(String flowComment) {
        this.flowComment = flowComment;
    }

    public Integer getNbOutExpected() {
        return nbOutExpected;
    }

    public void setNbOutExpected(Integer nbOutExpected) {
        this.nbOutExpected = nbOutExpected;
    }

    public Integer getNbOutCompleted() {
        return nbOutCompleted;
    }

    public void setNbOutCompleted(Integer nbOutCompleted) {
        this.nbOutCompleted = nbOutCompleted;
    }

    public Integer getNbError() {
        return nbError;
    }

    public void setNbError(Integer nbError) {
        this.nbError = nbError;
    }

    public Integer getNbRemitance() {
        return nbRemitance;
    }

    public void setNbRemitance(Integer nbRemitance) {
        this.nbRemitance = nbRemitance;
    }

    public Integer getNbTransaction() {
        return nbTransaction;
    }

    public void setNbTransaction(Integer nbTransaction) {
        this.nbTransaction = nbTransaction;
    }

    public Integer getNbReplay() {
        return nbReplay;
    }

    public void setNbReplay(Integer nbReplay) {
        this.nbReplay = nbReplay;
    }

    public String getIssuingPartnerCode() {
        return issuingPartnerCode;
    }

    public void setIssuingPartnerCode(String issuingPartnerCode) {
        this.issuingPartnerCode = issuingPartnerCode;
    }

    public String getIssuingPartnerLink() {
        return issuingPartnerLink;
    }

    public void setIssuingPartnerLink(String issuingPartnerLink) {
        this.issuingPartnerLink = issuingPartnerLink;
    }

    public String getRecipientPartnerCode() {
        return recipientPartnerCode;
    }

    public void setRecipientPartnerCode(String recipientPartnerCode) {
        this.recipientPartnerCode = recipientPartnerCode;
    }

    public String getRecipientPartnerLink() {
        return recipientPartnerLink;
    }

    public void setRecipientPartnerLink(String recipientPartnerLink) {
        this.recipientPartnerLink = recipientPartnerLink;
    }

    public String getLastLogId() {
        return lastLogId;
    }

    public void setLastLogId(String lastLogId) {
        this.lastLogId = lastLogId;
    }

    public String getLastLogComponent() {
        return lastLogComponent;
    }

    public void setLastLogComponent(String lastLogComponent) {
        this.lastLogComponent = lastLogComponent;
    }

    public LocalDateTime getLastLogDatetime() {
        return lastLogDatetime;
    }

    public void setLastLogDatetime(LocalDateTime lastLogDatetime) {
        this.lastLogDatetime = lastLogDatetime;
    }

    public String getLastLogStatusCode() {
        return lastLogStatusCode;
    }

    public void setLastLogStatusCode(String lastLogStatusCode) {
        this.lastLogStatusCode = lastLogStatusCode;
    }

    public LocalDateTime getLastUpdateDatetime() {
        return lastUpdateDatetime;
    }

    public void setLastUpdateDatetime(LocalDateTime lastUpdateDatetime) {
        this.lastUpdateDatetime = lastUpdateDatetime;
    }

    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    public String getRootErrorCode() {
        return rootErrorCode;
    }

    public void setRootErrorCode(String rootErrorCode) {
        this.rootErrorCode = rootErrorCode;
    }

    public String getRootErrorLogId() {
        return rootErrorLogId;
    }

    public void setRootErrorLogId(String rootErrorLogId) {
        this.rootErrorLogId = rootErrorLogId;
    }

    public LocalDateTime getRootErrorDatetime() {
        return rootErrorDatetime;
    }

    public void setRootErrorDatetime(LocalDateTime rootErrorDatetime) {
        this.rootErrorDatetime = rootErrorDatetime;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public String getInputFileSize() {
        return inputFileSize;
    }

    public void setInputFileSize(String inputFileSize) {
        this.inputFileSize = inputFileSize;
    }

    public BigDecimal getRefFlowId() {
        return refFlowId;
    }

    public void setRefFlowId(BigDecimal refFlowId) {
        this.refFlowId = refFlowId;
    }

    public LocalDateTime getBeginFlowDatetime() {
        return beginFlowDatetime;
    }

    public void setBeginFlowDatetime(LocalDateTime beginFlowDatetime) {
        this.beginFlowDatetime = beginFlowDatetime;
    }

    public LocalDateTime getEndFlowDatetime() {
        return endFlowDatetime;
    }

    public void setEndFlowDatetime(LocalDateTime endFlowDatetime) {
        this.endFlowDatetime = endFlowDatetime;
    }

    public LocalDateTime getCurrentClientDatetime() {
        return currentClientDatetime;
    }

    public void setCurrentClientDatetime(LocalDateTime currentClientDatetime) {
        this.currentClientDatetime = currentClientDatetime;
    }

    public LocalDate getBeginFlowDate() {
        return beginFlowDate;
    }

    public void setBeginFlowDate(LocalDate beginFlowDate) {
        this.beginFlowDate = beginFlowDate;
    }

    public LocalDate getEndFlowDate() {
        return endFlowDate;
    }

    public void setEndFlowDate(LocalDate endFlowDate) {
        this.endFlowDate = endFlowDate;
    }

    public LocalDate getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getReplayId() {
        return replayId;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "FlowSummary{" + "flowOccurId='" + flowOccurId + '\'' + ", flowCode='" + flowCode
                + '\'' + ", flowStatusCode='" + flowStatusCode + '\'' + ", issuingPartnerCode='"
                + issuingPartnerCode + '\'' + ", beginFlowDatetime=" + beginFlowDatetime + '}';
    }
}
