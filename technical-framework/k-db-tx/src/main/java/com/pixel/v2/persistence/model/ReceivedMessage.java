package com.pixel.v2.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Index;
import java.time.OffsetDateTime;

/**
 * Unified entity for persisting received messages from various sources
 * (MQ, HTTP API, File processing)
 */
@Entity
@Table(name = "RECEIVED_MESSAGE", indexes = {
    @Index(name = "idx_received_at", columnList = "receivedAt"),
    @Index(name = "idx_source", columnList = "source"),
    @Index(name = "idx_message_id", columnList = "messageId")
})
public class ReceivedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime receivedAt;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(length = 255)
    private String messageId;

    @Column(length = 50)
    private String messageType;

    // File-specific fields
    @Column(length = 500)
    private String fileName;

    private Long lineNumber;

    // Processing metadata
    @Column(length = 50)
    private String processingStatus;

    private OffsetDateTime processedAt;

    @Column(length = 1000)
    private String processingError;

    // Message payload
    @Lob
    @Column(nullable = false)
    private String payload;

    // Audit fields
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    // Default constructor
    public ReceivedMessage() {
        this.createdAt = OffsetDateTime.now();
        this.receivedAt = OffsetDateTime.now();
        this.processingStatus = "RECEIVED";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getProcessingError() {
        return processingError;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ReceivedMessage{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", messageId='" + messageId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", lineNumber=" + lineNumber +
                ", processingStatus='" + processingStatus + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
}