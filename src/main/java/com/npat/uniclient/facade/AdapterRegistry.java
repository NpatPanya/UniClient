package com.npat.uniclient.facade;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.exception.UniClientException;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.core.port.TransportPort;
import com.npat.uniclient.core.support.DependencyRequirement;
import com.npat.uniclient.adapter.crosscutting.RetryingTransportPort;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Resolves each service-client strategy through its injected adapter factory.
 *
 * <p>The registry owns strategy selection, optional-dependency checks, and the standard retry
 * decoration. Concrete transport adapters are supplied by the composition root.</p>
 */
public final class AdapterRegistry implements AdapterResolver {
    private static final String CXF_CLASS_NAME = "org.apache.cxf.endpoint.Client";
    private static final String CXF_ENGINE_NAME = "APACHE_CXF";
    private static final String CXF_MAVEN_COORDINATE = "org.apache.cxf:cxf-rt-frontend-jaxws";

    private final DependencyAvailabilityPort availability;
    private final EnumMap<ServiceClient, Supplier<TransportPort>> factories;

    /**
     * Creates a registry with one factory for every supported service-client strategy.
     *
     * @param availability optional-dependency availability port
     * @param adapterFactories strategy-to-adapter factories
     * @throws UniClientException if any ServiceClient value has no factory
     */
    public AdapterRegistry(DependencyAvailabilityPort availability,
                           Map<ServiceClient, Supplier<TransportPort>> adapterFactories) {
        this.availability = Objects.requireNonNull(availability, "availability");
        Objects.requireNonNull(adapterFactories, "adapterFactories");
        this.factories = new EnumMap<>(ServiceClient.class);

        for (ServiceClient engine : ServiceClient.values()) {
            Supplier<TransportPort> factory = adapterFactories.get(engine);
            if (factory == null) {
                throw new UniClientException("AdapterRegistry requires a factory for " + engine);
            }
            this.factories.put(engine, factory);
        }

        if (adapterFactories.size() != ServiceClient.values().length) {
            throw new UniClientException("AdapterRegistry contains an unknown service-client factory");
        }
    }

    @Override
    public TransportPort resolve(ServiceClient engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine == ServiceClient.APACHE_CXF) {
            DependencyRequirement.require(
                availability, CXF_CLASS_NAME, CXF_ENGINE_NAME, CXF_MAVEN_COORDINATE);
        }
        TransportPort adapter = Objects.requireNonNull(factories.get(engine).get(),
            "Adapter factory returned null for " + engine);
        return new RetryingTransportPort(adapter);
    }
}
