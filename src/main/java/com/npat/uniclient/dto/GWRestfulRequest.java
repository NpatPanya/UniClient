package com.npat.uniclient.dto;

import com.npat.uniclient.dto.base.HttpRequestConfig;
import jakarta.ws.rs.core.MultivaluedMap;

public class GWRestfulRequest<B> extends RestfulRequest<B, Object> {

    private GWRestfulHeader rqtHeaderEntity;

    public GWRestfulRequest(B payload, HttpRequestConfig config, MultivaluedMap<String, String> pathParams,
                            MultivaluedMap<String, String> queryParams, GWRestfulHeader rqtHeaderEntity) {
        super(payload, config, pathParams, queryParams, null);
        this.rqtHeaderEntity = rqtHeaderEntity;
    }

    public GWRestfulHeader getRqtHeaderEntity() { return rqtHeaderEntity; }
    public void setRqtHeaderEntity(GWRestfulHeader rqtHeaderEntity) { this.rqtHeaderEntity = rqtHeaderEntity; }
}