package com.npat.uniclient.facade;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.exception.MissingClientDependencyException;
import com.npat.uniclient.core.exception.UniClientException;
import com.npat.uniclient.core.model.AuthConfig;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.model.SslConfig;
import com.npat.uniclient.core.model.TimeoutConfig;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.core.port.TransportPort;
import java.net.URI;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * JDK-only executable tests for Plan 2. Run the class after Maven compiles test sources.
 */
public final class ApplicationLayerPlan2Test {

    public static void main(String[] args) {
        requestBuilderStoresAllConfiguredValues();
        requestBuilderRejectsMissingTargetWithCoreException();
        facadeReturnsTheTransportResponseUnchanged();
        registryGuardsCxfBeforeCreatingItsAdapter();
        registryDoesNotProbeTheGuardForHttpUrlConnection();
        registryRequiresOneFactoryForEachServiceClient();
    }

    private static void requestBuilderStoresAllConfiguredValues() {
        TimeoutConfig timeout = TimeoutConfig.of(
            Duration.ofSeconds(2), Duration.ofSeconds(5), 2, Duration.ofMillis(50));
        AuthConfig auth = AuthConfig.bearer("token");
        SslConfig ssl = SslConfig.custom(null, null, "truststore.jks", "password");
        Object body = new Object();

        RequestSpec spec = RequestSpec.builder()
            .to("https://example.test/orders")
            .body(body)
            .header("X-Request-Id", "request-1")
            .timeout(timeout)
            .auth(auth)
            .ssl(ssl)
            .httpMethod("PUT")
            .build();

        assertEquals(URI.create("https://example.test/orders"), spec.target(), "target");
        assertSame(body, spec.body(), "body");
        assertEquals("request-1", spec.headers().get("X-Request-Id"), "header");
        assertEquals(timeout, spec.timeout(), "timeout");
        assertEquals(auth, spec.auth(), "auth");
        assertEquals(ssl, spec.ssl(), "ssl");
        assertEquals("PUT", spec.httpMethod(), "method");
    }

    private static void requestBuilderRejectsMissingTargetWithCoreException() {
        assertThrows(UniClientException.class, RequestSpec.builder()::build);
    }

    private static void facadeReturnsTheTransportResponseUnchanged() {
        ClientResponse expected = ClientResponse.builder().statusCode(200).body("ok").build();
        AtomicInteger resolveCalls = new AtomicInteger();
        AdapterResolver resolver = engine -> {
            resolveCalls.incrementAndGet();
            return spec -> expected;
        };

        ClientResponse actual = new ClientFacade(resolver).execute(
            RequestSpec.builder().to("https://example.test").build(),
            ServiceClient.HTTPURLCONNECTION);

        assertSame(expected, actual, "facade response");
        assertEquals(1, resolveCalls.get(), "resolver calls");
    }

    private static void registryGuardsCxfBeforeCreatingItsAdapter() {
        CountingAvailability availability = new CountingAvailability(false);
        AtomicInteger factoryCalls = new AtomicInteger();
        AdapterRegistry registry = registry(availability, factoryCalls);

        MissingClientDependencyException failure = assertThrows(
            MissingClientDependencyException.class,
            () -> registry.resolve(ServiceClient.APACHE_CXF));

        assertContains(failure.getMessage(), "org.apache.cxf:cxf-rt-frontend-jaxws", "CXF coordinate");
        assertEquals(0, factoryCalls.get(), "CXF factory calls");
        assertEquals(1, availability.probes.get(), "dependency probes");
    }

    private static void registryDoesNotProbeTheGuardForHttpUrlConnection() {
        CountingAvailability availability = new CountingAvailability(false);
        AdapterRegistry registry = registry(availability, new AtomicInteger());

        TransportPort resolved = registry.resolve(ServiceClient.HTTPURLCONNECTION);

        assertEquals(0, availability.probes.get(), "dependency probes");
        if (resolved == null) {
            throw new AssertionError("HTTPURLCONNECTION factory returned null");
        }
    }

    private static void registryRequiresOneFactoryForEachServiceClient() {
        EnumMap<ServiceClient, Supplier<TransportPort>> incomplete = new EnumMap<>(ServiceClient.class);
        incomplete.put(ServiceClient.HTTPURLCONNECTION, () -> spec -> ClientResponse.builder()
            .statusCode(200).build());

        assertThrows(UniClientException.class, () -> new AdapterRegistry(
            classpathAvailability(), incomplete));
    }

    private static AdapterRegistry registry(CountingAvailability availability, AtomicInteger factoryCalls) {
        EnumMap<ServiceClient, Supplier<TransportPort>> factories = new EnumMap<>(ServiceClient.class);
        for (ServiceClient client : ServiceClient.values()) {
            factories.put(client, () -> {
                factoryCalls.incrementAndGet();
                return spec -> ClientResponse.builder().statusCode(200).build();
            });
        }
        return new AdapterRegistry(availability, factories);
    }

    private static DependencyAvailabilityPort classpathAvailability() {
        return className -> true;
    }

    private static <T extends Throwable> T assertThrows(Class<T> type, ThrowingAction action) {
        try {
            action.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) {
                return type.cast(failure);
            }
            throw new AssertionError("Expected " + type.getName() + " but got "
                + failure.getClass().getName(), failure);
        }
        throw new AssertionError("Expected " + type.getName() + " to be thrown");
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!expected.equals(actual)) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertSame(Object expected, Object actual, String name) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected the same instance");
        }
    }

    private static void assertContains(String actual, String expected, String name) {
        if (actual == null || !actual.contains(expected)) {
            throw new AssertionError(name + ": expected '" + expected + "' in '" + actual + "'");
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run();
    }

    private static final class CountingAvailability implements DependencyAvailabilityPort {
        private final boolean available;
        private final AtomicInteger probes = new AtomicInteger();

        private CountingAvailability(boolean available) {
            this.available = available;
        }

        @Override
        public boolean isAvailable(String fullyQualifiedClassName) {
            probes.incrementAndGet();
            return available;
        }
    }
}
