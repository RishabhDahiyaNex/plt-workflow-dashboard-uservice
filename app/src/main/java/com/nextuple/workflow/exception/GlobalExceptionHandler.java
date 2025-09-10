package com.nextuple.workflow.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WorkflowNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWorkflowNotFoundException(WorkflowNotFoundException ex) {
        log.error("Workflow not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Workflow not found", ex.getMessage());
    }

    @ExceptionHandler(WorkflowOperationException.class)
    public ResponseEntity<Map<String, Object>> handleWorkflowOperationException(WorkflowOperationException ex) {
        log.error("Workflow operation failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Workflow operation failed", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}
