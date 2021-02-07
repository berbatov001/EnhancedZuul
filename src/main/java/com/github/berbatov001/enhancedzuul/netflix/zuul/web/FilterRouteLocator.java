package com.github.berbatov001.enhancedzuul.netflix.zuul.web;

import com.github.berbatov001.enhancedzuul.netflix.zuul.support.ZuulProperties;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class FilterRouteLocator {

    private Map<String, ZuulProperties.ZuulRoute> routes = new HashMap<>();
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final ZuulProperties properties;

    public FilterRouteLocator(ZuulProperties zuulProperties) {
        this.properties = zuulProperties;
    }

    void setRoutes(Map<String, ZuulProperties.ZuulRoute> routes) {
        this.routes = routes;
    }

    public Route getMatchingRoute(String path) {
        for (Map.Entry<String, ZuulProperties.ZuulRoute> entry : routes.entrySet()) {
            String pattern = entry.getKey();
            if (this.pathMatcher.match(pattern, path)) {
                ZuulProperties.ZuulRoute zuulRoute = entry.getValue();
                return getRoute(zuulRoute, path);
            }
        }
        return null;
    }

    Route getRoute(ZuulProperties.ZuulRoute route, String path) {
        if (route == null) {
            return null;
        }
        String targetPath = path;
        String prefix = this.properties.getPrefix();
        //如果用户配置的prefix没有前置"/"，默认加上，否则路由不到。
        if (StringUtils.hasText(prefix)) {
            if (!prefix.startsWith("/")) {
                prefix = "/" + prefix;
            }
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        if (path.startsWith(prefix + "/") && this.properties.isStripPrefix()) {
            targetPath = path.substring(prefix.length());
        }
        if (route.isStripPrefix()) {
            int index = route.getPath().indexOf("*") - 1;
            if (index > 0) {
                String routePrefix = route.getPath().substring(0, index);
                targetPath = targetPath.replaceFirst(routePrefix, "");
                prefix = prefix + routePrefix;
            }
        }
        boolean retryable = this.properties.isRetryable();
        if (route.isRetryable() != null) {
            retryable = route.isRetryable();
        }
        return new Route(route.getId(), targetPath, route.getLocation(), prefix, retryable, route.isCustomSensitiveHeaders() ? route.getSensitiveHeaders() : null, route.isStripPrefix());
    }
}
