package com.nextuple.workflow.service;

import com.nextuple.workflow.config.NiFiRegistryProperties;
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
public class NiFiRegistryService {

    private final RestTemplate restTemplate;
    private final NiFiRegistryProperties registryProperties;

    public Map<String, Object> getFlowByType(String workflowType) {
        try {
            String url = registryProperties.getUrl() + "/buckets";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map[].class);
            Map[] buckets = response.getBody();
            
            if (buckets != null) {
                for (Map<String, Object> bucket : buckets) {
                    Map<String, Object> flow = getFlowFromBucket((String) bucket.get("identifier"), workflowType);
                    if (flow != null) {
                        return flow;
                    }
                }
            }
            
            log.warn("No flow found for workflow type: {}", workflowType);
            return null;
        } catch (Exception e) {
            log.error("Failed to get flow by type: {}", workflowType, e);
            throw new WorkflowOperationException("Failed to get flow by type: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getFlowFromBucket(String bucketId, String workflowType) {
        try {
            String url = registryProperties.getUrl() + "/buckets/" + bucketId + "/flows";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map[].class);
            Map[] flows = response.getBody();
            
            if (flows != null) {
                for (Map<String, Object> flow : flows) {
                    if (workflowType.equals(flow.get("name")) || 
                        workflowType.equals(flow.get("type")) ||
                        ((String) flow.get("name")).contains(workflowType)) {
                        return getLatestFlowVersion(bucketId, (String) flow.get("identifier"));
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get flows from bucket: {}", bucketId, e);
            return null;
        }
    }

    private Map<String, Object> getLatestFlowVersion(String bucketId, String flowId) {
        try {
            String url = registryProperties.getUrl() + "/buckets/" + bucketId + "/flows/" + flowId + "/versions/latest";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get latest flow version for bucket: {}, flow: {}", bucketId, flowId, e);
            throw new WorkflowOperationException("Failed to get latest flow version: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getFlowContent(String bucketId, String flowId, int version) {
        try {
            String url = registryProperties.getUrl() + "/buckets/" + bucketId + "/flows/" + flowId + "/versions/" + version + "/export";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get flow content for bucket: {}, flow: {}, version: {}", bucketId, flowId, version, e);
            throw new WorkflowOperationException("Failed to get flow content: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (registryProperties.getUsername() != null && registryProperties.getPassword() != null) {
            String auth = registryProperties.getUsername() + ":" + registryProperties.getPassword();
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }
        return headers;
    }
}
