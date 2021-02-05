package com.github.berbatov001.envolvedzuul.netflix.zuul.filters.route;

import com.github.berbatov001.envolvedribbon.client.RemoteClient;
import com.github.berbatov001.envolvedribbon.netfix.ribbon.RibbonClient;
import com.github.berbatov001.envolvedribbon.netfix.ribbon.RibbonClientFactory;
import com.github.berbatov001.envolvedzuul.netflix.ribbon.apache.ApacheHttpClientFactory;
import com.github.berbatov001.envolvedzuul.netflix.zuul.support.FilterConstants;
import com.github.berbatov001.envolvedzuul.netflix.zuul.support.ZuulProperties;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.LoadBalancerContext;
import com.netflix.loadbalancer.Server;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class RibbonRoutingFilter extends ZuulFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RibbonRoutingFilter.class);

    @Autowired
    private RemoteClient remoteClient;

    private ZuulProperties zuulProperties;

    public RibbonRoutingFilter(ZuulProperties zuulProperties) {
        this.zuulProperties = zuulProperties;
    }

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.RIBBON_ROUTING_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return (ctx.getRouteHost() == null && ctx.get(FilterConstants.SERVICE_ID_KEY) != null && ctx.sendZuulResponse());
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String serviceName = (String) context.get(FilterConstants.SERVICE_ID_KEY);
        String requestUri = (String) context.get(FilterConstants.REQUEST_URI_KEY);
        String queryString = request.getQueryString();
        if (StringUtils.hasLength(queryString)) {
            requestUri = requestUri + "?" + queryString;
        }
        HttpMethod method = HttpMethod.resolve(request.getMethod());
        Object result;
        try {
            //如果是访问静态资源
            if (isStaticResource(requestUri)) {
                //读取Ribbon配置
                IClientConfig ribbonClientConfig = RibbonClientFactory.getNamedConfig(serviceName);
                int connectTimeOut = ribbonClientConfig.get(CommonClientConfigKey.ConnectTimeout, 60000);
                int connectionRequestTimeOut = ribbonClientConfig.get(CommonClientConfigKey.ConnectTimeout, 60000);
                int socketTimeOut = ribbonClientConfig.get(CommonClientConfigKey.ReadTimeout, 60000);
                boolean redirectsEnabled = ribbonClientConfig.get(CommonClientConfigKey.FollowRedirects, false);
                boolean isGZipPayload = ribbonClientConfig.get(CommonClientConfigKey.GZipPayload, true);

                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(connectTimeOut)
                        .setConnectionRequestTimeout(connectionRequestTimeOut)
                        .setSocketTimeout(socketTimeOut)
                        .setRedirectsEnabled(redirectsEnabled)
                        .setContentCompressionEnabled(isGZipPayload)
                        .build();
                CloseableHttpClient httpClient = ApacheHttpClientFactory.getApacheHttpClient(serviceName, requestConfig, ribbonClientConfig, zuulProperties);
                final HttpUriRequest remoteRequest = toRequest(requestConfig, request, serviceName, requestUri);
                CloseableHttpResponse remoteResponse = httpClient.execute(remoteRequest);
                int statusCode = remoteResponse.getStatusLine().getStatusCode();
                context.put(FilterConstants.REMOTE_RESPONSE_STATUS_CODE_KEY, statusCode);
                context.put(FilterConstants.REMOTE_RESPONSE_HEADERS_KEY, remoteResponse.getAllHeaders());
                if (remoteResponse.getEntity() != null) {
                    context.setResponseDataStream(remoteResponse.getEntity().getContent());
                }
                return "SUCCESS";
            } else {
                HttpHeaders httpHeaders = createHttpHeaders(request);
                result = execute(request, serviceName, requestUri, method, httpHeaders);
            }
            context.put(FilterConstants.REMOTE_RESULT_KEY, result);
            return result;
        } catch (Exception exception) {
            LOGGER.error("调用远端服务{}发生异常！url={}", serviceName, requestUri);
            throw new RuntimeException(exception);
        }
    }

    private boolean isStaticResource(String url) {
        //Swagger页面的app/v2/api-docs接口也必须用httpClient发送请求，否则返回值中的host是远端具体应用实例的ip而不是网关的ip，
        //Swagger页面上的Base URL 就会显示远端实际ip，这样所有测试接口的url都会以远端实际ip开头，出现跨域问题。
        boolean flag = url.endsWith(FilterConstants.HASH_MARK_ENDING) || url.endsWith(FilterConstants.SWAGGER_API_DOCS_SUFFIX);
        if (!flag) {
            int thePointPosition = url.indexOf(".");
            if (thePointPosition > 0) {
                String suffix;
                int theQuestionMarkPosition = url.indexOf("?");
                if (theQuestionMarkPosition > 0) {
                    if (theQuestionMarkPosition < thePointPosition) {
                        //针对 /client/config/readIce?name=eye.fbs 这种Url。
                        return false;
                    }
                    suffix = url.substring(thePointPosition, theQuestionMarkPosition);
                } else {
                    suffix = url.substring(thePointPosition);
                }
                int suffixHashCode = suffix.hashCode();
                flag = ((suffixHashCode ^ FilterConstants.BMP_IMG_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.GIF_IMG_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.JPG_IMG_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.PNG_IMG_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.CSS_RESOURCE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.JS_RESOURCE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.HTML_PAGE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.HTM_PAGE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.DOCX_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.DOC_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.XLSX_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.XLS_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.PPTX_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.PPT_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.PDF_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.TXT_FILE_SUFFIX_HASH_CODE) == 0)
                        | ((suffixHashCode ^ FilterConstants.WOFF2_FILE_SUFFIX_HASH_CODE) == 0);
            }
        }
        return flag;
    }

    private HttpUriRequest toRequest(final RequestConfig requestConfig, HttpServletRequest request, String serviceName, String originalUri) {
        try {
            RibbonClient ribbonClient = RibbonClientFactory.getRibbonClient(serviceName);
            LoadBalancerContext loadBalancerContext = ribbonClient.getLoadBalancerContext();
            Server server = ribbonClient.getServer();
            Assert.notNull(server, serviceName + "没有可用实例。");
            URI uri = new URI(originalUri);
            String finalURI = loadBalancerContext.reconstructURIWithServer(server, uri).toString();
            String method = request.getMethod().toUpperCase();
            final RequestBuilder builder = RequestBuilder.create(request.getMethod());
            builder.setUri(finalURI);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                builder.addHeader(headerName, headerValue);
            }
            //添加ZuulRequestHeaders中的内容，主要是X-Forwarded-Prefix=/app/，否则后端测试接口发送的请求过网关时都不带/app/，导致404.
            RequestContext context = RequestContext.getCurrentContext();
            Map<String, String> zuulRequestHeaders = context.getZuulRequestHeaders();
            for (String zuulRequestHeaderName : zuulRequestHeaders.keySet()) {
                String zuulRequestHeaderValue = zuulRequestHeaders.get(zuulRequestHeaderName);
                builder.addHeader(zuulRequestHeaderName, zuulRequestHeaderValue);
            }
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                String parameterValue = request.getParameter(parameterName);
                builder.addParameter(parameterName, parameterValue);
            }
            int contentLength = request.getContentLength();
            InputStream requestBody = getRequestBody(request);
            if (requestBody != null) {
                final BasicHttpEntity entity = new BasicHttpEntity();
                entity.setContent(requestBody);
                if (FilterConstants.HTTP_GET_METHOD.equals(method.toUpperCase()) && (contentLength < 0)) {
                    entity.setContentLength(0);
                } else if (contentLength > 0) {
                    entity.setContentLength(contentLength);
                }
                builder.setEntity(entity);
            }
            builder.setConfig(requestConfig);
            return builder.build();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = (InputStream) RequestContext.getCurrentContext().get(FilterConstants.REQUEST_ENTITY_KEY);
            if (requestEntity == null) {
                requestEntity = request.getInputStream();
            }
        } catch (IOException exception) {
            LOGGER.error("getRequestBody时发生异常！", exception);
        }
        return requestEntity;
    }

    /**
     * 将HttpServletRequest的Header转换成HttpHeaders。
     *
     * @param httpServletRequest 来自DispatcherServlet的原始请求。
     * @return HttpHeaders
     */
    private HttpHeaders createHttpHeaders(HttpServletRequest httpServletRequest) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        Map<String, List<String>> map = new LinkedCaseInsensitiveMap<>(8, Locale.ENGLISH);
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerEnumeration = httpServletRequest.getHeaders(headerName);
            List<String> headerList = Collections.list(headerEnumeration);
            map.put(headerName, headerList);
        }
        return new HttpHeaders(CollectionUtils.toMultiValueMap(map));
    }

    private Object execute(HttpServletRequest request, String serviceName, String path, HttpMethod method, HttpHeaders httpHeaders) {
        try {
            LOGGER.info("接收到请求{}{}，header是{}。", request.getRequestURI(), StringUtils.hasLength(request.getQueryString()) ? "?" + request.getQueryString() : "", httpHeaders);
            RequestContext context = RequestContext.getCurrentContext();
            ResponseEntity<Resource> responseEntity;
            if (request instanceof StandardMultipartHttpServletRequest) {
                //处理文件上传功能
                MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
                Map<String, String[]> parameterMap = multipartHttpServletRequest.getParameterMap();
                MultiValueMap<String, MultipartFile> multiFileMap = multipartHttpServletRequest.getMultiFileMap();
                MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
                for (Map.Entry<String, String[]> entity : parameterMap.entrySet()) {
                    Arrays.stream(entity.getValue()).forEach(value -> {
                        multipartBodyBuilder.part(entity.getKey(), value);
                    });
                }
                for (Map.Entry<String, List<MultipartFile>> entity : multiFileMap.entrySet()) {
                    entity.getValue().forEach(file -> {
                        multipartBodyBuilder.part(entity.getKey(), file.getResource(), MediaType.MULTIPART_FORM_DATA);
                    });
                }
                MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();
                LOGGER.info("开始文件上传。");
                responseEntity = remoteClient.callForResponseEntity(serviceName, path, method, httpHeaders, multipartBody, Resource.class);
            } else {
                InputStream body = new ServletServerHttpRequest(request).getBody();
                byte[] bytes = StreamUtils.copyToByteArray(body);
                LOGGER.info("开始调用远端服务。请求体是 {}", new String(bytes));
                responseEntity = remoteClient.callForResponseEntity(serviceName, path, method, httpHeaders, bytes, Resource.class);
            }
            int statusCode = responseEntity.getStatusCode().value();
            LOGGER.info("请求结束，响应码={}。", statusCode);
            context.put(FilterConstants.REMOTE_RESPONSE_STATUS_CODE_KEY, statusCode);
            context.put(FilterConstants.REMOTE_RESPONSE_HEADERS_KEY, responseEntity.getHeaders());
            if (responseEntity.getBody() != null) {
                context.setResponseDataStream(responseEntity.getBody().getInputStream());
            }
            return "SUCCESS";
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
