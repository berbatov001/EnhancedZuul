package com.github.berbatov001.enhancedzuul.netflix.zuul.support;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ConfigurationProperties("zuul")
public class ZuulProperties {
    private String prefix = "";
    private boolean stripPrefix = true;
    private Boolean retryable = false;
    private Map<String, ZuulRoute> routes = new LinkedHashMap<>();
    private boolean addProxyHeaders = true;
    private boolean addHostHeader = false;
    private Set<String> ignoredServices = new LinkedHashSet<>();
    private Set<String> ignoredPatterns = new LinkedHashSet<>();
    private Set<String> ignoredHeaders = new LinkedHashSet<>();
    private boolean ignoreSecurityHeaders = true;
    private boolean forceOriginalQueryStringEncoding = false;
    private String servletPath = "/zuul";
    private boolean ignoreLocalService = true;
    private boolean traceRequestBody = true;
    private boolean removeSemicolonContent = true;
    private boolean decodeUrl = true;
    private boolean sslHostnameValidationEnabled = true;
    private boolean setContentLength = false;
    private boolean includeDebugHeader = false;
    private int initialStreamBufferSize = 8192;
    private Host host = new Host();

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(boolean stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public Boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(Boolean retryable) {
        this.retryable = retryable;
    }

    public Map<String, ZuulRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, ZuulRoute> routes) {
        this.routes = routes;
    }

    public boolean isAddProxyHeaders() {
        return addProxyHeaders;
    }

    public void setAddProxyHeaders(boolean addProxyHeaders) {
        this.addProxyHeaders = addProxyHeaders;
    }

    public boolean isAddHostHeader() {
        return addHostHeader;
    }

    public void setAddHostHeader(boolean addHostHeader) {
        this.addHostHeader = addHostHeader;
    }

    public Set<String> getIgnoredServices() {
        return ignoredServices;
    }

    public void setIgnoredServices(Set<String> ignoredServices) {
        this.ignoredServices = ignoredServices;
    }

    public Set<String> getIgnoredPatterns() {
        return ignoredPatterns;
    }

    public void setIgnoredPatterns(Set<String> ignoredPatterns) {
        this.ignoredPatterns = ignoredPatterns;
    }

    public Set<String> getIgnoredHeaders() {
        return ignoredHeaders;
    }

    public void setIgnoredHeaders(Set<String> ignoredHeaders) {
        this.ignoredHeaders = ignoredHeaders;
    }

    public boolean isIgnoreSecurityHeaders() {
        return ignoreSecurityHeaders;
    }

    public void setIgnoreSecurityHeaders(boolean ignoreSecurityHeaders) {
        this.ignoreSecurityHeaders = ignoreSecurityHeaders;
    }

    public boolean isForceOriginalQueryStringEncoding() {
        return forceOriginalQueryStringEncoding;
    }

    public void setForceOriginalQueryStringEncoding(boolean forceOriginalQueryStringEncoding) {
        this.forceOriginalQueryStringEncoding = forceOriginalQueryStringEncoding;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public boolean isIgnoreLocalService() {
        return ignoreLocalService;
    }

    public void setIgnoreLocalService(boolean ignoreLocalService) {
        this.ignoreLocalService = ignoreLocalService;
    }

    public boolean isTraceRequestBody() {
        return traceRequestBody;
    }

    public void setTraceRequestBody(boolean traceRequestBody) {
        this.traceRequestBody = traceRequestBody;
    }

    public boolean isRemoveSemicolonContent() {
        return removeSemicolonContent;
    }

    public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
        this.removeSemicolonContent = removeSemicolonContent;
    }

    public boolean isDecodeUrl() {
        return decodeUrl;
    }

    public void setDecodeUrl(boolean decodeUrl) {
        this.decodeUrl = decodeUrl;
    }

    public boolean isSslHostnameValidationEnabled() {
        return sslHostnameValidationEnabled;
    }

    public void setSslHostnameValidationEnabled(boolean sslHostnameValidationEnabled) {
        this.sslHostnameValidationEnabled = sslHostnameValidationEnabled;
    }

    public boolean isSetContentLength() {
        return setContentLength;
    }

    public void setSetContentLength(boolean setContentLength) {
        this.setContentLength = setContentLength;
    }

