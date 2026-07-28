package com.npat.uniclient.adapter.codec;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.exception.UniClientException;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.model.SoapRequestConfig;
import com.npat.uniclient.core.port.PayloadCodecPort;
import com.npat.uniclient.core.port.RequestEncoderPort;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Encodes logical request bodies for each standard transport strategy.
 */
public final class DefaultRequestEncoder implements RequestEncoderPort {
    private final PayloadCodecPort jsonCodec;

    public DefaultRequestEncoder(PayloadCodecPort jsonCodec) {
        this.jsonCodec = Objects.requireNonNull(jsonCodec, "jsonCodec");
    }

    @Override
    public byte[] encode(ServiceClient engine, RequestSpec request) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(request, "request");

        Object body = request.body();
        if (body instanceof byte[] bytes) {
            return bytes.clone();
        }
        if (body instanceof String text) {
            return text.getBytes(StandardCharsets.UTF_8);
        }

        return switch (engine) {
            case REST_CLIENT, HTTPURLCONNECTION -> jsonCodec.serialize(body);
            case APACHE_CXF -> encodeSoap(request, body);
        };
    }

    private static byte[] encodeSoap(RequestSpec request, Object body) {
        SoapRequestConfig config = request.soap();
        if (config == null) {
            throw new UniClientException(
                "SOAP metadata is required when Apache CXF encodes a POJO request");
        }
        SoapEnvelopeMetadata metadata = new SoapEnvelopeMetadata(
            config.namespaceUri(), config.operationName(), config.action());
        return new SoapEnvelopeCodec(metadata).serialize(body);
    }
}
