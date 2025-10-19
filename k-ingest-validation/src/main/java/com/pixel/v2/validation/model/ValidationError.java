package com.pixel.v2.validation.model;

/**
 * Represents a validation error found in a payment message
 */
public class ValidationError {
    
    private String field;
    private String errorCode;
    private String message;
    private String xpath;
    private Severity severity;
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
    
    public ValidationError() {
        this.severity = Severity.HIGH;
    }
    
    public ValidationError(String field, String errorCode, String message) {
        this();
        this.field = field;
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public ValidationError(String field, String errorCode, String message, String xpath) {
        this(field, errorCode, message);
        this.xpath = xpath;
    }
    
    public ValidationError(String field, String errorCode, String message, Severity severity) {
        this(field, errorCode, message);
        this.severity = severity;
    }
    
    // Getters and setters
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getXpath() {
        return xpath;
    }
    
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationError{field='%s', code='%s', message='%s', severity='%s'}", 
                           field, errorCode, message, severity);
    }
}