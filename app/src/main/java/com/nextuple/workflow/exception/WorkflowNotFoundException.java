package com.nextuple.workflow.exception;

public class WorkflowNotFoundException extends RuntimeException {
    public WorkflowNotFoundException(String message) {
        super(message);
    }
    
    public WorkflowNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
