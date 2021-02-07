package com.github.berbatov001.enhancedzuul.grayscale.listener;

import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.github.berbatov001.enhancedribbon.util.ApplicationContextHolder;
import com.github.berbatov001.enhancedzuul.grayscale.support.GrayScaleProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * 该监听器负责监听Nacos配置中心中所有以gray.开头属性的变化
 *
 * @author berbatov001
 */
public class GrayScalePropertiesNacosListener implements ApplicationListener<NacosConfigReceivedEvent> {
    @Override
    public void onApplicationEvent(@NonNull NacosConfigReceivedEvent nacosConfigReceivedEvent) {
        GrayScaleProperties grayScaleProperties = ApplicationContextHolder.getApplicationContext().getBean(GrayScaleProperties.class);
        grayScaleProperties.freshRouters();
    }
}
