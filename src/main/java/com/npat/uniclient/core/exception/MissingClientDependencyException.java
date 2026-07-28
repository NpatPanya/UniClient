package com.npat.uniclient.core.exception;

import java.util.Objects;

/**
 * Thrown when a required optional library is not on the classpath.
 *
 * Example: trying to use ServiceClient.APACHE_CXF without CXF installed.
 *
 * This is the project's core principle in action: "fail loud, don't fail to install."
 * When an engine needs a library, this exception tells the consumer exactly what to install.
 *
 * Thread-safe: immutable message construction.
 */
public class MissingClientDependencyException extends UniClientException {

    private final String engineName;
    private final String mavenCoordinate;

    /**
     * Creates an exception for a missing dependency.
     *
     * @param engineName human-readable engine name (e.g., "APACHE_CXF", "JACKSON")
     * @param mavenCoordinate Maven artifact coordinate (e.g., "org.apache.cxf:cxf-rt-frontend-jaxws:3.5.0")
     */
    public MissingClientDependencyException(String engineName, String mavenCoordinate) {
        super(buildMessage(engineName, mavenCoordinate));
        this.engineName = Objects.requireNonNull(engineName);
        this.mavenCoordinate = Objects.requireNonNull(mavenCoordinate);
    }

    /**
     * Standardized message format across all optional-dependency failures.
     * Format: "APACHE_CXF requires org.apache.cxf:cxf-rt-frontend-jaxws. Add it to pom.xml or classpath."
     */
    private static String buildMessage(String engineName, String mavenCoordinate) {
        return String.format(
            "%s requires %s. Add it to pom.xml with <scope>provided</scope> or ensure it's on the classpath. " +
            "See docs/OPTIONAL_DEPENDENCIES.md for details.",
            engineName, mavenCoordinate
        );
    }

    public String engineName() { return engineName; }
    public String mavenCoordinate() { return mavenCoordinate; }
}
