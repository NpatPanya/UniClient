package com.npat.uniclient.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, thread-safe map of HTTP headers.
 * SRP: header value storage and access only.
 */
public final class HeaderSet {
    private final Map<String, String> headers;

    private HeaderSet(Map<String, String> headers) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
    }

    /**
     * Returns an empty HeaderSet.
     */
    public static HeaderSet empty() {
        return new HeaderSet(Map.of());
    }

    /**
     * Creates a HeaderSet from the given map.
     * @param headers header name → value map; may be empty or null (treated as empty)
     */
    public static HeaderSet of(Map<String, String> headers) {
        return new HeaderSet(headers != null ? headers : Map.of());
    }

    /**
     * Returns the value for a header name, or empty String if not present.
     */
    public String get(String name) {
        return headers.getOrDefault(name, "");
    }

    /**
     * Returns a view of all headers.
     */
    public Map<String, String> all() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HeaderSet)) return false;
        HeaderSet that = (HeaderSet) o;
        return headers.equals(that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers);
    }

    @Override
    public String toString() {
        return "HeaderSet{" + headers + '}';
    }
}
