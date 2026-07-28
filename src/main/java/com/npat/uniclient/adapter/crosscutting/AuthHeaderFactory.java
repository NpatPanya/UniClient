package com.npat.uniclient.adapter.crosscutting;

import com.npat.uniclient.core.model.AuthConfig;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Converts authentication configuration into an Authorization header entry.
 */
public final class AuthHeaderFactory {
    private AuthHeaderFactory() {
    }

    public static Optional<Map.Entry<String, String>> from(AuthConfig config) {
        if (config == null || config.type() == AuthConfig.AuthType.NONE) {
            return Optional.empty();
        }
        String value = switch (config.type()) {
            case BEARER -> "Bearer " + config.value();
            case BASIC -> "Basic " + Base64.getEncoder().encodeToString(
                (config.username() + ":" + config.password()).getBytes(StandardCharsets.UTF_8));
            case NONE -> throw new IllegalStateException("NONE auth was not handled");
        };
        return Optional.of(Map.entry("Authorization", value));
    }
}
