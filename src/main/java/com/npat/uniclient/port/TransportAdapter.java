package com.npat.uniclient.port;

import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;

/**
 * A transport boundary selected from a completed protocol request.
 */
public interface TransportAdapter {

    boolean supports(APIRequest<?, ?> request);

    APIResponse<?> send(APIRequest<?, ?> request);

    default APIResponse<?> send(APIRequest<?, ?> request, JsonCodec jsonCodec) {
        return send(request);
    }

    default DependencyAvailability dependencyAvailability() {
        return DependencyAvailability.available();
    }
}