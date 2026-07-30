package com.npat.uniclient.builder;


import com.bbl.gw.common.dto.APIRequest;
import com.bbl.gw.common.dto.componenet.AuthConfig;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseRequestBuilder<
        T extends APIRequest<?>,
        B extends BaseRequestBuilder<T, B>> {

    protected AuthConfig auth;
    protected Duration connTimeout;
    protected Duration readTimeout;
    protected LocalDateTime requestTime;
    protected T payload;

    public B auth(AuthConfig auth) {
        this.auth = auth;
        return self();
    }

    public B payload(T payload) {
        this.payload = payload;
        return self();
    }

    public B connTimeout(Duration timeout) {
        this.connTimeout = timeout;
        return self();
    }

    public B readTimeout(Duration timeout) {
        this.readTimeout = timeout;
        return self();
    }

    public B requestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
        return self();
    }

    protected abstract B self();

    public abstract T build();
}