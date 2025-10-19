package com.pixel.v2.validation.model;

/**
 * Represents a validation warning found in a payment message
 */
public class ValidationWarning {
    
    private String field;
    private String warningCode;
    private String message;
    private String xpath;
    private WarningType type;
    
    public enum WarningType {
        FORMATTING, PERFORMANCE, COMPLIANCE, BEST_PRACTICE
    }
    
    public ValidationWarning() {
        this.type = WarningType.COMPLIANCE;
    }
    
    public ValidationWarning(String field, String warningCode, String message) {
        this();
        this.field = field;
        this.warningCode = warningCode;
        this.message = message;
    }
    
    public ValidationWarning(String field, String warningCode, String message, String xpath) {
        this(field, warningCode, message);
        this.xpath = xpath;
    }
    
    public ValidationWarning(String field, String warningCode, String message, WarningType type) {
        this(field, warningCode, message);
        this.type = type;
    }
    
    // Getters and setters
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getWarningCode() {
        return warningCode;
    }
    
    public void setWarningCode(String warningCode) {
        this.warningCode = warningCode;
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
    
    public WarningType getType() {
        return type;
    }
    
    public void setType(WarningType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationWarning{field='%s', code='%s', message='%s', type='%s'}", 
                           field, warningCode, message, type);
    }
}