package com.npat.uniclient.plan6;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.exception.PayloadCodecException;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.core.port.PayloadCodecPort;
import com.npat.uniclient.core.port.TransportPort;
import com.npat.uniclient.facade.ClientFacade;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dependency-free composition test for the core and application layers.
 *
 * <p>Run this executable test without a network, Jackson, or CXF on the runtime classpath.</p>
 */
public final class CoreApplicationPlan6Test {

    public static void main(String[] args) {
        FakePayloadCodec codec = new FakePayloadCodec();
        FakeTransport transport = new FakeTransport();
        FakeDependencyAvailability availability = new FakeDependencyAvailability(false);

        byte[] requestBody = codec.serialize(new Order("A-17"));
        RequestSpec spec = RequestSpec.builder()
            .to("https://example.test/orders")
            .body(requestBody)
            .build();

        ClientResponse response = new ClientFacade(engine -> transport)
            .execute(spec, ServiceClient.REST_CLIENT);

        assertEquals(201, response.statusCode(), "fake response status");
        assertEquals("order-created", response.bodyAsString(), "fake response body");
        assertEquals(1, transport.calls.get(), "fake transport calls");
        assertEquals("order:A-17", new String(transport.lastBody, StandardCharsets.UTF_8),
            "fake transport body");
        assertEquals("A-17", codec.deserialize(requestBody, Order.class).id, "fake codec round trip");
        if (availability.isAvailable("com.fasterxml.jackson.databind.ObjectMapper")) {
            throw new AssertionError("Fake dependency availability should report Jackson absent");
        }
    }

    private static final class FakeTransport implements TransportPort {
        private final AtomicInteger calls = new AtomicInteger();
        private byte[] lastBody;

        @Override
        public ClientResponse execute(RequestSpec spec) {
            calls.incrementAndGet();
            lastBody = ((byte[]) spec.body()).clone();
            return ClientResponse.builder().statusCode(201).body("order-created").build();
        }
    }

    private static final class FakePayloadCodec implements PayloadCodecPort {
        @Override
        public byte[] serialize(Object body) {
            return ("order:" + ((Order) body).id).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public <T> T deserialize(byte[] body, Class<T> type) throws PayloadCodecException {
            if (type != Order.class) {
                throw new PayloadCodecException("Fake codec only supports Order");
            }
            return type.cast(new Order(new String(body, StandardCharsets.UTF_8).substring(6)));
        }
    }

    private static final class FakeDependencyAvailability implements DependencyAvailabilityPort {
        private final boolean available;

        private FakeDependencyAvailability(boolean available) {
            this.available = available;
        }

        @Override
        public boolean isAvailable(String fullyQualifiedClassName) {
            return available;
        }
    }

    private static final class Order {
        private final String id;

        private Order(String id) {
            this.id = id;
        }
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }
}
