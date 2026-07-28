package com.npat.uniclient.core.model;

import com.npat.uniclient.core.exception.UniClientException;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable request specification: target endpoint, body, headers, and configuration.
 * Thread-safe for high-throughput scenarios — all fields are final and immutable.
 * SRP: request data storage only. No I/O, no serialization, no retry logic.
 */
public final class RequestSpec {
    private final URI target;
    private final Object body;
    private final HeaderSet headers;
    private final TimeoutConfig timeout;
    private final AuthConfig auth;
    private final SslConfig ssl;
    private final String httpMethod;

    private RequestSpec(Builder b) {
        this.target = b.target;
        this.body = b.body;
        this.headers = b.headers != null ? b.headers : HeaderSet.empty();
        this.timeout = b.timeout != null ? b.timeout : TimeoutConfig.defaults();
        this.auth = b.auth != null ? b.auth : AuthConfig.none();
        this.ssl = b.ssl != null ? b.ssl : SslConfig.platformDefault();
        this.httpMethod = b.httpMethod != null ? b.httpMethod : "POST";
    }

    /**
     * Creates a new RequestSpec builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Accessors (immutable getters)
    public URI target() { return target; }
    public Object body() { return body; }
    public HeaderSet headers() { return headers; }
    public TimeoutConfig timeout() { return timeout; }
    public AuthConfig auth() { return auth; }
    public SslConfig ssl() { return ssl; }
    public String httpMethod() { return httpMethod; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestSpec)) return false;
        RequestSpec that = (RequestSpec) o;
        return Objects.equals(target, that.target)
            && Objects.equals(body, that.body)
            && Objects.equals(headers, that.headers)
            && Objects.equals(timeout, that.timeout)
            && Objects.equals(auth, that.auth)
            && Objects.equals(ssl, that.ssl)
            && Objects.equals(httpMethod, that.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, body, headers, timeout, auth, ssl, httpMethod);
    }

    @Override
    public String toString() {
        return "RequestSpec{" +
            "target=" + target +
            ", method=" + httpMethod +
            ", body=" + (body != null ? body.getClass().getSimpleName() : "null") +
            ", headers=" + headers.all().size() +
            '}';
    }

    // Builder for fluent, zero-boilerplate construction
    public static final class Builder {
        private URI target;
        private Object body;
        private HeaderSet headers;
        private TimeoutConfig timeout;
        private AuthConfig auth;
        private SslConfig ssl;
        private String httpMethod = "POST";

        public Builder to(String uri) {
            this.target = URI.create(Objects.requireNonNull(uri, "uri"));
            return this;
        }

        public Builder to(URI uri) {
            this.target = Objects.requireNonNull(uri, "uri");
            return this;
        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        public Builder header(String name, String value) {
            if (headers == null) {
                headers = HeaderSet.of(new HashMap<>());
            }
            // Rebuild with new header (HeaderSet is immutable, so we extract, modify, rebuild)
            Map<String, String> map = new HashMap<>(headers.all());
            map.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
            headers = HeaderSet.of(map);
            return this;
        }

        public Builder headers(Map<String, String> headersMap) {
            this.headers = HeaderSet.of(headersMap);
            return this;
        }

        public Builder timeout(TimeoutConfig timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }

        public Builder auth(AuthConfig auth) {
            this.auth = Objects.requireNonNull(auth, "auth");
            return this;
        }

        public Builder ssl(SslConfig ssl) {
            this.ssl = Objects.requireNonNull(ssl, "ssl");
            return this;
        }

        public Builder httpMethod(String method) {
            this.httpMethod = Objects.requireNonNull(method, "httpMethod");
            return this;
        }

        /**
         * Builds the RequestSpec. Target endpoint is required.
         * @throws UniClientException if target is not set
         */
        public RequestSpec build() {
            if (target == null) {
                throw new UniClientException(
                    "RequestSpec requires a target destination — call .to(uri) before .build()");
            }
            return new RequestSpec(this);
        }
    }
}
