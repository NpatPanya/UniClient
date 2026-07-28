package com.npat.uniclient.core.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable SSL/TLS configuration (custom keystore/truststore paths and passwords).
 * SRP: SSL config storage only. SSLContext building happens in the cross-cutting adapter (Plan 5).
 */
public final class SslConfig {
    private final SslType type;
    private final String keystorePath;
    private final String keystorePassword;
    private final String truststorePath;
    private final String truststorePassword;
    private final String keyAlgorithm; // e.g., "SunX509"
    private final String trustAlgorithm; // e.g., "SunX509"
    private final String protocol; // e.g., "TLSv1.2"

    private SslConfig(SslType type, String keystorePath, String keystorePassword,
                      String truststorePath, String truststorePassword,
                      String keyAlgorithm, String trustAlgorithm, String protocol) {
        this.type = type;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.truststorePath = truststorePath;
        this.truststorePassword = truststorePassword;
        this.keyAlgorithm = keyAlgorithm;
        this.trustAlgorithm = trustAlgorithm;
        this.protocol = protocol;
    }

    /**
     * Returns a platform-default SSL configuration (no custom keystores).
     */
    public static SslConfig platformDefault() {
        return new SslConfig(SslType.PLATFORM_DEFAULT, null, null, null, null, null, null, null);
    }

    /**
     * Creates a custom SSL configuration with keystore and truststore paths.
     */
    public static SslConfig custom(String keystorePath, String keystorePassword,
                                   String truststorePath, String truststorePassword) {
        // At least one must be provided
        if (keystorePath == null && truststorePath == null) {
            throw new IllegalArgumentException("At least keystorePath or truststorePath must be provided");
        }
        return new SslConfig(SslType.CUSTOM, keystorePath, keystorePassword,
            truststorePath, truststorePassword, "SunX509", "SunX509", "TLSv1.2");
    }

    public SslType type() { return type; }
    public String keystorePath() { return keystorePath; }
    public String keystorePassword() { return keystorePassword; }
    public String truststorePath() { return truststorePath; }
    public String truststorePassword() { return truststorePassword; }
    public String keyAlgorithm() { return keyAlgorithm; }
    public String trustAlgorithm() { return trustAlgorithm; }
    public String protocol() { return protocol; }

    public enum SslType {
        PLATFORM_DEFAULT, CUSTOM
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SslConfig)) return false;
        SslConfig that = (SslConfig) o;
        return type == that.type
            && Objects.equals(keystorePath, that.keystorePath)
            && Objects.equals(keystorePassword, that.keystorePassword)
            && Objects.equals(truststorePath, that.truststorePath)
            && Objects.equals(truststorePassword, that.truststorePassword)
            && Objects.equals(keyAlgorithm, that.keyAlgorithm)
            && Objects.equals(trustAlgorithm, that.trustAlgorithm)
            && Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, keystorePath, keystorePassword, truststorePath,
            truststorePassword, keyAlgorithm, trustAlgorithm, protocol);
    }

    @Override
    public String toString() {
        return "SslConfig{" + type + '}';
    }
}
