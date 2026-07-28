package com.npat.uniclient.core.model;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Immutable HTTP response from a transport adapter.
 * Final class: thread-safe, immutable for high-throughput, no subclassing to preserve invariants.
 * SRP: response data storage and deserialization accessors only. No retry, no caching.
 */
public final class ClientResponse {
    private final int statusCode;
    private final byte[] rawBody;
    private final HeaderSet headers;
    private final String contentType;

    /**
     * Package-private constructor: only adapters create responses.
     */
    ClientResponse(int statusCode, byte[] rawBody, HeaderSet headers, String contentType) {
        this.statusCode = statusCode;
        this.rawBody = rawBody != null ? rawBody.clone() : new byte[0]; // Defensive copy
        this.headers = headers != null ? headers : HeaderSet.empty();
        this.contentType = contentType;
    }

    /**
     * Creates a new response builder for testing or adapter use.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Accessors (immutable getters)
    public int statusCode() { return statusCode; }
    public byte[] rawBody() { return rawBody.clone(); } // Defensive copy to maintain immutability
    public String bodyAsString() { return new String(rawBody, StandardCharsets.UTF_8); }
    public HeaderSet headers() { return headers; }
    public String contentType() { return contentType; }

    /**
     * Success check: HTTP 2xx status.
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Client error check: HTTP 4xx status.
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Server error check: HTTP 5xx status.
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientResponse)) return false;
        ClientResponse that = (ClientResponse) o;
        return statusCode == that.statusCode
            && java.util.Arrays.equals(rawBody, that.rawBody)
            && Objects.equals(headers, that.headers)
            && Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, java.util.Arrays.hashCode(rawBody), headers, contentType);
    }

    @Override
    public String toString() {
        return "ClientResponse{" +
            "status=" + statusCode +
            ", bodySize=" + rawBody.length +
            ", contentType='" + contentType + '\'' +
            '}';
    }

    // Builder for convenient test/adapter construction
    public static final class Builder {
        private int statusCode;
        private byte[] rawBody;
        private HeaderSet headers;
        private String contentType;

        public Builder statusCode(int code) {
            this.statusCode = code;
            return this;
        }

        public Builder body(byte[] body) {
            this.rawBody = body;
            return this;
        }

        public Builder body(String body) {
            this.rawBody = body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
            return this;
        }

        public Builder headers(HeaderSet headers) {
            this.headers = headers;
            return this;
        }

        public Builder contentType(String type) {
            this.contentType = type;
            return this;
        }

        public ClientResponse build() {
            if (statusCode == 0) {
                throw new IllegalArgumentException("statusCode must be set");
            }
            return new ClientResponse(statusCode, rawBody, headers, contentType);
        }
    }
}
