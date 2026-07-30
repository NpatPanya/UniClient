package com.npat.uniclient.dto.componenet;

import java.util.Objects;

import static com.npat.uniclient.dto.componenet.AuthConfig.AuthType.*;

public final class AuthConfig {
    public final AuthType type;
    public final String token;
    public final String username;
    public final String password;


    public AuthConfig(AuthType type, String token, String username, String password) {
        this.type = type;
        this.token = token;
        this.username = username;
        this.password = password;
    }

    public enum AuthType {
        NONE, BEARER, BASIC
    }

    public static AuthConfig none() {
        return new AuthConfig(NONE, null, null, null);
    }


    public static AuthConfig bearer(String token) {
        validateString(token,"token");
        return new AuthConfig(BEARER, token, null, null);
    }

    public static AuthConfig basic(String username, String password) {
        validateString(username, "username");
        validateString(password, "password");
        return new AuthConfig(BASIC, null, username, password);
    }

    public AuthType getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        AuthConfig that = (AuthConfig) object;
        return type == that.type && Objects.equals(token, that.token) && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, token, username, password);
    }

    private static void validateString(String value, String valueName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(valueName + " can't be null or empty");
        }
    }


}
