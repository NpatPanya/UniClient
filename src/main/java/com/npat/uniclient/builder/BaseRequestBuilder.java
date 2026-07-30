package com.npat.uniclient.builder;

import com.npat.uniclient.dto.base.BaseRequestConfig;
import com.npat.uniclient.dto.componenet.AuthConfig;
import com.npat.uniclient.exception.RequestValidationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.net.URI;
import java.time.Duration;

abstract class BaseRequestBuilder<B extends BaseRequestBuilder<B>> {

    protected AuthConfig auth;
    protected Duration connTimeout;
    protected Duration readTimeout;
    protected long maxResponseBytes = BaseRequestConfig.DEFAULT_MAX_RESPONSE_BYTES;

    public B auth(AuthConfig auth) {
        this.auth = auth;
        return self();
    }

    public B connTimeout(Duration timeout) {
        this.connTimeout = timeout;
        return self();
    }

    public B readTimeout(Duration timeout) {
        this.readTimeout = timeout;
        return self();
    }

    public B maxResponseBytes(long maxResponseBytes) {
        this.maxResponseBytes = maxResponseBytes;
        return self();
    }

    protected abstract B self();

    protected final void validateTimeoutsAndResponseLimit() {
        validatePositive(connTimeout, "connection timeout");
        validatePositive(readTimeout, "read timeout");
        if (maxResponseBytes <= 0) {
            throw new RequestValidationException("max response bytes must be positive");
        }
    }

    protected final void validateHttpEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new RequestValidationException("HTTP endpoint is required");
        }
        try {
            URI uri = URI.create(endpoint);
            if (!uri.isAbsolute() || uri.getHost() == null
                    || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new RequestValidationException("HTTP endpoint must be an absolute http or https URI");
            }
        } catch (IllegalArgumentException exception) {
            throw new RequestValidationException("HTTP endpoint must be an absolute http or https URI");
        }
    }

    protected final void validateNoAuthorizationConflict(MultivaluedMap<String, String> headers) {
        if (auth == null || auth.getType() == AuthConfig.AuthType.NONE) {
            return;
        }
        for (String headerName : headers.keySet()) {
            if ("Authorization".equalsIgnoreCase(headerName)) {
                throw new RequestValidationException("structured authentication cannot be combined with an Authorization header");
            }
        }
    }

    protected final MultivaluedMap<String, String> copyHeadersWithJsonDefault(
            MultivaluedMap<String, String> headers, Object body) {
        MultivaluedMap<String, String> copied = new MultivaluedHashMap<>();
        copied.putAll(headers);
        if (requiresAutomaticJson(body) && !containsHeader(copied, "Content-Type")) {
            copied.add("Content-Type", "application/json");
        }
        return copied;
    }

    protected final boolean containsHeader(MultivaluedMap<String, String> headers, String name) {
        return headers.keySet().stream().anyMatch(existing -> name.equalsIgnoreCase(existing));
    }

    private boolean requiresAutomaticJson(Object body) {
        return body != null && !(body instanceof String) && !(body instanceof byte[]);
    }

    private void validatePositive(Duration timeout, String label) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new RequestValidationException(label + " must be positive");
        }
    }
}