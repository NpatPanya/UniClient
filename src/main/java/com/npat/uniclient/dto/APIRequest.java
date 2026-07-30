package com.npat.uniclient.dto;

/**
 * API Request DTO for capturing request data.
 *
 * @author 2521106332
 * @since 5 พ.ย. 2568
 */
public abstract class APIRequest<T> {

    private final T payload;

    protected APIRequest(T payload) {
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }

}
