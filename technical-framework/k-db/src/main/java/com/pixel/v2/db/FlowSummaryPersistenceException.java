package com.pixel.v2.db;

/**
 * Exception thrown when FlowSummary persistence operations fail.
 */
public class FlowSummaryPersistenceException extends RuntimeException {

    public FlowSummaryPersistenceException(String message) {
        super(message);
    }

    public FlowSummaryPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}