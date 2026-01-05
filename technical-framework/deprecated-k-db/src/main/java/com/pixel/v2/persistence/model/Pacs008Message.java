package com.pixel.v2.persistence.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pacs008_messages")
public class Pacs008Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jms_message_id", length = 100)
    private String jmsMessageId;

    @Column(name = "jms_correlation_id", length = 100)
    private String jmsCorrelationId;

    @Column(name = "message_type", length = 50)
    private String messageType;

    @Column(name = "message_body", columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "processed_timestamp")
    private LocalDateTime processedTimestamp;

    @Column(name = "processing_route", length = 50)
    private String processingRoute;

    @Column(name = "jms_priority")
    private Integer jmsPriority;

    @Column(name = "jms_timestamp")
    private Long jmsTimestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (processedTimestamp == null) {
            processedTimestamp = LocalDateTime.now();
        }
    }

    // Constructors
    public Pacs008Message() {}

    public Pacs008Message(String jmsMessageId, String messageBody, String messageType) {
        this.jmsMessageId = jmsMessageId;
        this.messageBody = messageBody;
        this.messageType = messageType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJmsMessageId() {
        return jmsMessageId;
    }

    public void setJmsMessageId(String jmsMessageId) {
        this.jmsMessageId = jmsMessageId;
    }

    public String getJmsCorrelationId() {
        return jmsCorrelationId;
    }

    public void setJmsCorrelationId(String jmsCorrelationId) {
        this.jmsCorrelationId = jmsCorrelationId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public LocalDateTime getProcessedTimestamp() {
        return processedTimestamp;
    }

    public void setProcessedTimestamp(LocalDateTime processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }

    public String getProcessingRoute() {
        return processingRoute;
    }

    public void setProcessingRoute(String processingRoute) {
        this.processingRoute = processingRoute;
    }

    public Integer getJmsPriority() {
        return jmsPriority;
    }

    public void setJmsPriority(Integer jmsPriority) {
        this.jmsPriority = jmsPriority;
    }

    public Long getJmsTimestamp() {
        return jmsTimestamp;
    }

    public void setJmsTimestamp(Long jmsTimestamp) {
        this.jmsTimestamp = jmsTimestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Pacs008Message{" + "id=" + id + ", jmsMessageId='" + jmsMessageId + '\''
                + ", jmsCorrelationId='" + jmsCorrelationId + '\'' + ", messageType='" + messageType
                + '\'' + ", processingRoute='" + processingRoute + '\'' + ", processedTimestamp="
                + processedTimestamp + ", createdAt=" + createdAt + '}';
    }
}
