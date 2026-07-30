package com.npat.uniclient.dto.componenet;

import java.util.Objects;

public final class TimeoutConfig {

    private final int connTimeout;
    private final int readTimeout;

    public TimeoutConfig(int connTimeout, int readTimeout) {
        validateInt(connTimeout, "connTimeout");
        validateInt(readTimeout, "readTimeout");
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TimeoutConfig that = (TimeoutConfig) object;
        return connTimeout == that.connTimeout && readTimeout == that.readTimeout;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connTimeout, readTimeout);
    }


    private static void validateInt(int value, String valueName) {
        if (value <= 0) {
            throw new IllegalArgumentException("Invalid " + valueName + " value");
        }
    }
}
