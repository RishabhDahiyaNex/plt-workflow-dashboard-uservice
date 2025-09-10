package com.nextuple.workflow.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {
    private String workflowId;
    private String workflowName;
    private String workflowType;
    private String status;
    private String description;
    private Map<String, Object> parameters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
