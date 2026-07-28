package com.npat.uniclient.adapter.codec;

import com.npat.uniclient.core.exception.MissingClientDependencyException;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.core.port.PayloadCodecPort;
import java.util.Objects;

/**
 * Selects Jackson when present and otherwise returns the dependency-free JSON codec.
 */
public final class PayloadCodecResolver {
    public static final String JACKSON_OBJECT_MAPPER = "com.fasterxml.jackson.databind.ObjectMapper";
    private static final String JACKSON_CODEC =
        "com.npat.uniclient.adapter.codec.JacksonJsonCodec";

    private final DependencyAvailabilityPort availability;

    public PayloadCodecResolver(DependencyAvailabilityPort availability) {
        this.availability = Objects.requireNonNull(availability, "availability");
    }

    public PayloadCodecPort resolveJson() {
        if (!availability.isAvailable(JACKSON_OBJECT_MAPPER)) {
            return new BuiltinJsonCodec();
        }
        try {
            return (PayloadCodecPort) Class.forName(JACKSON_CODEC)
                .getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException | ClassCastException | LinkageError failure) {
            throw new MissingClientDependencyException(
                "JACKSON", "com.fasterxml.jackson.core:jackson-databind");
        }
    }
}