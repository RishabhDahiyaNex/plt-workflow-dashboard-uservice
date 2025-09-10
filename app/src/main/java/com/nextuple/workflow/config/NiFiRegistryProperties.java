package com.nextuple.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nifi-registry")
public class NiFiRegistryProperties {
    private String url;
    private String username;
    private String password;
}
