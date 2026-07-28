package com.npat.uniclient;

/**
 * Selects the transport strategy used to execute a request.
 */
public enum ServiceClient {
    REST_CLIENT,
    APACHE_CXF,
    HTTPURLCONNECTION
}
