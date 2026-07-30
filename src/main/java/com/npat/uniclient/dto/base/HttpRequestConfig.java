package com.npat.uniclient.dto.base;

import com.npat.uniclient.domain.HTTP_METHOD;
import com.npat.uniclient.domain.HttpTransportKind;
import com.npat.uniclient.dto.componenet.AuthConfig;
import com.npat.uniclient.dto.componenet.HeaderConfig;
import com.npat.uniclient.dto.componenet.URLConfig;

import javax.net.ssl.SSLContext;
import java.time.Duration;

public class HttpRequestConfig extends BaseRequestConfig {

    private final HTTP_METHOD httpMethod;
    private final HeaderConfig headerConfig;
    private final SSLContext sslContext;
    private final boolean followRedirects;
    private final HttpTransportKind transportKind;

    public HttpRequestConfig(URLConfig urlConfig, AuthConfig authentication, Duration connTimeout,
                             Duration readTimeout, HTTP_METHOD httpMethod, HeaderConfig headerConfig) {
        this(urlConfig, authentication, connTimeout, readTimeout, httpMethod, headerConfig,
                null, false, DEFAULT_MAX_RESPONSE_BYTES, HttpTransportKind.REST_CLIENT);
    }

    public HttpRequestConfig(URLConfig urlConfig, AuthConfig authentication, Duration connTimeout,
                             Duration readTimeout, HTTP_METHOD httpMethod, HeaderConfig headerConfig,
                             SSLContext sslContext, boolean followRedirects, long maxResponseBytes, HttpTransportKind transportKind) {
        super(urlConfig, authentication, connTimeout, readTimeout, maxResponseBytes);
        this.httpMethod = httpMethod;
        this.headerConfig = headerConfig;
        this.sslContext = sslContext;
        this.followRedirects = followRedirects;
        this.transportKind = transportKind;
    }

    public HTTP_METHOD getHttpMethod() { return httpMethod; }
    public HeaderConfig getHeaderConfig() { return headerConfig; }
    public SSLContext getSslContext() { return sslContext; }
    public boolean isFollowRedirects() { return followRedirects; }
    public HttpTransportKind getTransportKind() { return transportKind; }
}