package com.github.berbatov001.enhancedzuul.netflix.zuul.filters;

import com.github.berbatov001.enhancedzuul.netflix.zuul.support.ZuulProperties;
import com.netflix.zuul.context.RequestContext;

import java.util.Collection;

public class RequestHelper {

    private static final String IGNORED_HEADERS = "ignoredHeaders";

    private ZuulProperties zuulProperties;

    public RequestHelper(ZuulProperties zuulProperties) {
        this.zuulProperties = zuulProperties;
    }

    public boolean isIncludedHeader(String headerName) {
        String name = headerName.toLowerCase();
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.containsKey(IGNORED_HEADERS)) {
            Object object = ctx.get(IGNORED_HEADERS);
            if (object instanceof Collection && ((Collection<?>) object).contains(name)) {
                return false;
            }
        }
        switch (name) {
            case "host":
                if (zuulProperties.isAddHostHeader()) {
                    return true;
                }
            case "connection":
            case "content-length":
            case "server":
            case "transfer-encoding":
            case "x-application-context":
                return false;
            default:
                return true;
        }
    }
}
