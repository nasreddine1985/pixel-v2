package com.pixel.v2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entity representing LOG_EVENT table Contains detailed logging information for flow processing
 */
@Entity
@Table(name = "LOG_EVENT",
        indexes = {@Index(name = "idx_log_datats", columnList = "datats"),
                @Index(name = "idx_log_flowid", columnList = "flowid"),
                @Index(name = "idx_log_flowcode", columnList = "flowcode"),
                @Index(name = "idx_log_component", columnList = "component"),
                @Index(name = "idx_log_day", columnList = "logDay"),
                @Index(name = "idx_log_logrole", columnList = "logrole")})
public class LogEvent {

    @Id
    @Column(name = "LOGID", length = 64, nullable = false)
    private String logId;

    @Column(name = "DATATS", nullable = false, precision = 6)
    private LocalDateTime datats;

    @Column(name = "FLOWID", length = 64, nullable = false)
    private String flowId;

    @Column(name = "HALFFLOWID", length = 64, nullable = false)
    private String halfFlowId;

    @Column(name = "FLOWCODE", length = 64, nullable = false)
    private String flowCode;

    @Column(name = "HALFFLOWCODE", length = 64, nullable = false)
    private String halfFlowCode;

    @Column(name = "CONTEXTID", length = 64)
    private String contextId;

    @Column(name = "CLIENTLOGTIMESTAMP", nullable = false, precision = 6)
    private LocalDateTime clientLogTimestamp;

    @Column(name = "DBLOGTIMESTAMP", nullable = false, precision = 6)
    private LocalDateTime dbLogTimestamp;

    @Column(name = "TXT", length = 2048, nullable = false)
    private String txt;

    @Column(name = "LONGTXT", columnDefinition = "TEXT")
    private String longTxt;

    @Column(name = "LOGROLE", length = 16, nullable = false)
    private String logRole;

    @Column(name = "CODE", length = 32)
    private String code;

    @Column(name = "CUSTOMSTEP", length = 128)
    private String customStep;

    @Column(name = "COMPONENT", length = 64, nullable = false)
    private String component;

    @Column(name = "INSTANCEID", length = 16, nullable = false)
    private String instanceId;

    @Column(name = "SERVICEPATH", length = 512)
    private String servicePath;

    @Column(name = "PROCESSPATH", length = 512)
    private String processPath;

    @Column(name = "REFFLOWID", precision = 10, scale = 0)
    private BigDecimal refFlowId;

    @Column(name = "BEGINPROCESS", precision = 6)
    private LocalDateTime beginProcess;

    @Column(name = "ENDPROCESS", precision = 6)
    private LocalDateTime endProcess;

    @Column(name = "CONTEXTTIMESTAMP", precision = 6)
    private LocalDateTime contextTimestamp;

    @Column(name = "MSGSENTTIMESTAMP", precision = 6)
    private LocalDateTime msgSentTimestamp;

    @Column(name = "MESSAGINGTYPE", length = 10)
    private String messagingType;

    @Column(name = "MSGID", length = 64)
    private String msgId;

    @Column(name = "MSGPRIORITY")
    private Integer msgPriority;

    @Column(name = "MSGCORRELATIONID", length = 256)
    private String msgCorrelationId;

    @Column(name = "MSGSOURCESYSTEM", length = 128)
    private String msgSourceSystem;

    @Column(name = "MSGPRIVATECONTEXT", columnDefinition = "TEXT")
    private String msgPrivateContext;

    @Column(name = "MSGTRANSACTIONID", length = 64)
    private String msgTransactionId;

    @Column(name = "MSGPROPERTIES", columnDefinition = "TEXT")
    private String msgProperties;

    @Column(name = "MSGBATCHNAME", length = 256)
    private String msgBatchName;

    @Column(name = "MSGBATCHMSGNO")
    private Integer msgBatchMsgNo;

    @Column(name = "MSGBATCHSIZE")
    private Integer msgBatchSize;

    @Column(name = "XMLMSGACTION", length = 256)
    private String xmlMsgAction;

    @Column(name = "MSGRESUBMITIND", length = 64)
    private String msgResubmitInd;

    @Column(name = "MSGBODY", columnDefinition = "TEXT")
    private String msgBody;

    @Column(name = "LOG_DAY", insertable = false, updatable = false)
    private LocalDate logDay;

    // Default constructor
    public LogEvent() {
        this.datats = LocalDateTime.now();
        this.clientLogTimestamp = LocalDateTime.now();
        this.dbLogTimestamp = LocalDateTime.now();
    }

