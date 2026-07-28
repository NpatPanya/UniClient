package com.npat.uniclient.facade;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.port.TransportPort;

/**
 * Application-layer port for resolving a transport strategy.
 */
@FunctionalInterface
public interface AdapterResolver {

    /**
     * Resolves the transport for an engine selection.
     *
     * @param engine selected transport strategy
     * @return transport implementation for the strategy
     */
    TransportPort resolve(ServiceClient engine);
}
