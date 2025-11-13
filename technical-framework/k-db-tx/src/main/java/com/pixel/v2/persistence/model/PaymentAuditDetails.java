package com.pixel.v2.persistence.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing PAYMENT_AUDIT_DETAILS table Contains detailed payment audit information
 */
@Entity
@Table(name = "PAYMENT_AUDIT_DETAILS",
        indexes = {@Index(name = "idx_payment_audit_id", columnList = "paymentAuditId"),
                @Index(name = "idx_flow_occur_id_audit", columnList = "flowOccurId"),
                @Index(name = "idx_log_day_audit", columnList = "logDay"),
                @Index(name = "idx_currency", columnList = "currency"),
                @Index(name = "idx_country_audit", columnList = "country"),
                @Index(name = "idx_region_audit", columnList = "region"),
                @Index(name = "idx_message_type", columnList = "messageType")})
public class PaymentAuditDetails {

    @Id
    @Column(name = "PAYMENT_AUDIT_ID", precision = 20, scale = 0)
    private BigDecimal paymentAuditId;

    @Column(name = "FLOW_OCCUR_ID", length = 64, nullable = false)
    private String flowOccurId;

    @Column(name = "DATATS", nullable = false, precision = 6)
    private LocalDateTime datats;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "CURRENCY", length = 3)
    private String currency;

    @Column(name = "DEBTOR_ACCOUNT", length = 140)
    private String debtorAccount;

    @Column(name = "DEBTOR_NAME", length = 140)
    private String debtorName;

    @Column(name = "CREDITOR_ACCOUNT", length = 140)
    private String creditorAccount;

    @Column(name = "CREDITOR_NAME", length = 140)
    private String creditorName;

    @Column(name = "CREDITOR_BANK", length = 140)
    private String creditorBank;

    @Column(name = "PAYMENT_REF", length = 140)
    private String paymentRef;

    @Column(name = "IS_VIRTUAL", length = 1)
    private Character isVirtual;

    @Column(name = "VIRTUAL_ACCOUNT", length = 35)
    private String virtualAccount;

    @Column(name = "MESSAGE_TYPE", length = 140)
    private String messageType;

    @Column(name = "RECIPIENT_PARTNER", length = 20)
    private String recipientPartner;

    @Column(name = "LOG_DAY")
    private LocalDate logDay;

    @Column(name = "MESSAGE_USER_REFERENCE", length = 16)
    private String messageUserReference;

    @Column(name = "COUNTRY", length = 20)
    private String country;

    @Column(name = "SENDER", length = 35)
    private String sender;

    @Column(name = "DEBTOR_BANK", length = 70)
    private String debtorBank;

    @Column(name = "MESSAGE_ID", length = 35)
    private String messageId;

    @Column(name = "REQUESTED_EXECUTION_DATE")
    private LocalDate requestedExecutionDate;

    @Column(name = "UETR", length = 36)
    private String uetr;

    @Column(name = "REGION", length = 5)
    private String region;

    // Default constructor
    public PaymentAuditDetails() {
        this.datats = LocalDateTime.now();
        this.logDay = LocalDate.now();
    }

    // Constructor with required fields
    public PaymentAuditDetails(BigDecimal paymentAuditId, String flowOccurId) {
        this();
        this.paymentAuditId = paymentAuditId;
        this.flowOccurId = flowOccurId;
    }

    // Getters and Setters
    public BigDecimal getPaymentAuditId() {
        return paymentAuditId;
    }

    public void setPaymentAuditId(BigDecimal paymentAuditId) {
        this.paymentAuditId = paymentAuditId;
    }

    public String getFlowOccurId() {
        return flowOccurId;
    }

    public void setFlowOccurId(String flowOccurId) {
        this.flowOccurId = flowOccurId;
    }

    public LocalDateTime getDatats() {
        return datats;
    }

    public void setDatats(LocalDateTime datats) {
        this.datats = datats;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(String debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public String getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(String creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public String getCreditorBank() {
        return creditorBank;
    }

    public void setCreditorBank(String creditorBank) {
        this.creditorBank = creditorBank;
    }

    public String getPaymentRef() {
        return paymentRef;
    }

    public void setPaymentRef(String paymentRef) {
        this.paymentRef = paymentRef;
    }

    public Character getIsVirtual() {
        return isVirtual;
    }

    public void setIsVirtual(Character isVirtual) {
        this.isVirtual = isVirtual;
    }

    public String getVirtualAccount() {
        return virtualAccount;
    }

    public void setVirtualAccount(String virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getRecipientPartner() {
        return recipientPartner;
    }

    public void setRecipientPartner(String recipientPartner) {
        this.recipientPartner = recipientPartner;
    }

    public LocalDate getLogDay() {
        return logDay;
    }

    public void setLogDay(LocalDate logDay) {
        this.logDay = logDay;
    }

    public String getMessageUserReference() {
        return messageUserReference;
    }

    public void setMessageUserReference(String messageUserReference) {
        this.messageUserReference = messageUserReference;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDebtorBank() {
        return debtorBank;
    }

    public void setDebtorBank(String debtorBank) {
        this.debtorBank = debtorBank;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public String getUetr() {
        return uetr;
    }

    public void setUetr(String uetr) {
        this.uetr = uetr;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "PaymentAuditDetails{" + "paymentAuditId=" + paymentAuditId + ", flowOccurId='"
                + flowOccurId + '\'' + ", amount=" + amount + ", currency='" + currency + '\''
                + ", messageType='" + messageType + '\'' + ", country='" + country + '\'' + '}';
    }
}
