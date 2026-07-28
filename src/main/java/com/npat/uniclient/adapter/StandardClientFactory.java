package com.npat.uniclient.adapter;

import com.npat.uniclient.adapter.codec.DefaultRequestEncoder;
import com.npat.uniclient.adapter.codec.PayloadCodecResolver;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.facade.AdapterRegistry;
import com.npat.uniclient.facade.ClientFacade;
import java.util.Objects;

/**
 * Builds the standard facade with transport selection and automatic body encoding.
 */
public final class StandardClientFactory {
    private StandardClientFactory() {
    }

    public static ClientFacade create(DependencyAvailabilityPort availability) {
        Objects.requireNonNull(availability, "availability");
        AdapterRegistry registry = new AdapterRegistry(
            availability, StandardAdapterFactories.create());
        DefaultRequestEncoder encoder = new DefaultRequestEncoder(
            new PayloadCodecResolver(availability).resolveJson());
        return new ClientFacade(registry, encoder);
    }
}
