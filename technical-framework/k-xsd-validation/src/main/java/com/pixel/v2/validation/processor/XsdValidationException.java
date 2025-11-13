package com.pixel.v2.validation.processor;

/**
 * Custom exception for XSD validation errors
 */
public class XsdValidationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public XsdValidationException(String message) {
        super(message);
    }
    
    public XsdValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}