package com.npat.uniclient.dto.componenet;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Objects;

public final class HeaderConfig {

    private final MultivaluedMap<String, String> headers;

    private HeaderConfig(MultivaluedMap<String, String> headers) {
        this.headers = new MultivaluedHashMap<>(headers);
    }

    public static HeaderConfig empty() {
        return new HeaderConfig(new MultivaluedHashMap<>());
    }

    public static HeaderConfig of(MultivaluedMap<String, String> headers) {
        return headers == null ? empty() : new HeaderConfig(headers);
    }

    public String getFirst(String header) { return headers.getFirst(header); }
    public List<String> get(String header) { return headers.get(header); }
    public MultivaluedMap<String, String> all() { return new MultivaluedHashMap<>(headers); }

    @Override
    public boolean equals(Object object) {
        return object instanceof HeaderConfig that && Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() { return Objects.hashCode(headers); }
}