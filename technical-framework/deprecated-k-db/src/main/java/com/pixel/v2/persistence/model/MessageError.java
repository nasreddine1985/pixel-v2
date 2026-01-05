package com.pixel.v2.persistence.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_errors")
public class MessageError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jms_message_id", length = 100)
    private String jmsMessageId;

    @Column(name = "error_route", length = 50)
    private String errorRoute;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_timestamp")
    private LocalDateTime errorTimestamp;

    @Column(name = "message_body", columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (errorTimestamp == null) {
            errorTimestamp = LocalDateTime.now();
        }
    }

    // Constructors
    public MessageError() {}

    public MessageError(String jmsMessageId, String errorRoute, String errorMessage) {
        this.jmsMessageId = jmsMessageId;
        this.errorRoute = errorRoute;
        this.errorMessage = errorMessage;
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

    public String getErrorRoute() {
        return errorRoute;
    }

    public void setErrorRoute(String errorRoute) {
        this.errorRoute = errorRoute;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getErrorTimestamp() {
        return errorTimestamp;
    }

    public void setErrorTimestamp(LocalDateTime errorTimestamp) {
        this.errorTimestamp = errorTimestamp;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MessageError{" + "id=" + id + ", jmsMessageId='" + jmsMessageId + '\''
                + ", errorRoute='" + errorRoute + '\'' + ", errorMessage='" + errorMessage + '\''
                + ", errorTimestamp=" + errorTimestamp + ", createdAt=" + createdAt + '}';
    }
}
