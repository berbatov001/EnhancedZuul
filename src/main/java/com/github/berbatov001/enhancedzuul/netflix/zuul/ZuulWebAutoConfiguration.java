package com.github.berbatov001.enhancedzuul.netflix.zuul;

import com.github.berbatov001.enhancedzuul.netflix.zuul.filters.post.WriteResponseFilter;
import com.github.berbatov001.enhancedzuul.netflix.zuul.filters.pre.PreDecorationFilter;
import com.github.berbatov001.enhancedzuul.netflix.zuul.filters.route.RibbonRoutingFilter;
import com.github.berbatov001.enhancedzuul.netflix.zuul.metrics.EmptyCounterFactory;
import com.github.berbatov001.enhancedzuul.netflix.zuul.metrics.EmptyTracerFactory;
import com.github.berbatov001.enhancedzuul.netflix.zuul.support.ZuulProperties;
import com.github.berbatov001.enhancedzuul.netflix.zuul.web.FilterRouteLocator;
import com.github.berbatov001.enhancedzuul.netflix.zuul.web.ZuulController;
import com.github.berbatov001.enhancedzuul.netflix.zuul.web.ZuulHandlerMapping;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.monitoring.CounterFactory;
import com.netflix.zuul.monitoring.TracerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties({ZuulProperties.class})
public class ZuulWebAutoConfiguration {

    private ZuulProperties zuulProperties;

    @Autowired
    public ZuulWebAutoConfiguration(ZuulProperties zuulProperties) {
        this.zuulProperties = zuulProperties;
    }

    @Bean
    public ZuulController zuulController() {
        return new ZuulController();
    }

    @Bean
    public ZuulHandlerMapping zuulHandlerMapping() {
        return new ZuulHandlerMapping(filterRouterLocator(), zuulController(), this.zuulProperties);
    }

    @Bean
    public PreDecorationFilter perDecorationFilter() {
        return new PreDecorationFilter(zuulProperties, filterRouterLocator());
    }

    @Bean
    public RibbonRoutingFilter ribbonRoutingFilter() {
        return new RibbonRoutingFilter(zuulProperties);
    }

    @Bean
    public WriteResponseFilter writeResponseFilter() {
        return new WriteResponseFilter(zuulProperties);
    }

    @Bean
    public FilterRouteLocator filterRouterLocator() {
        return new FilterRouteLocator(zuulProperties);
    }

    @Configuration
    public static class ZuulConfiguration {
        @Autowired
        private Map<String, ZuulFilter> filters;

        @Bean
        public ZuulFilterInitializer zuulFilterInitializer(CounterFactory counterFactory, TracerFactory tracerFactory) {
            FilterLoader filterLoader = FilterLoader.getInstance();
            FilterRegistry filterRegistry = FilterRegistry.instance();
            return new ZuulFilterInitializer(this.filters, counterFactory, tracerFactory, filterLoader, filterRegistry);
        }

        @Bean
        public CounterFactory counterFactory() {
            return new EmptyCounterFactory();
        }

        @Bean
        public TracerFactory tracerFactory() {
            return new EmptyTracerFactory();
        }
    }
}
