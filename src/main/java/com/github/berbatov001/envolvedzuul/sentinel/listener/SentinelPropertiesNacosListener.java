package com.github.berbatov001.envolvedzuul.sentinel.listener;

import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.github.berbatov001.envolvedribbon.util.ApplicationContextHolder;
import com.github.berbatov001.envolvedzuul.sentinel.SentinelRulesInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * 该监听器负责监听Nacos配置中心中所有以sentinel.开头属性的变化
 *
 * @author berbatov001
 */
public class SentinelPropertiesNacosListener implements ApplicationListener<NacosConfigReceivedEvent> {
    @Override
    public void onApplicationEvent(@NonNull NacosConfigReceivedEvent nacosConfigReceivedEvent) {
        SentinelRulesInitializer sentinelRulesInitializer = ApplicationContextHolder.getApplicationContext().getBean(SentinelRulesInitializer.class);
        sentinelRulesInitializer.initFlowRules();
    }
}
