package com.github.berbatov001.envolvedzuul.sentinel;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelAspectAutoConfiguration {

    private SentinelProperties sentinelProperties;

    @Autowired
    public SentinelAspectAutoConfiguration(SentinelProperties sentinelProperties) {
        this.sentinelProperties = sentinelProperties;
    }

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @Bean
    public SentinelRulesInitializer sentinelRulesInitializer() {
        return new SentinelRulesInitializer(sentinelProperties);
    }
}
