package com.npat.uniclient.dto;

public class GWRestfulResponse<B> extends RestfulResponse<B> {
    // This class extends RestfulResponse with GWRestfulHeader as the header entity type
    private GWRestfulHeader rspHeaderEntity;    // e.g. GWRestfulHeader

    public GWRestfulHeader getRspHeaderEntity() {
        return rspHeaderEntity;
    }

    public void setRspHeaderEntity(GWRestfulHeader rspHeaderEntity) {
        this.rspHeaderEntity = rspHeaderEntity;
    }
}
