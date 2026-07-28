package com.npat.uniclient.adapter.codec;

import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.core.port.PayloadCodecPort;
import java.util.Objects;

/**
 * Selects Jackson when present and otherwise returns the dependency-free JSON codec.
 */
public final class PayloadCodecResolver {
    public static final String JACKSON_OBJECT_MAPPER = "com.fasterxml.jackson.databind.ObjectMapper";

    private final DependencyAvailabilityPort availability;

    public PayloadCodecResolver(DependencyAvailabilityPort availability) {
        this.availability = Objects.requireNonNull(availability, "availability");
    }

    public PayloadCodecPort resolveJson() {
        return availability.isAvailable(JACKSON_OBJECT_MAPPER)
            ? new JacksonJsonCodec()
            : new BuiltinJsonCodec();
    }
}
