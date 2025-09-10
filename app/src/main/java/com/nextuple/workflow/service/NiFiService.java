package com.nextuple.workflow.service;

import com.nextuple.workflow.config.NiFiProperties;
import com.nextuple.workflow.exception.WorkflowOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NiFiService {

    private final RestTemplate restTemplate;
    private final NiFiProperties nifiProperties;

    public Map<String, Object> getRootProcessGroup() {
        try {
            String url = nifiProperties.getUrl() + "/flow/process-groups/root";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get root process group", e);
            throw new WorkflowOperationException("Failed to get root process group: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProcessGroupByName(String name) {
        try {
            Map<String, Object> rootGroup = getRootProcessGroup();
            Map<String, Object> processGroupFlow = (Map<String, Object>) rootGroup.get("processGroupFlow");
            Map<String, Object> flow = (Map<String, Object>) processGroupFlow.get("flow");
            
            if (flow != null && flow.containsKey("processGroups")) {
                for (Map<String, Object> pg : (Iterable<Map<String, Object>>) flow.get("processGroups")) {
                    Map<String, Object> component = (Map<String, Object>) pg.get("component");
                    if (component != null && name.equals(component.get("name"))) {
                        return pg;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get process group by name: {}", name, e);
            throw new WorkflowOperationException("Failed to get process group by name: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> createProcessGroup(String name, String parentGroupId) {
        try {
            String url = nifiProperties.getUrl() + "/process-groups/" + parentGroupId + "/process-groups";
            
            Map<String, Object> requestBody = Map.of(
                "revision", Map.of("version", 0),
                "component", Map.of(
                    "name", name,
                    "position", Map.of("x", 0, "y", 0)
                )
            );
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to create process group: {}", name, e);
            throw new WorkflowOperationException("Failed to create process group: " + e.getMessage(), e);
        }
    }

    public void startProcessGroup(String processGroupId) {
        try {
            String url = nifiProperties.getUrl() + "/flow/process-groups/" + processGroupId;
            
            Map<String, Object> requestBody = Map.of(
                "id", processGroupId,
                "state", "RUNNING"
            );
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            log.info("Started process group: {}", processGroupId);
        } catch (Exception e) {
            log.error("Failed to start process group: {}", processGroupId, e);
            throw new WorkflowOperationException("Failed to start process group: " + e.getMessage(), e);
        }
    }

    public void stopProcessGroup(String processGroupId) {
        try {
            String url = nifiProperties.getUrl() + "/flow/process-groups/" + processGroupId;
            
            Map<String, Object> requestBody = Map.of(
                "id", processGroupId,
                "state", "STOPPED"
            );
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            log.info("Stopped process group: {}", processGroupId);
        } catch (Exception e) {
            log.error("Failed to stop process group: {}", processGroupId, e);
            throw new WorkflowOperationException("Failed to stop process group: " + e.getMessage(), e);
        }
    }

    public void deleteProcessGroup(String processGroupId, long version) {
        try {
            String url = nifiProperties.getUrl() + "/process-groups/" + processGroupId + "?version=" + version;
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Deleted process group: {}", processGroupId);
        } catch (Exception e) {
            log.error("Failed to delete process group: {}", processGroupId, e);
            throw new WorkflowOperationException("Failed to delete process group: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProcessGroupStatus(String processGroupId) {
        try {
            String url = nifiProperties.getUrl() + "/flow/process-groups/" + processGroupId + "/status";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get process group status: {}", processGroupId, e);
            throw new WorkflowOperationException("Failed to get process group status: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (nifiProperties.getUsername() != null && nifiProperties.getPassword() != null) {
            String auth = nifiProperties.getUsername() + ":" + nifiProperties.getPassword();
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }
        return headers;
    }
}
