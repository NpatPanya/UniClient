package com.npat.uniclient.adapter;

import com.npat.uniclient.adapter.soap.ApacheCxfAdapter;
import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.adapter.crosscutting.RetryingTransportPort;
import com.npat.uniclient.facade.AdapterRegistry;
import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.TransportPort;

/**
 * Executable smoke tests for the optional CXF transport boundary.
 */
public final class ApacheCxfAdapterPlan3Test {

    public static void main(String[] args) {
        adapterImplementsTheTransportPort();
        adapterRejectsUnserializedObjectsBeforeOpeningCXF();
        standardFactoriesRegisterAllStrategies();
    }

    private static void adapterImplementsTheTransportPort() {
        if (!(new ApacheCxfAdapter() instanceof TransportPort)) {
            throw new AssertionError("ApacheCxfAdapter must implement TransportPort");
        }
    }

    private static void adapterRejectsUnserializedObjectsBeforeOpeningCXF() {
        RequestSpec spec = RequestSpec.builder()
            .to("http://127.0.0.1:1")
            .body(new Object())
            .build();
        try {
            new ApacheCxfAdapter().execute(spec);
        } catch (ClientTransportException expected) {
            return;
        }
        throw new AssertionError("Expected unsupported body to fail before CXF invocation");
    }
    private static void standardFactoriesRegisterAllStrategies() {
        AdapterRegistry registry = new AdapterRegistry(
            className -> true, StandardAdapterFactories.create());
        for (ServiceClient engine : ServiceClient.values()) {
            if (!(registry.resolve(engine) instanceof RetryingTransportPort)) {
                throw new AssertionError(engine + " was not registered with retry behavior");
            }
        }
    }
}
