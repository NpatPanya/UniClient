package com.npat.uniclient.port;

import com.npat.uniclient.exception.MissingDependencyException;

/** Checks only the dependency selected for a completed request. */
@FunctionalInterface
public interface DependencyAvailability {

    void requireAvailable();

    static DependencyAvailability available() {
        return () -> { };
    }

    static DependencyAvailability missing(String enablementGuidance) {
        return () -> { throw new MissingDependencyException(enablementGuidance); };
    }
}