    public boolean isIncludeDebugHeader() {
        return includeDebugHeader;
    }

    public void setIncludeDebugHeader(boolean includeDebugHeader) {
        this.includeDebugHeader = includeDebugHeader;
    }

    public int getInitialStreamBufferSize() {
        return initialStreamBufferSize;
    }

    public void setInitialStreamBufferSize(int initialStreamBufferSize) {
        this.initialStreamBufferSize = initialStreamBufferSize;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public static class ZuulRoute {
        private String id;
        private String path;
        private String serviceId;
        private String url;
        private boolean stripPrefix = true;
        private Boolean retryable;
        private Set<String> sensitiveHeaders = new LinkedHashSet<>();
        private boolean customSensitiveHeaders = false;

        public ZuulRoute() {
        }

        public ZuulRoute(String id, String path, String serviceId, String url, boolean stripPrefix, Boolean retryable, Set<String> sensitiveHeaders) {
            this.id = id;
            this.path = path;
            this.serviceId = serviceId;
            this.url = url;
            this.stripPrefix = stripPrefix;
            this.retryable = retryable;
            this.sensitiveHeaders = sensitiveHeaders;
            this.customSensitiveHeaders = sensitiveHeaders != null;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isStripPrefix() {
            return stripPrefix;
        }

        public void setStripPrefix(boolean stripPrefix) {
            this.stripPrefix = stripPrefix;
        }

        public Boolean isRetryable() {
            return retryable;
        }

        public void setRetryable(Boolean retryable) {
            this.retryable = retryable;
        }

        public Set<String> getSensitiveHeaders() {
            return sensitiveHeaders;
        }

        public void setSensitiveHeaders(Set<String> sensitiveHeaders) {
            this.sensitiveHeaders = sensitiveHeaders;
        }

        public boolean isCustomSensitiveHeaders() {
            return customSensitiveHeaders;
        }

        public void setCustomSensitiveHeaders(boolean customSensitiveHeaders) {
            this.customSensitiveHeaders = customSensitiveHeaders;
        }

        public String getLocation() {
            if (StringUtils.hasText(this.url)) {
                return this.url;
            }
            return this.serviceId;
        }
    }

    public static class Host {
        private int maxTotalConnections = 200;
        private int maxPerRouteConnection = 20;
        private int socketTimeoutMillis = 10000;
        private int connectTimeoutMillis = 2000;
        private int connectionRequestTimeoutMillis = -1;
        private long timeToLive = -1;
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        public Host() {
        }

        public Host(int maxTotalConnections, int maxPerRouteConnection, int socketTimeoutMillis, int connectTimeoutMillis, long timeToLive, TimeUnit timeUnit) {
            this.maxTotalConnections = maxTotalConnections;
            this.maxPerRouteConnection = maxPerRouteConnection;
            this.socketTimeoutMillis = socketTimeoutMillis;
            this.connectTimeoutMillis = connectTimeoutMillis;
            this.timeToLive = timeToLive;
            this.timeUnit = timeUnit;
        }

        public int getMaxTotalConnections() {
            return maxTotalConnections;
        }

        public void setMaxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
        }

        public int getMaxPerRouteConnection() {
            return maxPerRouteConnection;
        }

        public void setMaxPerRouteConnection(int maxPerRouteConnection) {
            this.maxPerRouteConnection = maxPerRouteConnection;
        }

        public int getSocketTimeoutMillis() {
            return socketTimeoutMillis;
        }

        public void setSocketTimeoutMillis(int socketTimeoutMillis) {
            this.socketTimeoutMillis = socketTimeoutMillis;
        }

        public int getConnectTimeoutMillis() {
            return connectTimeoutMillis;
        }

        public void setConnectTimeoutMillis(int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
        }

        public int getConnectionRequestTimeoutMillis() {
            return connectionRequestTimeoutMillis;
        }

        public void setConnectionRequestTimeoutMillis(int connectionRequestTimeoutMillis) {
            this.connectionRequestTimeoutMillis = connectionRequestTimeoutMillis;
        }

        public long getTimeToLive() {
            return timeToLive;
        }

        public void setTimeToLive(long timeToLive) {
            this.timeToLive = timeToLive;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }
    }
}