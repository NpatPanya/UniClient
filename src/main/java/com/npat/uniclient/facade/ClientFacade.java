package com.npat.uniclient.facade;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import java.util.Objects;

/**
 * Application use case that resolves a transport and executes one request.
 */
public final class ClientFacade {
    private final AdapterResolver resolver;

    /**
     * Creates a facade with an injectable transport resolver.
     *
     * @param resolver strategy resolver, typically an AdapterRegistry
     */
    public ClientFacade(AdapterResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * Executes a request through the selected transport strategy.
     *
     * @param spec request to execute
     * @param engine selected transport strategy
     * @return response returned by the transport
     */
    public ClientResponse execute(RequestSpec spec, ServiceClient engine) {
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(engine, "engine");
        return resolver.resolve(engine).execute(spec);
    }
}
