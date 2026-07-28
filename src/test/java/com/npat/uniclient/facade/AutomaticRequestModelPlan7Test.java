package com.npat.uniclient.facade;

import com.npat.uniclient.core.model.AuthConfig;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.model.SslConfig;
import com.npat.uniclient.core.model.SoapRequestConfig;
import com.npat.uniclient.core.model.TimeoutConfig;
import java.time.Duration;

/** Executable tests for the automatic request-encoding model. */
public final class AutomaticRequestModelPlan7Test {

    public static void main(String[] args) {
        requestStoresSoapMetadataAndWithBodyPreservesConfiguration();
    }

    private static void requestStoresSoapMetadataAndWithBodyPreservesConfiguration() {
        TimeoutConfig timeout = TimeoutConfig.of(
            Duration.ofSeconds(1), Duration.ofSeconds(2), 1, Duration.ZERO);
        AuthConfig auth = AuthConfig.bearer("token");
        SslConfig ssl = SslConfig.platformDefault();
        SoapRequestConfig soap = new SoapRequestConfig(
            "urn:orders", "CreateOrder", "urn:orders:CreateOrder");

        RequestSpec original = RequestSpec.builder()
            .to("https://example.test/orders")
            .body(new Order("A-17"))
            .header("X-Test", "value")
            .timeout(timeout)
            .auth(auth)
            .ssl(ssl)
            .httpMethod("PUT")
            .soap(soap)
            .build();
        byte[] encoded = "encoded".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        RequestSpec wire = original.withBody(encoded);

        assertSame(encoded, wire.body(), "body");
        assertEquals(original.target(), wire.target(), "target");
        assertEquals(original.headers(), wire.headers(), "headers");
        assertEquals(timeout, wire.timeout(), "timeout");
        assertEquals(auth, wire.auth(), "auth");
        assertEquals(ssl, wire.ssl(), "ssl");
        assertEquals("PUT", wire.httpMethod(), "method");
        assertEquals(soap, wire.soap(), "soap metadata");
    }

    private static final class Order {
        private final String id;

        private Order(String id) {
            this.id = id;
        }
    }

    private static void assertSame(Object expected, Object actual, String name) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected the same instance");
        }
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }
}
