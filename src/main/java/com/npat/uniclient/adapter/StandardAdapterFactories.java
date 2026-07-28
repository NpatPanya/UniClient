package com.npat.uniclient.adapter;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.adapter.http.HttpURLConnectionAdapter;
import com.npat.uniclient.adapter.rest.RestClientAdapter;
import com.npat.uniclient.adapter.soap.ApacheCxfAdapter;
import com.npat.uniclient.core.port.TransportPort;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Composition helper that registers the three Plan 3 transport adapters with Plan 2's registry.
 */
public final class StandardAdapterFactories {
    private StandardAdapterFactories() {
    }

    /**
     * Returns one factory for each supported service-client strategy.
     */
    public static Map<ServiceClient, Supplier<TransportPort>> create() {
        EnumMap<ServiceClient, Supplier<TransportPort>> factories = new EnumMap<>(ServiceClient.class);
        factories.put(ServiceClient.HTTPURLCONNECTION, HttpURLConnectionAdapter::new);
        factories.put(ServiceClient.REST_CLIENT, RestClientAdapter::new);
        factories.put(ServiceClient.APACHE_CXF, ApacheCxfAdapter::new);
        return factories;
    }
}
