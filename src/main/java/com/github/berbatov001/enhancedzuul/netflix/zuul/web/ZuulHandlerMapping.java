package com.github.berbatov001.enhancedzuul.netflix.zuul.web;

import com.github.berbatov001.enhancedzuul.netflix.zuul.support.ZuulProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

public class ZuulHandlerMapping extends AbstractUrlHandlerMapping implements EnvironmentAware {

    private static Logger LOGGER = LoggerFactory.getLogger(ZuulHandlerMapping.class);

    private final FilterRouteLocator routeLocator;

    private final ZuulController zuulController;

    private final ZuulProperties zuulProperties;

    private Map<String, Object> handlerMap;

    private volatile boolean virgin = true;

    private Environment environment;

    public ZuulHandlerMapping(FilterRouteLocator routeLocator, ZuulController zuulController, ZuulProperties zuulProperties) {
        this.routeLocator = routeLocator;
        this.zuulController = zuulController;
        this.zuulProperties = zuulProperties;
        setOrder(-200);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected Object lookupHandler(String urlPath, HttpServletRequest request) throws Exception {
        if (virgin) {
            synchronized (this) {
                if (virgin) {
                    registerHandler(zuulProperties);
                    virgin = false;
                }
            }
        }
        return lookupInInternal(urlPath, request);
    }

    private Object lookupInInternal(String urlPath, HttpServletRequest request) throws Exception {
        Object handler = this.handlerMap.get(urlPath);
        if (handler != null) {
            if (handler instanceof String) {
                String handlerName = (String) handler;
                handler = obtainApplicationContext().getBean(handlerName);
            }
            validateHandler(handler, request);
            return buildPathExposingHandler(handler, urlPath, urlPath, null);
        }
        List<String> matchingPatterns = new ArrayList<>();
        for (String registeredPattern : this.handlerMap.keySet()) {
            if (getPathMatcher().match(registeredPattern, urlPath)) {
                matchingPatterns.add(registeredPattern);
            } else if (useTrailingSlashMatch()) {
                if (!registeredPattern.endsWith("/") && getPathMatcher().match(registeredPattern + "/", urlPath)) {
                    matchingPatterns.add(registeredPattern + "/");
                }
            }
        }

        String bestMatch = null;
        Comparator<String> patternComparator = getPathMatcher().getPatternComparator(urlPath);
        if (!matchingPatterns.isEmpty()) {
            matchingPatterns.sort(patternComparator);
            if (LOGGER.isTraceEnabled() && matchingPatterns.size() > 1) {
                LOGGER.trace("Matching patterns " + matchingPatterns);
            }
            bestMatch = matchingPatterns.get(0);
        }
        if (bestMatch != null) {
            handler = this.handlerMap.get(bestMatch);
            if (handler == null) {
                if (bestMatch.endsWith("/")) {
                    handler = this.handlerMap.get(bestMatch.substring(0, bestMatch.length() - 1));
                }
                if (handler == null) {
                    throw new IllegalStateException("没有合适的handler来处理【" + bestMatch + "】。");
                }
                if (handler instanceof String) {
                    String handlerName = (String) handler;
                    handler = obtainApplicationContext().getBean(handlerName);
                }
                validateHandler(handler, request);
                String pathWithinMapping = getPathMatcher().extractPathWithinPattern(bestMatch, urlPath);

                Map<String, String> uriTemplateVariables = new LinkedHashMap<>();
                for (String matchingPattern : matchingPatterns) {
                    if (patternComparator.compare(bestMatch, matchingPattern) == 0) {
                        Map<String, String> vars = getPathMatcher().extractUriTemplateVariables(matchingPattern, urlPath);
                        Map<String, String> decodeVars = getUrlPathHelper().decodePathVariables(request, vars);
                        uriTemplateVariables.putAll(decodeVars);
                    }
                }
                if (LOGGER.isTraceEnabled() && uriTemplateVariables.size() > 0) {
                    LOGGER.trace("URI variables " + uriTemplateVariables);
                }
                return buildPathExposingHandler(handler, bestMatch, pathWithinMapping, uriTemplateVariables);
            }
        }
        return null;
    }

    public void registerHandler(ZuulProperties zuulProperties) {
        try {
            Method method = ZuulController.class.getMethod("handleRequest", HttpServletRequest.class, HttpServletResponse.class);
            HandlerMethod handlerMethod = new HandlerMethod(this.zuulController, method);
            Map<String, Object> newHandlerMap = new LinkedHashMap<>(16);
            Map<String, ZuulProperties.ZuulRoute> newRoutes = new HashMap<>(16);

            zuulProperties.getRoutes().forEach((key, zuulRoute) -> {
                Route route = convertZuulRouteToRoute(key, zuulRoute);
                if (route != null) {
                    if (environment.containsProperty("zuul.routes." + key + ".path")) {
                        String fullPath = route.getFullPath();
                        newHandlerMap.put(fullPath, handlerMethod);
                        newRoutes.put(fullPath, zuulRoute);
                    }
                }
            });
            this.handlerMap = newHandlerMap;
            this.routeLocator.setRoutes(newRoutes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Route convertZuulRouteToRoute(String key, ZuulProperties.ZuulRoute zuulRoute) {
        String path = zuulRoute.getPath();
        if (!StringUtils.hasText(path)) {
            return null;
        }
        if (!StringUtils.hasText(zuulRoute.getLocation())) {
            zuulRoute.setServiceId(key);
        }
        if (!StringUtils.hasText(zuulRoute.getId())) {
            zuulRoute.setId(key);
        }
        return routeLocator.getRoute(zuulRoute, path);
    }
}
