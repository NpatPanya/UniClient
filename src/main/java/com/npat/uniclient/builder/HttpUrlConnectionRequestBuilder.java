package com.npat.uniclient.builder;

import com.npat.uniclient.domain.HTTP_METHOD;
import com.npat.uniclient.domain.HttpTransportKind;
import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.dto.RestfulRequest;
import com.npat.uniclient.dto.base.HttpRequestConfig;
import com.npat.uniclient.dto.componenet.HeaderConfig;
import com.npat.uniclient.dto.componenet.URLConfig;
import com.npat.uniclient.exception.RequestValidationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import javax.net.ssl.SSLContext;

public final class HttpUrlConnectionRequestBuilder<P, T>
        extends BaseRequestBuilder<HttpUrlConnectionRequestBuilder<P, T>> {

    private String endpoint;
    private P body;
    private ResponseType<?> responseType;
    private HTTP_METHOD method;
    private SSLContext sslContext;
    private boolean followRedirects;
    private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

    @SuppressWarnings("unchecked")
    public <N> HttpUrlConnectionRequestBuilder<N, T> body(N body) {
        this.body = (P) body;
        return (HttpUrlConnectionRequestBuilder<N, T>) this;
    }

    public <N> HttpUrlConnectionRequestBuilder<P, N> responseType(Class<N> responseType) {
        return responseType(ResponseType.of(responseType));
    }

    @SuppressWarnings("unchecked")
    public <N> HttpUrlConnectionRequestBuilder<P, N> responseType(ResponseType<N> responseType) {
        this.responseType = responseType;
        return (HttpUrlConnectionRequestBuilder<P, N>) this;
    }

    public HttpUrlConnectionRequestBuilder<P, T> endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public HttpUrlConnectionRequestBuilder<P, T> method(HTTP_METHOD method) {
        this.method = method;
        return this;
    }

    public HttpUrlConnectionRequestBuilder<P, T> header(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public HttpUrlConnectionRequestBuilder<P, T> sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public HttpUrlConnectionRequestBuilder<P, T> followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    @Override
    protected HttpUrlConnectionRequestBuilder<P, T> self() {
        return this;
    }

    public RestfulRequest<P, T> build() {
        validateTimeoutsAndResponseLimit();
        validateHttpEndpoint(endpoint);
        if (method == null) {
            throw new RequestValidationException("HTTP method is required");
        }
        validateNoAuthorizationConflict(headers);
        return new RestfulRequest<>(body, new HttpRequestConfig(URLConfig.rest(endpoint), auth, connTimeout, readTimeout,
                method, HeaderConfig.of(copyHeadersWithJsonDefault(headers, body)), sslContext, followRedirects, maxResponseBytes, HttpTransportKind.HTTP_URL_CONNECTION),
                new MultivaluedHashMap<>(), new MultivaluedHashMap<>(), responseType);
    }
}