package com.nextuple.workflow.service;

import com.nextuple.workflow.exception.WorkflowNotFoundException;
import com.nextuple.workflow.exception.WorkflowOperationException;
import com.nextuple.workflow.model.request.WorkflowConfigRequest;
import com.nextuple.workflow.model.response.WorkflowResponse;
import com.nextuple.workflow.model.response.WorkflowStatusResponse;
import com.nextuple.workflow.util.WorkflowParamPayloadMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final NiFiService nifiService;
    private final NiFiRegistryService registryService;
    private final WorkflowParamPayloadMapping parameterMapping;

    public WorkflowResponse configureWorkflow(WorkflowConfigRequest request) {
        log.info("Configuring workflow: {} of type: {}", request.getWorkflowName(), request.getWorkflowType());
        
        try {
            Map<String, Object> existingWorkflow = nifiService.getProcessGroupByName(request.getWorkflowName());
            if (existingWorkflow != null) {
                throw new WorkflowOperationException("Workflow with name '" + request.getWorkflowName() + "' already exists");
            }

            Map<String, Object> flowDefinition = registryService.getFlowByType(request.getWorkflowType());
            if (flowDefinition == null) {
                throw new WorkflowNotFoundException("No flow definition found for workflow type: " + request.getWorkflowType());
            }

            Map<String, Object> mappedParameters = parameterMapping.mapParametersForWorkflowType(
                request.getWorkflowType(), request.getParameters());

            Map<String, Object> rootGroup = nifiService.getRootProcessGroup();
            Map<String, Object> processGroupFlow = (Map<String, Object>) rootGroup.get("processGroupFlow");
            Map<String, Object> flow = (Map<String, Object>) processGroupFlow.get("flow");
            String rootGroupId = (String) flow.get("id");

            Map<String, Object> newProcessGroup = nifiService.createProcessGroup(request.getWorkflowName(), rootGroupId);
            Map<String, Object> component = (Map<String, Object>) newProcessGroup.get("component");
            String processGroupId = (String) component.get("id");

            log.info("Successfully configured workflow: {} with ID: {}", request.getWorkflowName(), processGroupId);

            return WorkflowResponse.builder()
                .workflowId(processGroupId)
                .workflowName(request.getWorkflowName())
                .workflowType(request.getWorkflowType())
                .status("CONFIGURED")
                .description(request.getDescription())
                .parameters(mappedParameters)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("Failed to configure workflow: {}", request.getWorkflowName(), e);
            throw new WorkflowOperationException("Failed to configure workflow: " + e.getMessage(), e);
        }
    }

    public void startWorkflow(String workflowName) {
        log.info("Starting workflow: {}", workflowName);
        
        Map<String, Object> processGroup = nifiService.getProcessGroupByName(workflowName);
        if (processGroup == null) {
            throw new WorkflowNotFoundException("Workflow not found: " + workflowName);
        }

        Map<String, Object> component = (Map<String, Object>) processGroup.get("component");
        String processGroupId = (String) component.get("id");

        nifiService.startProcessGroup(processGroupId);
        log.info("Successfully started workflow: {}", workflowName);
    }

    public void stopWorkflow(String workflowName) {
        log.info("Stopping workflow: {}", workflowName);
        
        Map<String, Object> processGroup = nifiService.getProcessGroupByName(workflowName);
        if (processGroup == null) {
            throw new WorkflowNotFoundException("Workflow not found: " + workflowName);
        }

        Map<String, Object> component = (Map<String, Object>) processGroup.get("component");
        String processGroupId = (String) component.get("id");

        nifiService.stopProcessGroup(processGroupId);
        log.info("Successfully stopped workflow: {}", workflowName);
    }

    public WorkflowResponse updateWorkflow(WorkflowConfigRequest request) {
        log.info("Updating workflow: {}", request.getWorkflowName());
        
        try {
            stopWorkflow(request.getWorkflowName());
        } catch (WorkflowNotFoundException e) {
        }

        try {
            deleteWorkflow(request.getWorkflowName());
        } catch (WorkflowNotFoundException e) {
        }

        return configureWorkflow(request);
    }

    public void deleteWorkflow(String workflowName) {
        log.info("Deleting workflow: {}", workflowName);
        
        Map<String, Object> processGroup = nifiService.getProcessGroupByName(workflowName);
        if (processGroup == null) {
            throw new WorkflowNotFoundException("Workflow not found: " + workflowName);
        }

        Map<String, Object> component = (Map<String, Object>) processGroup.get("component");
        String processGroupId = (String) component.get("id");

        try {
            nifiService.stopProcessGroup(processGroupId);
        } catch (Exception e) {
            log.warn("Failed to stop workflow before deletion: {}", e.getMessage());
        }

        Map<String, Object> revision = (Map<String, Object>) processGroup.get("revision");
        long version = ((Number) revision.get("version")).longValue();

        nifiService.deleteProcessGroup(processGroupId, version);
        log.info("Successfully deleted workflow: {}", workflowName);
    }

    public List<WorkflowResponse> listAllWorkflows() {
        log.info("Listing all workflows");
        
        List<WorkflowResponse> workflows = new ArrayList<>();
        Map<String, Object> rootGroup = nifiService.getRootProcessGroup();
        Map<String, Object> processGroupFlow = (Map<String, Object>) rootGroup.get("processGroupFlow");
        Map<String, Object> flow = (Map<String, Object>) processGroupFlow.get("flow");

        if (flow != null && flow.containsKey("processGroups")) {
            for (Map<String, Object> pg : (Iterable<Map<String, Object>>) flow.get("processGroups")) {
                Map<String, Object> component = (Map<String, Object>) pg.get("component");
                if (component != null) {
                    WorkflowResponse workflow = WorkflowResponse.builder()
                        .workflowId((String) component.get("id"))
                        .workflowName((String) component.get("name"))
                        .workflowType("UNKNOWN") // Type not available in process group info
                        .status(getProcessGroupStatusString((String) component.get("id")))
                        .description((String) component.get("comments"))
                        .build();
                    workflows.add(workflow);
                }
            }
        }

        log.info("Found {} workflows", workflows.size());
        return workflows;
    }

    public WorkflowResponse getWorkflowDetail(String workflowName) {
        log.info("Getting workflow detail for: {}", workflowName);
        
        Map<String, Object> processGroup = nifiService.getProcessGroupByName(workflowName);
        if (processGroup == null) {
            throw new WorkflowNotFoundException("Workflow not found: " + workflowName);
        }

        Map<String, Object> component = (Map<String, Object>) processGroup.get("component");
        String processGroupId = (String) component.get("id");

        return WorkflowResponse.builder()
            .workflowId(processGroupId)
            .workflowName((String) component.get("name"))
            .workflowType("UNKNOWN") // Type not available in process group info
            .status(getProcessGroupStatusString(processGroupId))
            .description((String) component.get("comments"))
            .build();
    }

    public WorkflowStatusResponse getWorkflowStatus(String workflowName) {
        log.info("Getting workflow status for: {}", workflowName);
        
        Map<String, Object> processGroup = nifiService.getProcessGroupByName(workflowName);
        if (processGroup == null) {
            throw new WorkflowNotFoundException("Workflow not found: " + workflowName);
        }

        Map<String, Object> component = (Map<String, Object>) processGroup.get("component");
        String processGroupId = (String) component.get("id");

        Map<String, Object> statusResponse = nifiService.getProcessGroupStatus(processGroupId);
        Map<String, Object> processGroupStatus = (Map<String, Object>) statusResponse.get("processGroupStatus");

        return WorkflowStatusResponse.builder()
            .workflowName(workflowName)
            .status((String) processGroupStatus.get("name"))
            .state((String) processGroupStatus.get("runStatus"))
            .runningCount(((Number) processGroupStatus.get("runningCount")).intValue())
            .stoppedCount(((Number) processGroupStatus.get("stoppedCount")).intValue())
            .invalidCount(((Number) processGroupStatus.get("invalidCount")).intValue())
            .disabledCount(((Number) processGroupStatus.get("disabledCount")).intValue())
            .activeThreadCount(((Number) processGroupStatus.get("activeThreadCount")).intValue())
            .build();
    }

    private String getProcessGroupStatusString(String processGroupId) {
        try {
            Map<String, Object> statusResponse = nifiService.getProcessGroupStatus(processGroupId);
            Map<String, Object> processGroupStatus = (Map<String, Object>) statusResponse.get("processGroupStatus");
            return (String) processGroupStatus.get("runStatus");
        } catch (Exception e) {
            log.warn("Failed to get status for process group: {}", processGroupId, e);
            return "UNKNOWN";
        }
    }
}
