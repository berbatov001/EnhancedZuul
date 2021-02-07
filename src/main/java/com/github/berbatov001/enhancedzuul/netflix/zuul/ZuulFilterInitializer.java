package com.github.berbatov001.enhancedzuul.netflix.zuul;

import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.monitoring.CounterFactory;
import com.netflix.zuul.monitoring.TracerFactory;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.Map;

public class ZuulFilterInitializer {
    private final Map<String, ZuulFilter> filters;
    private final CounterFactory counterFactory;
    private final TracerFactory tracerFactory;
    private final FilterLoader filterLoader;
    private final FilterRegistry filterRegistry;

    public ZuulFilterInitializer(Map<String, ZuulFilter> filters, CounterFactory counterFactory, TracerFactory tracerFactory, FilterLoader filterLoader, FilterRegistry filterRegistry) {
        this.filters = filters;
        this.counterFactory = counterFactory;
        this.tracerFactory = tracerFactory;
        this.filterLoader = filterLoader;
        this.filterRegistry = filterRegistry;
    }

    @PostConstruct
    public void contextInitialized() {
        TracerFactory.initialize(tracerFactory);
        CounterFactory.initialize(counterFactory);
        for (Map.Entry<String, ZuulFilter> entry : this.filters.entrySet()) {
            filterRegistry.put(entry.getKey(), entry.getValue());
        }
    }

    @PreDestroy
    public void contextDestroyed() {
        for (Map.Entry<String, ZuulFilter> entry : this.filters.entrySet()) {
            filterRegistry.remove(entry.getKey());
        }
        clearLoaderCache();
        TracerFactory.initialize(null);
        CounterFactory.initialize(null);
    }

    private void clearLoaderCache() {
        Field field = ReflectionUtils.findField(FilterLoader.class, "hashFiltersByType");
        ReflectionUtils.makeAccessible(field);
        @SuppressWarnings("rawtypes")
        Map cache = (Map)ReflectionUtils.getField(field, filterLoader);
        if (cache != null) {
            cache.clear();
        }
    }
}
