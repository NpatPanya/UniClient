package com.npat.uniclient.core.support;

import com.npat.uniclient.core.exception.MissingClientDependencyException;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import java.util.Objects;

/**
 * Helper for checking optional dependencies before instantiating adapters.
 *
 * Used by the adapter registry (Plan 2 Sub 2.3) to fail loud with a clear message
 * if an engine's required library isn't on the classpath.
 *
 * Thread-safe: pure helper, no state.
 */
public final class DependencyRequirement {

    private DependencyRequirement() {
        // Utility class
    }

    /**
     * Requires a dependency to be available; throws if not.
     *
     * @param availability the availability port (e.g., ClasspathDependencyAvailability)
     * @param fullyQualifiedClassName the class to probe for (e.g., "org.apache.cxf.endpoint.Client")
     * @param engineName human-readable engine name (e.g., "APACHE_CXF")
     * @param mavenCoordinate Maven artifact (e.g., "org.apache.cxf:cxf-rt-frontend-jaxws:3.5.0")
     * @throws MissingClientDependencyException if the class is not available
     */
    public static void require(DependencyAvailabilityPort availability,
                               String fullyQualifiedClassName,
                               String engineName,
                               String mavenCoordinate) {
        Objects.requireNonNull(availability, "availability");
        Objects.requireNonNull(fullyQualifiedClassName, "fullyQualifiedClassName");
        Objects.requireNonNull(engineName, "engineName");
        Objects.requireNonNull(mavenCoordinate, "mavenCoordinate");

        if (!availability.isAvailable(fullyQualifiedClassName)) {
            throw new MissingClientDependencyException(engineName, mavenCoordinate);
        }
    }
}
