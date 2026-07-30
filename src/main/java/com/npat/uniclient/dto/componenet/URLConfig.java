package com.npat.uniclient.dto.componenet;

import java.util.Objects;

public class URLConfig {

    private final String host;
    private final int port;
    private final String destURL;
    private final String baseURL;
    private final String resourcePath;

    private URLConfig(String host, int port, String destURL, String baseURL, String resourcePath) {
        this.host = host;
        this.port = port;
        this.destURL = destURL;
        this.baseURL = baseURL;
        this.resourcePath = resourcePath;
    }

    public static URLConfig socket(String host, int port) {
        validateString(host, "host");
        validateInt(port, "port");
        return new URLConfig(host, port, null, null, null);
    }

    public static URLConfig soap(String destURl) {
        validateString(destURl, "destURl");
        return new URLConfig(null, 0, destURl, null, null);
    }

    public static URLConfig rest(String baseURL, String resourcePath) {
        validateString(baseURL, "baseURL");
        validateString(resourcePath, "resourcePath");
        return new URLConfig(null, 0, null, baseURL, resourcePath);
    }

    public static URLConfig rest(String destURL) {
        validateString(destURL, "destURL");
        return new URLConfig(null, 0, destURL, null, null);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDestURL() {
        return destURL;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        URLConfig urLconfig = (URLConfig) object;
        return port == urLconfig.port && Objects.equals(host, urLconfig.host) && Objects.equals(destURL, urLconfig.destURL) && Objects.equals(baseURL, urLconfig.baseURL) && Objects.equals(resourcePath, urLconfig.resourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, destURL, baseURL, resourcePath);
    }

    private static void validateString(String value, String valueName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(valueName + " can't be null or empty");
        }
    }

    private static void validateInt(int value, String valueName) {
        if (value <= 0) {
            throw new IllegalArgumentException("Invalid " + valueName + " value");
        }
    }
}
