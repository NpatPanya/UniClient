package com.npat.uniclient.domain;

import java.util.Objects;

public record SocketFormat(String identifier) {
    public SocketFormat {
        Objects.requireNonNull(identifier, "identifier");
        if (identifier.isBlank()) {
            throw new IllegalArgumentException("Socket format identifier must not be blank");
        }
    }

    public static SocketFormat of(String identifier) {
        return new SocketFormat(identifier);
    }
}