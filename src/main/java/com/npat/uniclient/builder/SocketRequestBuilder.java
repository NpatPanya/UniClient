package com.npat.uniclient.builder;

import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.domain.SocketFormat;
import com.npat.uniclient.dto.SocketRequest;
import com.npat.uniclient.dto.base.BaseRequestConfig;
import com.npat.uniclient.dto.componenet.URLConfig;
import com.npat.uniclient.exception.RequestValidationException;

public final class SocketRequestBuilder<P, T> extends BaseRequestBuilder<SocketRequestBuilder<P, T>> {

    private String host;
    private int port;
    private P body;
    private SocketFormat format;
    private ResponseType<?> responseType;

    public SocketRequestBuilder<P, T> host(String host) { this.host = host; return this; }
    public SocketRequestBuilder<P, T> port(int port) { this.port = port; return this; }
    @SuppressWarnings("unchecked")
    public <N> SocketRequestBuilder<N, T> body(N body) { this.body = (P) body; return (SocketRequestBuilder<N, T>) this; }
    public SocketRequestBuilder<P, T> format(SocketFormat format) { this.format = format; return this; }
    public <N> SocketRequestBuilder<P, N> responseType(Class<N> responseType) { return responseType(ResponseType.of(responseType)); }
    @SuppressWarnings("unchecked")
    public <N> SocketRequestBuilder<P, N> responseType(ResponseType<N> responseType) { this.responseType = responseType; return (SocketRequestBuilder<P, N>) this; }

    @Override protected SocketRequestBuilder<P, T> self() { return this; }

    public SocketRequest<P, T> build() {
        validateTimeoutsAndResponseLimit();
        if (host == null || host.isBlank()) { throw new RequestValidationException("socket host is required"); }
        if (port < 1 || port > 65535) { throw new RequestValidationException("socket port must be between 1 and 65535"); }
        if (format == null) { throw new RequestValidationException("socket format is required"); }
        return new SocketRequest<>(body, new BaseRequestConfig(URLConfig.socket(host, port), auth, connTimeout, readTimeout,
                maxResponseBytes), format, responseType);
    }
}