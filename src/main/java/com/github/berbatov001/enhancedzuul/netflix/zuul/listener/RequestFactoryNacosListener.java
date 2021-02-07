package com.github.berbatov001.enhancedzuul.netflix.zuul.listener;

import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.github.berbatov001.enhancedribbon.client.support.CustomizeClientHttpRequestFactory;
import com.github.berbatov001.enhancedribbon.client.support.RestTemplateConnectionProperties;
import com.github.berbatov001.enhancedribbon.util.ApplicationContextHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * 该监听器负责监听Nacos配置中心中所有以rest-template.开头属性的变化
 *
 * @author berbatov001
 */
public class RequestFactoryNacosListener implements ApplicationListener<NacosConfigReceivedEvent> {

    @Override
    public void onApplicationEvent(@NonNull NacosConfigReceivedEvent nacosConfigReceivedEvent) {
        //获取Bean工厂
        AnnotationConfigServletWebServerApplicationContext context = (AnnotationConfigServletWebServerApplicationContext) ApplicationContextHolder.getApplicationContext();
        ConfigurableListableBeanFactory configurableListableBeanFactory = context.getBeanFactory();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) configurableListableBeanFactory;

        String[] customizedClientHttpRequestFactoryNames = beanFactory.getBeanNamesForType(CustomizeClientHttpRequestFactory.class);
        String[] restTemplateConnectionPropertiesNames = beanFactory.getBeanNamesForType(RestTemplateConnectionProperties.class);
        if (customizedClientHttpRequestFactoryNames.length > 0 && restTemplateConnectionPropertiesNames.length > 0) {
            String customizedClientHttpRequestFactoryName = customizedClientHttpRequestFactoryNames[0];
            CustomizeClientHttpRequestFactory customizeClientHttpRequestFactory = (CustomizeClientHttpRequestFactory) beanFactory.getBean(customizedClientHttpRequestFactoryName);

            String restTemplateConnectionPropertiesName = restTemplateConnectionPropertiesNames[0];
            RestTemplateConnectionProperties restTemplateConnectionProperties = (RestTemplateConnectionProperties) beanFactory.getBean(restTemplateConnectionPropertiesName);

            //刷新RestTemplateConnectionProperties实例的各属性。
            ConfigurationPropertiesBindingPostProcessor configurationPropertiesBindingPostProcessor = beanFactory.getBean(ConfigurationPropertiesBindingPostProcessor.class);
            configurationPropertiesBindingPostProcessor.postProcessBeforeInitialization(restTemplateConnectionProperties, restTemplateConnectionPropertiesName);

            customizeClientHttpRequestFactory.enhance(restTemplateConnectionProperties);
        }
    }
}
