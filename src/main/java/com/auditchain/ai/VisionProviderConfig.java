package com.auditchain.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class VisionProviderConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(VisionProvider.class)
    public VisionProvider defaultVisionProvider(MockVisionProvider mock) {
        return mock;
    }
}
