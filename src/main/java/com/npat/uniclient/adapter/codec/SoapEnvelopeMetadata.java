package com.npat.uniclient.adapter.codec;

import java.util.Objects;

/**
 * Immutable SOAP operation metadata supplied to {@link SoapEnvelopeCodec}.
 */
public final class SoapEnvelopeMetadata {
    private final String namespaceUri;
    private final String operationName;
    private final String action;

    public SoapEnvelopeMetadata(String namespaceUri, String operationName, String action) {
        this.namespaceUri = requireText(namespaceUri, "namespaceUri");
        this.operationName = requireXmlName(operationName);
        this.action = requireText(action, "action");
    }

    public String namespaceUri() {
        return namespaceUri;
    }

    public String operationName() {
        return operationName;
    }

    public String action() {
        return action;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String requireXmlName(String value) {
        requireText(value, "operationName");
        if (!value.matches("[A-Za-z_][A-Za-z0-9_.-]*")) {
            throw new IllegalArgumentException("operationName is not a valid XML name: " + value);
        }
        return value;
    }
}
