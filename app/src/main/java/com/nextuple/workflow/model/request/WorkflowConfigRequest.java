package com.nextuple.workflow.model.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
public class WorkflowConfigRequest {
    @NotBlank(message = "Workflow name is required")
    private String workflowName;
    
    @NotBlank(message = "Workflow type is required")
    private String workflowType;
    
    @NotNull(message = "Parameters are required")
    private Map<String, Object> parameters;
    
    private String description;
}
