package com.npat.uniclient.adapter;

import com.npat.uniclient.core.exception.ClientTransportException;
import java.nio.charset.StandardCharsets;

/**
 * Converts the already-serialized body forms accepted by transport adapters into bytes.
 */
public final class TransportBody {
    private TransportBody() {
    }

    public static byte[] toBytes(Object body) {
        if (body == null) {
            return new byte[0];
        }
        if (body instanceof byte[] bytes) {
            return bytes.clone();
        }
        if (body instanceof String text) {
            return text.getBytes(StandardCharsets.UTF_8);
        }
        throw new ClientTransportException(
            "Transport adapters accept only serialized byte[] or String bodies; "
                + "use a PayloadCodecPort for " + body.getClass().getName());
    }
}
