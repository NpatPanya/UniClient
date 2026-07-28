package com.npat.uniclient.core.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable timeout configuration for connection and read operations.
 * SRP: timeout values only. No retry logic here.
 */
public final class TimeoutConfig {
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int maxRetries;
    private final Duration retryBackoff;

    private TimeoutConfig(Duration connectTimeout, Duration readTimeout, int maxRetries, Duration retryBackoff) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxRetries = maxRetries;
        this.retryBackoff = retryBackoff;
    }

    /**
     * Returns a default configuration: 10s connect, 30s read, no retries.
     */
    public static TimeoutConfig defaults() {
        return new TimeoutConfig(
            Duration.ofSeconds(10),
            Duration.ofSeconds(30),
            0,
            Duration.ofMillis(100)
        );
    }

    /**
     * Creates a custom configuration.
     */
    public static TimeoutConfig of(Duration connectTimeout, Duration readTimeout, int maxRetries, Duration retryBackoff) {
        Objects.requireNonNull(connectTimeout, "connectTimeout");
        Objects.requireNonNull(readTimeout, "readTimeout");
        if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must be >= 0");
        Objects.requireNonNull(retryBackoff, "retryBackoff");
        return new TimeoutConfig(connectTimeout, readTimeout, maxRetries, retryBackoff);
    }

    public Duration connectTimeout() { return connectTimeout; }
    public Duration readTimeout() { return readTimeout; }
    public int maxRetries() { return maxRetries; }
    public Duration retryBackoff() { return retryBackoff; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeoutConfig)) return false;
        TimeoutConfig that = (TimeoutConfig) o;
        return maxRetries == that.maxRetries
            && connectTimeout.equals(that.connectTimeout)
            && readTimeout.equals(that.readTimeout)
            && retryBackoff.equals(that.retryBackoff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectTimeout, readTimeout, maxRetries, retryBackoff);
    }

    @Override
    public String toString() {
        return "TimeoutConfig{" +
            "connect=" + connectTimeout +
            ", read=" + readTimeout +
            ", maxRetries=" + maxRetries +
            ", backoff=" + retryBackoff +
            '}';
    }
}
