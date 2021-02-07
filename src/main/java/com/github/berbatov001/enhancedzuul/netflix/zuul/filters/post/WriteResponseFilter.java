package com.github.berbatov001.enhancedzuul.netflix.zuul.filters.post;

import com.github.berbatov001.enhancedzuul.netflix.zuul.support.FilterConstants;
import com.github.berbatov001.enhancedzuul.netflix.zuul.support.ZuulProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulHeaders;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.util.HTTPRequestUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class WriteResponseFilter extends ZuulFilter {

    private final static Logger LOGGER = LoggerFactory.getLogger(WriteResponseFilter.class);

    private static final String IGNORED_HEADERS = "ignoredHeaders";

    private final ZuulProperties zuulProperties;

    private boolean userServlet31 = true;

    public WriteResponseFilter(ZuulProperties zuulProperties) {
        this.zuulProperties = zuulProperties;
        try {
            HttpServletResponse.class.getMethod("setContentLengthLong", long.class);
        } catch (NoSuchMethodException e) {
            userServlet31 = false;
        }
    }

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SEND_RESPONSE_FILER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        return context.get(FilterConstants.REMOTE_RESPONSE_HEADERS_KEY) != null || context.getResponseDataStream() != null;
    }

    @Override
    public Object run() {
        setHeader();
        writeResponse();
        return null;
    }

    private void setHeader() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletResponse originalResponse = context.getResponse();
        int statusCode = (Integer) context.get(FilterConstants.REMOTE_RESPONSE_STATUS_CODE_KEY);
        originalResponse.setStatus(statusCode);
        Long contentLength = null;
        boolean responseGZipped = false;
        if (context.get(FilterConstants.REMOTE_RESPONSE_HEADERS_KEY) instanceof Header[]) {
            Header[] headers = (Header[]) context.get(FilterConstants.REMOTE_RESPONSE_HEADERS_KEY);
            for (final Header header : headers) {
                String headerName = header.getName();
                String headerValue = header.getValue();
                if (headerName.equalsIgnoreCase(ZuulHeaders.CONTENT_ENCODING) && HTTPRequestUtils.getInstance().isGzipped(headerValue)) {
                    responseGZipped = true;
                }
                if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                    contentLength = Long.valueOf(headerValue);
                }
                if (isIncludedHeader(headerName)) {
                    originalResponse.addHeader(headerName, headerValue);
                }
            }
        } else {
            HttpHeaders httpHeaders = (HttpHeaders) context.get(FilterConstants.REMOTE_RESPONSE_HEADERS_KEY);
            for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
                String name = header.getKey();
                for (String value : header.getValue()) {
                    if (name.equalsIgnoreCase(ZuulHeaders.CONTENT_ENCODING) && HTTPRequestUtils.getInstance().isGzipped(value)) {
                        responseGZipped = true;
                    }
                    if (name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                        contentLength = Long.valueOf(value);
                    }
                    if (isIncludedHeader(name)) {
                        originalResponse.addHeader(name, value);
                    }
                }
            }
        }
        if (!this.zuulProperties.isSetContentLength() && contentLength != null && responseGZipped) {
            if (userServlet31) {
                originalResponse.setContentLengthLong(contentLength);
            } else {
                if (isLongSafe(contentLength)) {
                    originalResponse.setContentLength(contentLength.intValue());
                }
            }
        }
        context.setResponseGZipped(responseGZipped);
    }

    private void writeResponse() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletResponse originalResponse = context.getResponse();
        if (originalResponse.getCharacterEncoding() != null) {
            originalResponse.setCharacterEncoding("UTF-8");
        }
        String servletResponseContentEncoding = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            if (context.getResponseDataStream() != null) {
                outputStream = originalResponse.getOutputStream();
                inputStream = new BufferedInputStream(context.getResponseDataStream());
                if (context.getResponseGZipped()) {
                    if ((isGzipRequested(context))) {
                        servletResponseContentEncoding = "gzip";
                    } else {
                        inputStream = handleGzipStream(inputStream);
                    }
                }
                if (servletResponseContentEncoding != null) {
                    originalResponse.setHeader(ZuulHeaders.CONTENT_ENCODING, servletResponseContentEncoding);
                }
                writeResponse(inputStream, outputStream);
            }
        } catch (Exception exception) {
            String serviceName = (String) context.get(FilterConstants.SERVICE_ID_KEY);
            String requestUri = (String) context.get(FilterConstants.REQUEST_URI_KEY);
            LOGGER.error("接收{}的{}接口返回结果时发生错误：{}", serviceName, requestUri, exception.getMessage());
            throw new RuntimeException(exception);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("关闭inputStream时发生异常！", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("关闭outputStream时发生异常！", e);
                }
            }
        }
    }

    private void writeResponse(InputStream inputStream, OutputStream outputStream) throws Exception {
        int initialStreamBufferSize = zuulProperties.getInitialStreamBufferSize();
        byte[] bytes = new byte[initialStreamBufferSize];
        int bytesRead;
        while ((bytesRead = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, bytesRead);
        }
    }

    private boolean isLongSafe(long value) {
        return value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE;
    }

    private boolean isGzipRequested(RequestContext context) {
        final String requestEncoding = context.getRequest().getHeader(ZuulHeaders.ACCEPT_ENCODING);
        return requestEncoding != null && HTTPRequestUtils.getInstance().isGzipped(requestEncoding);
    }

    private InputStream handleGzipStream(InputStream inputStream) throws Exception {
        RecordingInputStream stream = new RecordingInputStream(inputStream);
        try {
            return new GZIPInputStream(stream);
        } catch (java.util.zip.ZipException | EOFException exception) {
            if (stream.getBytesRead() == 0) {
                return inputStream;
            } else {
                LOGGER.warn("gizp response expected but failed to read gzip headers, assuming unencoding response for request" + RequestContext.getCurrentContext().getRequest().getRequestURL().toString());
                stream.reset();
                return stream;
            }
        } finally {
            stream.stopRecording();
        }
    }

    private boolean isIncludedHeader(String headerName) {
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

    private static class RecordingInputStream extends InputStream {

        private InputStream delegate;

        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        public RecordingInputStream(InputStream delegate) {
            super();
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = delegate.read(b, off, len);
            if (buffer != null && read != -1) {
                buffer.write(b, off, read);
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            super.close();
        }

        @Override
        public synchronized void reset() {
            if (buffer == null) {
                throw new IllegalStateException("Stream is note recording.");
            }
            this.delegate = new SequenceInputStream(new ByteArrayInputStream(buffer.toByteArray()), delegate);
            this.buffer = new ByteArrayOutputStream();
        }

        @Override
        public int read() throws IOException {
            int read = delegate.read();
            if (buffer != null && read != -1) {
                buffer.write(read);
            }
            return read;
        }

        private int getBytesRead() {
            return (buffer == null) ? -1 : buffer.size();
        }

        private void stopRecording() {
            this.buffer = null;
        }
    }
}
