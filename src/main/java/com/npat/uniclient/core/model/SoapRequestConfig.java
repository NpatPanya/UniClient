package com.npat.uniclient.core.model;

import java.util.Objects;

/**
 * Immutable metadata required to construct one SOAP operation envelope.
 */
public final class SoapRequestConfig {
    private final String namespaceUri;
    private final String operationName;
    private final String action;

    public SoapRequestConfig(String namespaceUri, String operationName, String action) {
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
            throw new IllegalArgumentException(
                "operationName is not a valid XML name: " + value);
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SoapRequestConfig that)) {
            return false;
        }
        return namespaceUri.equals(that.namespaceUri)
            && operationName.equals(that.operationName)
            && action.equals(that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespaceUri, operationName, action);
    }

    @Override
    public String toString() {
        return "SoapRequestConfig{" + namespaceUri + ", " + operationName + "}";
    }
}
