package com.npat.uniclient.dto;

import jakarta.ws.rs.core.MultivaluedMap;

public class RestfulResponse<T> extends APIResponse<T> {
    private String rawPayload;
    private String contentType;
    private MultivaluedMap<String, String> httpHeaders;

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public MultivaluedMap<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(MultivaluedMap<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
}