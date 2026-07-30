package com.npat.uniclient.dto.base;

import com.bbl.gw.common.constant.HTTP_METHOD;
import com.bbl.gw.common.dto.componenet.AuthConfig;
import com.bbl.gw.common.dto.componenet.HeaderConfig;
import com.bbl.gw.common.dto.componenet.URLConfig;

import java.time.Duration;

public class HttpRequestConfig extends BaseRequestConfig {

    private final HTTP_METHOD httpMethod;
    private final HeaderConfig headerConfig;

    public HttpRequestConfig(URLConfig urlConfig, AuthConfig authentication, Duration connTimeout, Duration readTimeout, HTTP_METHOD httpMethod, HeaderConfig headerConfig) {
        super(urlConfig, authentication, connTimeout, readTimeout);
        this.httpMethod = httpMethod;
        this.headerConfig = headerConfig;
    }

    public HTTP_METHOD getHttpMethod() {
        return httpMethod;
    }

    public HeaderConfig getHeaderConfig() {
        return headerConfig;
    }
}
