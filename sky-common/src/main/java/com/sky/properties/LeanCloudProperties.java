package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "sky.leancloud")
public class LeanCloudProperties {
    private String appId;
    private String appKey;
    private String serverUrl;
}
