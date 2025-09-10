package com.nextuple.workflow.model.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class WorkflowActionRequest {
    @NotBlank(message = "Workflow name is required")
    private String workflowName;
}
