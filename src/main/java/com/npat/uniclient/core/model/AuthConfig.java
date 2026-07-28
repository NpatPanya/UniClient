package com.npat.uniclient.core.model;

import java.util.Objects;

/**
 * Immutable authentication configuration (Bearer token or Basic auth).
 * SRP: auth credential storage only. Encoding (Base64) happens in the transport adapter.
 */
public final class AuthConfig {
    private final AuthType type;
    private final String value;
    private final String username; // For Basic auth
    private final String password; // For Basic auth

    private AuthConfig(AuthType type, String value, String username, String password) {
        this.type = type;
        this.value = value;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns a no-auth configuration (no Authorization header).
     */
    public static AuthConfig none() {
        return new AuthConfig(AuthType.NONE, null, null, null);
    }

    /**
     * Creates a Bearer token configuration.
     * @param token the bearer token (e.g., "abc123xyz")
     */
    public static AuthConfig bearer(String token) {
        Objects.requireNonNull(token, "token");
        return new AuthConfig(AuthType.BEARER, token, null, null);
    }

    /**
     * Creates a Basic authentication configuration.
     * @param username username
     * @param password password
     */
    public static AuthConfig basic(String username, String password) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        return new AuthConfig(AuthType.BASIC, null, username, password);
    }

    public AuthType type() { return type; }
    public String value() { return value; }
    public String username() { return username; }
    public String password() { return password; }

    public enum AuthType {
        NONE, BEARER, BASIC
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthConfig)) return false;
        AuthConfig that = (AuthConfig) o;
        return type == that.type
            && Objects.equals(value, that.value)
            && Objects.equals(username, that.username)
            && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, username, password);
    }

    @Override
    public String toString() {
        return "AuthConfig{" + type + '}';
    }
}
