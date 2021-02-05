package com.github.berbatov001.envolvedzuul.netflix.ribbon.apache;

import com.github.berbatov001.envolvedzuul.netflix.zuul.support.FilterConstants;
import com.github.berbatov001.envolvedzuul.netflix.zuul.support.ZuulProperties;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ApacheHttpClientFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientFactory.class);

    public final static Map<String, HttpClientHolder> NAMED_HTTP_CLIENT_HOLDER_MAP = new ConcurrentHashMap<>();

    public static synchronized CloseableHttpClient getApacheHttpClient(String serviceName, RequestConfig requestConfig, IClientConfig ribbonClientConfig, ZuulProperties zuulProperties) {
        HttpClientHolder httpClientHolder = NAMED_HTTP_CLIENT_HOLDER_MAP.get(serviceName);
        if (httpClientHolder != null) {
            return httpClientHolder.getHttpClient();
        }

        //创建RegistryBuilder
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create().register(FilterConstants.HTTP_SCHEME, PlainConnectionSocketFactory.INSTANCE);
        if (zuulProperties.isSslHostnameValidationEnabled()) {
            registryBuilder.register(FilterConstants.HTTP_SCHEME, SSLConnectionSocketFactory.getSocketFactory());
        } else {
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new DisabledValidationTrustManager()}, new SecureRandom());
                registryBuilder.register(FilterConstants.HTTPS_SCHEME, new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE));
            } catch (NoSuchAlgorithmException | KeyManagementException exception) {
                LOGGER.warn("创建SSLContext失败。", exception);
            }
        }
        //通过RegistryBuilder创建Registry
        final Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        //创建HttpClientConnectionManager，参数时Registry
        int timeToLive = ribbonClientConfig.get(CommonClientConfigKey.PoolKeepAliveTime);
        String timeUnit = ribbonClientConfig.get(CommonClientConfigKey.PoolKeepAliveTimeUnits);
        int maxTotalConnections = ribbonClientConfig.get(CommonClientConfigKey.MaxTotalConnections);
        int maxPerRouteConnections = ribbonClientConfig.get(CommonClientConfigKey.MaxConnectionsPerHost);
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry, null, null, null, timeToLive, TimeUnit.valueOf(timeUnit));
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxPerRouteConnections);
        //这里缺少PoolingHttpClientConnectionManager的定时
        //创建HttpclientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().disableContentCompression().disableCookieManagement().useSystemProperties();
        //通过HttpClientBuilder创建HttpClient，参数是HttpClientConnectionManager和RequestConfig
        CloseableHttpClient httpClient = httpClientBuilder.setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager).build();

        int timerRepeat = ribbonClientConfig.get(CommonClientConfigKey.ConnectionCleanerRepeatInterval);
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("ApacheHttpClientFactory.connectioinManagerTimer").setDaemon(true).build());
        scheduledExecutorService.scheduleAtFixedRate(connectionManager::closeExpiredConnections, 30000, timerRepeat, TimeUnit.MILLISECONDS);
        httpClientHolder = new HttpClientHolder();
        httpClientHolder.setHttpClient(httpClient);
        httpClientHolder.setScheduledExecutorService(scheduledExecutorService);
        NAMED_HTTP_CLIENT_HOLDER_MAP.put(serviceName, httpClientHolder);
        return httpClient;
    }

    private static class DisabledValidationTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