    // Constructor with essential fields
    public LogEvent(String logId, String flowId, String flowCode, String component) {
        this();
        this.logId = logId;
        this.flowId = flowId;
        this.flowCode = flowCode;
        this.component = component;
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public LocalDateTime getDatats() {
        return datats;
    }

    public void setDatats(LocalDateTime datats) {
        this.datats = datats;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getHalfFlowId() {
        return halfFlowId;
    }

    public void setHalfFlowId(String halfFlowId) {
        this.halfFlowId = halfFlowId;
    }

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getHalfFlowCode() {
        return halfFlowCode;
    }

    public void setHalfFlowCode(String halfFlowCode) {
        this.halfFlowCode = halfFlowCode;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public LocalDateTime getClientLogTimestamp() {
        return clientLogTimestamp;
    }

    public void setClientLogTimestamp(LocalDateTime clientLogTimestamp) {
        this.clientLogTimestamp = clientLogTimestamp;
    }

    public LocalDateTime getDbLogTimestamp() {
        return dbLogTimestamp;
    }

    public void setDbLogTimestamp(LocalDateTime dbLogTimestamp) {
        this.dbLogTimestamp = dbLogTimestamp;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getLongTxt() {
        return longTxt;
    }

    public void setLongTxt(String longTxt) {
        this.longTxt = longTxt;
    }

    public String getLogRole() {
        return logRole;
    }

    public void setLogRole(String logRole) {
        this.logRole = logRole;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomStep() {
        return customStep;
    }

    public void setCustomStep(String customStep) {
        this.customStep = customStep;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getProcessPath() {
        return processPath;
    }

    public void setProcessPath(String processPath) {
        this.processPath = processPath;
    }

    public BigDecimal getRefFlowId() {
        return refFlowId;
    }

    public void setRefFlowId(BigDecimal refFlowId) {
        this.refFlowId = refFlowId;
    }

    public LocalDateTime getBeginProcess() {
        return beginProcess;
    }

    public void setBeginProcess(LocalDateTime beginProcess) {
        this.beginProcess = beginProcess;
    }

    public LocalDateTime getEndProcess() {
        return endProcess;
    }

    public void setEndProcess(LocalDateTime endProcess) {
        this.endProcess = endProcess;
    }

    public LocalDateTime getContextTimestamp() {
        return contextTimestamp;
    }

    public void setContextTimestamp(LocalDateTime contextTimestamp) {
        this.contextTimestamp = contextTimestamp;
    }

    public LocalDateTime getMsgSentTimestamp() {
        return msgSentTimestamp;
    }

    public void setMsgSentTimestamp(LocalDateTime msgSentTimestamp) {
        this.msgSentTimestamp = msgSentTimestamp;
    }

    public String getMessagingType() {
        return messagingType;
    }

    public void setMessagingType(String messagingType) {
        this.messagingType = messagingType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Integer getMsgPriority() {
        return msgPriority;
    }

    public void setMsgPriority(Integer msgPriority) {
        this.msgPriority = msgPriority;
    }

    public String getMsgCorrelationId() {
        return msgCorrelationId;
    }

    public void setMsgCorrelationId(String msgCorrelationId) {
        this.msgCorrelationId = msgCorrelationId;
    }

    public String getMsgSourceSystem() {
        return msgSourceSystem;
    }

    public void setMsgSourceSystem(String msgSourceSystem) {
        this.msgSourceSystem = msgSourceSystem;
    }

    public String getMsgPrivateContext() {
        return msgPrivateContext;
    }

    public void setMsgPrivateContext(String msgPrivateContext) {
        this.msgPrivateContext = msgPrivateContext;
    }

    public String getMsgTransactionId() {
        return msgTransactionId;
    }

    public void setMsgTransactionId(String msgTransactionId) {
        this.msgTransactionId = msgTransactionId;
    }

    public String getMsgProperties() {
        return msgProperties;
    }

    public void setMsgProperties(String msgProperties) {
        this.msgProperties = msgProperties;
    }

    public String getMsgBatchName() {
        return msgBatchName;
    }

    public void setMsgBatchName(String msgBatchName) {
        this.msgBatchName = msgBatchName;
    }

    public Integer getMsgBatchMsgNo() {
        return msgBatchMsgNo;
    }

    public void setMsgBatchMsgNo(Integer msgBatchMsgNo) {
        this.msgBatchMsgNo = msgBatchMsgNo;
    }

    public Integer getMsgBatchSize() {
        return msgBatchSize;
    }

    public void setMsgBatchSize(Integer msgBatchSize) {
        this.msgBatchSize = msgBatchSize;
    }

    public String getXmlMsgAction() {
        return xmlMsgAction;
    }

    public void setXmlMsgAction(String xmlMsgAction) {
        this.xmlMsgAction = xmlMsgAction;
    }

    public String getMsgResubmitInd() {
        return msgResubmitInd;
    }

    public void setMsgResubmitInd(String msgResubmitInd) {
        this.msgResubmitInd = msgResubmitInd;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public LocalDate getLogDay() {
        return logDay;
    }

    public void setLogDay(LocalDate logDay) {
        this.logDay = logDay;
    }

    @Override
    public String toString() {
        return "LogEvent{" + "logId='" + logId + '\'' + ", flowId='" + flowId + '\''
                + ", flowCode='" + flowCode + '\'' + ", component='" + component + '\''
                + ", logRole='" + logRole + '\'' + ", datats=" + datats + '}';
    }
}
