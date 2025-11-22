package com.pixel.v2.persistence.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Unified entity for persisting received messages from various sources (MQ, HTTP API, File
 * processing)
 */
@Entity
@Table(name = "tb_messages", schema = "pixel_v2",
        indexes = {@Index(name = "idx_tb_messages_received_at", columnList = "receivedAt"),
                @Index(name = "idx_tb_messages_source", columnList = "source"),
                @Index(name = "idx_tb_messages_message_id", columnList = "messageId"),
                @Index(name = "idx_tb_messages_correlation_id", columnList = "correlationId"),
                @Index(name = "idx_tb_messages_status", columnList = "processingStatus"),
                @Index(name = "idx_tb_messages_type", columnList = "messageType")})
public class ReceivedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", length = 255)
    private String messageId;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "message_type", length = 50)
    private String messageType;

    @Column(nullable = false, length = 100)
    private String source;

    // Message payload
    @Lob
    @Column(nullable = false)
    private String payload;

    // File-specific fields
    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "line_number")
    private Long lineNumber;

    // Processing metadata
    @Column(name = "processing_status", length = 50)
    private String processingStatus;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "error_message", length = 1000)
    private String processingError;

    // Audit fields
    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
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

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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
        return "ReceivedMessage{" + "id=" + id + ", source='" + source + '\'' + ", messageId='"
                + messageId + '\'' + ", messageType='" + messageType + '\'' + ", fileName='"
                + fileName + '\'' + ", lineNumber=" + lineNumber + ", processingStatus='"
                + processingStatus + '\'' + ", receivedAt=" + receivedAt + '}';
    }
}
