package com.npat.uniclient.dto;

import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.dto.base.HttpRequestConfig;
import jakarta.ws.rs.core.MultivaluedMap;

public class RestfulRequest<P, T> extends APIRequest<P, RestfulResponse<T>> {

    private final HttpRequestConfig config;
    private final MultivaluedMap<String, String> pathParams;
    private final MultivaluedMap<String, String> queryParams;

    public RestfulRequest(P payload, HttpRequestConfig config, MultivaluedMap<String, String> pathParams,
                          MultivaluedMap<String, String> queryParams, ResponseType<?> responseType) {
        super(payload, responseType);
        this.config = config;
        this.pathParams = pathParams;
        this.queryParams = queryParams;
    }

    public HttpRequestConfig getConfig() {
        return config;
    }

    public MultivaluedMap<String, String> getPathParams() {
        return pathParams;
    }

    public MultivaluedMap<String, String> getQueryParams() {
        return queryParams;
    }
}