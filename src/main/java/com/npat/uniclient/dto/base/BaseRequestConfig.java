package com.npat.uniclient.dto.base;

import com.npat.uniclient.dto.componenet.AuthConfig;
import com.npat.uniclient.dto.componenet.URLConfig;

import java.time.Duration;
import java.time.LocalDateTime;

public class BaseRequestConfig {

    public static final long DEFAULT_MAX_RESPONSE_BYTES = 10L * 1024L * 1024L;

    private final URLConfig urlConfig;
    private final AuthConfig authentication;
    private final Duration connTimeout;
    private final Duration readTimeout;
    private final long maxResponseBytes;
    private final LocalDateTime requestTime;

    public BaseRequestConfig(URLConfig urlConfig, AuthConfig authentication, Duration connTimeout, Duration readTimeout) {
        this(urlConfig, authentication, connTimeout, readTimeout, DEFAULT_MAX_RESPONSE_BYTES);
    }

    public BaseRequestConfig(URLConfig urlConfig, AuthConfig authentication, Duration connTimeout,
                             Duration readTimeout, long maxResponseBytes) {
        this.urlConfig = urlConfig;
        this.authentication = authentication;
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
        this.maxResponseBytes = maxResponseBytes;
        this.requestTime = LocalDateTime.now();
    }

    public URLConfig getUrlConfig() {
        return urlConfig;
    }

    public AuthConfig getAuthentication() {
        return authentication;
    }

    public Duration getConnTimeout() {
        return connTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public long getMaxResponseBytes() {
        return maxResponseBytes;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }
}