package com.github.berbatov001.envolvedzuul.netflix.zuul.web;

import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class Route {
    private String id;
    private String path;
    private String fullPath;
    private String location;
    private String prefix;
    private Boolean retryable;
    private Set<String> sensitiveHeaders;
    private boolean customSensitiveHeaders;
    private boolean prefixStripped = true;

    Route(String id, String path, String location, String prefix, Boolean retryable, Set<String> ignoredHeaders) {
        this.id = id;
        this.prefix = StringUtils.hasText(prefix) ? prefix : "";
        this.path = path;
        this.fullPath = prefix + path;
        this.location = location;
        this.retryable = retryable;
        this.sensitiveHeaders = new LinkedHashSet<>();
        if (ignoredHeaders != null) {
            this.customSensitiveHeaders = true;
            for (String header : ignoredHeaders) {
                this.sensitiveHeaders.add(header.toLowerCase());
            }
        }
    }

    Route(String id, String path, String location, String prefix, Boolean retryable, Set<String> ignoredHeaders, boolean prefixStripped) {
        this(id, path, location, prefix, retryable, ignoredHeaders);
        this.prefixStripped = prefixStripped;
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

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Boolean getRetryable() {
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

    public boolean isPrefixStripped() {
        return prefixStripped;
    }

    public void setPrefixStripped(boolean prefixStripped) {
        this.prefixStripped = prefixStripped;
    }

    @Override
    public String toString() {
        return "Route{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", fullPath='" + fullPath + '\'' +
                ", location='" + location + '\'' +
                ", prefix='" + prefix + '\'' +
                ", retryable=" + retryable +
                ", sensitiveHeaders=" + sensitiveHeaders +
                ", customSensitiveHeaders=" + customSensitiveHeaders +
                ", prefixStripped=" + prefixStripped +
                '}';
    }
}
