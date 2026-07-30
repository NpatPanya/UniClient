package com.npat.uniclient.dto;

import com.npat.uniclient.domain.ResponseType;

/**
 * Base request paired with its concrete protocol response type.
 *
 * @param <P> request payload type
 * @param <R> paired response type
 */
public abstract class APIRequest<P, R extends APIResponse<?>> {

    private final P payload;
    private final ResponseType<?> responseType;

    protected APIRequest(P payload, ResponseType<?> responseType) {
        this.payload = payload;
        this.responseType = responseType;
    }

    public P getPayload() {
        return payload;
    }

    public ResponseType<?> getResponseType() {
        return responseType;
    }
}