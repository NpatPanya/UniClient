package com.npat.uniclient.adapter;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.adapter.http.HttpURLConnectionAdapter;
import com.npat.uniclient.adapter.rest.RestClientAdapter;
import com.npat.uniclient.core.exception.MissingClientDependencyException;
import com.npat.uniclient.core.port.TransportPort;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Composition helper that registers the three Plan 3 transport adapters with Plan 2's registry.
 */
public final class StandardAdapterFactories {
    private static final String CXF_ADAPTER =
        "com.npat.uniclient.adapter.soap.ApacheCxfAdapter";

    private StandardAdapterFactories() {
    }

    /**
     * Returns one factory for each supported service-client strategy.
     */
    public static Map<ServiceClient, Supplier<TransportPort>> create() {
        EnumMap<ServiceClient, Supplier<TransportPort>> factories = new EnumMap<>(ServiceClient.class);
        factories.put(ServiceClient.HTTPURLCONNECTION, HttpURLConnectionAdapter::new);
        factories.put(ServiceClient.REST_CLIENT, RestClientAdapter::new);
        factories.put(ServiceClient.APACHE_CXF, StandardAdapterFactories::newApacheCxfAdapter);
        return factories;
    }

    private static TransportPort newApacheCxfAdapter() {
        try {
            Class<?> adapterType = Class.forName(CXF_ADAPTER);
            return (TransportPort) adapterType.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException | LinkageError failure) {
            throw new MissingClientDependencyException(
                "APACHE_CXF", "org.apache.cxf:cxf-rt-frontend-jaxws");
        }
    }
}