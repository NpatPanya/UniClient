package com.npat.uniclient.crosscutting;

import com.npat.uniclient.adapter.crosscutting.AuthHeaderFactory;
import com.npat.uniclient.adapter.crosscutting.MetadataHeaderFactory;
import com.npat.uniclient.adapter.crosscutting.RequestHeaderAssembler;
import com.npat.uniclient.adapter.crosscutting.RetryingTransportPort;
import com.npat.uniclient.adapter.crosscutting.SslContextFactory;
import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.AuthConfig;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.model.SslConfig;
import com.npat.uniclient.core.model.TimeoutConfig;
import com.npat.uniclient.core.port.TransportPort;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLContext;

/**
 * Executable tests for Plan 5 cross-cutting adapters.
 */
public final class CrossCuttingAdaptersPlan5Test {

    public static void main(String[] args) throws Exception {
        sslFactoryBuildsThePlatformContext();
        sslFactoryLoadsACustomTrustStore();
        authFactoryBuildsBearerAndBasicHeaders();
        authFactoryReturnsEmptyForNoAuth();
        retryingTransportRetriesThenReturnsSuccess();
        retryingTransportRethrowsTheLastFailure();
        metadataFactoryGeneratesAndPropagatesCorrelationIds();
        headerAssemblerMergesAuthAndMetadata();
    }

    private static void sslFactoryBuildsThePlatformContext() {
        SSLContext context = SslContextFactory.from(SslConfig.platformDefault());
        if (context == null || context.getSocketFactory() == null) {
            throw new AssertionError("Platform SSL context was not created");
        }
    }

    private static void sslFactoryLoadsACustomTrustStore() throws Exception {
        Path trustStorePath = Files.createTempFile("uniclient-plan5", ".p12");
        try {
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(null, "changeit".toCharArray());
            try (OutputStream output = Files.newOutputStream(trustStorePath)) {
                trustStore.store(output, "changeit".toCharArray());
            }

            SSLContext context = SslContextFactory.from(SslConfig.custom(
                null, null, trustStorePath.toString(), "changeit"));
            if (context == null) {
                throw new AssertionError("Custom SSL context was not created");
            }
        } finally {
            Files.deleteIfExists(trustStorePath);
        }
    }

    private static void authFactoryBuildsBearerAndBasicHeaders() {
        Optional<Map.Entry<String, String>> bearer = AuthHeaderFactory.from(AuthConfig.bearer("abc"));
        Optional<Map.Entry<String, String>> basic = AuthHeaderFactory.from(AuthConfig.basic("user", "pass"));

        assertEquals("Authorization", bearer.orElseThrow().getKey(), "bearer name");
        assertEquals("Bearer abc", bearer.orElseThrow().getValue(), "bearer value");
        assertEquals("Basic dXNlcjpwYXNz", basic.orElseThrow().getValue(), "basic value");
    }

    private static void authFactoryReturnsEmptyForNoAuth() {
        if (AuthHeaderFactory.from(null).isPresent()
            || AuthHeaderFactory.from(AuthConfig.none()).isPresent()) {
            throw new AssertionError("No-auth configuration must produce no header");
        }
    }

    private static void retryingTransportRetriesThenReturnsSuccess() {
        AtomicInteger calls = new AtomicInteger();
        TransportPort flaky = spec -> {
            if (calls.incrementAndGet() < 3) {
                throw new ClientTransportException("temporary");
            }
            return ClientResponse.builder().statusCode(200).body("ok").build();
        };
        RequestSpec spec = RequestSpec.builder().to("https://example.test")
            .timeout(TimeoutConfig.of(Duration.ZERO, Duration.ofSeconds(1), 2, Duration.ZERO))
            .build();

        ClientResponse response = new RetryingTransportPort(flaky).execute(spec);

        assertEquals(3, calls.get(), "retry calls");
        assertEquals("ok", response.bodyAsString(), "retry response");
    }

    private static void retryingTransportRethrowsTheLastFailure() {
        AtomicInteger calls = new AtomicInteger();
        ClientTransportException last = new ClientTransportException("last");
        TransportPort failing = spec -> {
            calls.incrementAndGet();
            throw last;
        };
        RequestSpec spec = RequestSpec.builder().to("https://example.test")
            .timeout(TimeoutConfig.of(Duration.ZERO, Duration.ofSeconds(1), 2, Duration.ZERO))
            .build();

        ClientTransportException actual = assertThrows(
            ClientTransportException.class, () -> new RetryingTransportPort(failing).execute(spec));

        if (actual != last || calls.get() != 3) {
            throw new AssertionError("Retrying transport did not rethrow the last failure after three attempts");
        }
    }

    private static void metadataFactoryGeneratesAndPropagatesCorrelationIds() {
        MetadataHeaderFactory factory = new MetadataHeaderFactory("orders", "1.2");
        RequestSpec generatedSpec = RequestSpec.builder().to("https://example.test").build();
        Map<String, String> generated = factory.standardHeaders(generatedSpec);
        assertUuid(generated.get(MetadataHeaderFactory.CORRELATION_ID), "generated correlation ID");
        assertEquals("orders", generated.get("X-Service-Name"), "service name");
        assertEquals("1.2", generated.get("X-Service-Version"), "service version");

        RequestSpec suppliedSpec = RequestSpec.builder().to("https://example.test")
            .header(MetadataHeaderFactory.CORRELATION_ID, "caller-id").build();
        assertEquals("caller-id", factory.standardHeaders(suppliedSpec)
            .get(MetadataHeaderFactory.CORRELATION_ID), "supplied correlation ID");
    }

    private static void headerAssemblerMergesAuthAndMetadata() {
        RequestSpec spec = RequestSpec.builder().to("https://example.test")
            .auth(AuthConfig.bearer("token")).build();
        Map<String, String> headers = new RequestHeaderAssembler(
            new MetadataHeaderFactory("orders", "1.0")).assemble(spec);

        assertEquals("Bearer token", headers.get("Authorization"), "assembled auth");
        assertUuid(headers.get(MetadataHeaderFactory.CORRELATION_ID), "assembled correlation ID");
    }

    private static void assertUuid(String value, String name) {
        try {
            java.util.UUID.fromString(value);
        } catch (RuntimeException failure) {
            throw new AssertionError(name + " was not a UUID: " + value, failure);
        }
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
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}
