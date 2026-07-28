package com.npat.uniclient.adapter.crosscutting;

import com.npat.uniclient.core.model.RequestSpec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creates correlation and optional service metadata headers for one request.
 */
public final class MetadataHeaderFactory {
    public static final String CORRELATION_ID = "X-Correlation-ID";

    private final String serviceName;
    private final String serviceVersion;

    public MetadataHeaderFactory() {
        this(null, null);
    }

    public MetadataHeaderFactory(String serviceName, String serviceVersion) {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
    }

    public Map<String, String> standardHeaders(RequestSpec spec) {
        Map<String, String> headers = new LinkedHashMap<>();
        String correlationId = findIgnoreCase(spec.headers().all(), CORRELATION_ID);
        headers.put(CORRELATION_ID, correlationId == null ? UUID.randomUUID().toString() : correlationId);
        if (serviceName != null && !serviceName.isBlank()) {
            headers.put("X-Service-Name", serviceName);
        }
        if (serviceVersion != null && !serviceVersion.isBlank()) {
            headers.put("X-Service-Version", serviceVersion);
        }
        return headers;
    }

    private static String findIgnoreCase(Map<String, String> headers, String expected) {
        return headers.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(expected))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
}
