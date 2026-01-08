package com.pixel.v2.referential.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_transport_email table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_TRANSPORT_EMAIL", schema = "TIB_AUDIT_TEC")
public class RefTransportEmail {

    @Id
    @Column(name = "TRANSPORT_ID")
    private Integer transportId;

    @Column(name = "EMAIL_NAME")
    private String emailName;

    @Column(name = "EMAIL_FROM")
    private String emailFrom;

    @Column(name = "EMAIL_RECIPIENT_TO")
    private String emailRecipientTo;

    @Column(name = "EMAIL_RECIPIENT_CC")
    private String emailRecipientCc;

    @Column(name = "EMAIL_SUBJECT")
    private String emailSubject;

    @Column(name = "EMAIL_BODY")
    private String emailBody;

    @Column(name = "EMAIL_SIGNATURE")
    private String emailSignature;

    @Column(name = "HAS_ATTACHMENT")
    private Boolean hasAttachment;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Many-to-one relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSPORT_ID", insertable = false, updatable = false)
    private RefTransport transport;

    // Default constructor
    public RefTransportEmail() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefTransportEmail(Integer transportId, String emailName, String emailFrom) {
        this.transportId = transportId;
        this.emailName = emailName;
        this.emailFrom = emailFrom;
        this.hasAttachment = false;
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getTransportId() {
        return transportId;
    }

    public void setTransportId(Integer transportId) {
        this.transportId = transportId;
    }

    public String getEmailName() {
        return emailName;
    }

    public void setEmailName(String emailName) {
        this.emailName = emailName;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailRecipientTo() {
        return emailRecipientTo;
    }

    public void setEmailRecipientTo(String emailRecipientTo) {
        this.emailRecipientTo = emailRecipientTo;
    }

    public String getEmailRecipientCc() {
        return emailRecipientCc;
    }

    public void setEmailRecipientCc(String emailRecipientCc) {
        this.emailRecipientCc = emailRecipientCc;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public String getEmailSignature() {
        return emailSignature;
    }

    public void setEmailSignature(String emailSignature) {
        this.emailSignature = emailSignature;
    }

    public Boolean getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(Boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
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

    public RefTransport getTransport() {
        return transport;
    }

    public void setTransport(RefTransport transport) {
        this.transport = transport;
    }

    @Override
    public String toString() {
        return "RefTransportEmail{" + "transportId=" + transportId + ", emailName='" + emailName
                + '\'' + ", emailFrom='" + emailFrom + '\'' + ", emailRecipientTo='"
                + emailRecipientTo + '\'' + ", emailRecipientCc='" + emailRecipientCc + '\''
                + ", emailSubject='" + emailSubject + '\'' + ", hasAttachment=" + hasAttachment
                + ", creationDte=" + creationDte + ", updateDte=" + updateDte + '}';
    }
}
