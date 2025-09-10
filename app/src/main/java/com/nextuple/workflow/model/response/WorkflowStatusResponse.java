package com.nextuple.workflow.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStatusResponse {
    private String workflowName;
    private String status;
    private String state;
    private Integer runningCount;
    private Integer stoppedCount;
    private Integer invalidCount;
    private Integer disabledCount;
    private Integer activeThreadCount;
}
