package com.npat.uniclient.dto.base;

import com.bbl.gw.common.dto.componenet.AuthConfig;
import com.bbl.gw.common.dto.componenet.URLConfig;

import java.time.Duration;
import java.time.LocalDateTime;

public class BaseRequestConfig {

    private final URLConfig urlConfig;
    private final AuthConfig authentication;
    private final Duration connTimeout;
    private final Duration readTimeout;
    private final LocalDateTime requestTime;

    public BaseRequestConfig(URLConfig urlConfig, AuthConfig authentication, Duration connTimeout, Duration readTimeout) {
        this.urlConfig = urlConfig;
        this.authentication = authentication;
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
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

    public LocalDateTime getRequestTime() {
        return requestTime;
    }
}
