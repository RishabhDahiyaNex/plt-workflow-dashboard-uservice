package com.nextuple.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "azure.storage")
public class AzureStorageProperties {
    private String containerName;
    private String accountName;
    private String accountKey;
}
