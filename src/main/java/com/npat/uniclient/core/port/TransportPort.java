package com.npat.uniclient.core.port;

import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.exception.ClientTransportException;

/**
 * Port for sending a request over a network transport and receiving a response.
 * One reason to change: how bytes physically go out and come back (HTTP, SOAP, etc).
 *
 * Implementations: HttpURLConnectionAdapter, RestClientAdapter, ApacheCxfAdapter.
 * The core never imports a concrete adapter — it receives one via dependency injection.
 *
 * Thread-safety: implementations must be thread-safe for high-throughput scenarios.
 * All state should be immutable or synchronized.
 */
public interface TransportPort {

    /**
     * Sends a request and returns a response.
     *
     * @param spec the request specification (target, headers, body, config)
     * @return the response (status, headers, body bytes)
     * @throws ClientTransportException on I/O, protocol, timeout, or connectivity failure
     * @throws com.npat.uniclient.core.exception.MissingClientDependencyException if a required
     *         optional transport library is not on the classpath (e.g. Apache CXF for SOAP)
     */
    ClientResponse execute(RequestSpec spec) throws ClientTransportException;
}
