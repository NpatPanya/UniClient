package com.npat.uniclient.core.exception;

/**
 * Thrown when a TransportPort implementation encounters an I/O, protocol, timeout, or
 * connectivity failure during request/response.
 *
 * Examples: connection refused, read timeout, HTTP protocol error, SSL handshake failure.
 * Wraps the underlying cause (IOException, SSLException, etc.) so callers see a consistent
 * exception vocabulary regardless of the transport engine.
 */
public class ClientTransportException extends UniClientException {

    public ClientTransportException(String message) {
        super(message);
    }

    public ClientTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientTransportException(Throwable cause) {
        super(cause);
    }
}
