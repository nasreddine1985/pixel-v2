package com.pixel.v2.validation.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of payment message validation
 */
public class ValidationResult {
    
    private boolean valid;
    private String messageId;
    private String messageType;
    private LocalDateTime validationTimestamp;
    private List<ValidationError> errors;
    private List<ValidationWarning> warnings;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.validationTimestamp = LocalDateTime.now();
    }
    
    public ValidationResult(String messageId, String messageType) {
        this();
        this.messageId = messageId;
        this.messageType = messageType;
    }
    
    // Getters and setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
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
    
    public LocalDateTime getValidationTimestamp() {
        return validationTimestamp;
    }
    
    public void setValidationTimestamp(LocalDateTime validationTimestamp) {
        this.validationTimestamp = validationTimestamp;
    }
    
    public List<ValidationError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
    
    public List<ValidationWarning> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<ValidationWarning> warnings) {
        this.warnings = warnings;
    }
    
    // Helper methods
    public void addError(ValidationError error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public void addError(String field, String code, String message) {
        addError(new ValidationError(field, code, message));
    }
    
    public void addWarning(ValidationWarning warning) {
        this.warnings.add(warning);
    }
    
    public void addWarning(String field, String code, String message) {
        addWarning(new ValidationWarning(field, code, message));
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public int getWarningCount() {
        return warnings.size();
    }
    
    @Override
    public String toString() {
        return String.format("ValidationResult{messageId='%s', messageType='%s', valid=%s, errors=%d, warnings=%d}", 
                           messageId, messageType, valid, getErrorCount(), getWarningCount());
    }
}