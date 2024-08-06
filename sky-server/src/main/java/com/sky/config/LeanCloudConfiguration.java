package com.sky.config;

import com.sky.properties.LeanCloudProperties;
import com.sky.utils.LeanCloudUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LeanCloudConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public LeanCloudUtil leanCloudUtil(LeanCloudProperties leanCloudProperties) {
        log.info("开始创建LeanCloud文件上传公共对象");
        return new LeanCloudUtil(
                leanCloudProperties.getAppId(),
                leanCloudProperties.getAppKey(),
                leanCloudProperties.getServerUrl());
    }
}
