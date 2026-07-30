package com.npat.uniclient.dto.componenet;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Objects;

public class HeaderConfig {

    public final MultivaluedMap<String, String> headers;


    private HeaderConfig(MultivaluedMap<String, String> headers) {
        this.headers = new MultivaluedHashMap<>(headers);
    }


    public static HeaderConfig empty() {
        return new HeaderConfig(new MultivaluedHashMap<>());
    }

    public static HeaderConfig of(MultivaluedMap<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("Headers must not be null or empty");
        }
        return new HeaderConfig(headers);
    }

    public String getFirst(String header) {
        return headers.getFirst(header);
    }

    public List<String> get(String header) {
        return headers.get(header);
    }

    public MultivaluedMap<String, String> all() {
        return new MultivaluedHashMap<>(headers);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        HeaderConfig that = (HeaderConfig) object;
        return Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(headers);
    }
}
