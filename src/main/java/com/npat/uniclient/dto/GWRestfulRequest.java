package com.npat.uniclient.dto;

import com.bbl.gw.common.dto.base.HttpRequestConfig;
import jakarta.ws.rs.core.MultivaluedMap;

public class GWRestfulRequest<B> extends RestfulRequest<B> {
    // This class extends RestfulRequest with GWRestfulHeader as the header entity type
    private GWRestfulHeader rqtHeaderEntity;        // e.g. GWRestfulHeader

    public GWRestfulRequest(B payload, HttpRequestConfig config, MultivaluedMap<String, String> pathParams, MultivaluedMap<String, String> queryParams, MultivaluedMap<String, String> httpHeaders, GWRestfulHeader rqtHeaderEntity) {
        super(payload, config, pathParams, queryParams, httpHeaders);
        this.rqtHeaderEntity = rqtHeaderEntity;
    }

    public GWRestfulHeader getRqtHeaderEntity() {
        return rqtHeaderEntity;
    }

    public void setRqtHeaderEntity(GWRestfulHeader rqtHeaderEntity) {
        this.rqtHeaderEntity = rqtHeaderEntity;
    }
}
