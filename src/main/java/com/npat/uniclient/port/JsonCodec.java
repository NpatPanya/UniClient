package com.npat.uniclient.port;

import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.dto.RestfulResponse;

/** Optional JSON serialization boundary; implementations belong outside the core. */
public interface JsonCodec {
    byte[] encode(Object value);
    <T> T decode(String payload, ResponseType<T> responseType);
    <T> RestfulResponse<T> decodeResponseEnvelope(String payload, ResponseType<T> responseType);
}