package com.github.berbatov001.enhancedzuul.netflix.zuul.support;

public class FilterConstants {
    public static final String SERVICE_ID_KEY = "serviceId";
    public static final String FORWARD_TO_KEY = "forward.to";
    public static final String PROXY_KEY = "proxy";
    public static final String REQUEST_ENTITY_KEY = "requestEntity";
    public static final String REQUEST_URI_KEY = "requestURI";
    public static final String RETRYABLE_KEY = "retryable";

    public static final int PRE_DECORATION_FILTER_ORDER = 5;
    public static final int GRAY_SALE_RELEASE_ORDER = 10;
    public static final int RIBBON_ROUTING_FILTER_ORDER = 20;
    public static final int SEND_RESPONSE_FILER_ORDER = 1000;

    public static final String POST_TYPE = "post";
    public static final String PRE_TYPE = "pre";
    public static final String ROUTE_TYPE = "route";

    public static final String FORWARD_LOCATION_PREFIX = "forward:";

    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = "https";

    public static final String SERVICE_HEADER = "X-Zuul-Service";
    public static final String SERVICE_ID_HEADER = "X-Zuul-ServiceId";

    public static final int JPG_IMG_SUFFIX_HASH_CODE = ".jpg".hashCode();
    public static final int GIF_IMG_SUFFIX_HASH_CODE = ".gif".hashCode();
    public static final int PNG_IMG_SUFFIX_HASH_CODE = ".png".hashCode();
    public static final int BMP_IMG_SUFFIX_HASH_CODE = ".bmp".hashCode();

    public static final int CSS_RESOURCE_SUFFIX_HASH_CODE = ".css".hashCode();
    public static final int JS_RESOURCE_SUFFIX_HASH_CODE = ".js".hashCode();

    public static final int HTML_PAGE_SUFFIX_HASH_CODE = ".html".hashCode();
    public static final int HTM_PAGE_SUFFIX_HASH_CODE = ".htm".hashCode();

    public static final int DOCX_FILE_SUFFIX_HASH_CODE = ".docx".hashCode();
    public static final int DOC_FILE_SUFFIX_HASH_CODE = ".doc".hashCode();
    public static final int XLSX_FILE_SUFFIX_HASH_CODE = ".xlsx".hashCode();
    public static final int XLS_FILE_SUFFIX_HASH_CODE = ".xls".hashCode();
    public static final int PPT_FILE_SUFFIX_HASH_CODE = ".ppt".hashCode();
    public static final int PPTX_FILE_SUFFIX_HASH_CODE = ".pptx".hashCode();
    public static final int PDF_FILE_SUFFIX_HASH_CODE = ".pdf".hashCode();
    public static final int TXT_FILE_SUFFIX_HASH_CODE = ".txt".hashCode();
    public static final int WOFF2_FILE_SUFFIX_HASH_CODE = ".woff2".hashCode();

    public static final String HASH_MARK_ENDING = "#/";

    public static final String SWAGGER_API_DOCS_SUFFIX = "/api-docs";

    public static final String REMOTE_RESPONSE_HEADERS_KEY = "remoteResponseHeaders";
    public static final String REMOTE_RESPONSE_STATUS_CODE_KEY = "remoteResponseStatusCode";
    public static final String REMOTE_RESULT_KEY = "remoteResult";

    public static final String HTTP_GET_METHOD = "GET";

    public static final String THE_COLON_MARK = ":";

    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;

    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    public static final String X_FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    public static final String X_FORWARDED_PREFIX_HEADER = "X-Forwarded-Prefix";
    public static final String X_FORWARDED_PORT_HEADER = "X-Forwarded-Port";
    public static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";

}
