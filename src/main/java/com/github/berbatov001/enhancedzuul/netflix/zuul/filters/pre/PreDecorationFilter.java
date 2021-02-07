package com.github.berbatov001.enhancedzuul.netflix.zuul.filters.pre;

import com.github.berbatov001.enhancedzuul.netflix.zuul.support.FilterConstants;
import com.github.berbatov001.enhancedzuul.netflix.zuul.support.ZuulProperties;
import com.github.berbatov001.enhancedzuul.netflix.zuul.web.FilterRouteLocator;
import com.github.berbatov001.enhancedzuul.netflix.zuul.web.Route;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;


public class PreDecorationFilter extends ZuulFilter {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private final FilterRouteLocator fileRouteLocator;

    private final ZuulProperties zuulProperties;

    public PreDecorationFilter(ZuulProperties zuulProperties, FilterRouteLocator fileRouteLocator) {
        this.zuulProperties = zuulProperties;
        this.urlPathHelper.setRemoveSemicolonContent(zuulProperties.isDecodeUrl());
        this.fileRouteLocator = fileRouteLocator;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return !ctx.containsKey(FilterConstants.FORWARD_TO_KEY) && !ctx.containsKey(FilterConstants.SERVICE_ID_KEY);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        final String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
        Route route = fileRouteLocator.getMatchingRoute(requestURI);
        if (route != null) {
            if (route.getRetryable() != null) {
                ctx.put(FilterConstants.RETRYABLE_KEY, route.getRetryable());
            }
            String location = route.getLocation();
            if (location != null) {
                ctx.put(FilterConstants.REQUEST_URI_KEY, route.getPath());
                ctx.put(FilterConstants.PROXY_KEY, route.getId());
                if (location.startsWith(FilterConstants.HTTP_SCHEME + FilterConstants.THE_COLON_MARK) || location.startsWith(FilterConstants.HTTPS_SCHEME + FilterConstants.THE_COLON_MARK)) {
                    ctx.setRouteHost(getUrl(location));
                    ctx.addOriginResponseHeader(FilterConstants.SERVICE_HEADER, location);
                } else if (location.startsWith(FilterConstants.FORWARD_LOCATION_PREFIX)) {
                    ctx.set(FilterConstants.FORWARD_TO_KEY, StringUtils.cleanPath(location.substring(FilterConstants.FORWARD_LOCATION_PREFIX.length()) + route.getPath()));
                    ctx.setRouteHost(null);
                    return null;
                } else {
                    ctx.set(FilterConstants.SERVICE_ID_KEY, location);
                    ctx.setRouteHost(null);
                    ctx.addOriginResponseHeader(FilterConstants.SERVICE_ID_HEADER, location);
                }
            }
            if (this.zuulProperties.isAddProxyHeaders()) {
                addProxyHeaders(ctx, route);
                String xForwardedFor = ctx.getRequest().getHeader(FilterConstants.X_FORWARDED_FOR_HEADER);
                String remoteAddr = ctx.getRequest().getRemoteAddr();
                if (xForwardedFor == null) {
                    xForwardedFor = remoteAddr;
                }
                ctx.addZuulRequestHeader(FilterConstants.X_FORWARDED_FOR_HEADER, xForwardedFor);
            }
            if (this.zuulProperties.isAddHostHeader()) {
                ctx.addZuulRequestHeader(HttpHeaders.HOST, toHostHeader(ctx.getRequest()));
            }
        }
        return null;
    }

    private void addProxyHeaders(RequestContext ctx, Route route) {
        HttpServletRequest request = ctx.getRequest();
        String host = toHostHeader(request);
        String port = String.valueOf(request.getServletPath());
        String proto = request.getScheme();
        if (hasHeader(request, FilterConstants.X_FORWARDED_HOST_HEADER)) {
            host = request.getHeader(FilterConstants.X_FORWARDED_HOST_HEADER) + "," + host;
        }
        if (!hasHeader(request, FilterConstants.X_FORWARDED_PORT_HEADER)) {
            if (hasHeader(request, FilterConstants.X_FORWARDED_PROTO_HEADER)) {
                StringBuilder builder = new StringBuilder();
                for (String previous : StringUtils.commaDelimitedListToStringArray(request.getHeader(FilterConstants.X_FORWARDED_PROTO_HEADER))) {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(FilterConstants.HTTPS_SCHEME.equals(previous) ? FilterConstants.HTTPS_PORT : FilterConstants.HTTP_PORT);
                }
                builder.append(",").append(port);
                port = builder.toString();
            }
        } else {
            port = request.getHeader(FilterConstants.X_FORWARDED_PORT_HEADER) + "," + port;
        }
        if (hasHeader(request, FilterConstants.X_FORWARDED_PROTO_HEADER)) {
            proto = request.getHeader(FilterConstants.X_FORWARDED_PROTO_HEADER) + "," + proto;
        }
        ctx.addZuulRequestHeader(FilterConstants.X_FORWARDED_HOST_HEADER, host);
        ctx.addZuulRequestHeader(FilterConstants.X_FORWARDED_PORT_HEADER, port);
        ctx.addZuulRequestHeader(FilterConstants.X_FORWARDED_PROTO_HEADER, proto);
        addProxyPrefix(ctx, route);
    }

    private boolean hasHeader(HttpServletRequest request, String name) {
        return StringUtils.hasLength(request.getHeader(name));
    }

    private void addProxyPrefix(RequestContext ctx, Route route) {
        String forwardedPrefix = ctx.getRequest().getHeader(FilterConstants.X_FORWARDED_PREFIX_HEADER);
        String contextPath = ctx.getRequest().getContextPath();
        String prefix = StringUtils.hasLength(forwardedPrefix) ? forwardedPrefix : (StringUtils.hasLength(contextPath) ? contextPath : null);
        if (StringUtils.hasText(route.getPrefix())) {
            StringBuilder newPrefixBuilder = new StringBuilder();
            if (prefix != null) {
                if (prefix.endsWith("/") && route.getPrefix().startsWith("/")) {
                    newPrefixBuilder.append(prefix, 0, prefix.length() - 1);
                } else {
                    newPrefixBuilder.append(prefix);
                }
            }
            newPrefixBuilder.append(route.getPrefix());
            prefix = newPrefixBuilder.toString();
        }
        if (prefix != null) {
            //Swagger需要读取请求头中的X-Forwarded-Prefix的值，并加到host和接口路径之间。
            ctx.addZuulRequestHeader(FilterConstants.X_FORWARDED_PREFIX_HEADER, prefix);
        }
    }

    private String toHostHeader(HttpServletRequest request) {
        int port = request.getServerPort();
        String scheme = request.getScheme();
        boolean isComplete = (port == FilterConstants.HTTP_PORT && FilterConstants.HTTP_SCHEME.equals(scheme) || (port == FilterConstants.HTTPS_PORT && FilterConstants.HTTPS_SCHEME.equals(scheme)));
        if (isComplete) {
            return request.getServerName();
        } else {
            return request.getServerName() + ":" + port;
        }
    }

    private URL getUrl(String target) {
        try {
            return new URL(target);
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Target URL is malformed.", exception);
        }
    }
}
