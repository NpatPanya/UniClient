package com.npat.uniclient.builder;

import com.bbl.gw.common.constant.HTTP_METHOD;
import com.bbl.gw.common.dto.SOAPRequest;
import com.bbl.gw.common.dto.base.HttpRequestConfig;
import com.bbl.gw.common.dto.componenet.HeaderConfig;
import com.bbl.gw.common.dto.componenet.URLConfig;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

public final class SoapRequestBuilder
        extends BaseRequestBuilder<SOAPRequest<?>, SoapRequestBuilder> {

    private String endpoint;
    private String soapAction;

    private final MultivaluedMap<String, String> headers =
            new MultivaluedHashMap<>();

    public SoapRequestBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public SoapRequestBuilder soapAction(String soapAction) {
        this.soapAction = soapAction;
        return this;
    }

    public SoapRequestBuilder header(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    @Override
    protected SoapRequestBuilder self() {
        return this;
    }

    @Override
    public SOAPRequest build() {

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("SOAP endpoint is required");
        }

        URLConfig urlConfig = URLConfig.soap(endpoint);

        HttpRequestConfig config =
                new HttpRequestConfig(
                        urlConfig,
                        auth,
                        connTimeout,
                        readTimeout,
                        HTTP_METHOD.POST,
                        HeaderConfig.of(headers)
                );

        return new SOAPRequest(
                payload,
                config,
                soapAction
        );
    }
}