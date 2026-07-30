package com.npat.uniclient.builder;


import com.bbl.gw.common.constant.HTTP_METHOD;
import com.bbl.gw.common.dto.RestfulRequest;
import com.bbl.gw.common.dto.base.HttpRequestConfig;
import com.bbl.gw.common.dto.componenet.HeaderConfig;
import com.bbl.gw.common.dto.componenet.URLConfig;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;


public final class RestRequestBuilder
        extends BaseRequestBuilder<RestfulRequest<?>, RestRequestBuilder> {

    private String baseUrl;
    private String resourcePath;

    private final MultivaluedMap<String, String> pathParams =
            new MultivaluedHashMap<>();

    private final MultivaluedMap<String, String> queryParams =
            new MultivaluedHashMap<>();

    private final MultivaluedMap<String, String> headers =
            new MultivaluedHashMap<>();

    private HTTP_METHOD method = HTTP_METHOD.POST;

    public RestRequestBuilder baseUrl(String url) {
        this.baseUrl = url;
        return this;
    }

    public RestRequestBuilder resourcePath(String path) {
        this.resourcePath = path;
        return this;
    }

    public RestRequestBuilder pathParam(String name, String value) {
        this.pathParams.add(name, value);
        return this;
    }

    public RestRequestBuilder queryParam(String name, String value) {
        this.queryParams.add(name, value);
        return this;
    }

    public RestRequestBuilder header(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    public RestRequestBuilder method(HTTP_METHOD method) {
        this.method = method;
        return this;
    }

    @Override
    protected RestRequestBuilder self() {
        return this;
    }

    @Override
    public RestfulRequest<?> build() {
        // validation

        URLConfig url =
                URLConfig.rest(baseUrl, resourcePath);

        HttpRequestConfig config =
                new HttpRequestConfig(
                        url,
                        auth,
                        connTimeout,
                        readTimeout,
                        method,
                        HeaderConfig.of(headers)
                );

        return new RestfulRequest(
                payload,
                config,
                pathParams,
                queryParams
        );
    }
}