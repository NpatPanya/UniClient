package com.npat.uniclient.dto;

import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.domain.SocketFormat;
import com.npat.uniclient.dto.base.BaseRequestConfig;

public final class SocketRequest<P, T> extends APIRequest<P, SocketResponse<T>> {

    private final BaseRequestConfig config;
    private final SocketFormat format;

    public SocketRequest(P payload, BaseRequestConfig config, SocketFormat format, ResponseType<?> responseType) {
        super(payload, responseType);
        this.config = config;
        this.format = format;
    }

    public BaseRequestConfig getConfig() { return config; }
    public SocketFormat getFormat() { return format; }
}