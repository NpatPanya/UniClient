package com.npat.uniclient.port;

import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;
import com.npat.uniclient.dto.SocketRequest;
import com.npat.uniclient.dto.SocketResponse;

/**
 * Neutral extension point for one caller-supplied socket protocol adapter.
 */
public interface SocketAdapter extends TransportAdapter {

    SocketResponse<?> send(SocketRequest<?, ?> request);

    @Override
    default boolean supports(APIRequest<?, ?> request) {
        return request instanceof SocketRequest<?, ?>;
    }

    @Override
    default APIResponse<?> send(APIRequest<?, ?> request) {
        return send((SocketRequest<?, ?>) request);
    }
}