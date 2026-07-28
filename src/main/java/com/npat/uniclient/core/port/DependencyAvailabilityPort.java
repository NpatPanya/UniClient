package com.npat.uniclient.core.port;

/**
 * Port for probing whether a runtime dependency (optional library) is on the classpath.
 * One reason to change: how the library detects if Apache CXF, Jackson, etc. are available.
 *
 * Implementation: ClasspathDependencyAvailability (reflection-based, using Class.forName).
 *
 * Thread-safety: probing must be thread-safe for high-throughput scenarios.
 * Implementations should use the ClassLoader safely.
 */
public interface DependencyAvailabilityPort {

    /**
     * Checks whether a class exists on the classpath (i.e., a dependency is available).
     *
     * @param fullyQualifiedClassName fully qualified class name (e.g., "org.apache.cxf.endpoint.Client")
     * @return true if the class can be loaded, false otherwise (never throws)
     */
    boolean isAvailable(String fullyQualifiedClassName);
}
