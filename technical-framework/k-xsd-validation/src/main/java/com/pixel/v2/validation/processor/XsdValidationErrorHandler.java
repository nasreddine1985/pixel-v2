package com.pixel.v2.validation.processor;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom error handler for XSD validation that collects and formats validation errors
 */
public class XsdValidationErrorHandler implements ErrorHandler {
    
    private final String validationMode;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    
    public XsdValidationErrorHandler(String validationMode) {
        this.validationMode = validationMode;
    }
    
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        String warningMessage = formatErrorMessage("WARNING", exception);
        warnings.add(warningMessage);
        
        if ("STRICT".equals(validationMode)) {
            // In strict mode, treat warnings as errors
            errors.add(warningMessage);
        }
    }
    
    @Override
    public void error(SAXParseException exception) throws SAXException {
        String errorMessage = formatErrorMessage("ERROR", exception);
        errors.add(errorMessage);
    }
    
    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        String errorMessage = formatErrorMessage("FATAL", exception);
        errors.add(errorMessage);
    }
    
    /**
     * Formats error message with location information
     */
    private String formatErrorMessage(String level, SAXParseException exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(level).append(": ");
        sb.append(exception.getMessage());
        
        if (exception.getLineNumber() > 0) {
            sb.append(" (Line: ").append(exception.getLineNumber());
            if (exception.getColumnNumber() > 0) {
                sb.append(", Column: ").append(exception.getColumnNumber());
            }
            sb.append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Returns true if there are validation errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Returns true if there are validation warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Returns a summary of all errors
     */
    public String getErrorSummary() {
        if (errors.isEmpty()) {
            return "No validation errors";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(errors.size()).append(" validation error(s): ");
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(errors.get(i));
        }
        return sb.toString();
    }
    
    /**
     * Returns a summary of all warnings
     */
    public String getWarningSummary() {
        if (warnings.isEmpty()) {
            return "No validation warnings";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(warnings.size()).append(" validation warning(s): ");
        for (int i = 0; i < warnings.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(warnings.get(i));
        }
        return sb.toString();
    }
    
    /**
     * Returns all errors as a list
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Returns all warnings as a list
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
}