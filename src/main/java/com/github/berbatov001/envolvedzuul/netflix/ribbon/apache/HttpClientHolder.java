package com.github.berbatov001.envolvedzuul.netflix.ribbon.apache;

import org.apache.http.impl.client.CloseableHttpClient;

import java.util.concurrent.ScheduledExecutorService;

public class HttpClientHolder {
    private CloseableHttpClient httpClient;
    public ScheduledExecutorService scheduledExecutorService;

    CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    void setHttpClient(CloseableHttpClient httpClient){
        this.httpClient = httpClient;
    }

    void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }
}
