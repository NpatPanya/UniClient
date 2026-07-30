package com.npat.uniclient.facade;

import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;
import com.npat.uniclient.exception.TransportException;
import com.npat.uniclient.port.JsonCodec;
import com.npat.uniclient.port.TransportAdapter;

import java.util.List;
import java.util.Objects;

/**
 * Resolves exactly one adapter for an already-built request.
 */
public final class AdapterRegistry {

    private final List<TransportAdapter> adapters;

    public AdapterRegistry(List<? extends TransportAdapter> adapters) {
        this.adapters = List.copyOf(Objects.requireNonNull(adapters, "adapters"));
    }

    public APIResponse<?> send(APIRequest<?, ?> request) {
        return send(request, null);
    }

    public APIResponse<?> send(APIRequest<?, ?> request, JsonCodec jsonCodec) {
        TransportAdapter adapter = adapters.stream()
                .filter(candidate -> candidate.supports(request))
                .findFirst()
                .orElseThrow(() -> new TransportException("No transport adapter supports the completed request"));
        adapter.dependencyAvailability().requireAvailable();
        return adapter.send(request, jsonCodec);
    }
}