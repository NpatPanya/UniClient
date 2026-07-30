package com.npat.uniclient.builder;

import com.bbl.gw.common.dto.SocketRequest;
import com.bbl.gw.common.dto.base.BaseRequestConfig;

import java.time.LocalDateTime;

import static com.bbl.gw.common.dto.componenet.URLConfig.socket;

public final class SocketRequestBuilder
        extends BaseRequestBuilder<SocketRequest<?>, SocketRequestBuilder> {

    private String host;
    private int port;
    private String encoding;
    private LocalDateTime requestTime;

    public SocketRequestBuilder host(String host) {
        this.host = host;
        return this;
    }

    public SocketRequestBuilder port(int port) {
        this.port = port;
        return this;
    }

    public SocketRequestBuilder encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    @Override
    protected SocketRequestBuilder self() {
        return this;
    }

    @Override
    public SocketRequest build() {

        if (host == null || host.isBlank()) {
            throw new IllegalStateException("host is required");
        }

        if (port <= 0) {
            throw new IllegalStateException("port is required");
        }

        if (encoding == null || encoding.isEmpty()) {
            throw new IllegalStateException("encoding is required");
        }

        BaseRequestConfig config =
                new BaseRequestConfig(
                        socket(host, port),
                        auth,
                        connTimeout,
                        readTimeout
                );

        return new SocketRequest(
                payload,
                config,
                encoding
        );
    }
}