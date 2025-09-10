package com.nextuple.workflow.controller;

import com.nextuple.workflow.model.request.WorkflowActionRequest;
import com.nextuple.workflow.model.request.WorkflowConfigRequest;
import com.nextuple.workflow.model.response.WorkflowResponse;
import com.nextuple.workflow.model.response.WorkflowStatusResponse;
import com.nextuple.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/configure")
    public ResponseEntity<WorkflowResponse> configureWorkflow(@Valid @RequestBody WorkflowConfigRequest request) {
        log.info("Received request to configure workflow: {}", request.getWorkflowName());
        WorkflowResponse response = workflowService.configureWorkflow(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startWorkflow(@Valid @RequestBody WorkflowActionRequest request) {
        log.info("Received request to start workflow: {}", request.getWorkflowName());
        workflowService.startWorkflow(request.getWorkflowName());
        return ResponseEntity.ok(Map.of(
            "message", "Workflow started successfully",
            "workflowName", request.getWorkflowName()
        ));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopWorkflow(@Valid @RequestBody WorkflowActionRequest request) {
        log.info("Received request to stop workflow: {}", request.getWorkflowName());
        workflowService.stopWorkflow(request.getWorkflowName());
        return ResponseEntity.ok(Map.of(
            "message", "Workflow stopped successfully",
            "workflowName", request.getWorkflowName()
        ));
    }

    @PutMapping("/update")
    public ResponseEntity<WorkflowResponse> updateWorkflow(@Valid @RequestBody WorkflowConfigRequest request) {
        log.info("Received request to update workflow: {}", request.getWorkflowName());
        WorkflowResponse response = workflowService.updateWorkflow(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workflowName}")
    public ResponseEntity<Map<String, String>> deleteWorkflow(@PathVariable String workflowName) {
        log.info("Received request to delete workflow: {}", workflowName);
        workflowService.deleteWorkflow(workflowName);
        return ResponseEntity.ok(Map.of(
            "message", "Workflow deleted successfully",
            "workflowName", workflowName
        ));
    }

    @GetMapping
    public ResponseEntity<List<WorkflowResponse>> listAllWorkflows() {
        log.info("Received request to list all workflows");
        List<WorkflowResponse> workflows = workflowService.listAllWorkflows();
        return ResponseEntity.ok(workflows);
    }

    @GetMapping("/{workflowName}/detail")
    public ResponseEntity<WorkflowResponse> getWorkflowDetail(@PathVariable String workflowName) {
        log.info("Received request to get workflow detail: {}", workflowName);
        WorkflowResponse response = workflowService.getWorkflowDetail(workflowName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workflowName}/status")
    public ResponseEntity<WorkflowStatusResponse> getWorkflowStatus(@PathVariable String workflowName) {
        log.info("Received request to get workflow status: {}", workflowName);
        WorkflowStatusResponse response = workflowService.getWorkflowStatus(workflowName);
        return ResponseEntity.ok(response);
    }
}
