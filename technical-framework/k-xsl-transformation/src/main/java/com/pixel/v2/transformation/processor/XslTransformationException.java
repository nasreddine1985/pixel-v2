package com.pixel.v2.transformation.processor;

/**
 * Custom exception for XSL transformation errors
 */
public class XslTransformationException extends Exception {
    
    public XslTransformationException(String message) {
        super(message);
    }
    
    public XslTransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}