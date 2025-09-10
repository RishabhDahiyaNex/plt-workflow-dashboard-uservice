package com.nextuple.workflow.exception;

public class WorkflowOperationException extends RuntimeException {
    public WorkflowOperationException(String message) {
        super(message);
    }
    
    public WorkflowOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
