package com.npat.uniclient.core.support;

import com.npat.uniclient.core.port.DependencyAvailabilityPort;

/**
 * Default implementation of DependencyAvailabilityPort: probes the classpath using reflection.
 *
 * Uses Class.forName(name, false, classLoader) to check if a class exists without initializing it.
 * Never throws — always returns true/false safely.
 *
 * Thread-safe: Class.forName is thread-safe; no mutable state here.
 */
public final class ClasspathDependencyAvailability implements DependencyAvailabilityPort {

    private static final ClassLoader CLASS_LOADER = ClasspathDependencyAvailability.class.getClassLoader();

    /**
     * Checks if a fully-qualified class can be loaded from the classpath.
     *
     * @param fullyQualifiedClassName e.g., "org.apache.cxf.endpoint.Client"
     * @return true if the class exists, false otherwise (never throws)
     */
    @Override
    public boolean isAvailable(String fullyQualifiedClassName) {
        try {
            Class.forName(fullyQualifiedClassName, false, CLASS_LOADER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
