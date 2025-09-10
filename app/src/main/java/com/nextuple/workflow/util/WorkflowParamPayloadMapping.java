package com.nextuple.workflow.util;

import com.nextuple.workflow.config.AzureStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowParamPayloadMapping {

    private final AzureStorageProperties azureStorageProperties;

    public Map<String, Object> mapParametersForWorkflowType(String workflowType, Map<String, Object> inputParameters) {
        log.debug("Mapping parameters for workflow type: {}", workflowType);
        
        Map<String, Object> mappedParameters = new HashMap<>(inputParameters);
        
        mappedParameters.put("azure.container.name", azureStorageProperties.getContainerName());
        mappedParameters.put("azure.account.name", azureStorageProperties.getAccountName());
        mappedParameters.put("azure.account.key", azureStorageProperties.getAccountKey());
        
        switch (workflowType.toUpperCase()) {
            case "CONFIGURATION_MIGRATION":
                return mapConfigurationMigrationParameters(mappedParameters);
            case "CONFIGURATION_MIGRATION_EXPORT":
                return mapConfigurationMigrationExportParameters(mappedParameters);
            case "CONFIGURATION_MIGRATION_IMPORT":
                return mapConfigurationMigrationImportParameters(mappedParameters);
            case "SETUP_INITIALIZATION":
                return mapSetupInitializationParameters(mappedParameters);
            default:
                log.warn("Unknown workflow type: {}. Using default parameter mapping.", workflowType);
                return mappedParameters;
        }
    }

    private Map<String, Object> mapConfigurationMigrationParameters(Map<String, Object> parameters) {
        parameters.putIfAbsent("migration.source.path", "/config/source");
        parameters.putIfAbsent("migration.target.path", "/config/target");
        parameters.putIfAbsent("migration.backup.enabled", true);
        parameters.putIfAbsent("migration.validation.enabled", true);
        return parameters;
    }

    private Map<String, Object> mapConfigurationMigrationExportParameters(Map<String, Object> parameters) {
        parameters.putIfAbsent("export.format", "JSON");
        parameters.putIfAbsent("export.compression.enabled", true);
        parameters.putIfAbsent("export.include.metadata", true);
        parameters.putIfAbsent("export.batch.size", 1000);
        return parameters;
    }

    private Map<String, Object> mapConfigurationMigrationImportParameters(Map<String, Object> parameters) {
        parameters.putIfAbsent("import.validation.strict", true);
        parameters.putIfAbsent("import.conflict.resolution", "MERGE");
        parameters.putIfAbsent("import.rollback.enabled", true);
        parameters.putIfAbsent("import.batch.size", 500);
        return parameters;
    }

    private Map<String, Object> mapSetupInitializationParameters(Map<String, Object> parameters) {
        parameters.putIfAbsent("setup.environment", "PRODUCTION");
        parameters.putIfAbsent("setup.create.defaults", true);
        parameters.putIfAbsent("setup.validate.dependencies", true);
        parameters.putIfAbsent("setup.timeout.minutes", 30);
        return parameters;
    }
}
