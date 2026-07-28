package com.npat.uniclient.plan7;

import com.npat.uniclient.adapter.StandardClientFactory;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.facade.ClientFacade;

/** Executable tests for the standard automatic-encoding composition. */
public final class StandardClientFactoryPlan7Test {

    public static void main(String[] args) {
        DependencyAvailabilityPort unavailable = className -> false;
        ClientFacade client = StandardClientFactory.create(unavailable);
        if (client == null) {
            throw new AssertionError("Standard client factory returned null");
        }
    }
}
