package com.github.berbatov001.enhancedzuul.netflix.zuul.listener;

import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.github.berbatov001.enhancedribbon.util.ApplicationContextHolder;
import com.github.berbatov001.enhancedzuul.netflix.zuul.support.ZuulProperties;
import com.github.berbatov001.enhancedzuul.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * 该监听器负责监听Nacos配置中心中所有以zuul.开头属性的变化
 *
 * @author berbatov001
 */
public class ZuulPropertiesNacosListener implements ApplicationListener<NacosConfigReceivedEvent> {
    @Override
    public void onApplicationEvent(@NonNull NacosConfigReceivedEvent nacosConfigReceivedEvent) {
        //获取Bean工厂
        AnnotationConfigServletWebServerApplicationContext context = (AnnotationConfigServletWebServerApplicationContext) ApplicationContextHolder.getApplicationContext();
        ConfigurableListableBeanFactory configurableListableBeanFactory = context.getBeanFactory();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) configurableListableBeanFactory;

        //获取目前在Bean工厂中的ZuulProperties实例
        String[] zuulPropertiesBeanNames = beanFactory.getBeanNamesForType(ZuulProperties.class);
        String zuulPropertiesBeanName = zuulPropertiesBeanNames[0];
        ZuulProperties zuulProperties = (ZuulProperties)beanFactory.getBean(zuulPropertiesBeanName);

        //刷新ZuulProperties实例的各个属性
        ConfigurationPropertiesBindingPostProcessor configurationPropertiesBindingPostProcessor = beanFactory.getBean(ConfigurationPropertiesBindingPostProcessor.class);
        configurationPropertiesBindingPostProcessor.postProcessBeforeInitialization(zuulProperties, zuulPropertiesBeanName);

        //刷新网关的路由策略
        ZuulHandlerMapping zuulHandlerMapping = beanFactory.getBean(ZuulHandlerMapping.class);
        zuulHandlerMapping.registerHandler(zuulProperties);
    }
}
