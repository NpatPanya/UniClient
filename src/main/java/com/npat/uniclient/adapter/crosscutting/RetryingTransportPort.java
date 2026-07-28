package com.npat.uniclient.adapter.crosscutting;

import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.TransportPort;
import java.time.Duration;
import java.util.Objects;

/**
 * Decorates a transport with bounded retries and interrupt-aware backoff.
 */
public final class RetryingTransportPort implements TransportPort {
    private final TransportPort delegate;

    public RetryingTransportPort(TransportPort delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public ClientResponse execute(RequestSpec spec) throws ClientTransportException {
        Objects.requireNonNull(spec, "spec");
        int maxAttempts = spec.timeout().maxRetries() + 1;
        ClientTransportException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return delegate.execute(spec);
            } catch (ClientTransportException failure) {
                lastFailure = failure;
                if (attempt == maxAttempts) {
                    throw failure;
                }
                sleep(spec.timeout().retryBackoff());
            }
        }
        throw lastFailure;
    }

    private static void sleep(Duration backoff) {
        try {
            long millis = Math.max(0, backoff.toMillis());
            Thread.sleep(millis);
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new ClientTransportException("Retry backoff was interrupted", failure);
        }
    }
}
