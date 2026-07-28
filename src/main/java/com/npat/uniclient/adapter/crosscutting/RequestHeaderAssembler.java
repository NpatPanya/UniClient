package com.npat.uniclient.adapter.crosscutting;

import com.npat.uniclient.core.model.RequestSpec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Merges caller, authentication, and standard metadata headers at the transport boundary.
 */
public final class RequestHeaderAssembler {
    private final MetadataHeaderFactory metadataFactory;

    public RequestHeaderAssembler() {
        this(new MetadataHeaderFactory());
    }

    public RequestHeaderAssembler(MetadataHeaderFactory metadataFactory) {
        this.metadataFactory = Objects.requireNonNull(metadataFactory, "metadataFactory");
    }

    public Map<String, String> assemble(RequestSpec spec) {
        Map<String, String> headers = new LinkedHashMap<>(spec.headers().all());
        AuthHeaderFactory.from(spec.auth()).ifPresent(entry -> headers.put(entry.getKey(), entry.getValue()));
        headers.putAll(metadataFactory.standardHeaders(spec));
        return Map.copyOf(headers);
    }
}
