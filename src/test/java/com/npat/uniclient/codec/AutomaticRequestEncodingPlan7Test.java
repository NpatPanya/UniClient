package com.npat.uniclient.codec;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.adapter.codec.BuiltinJsonCodec;
import com.npat.uniclient.adapter.codec.DefaultRequestEncoder;
import com.npat.uniclient.core.exception.UniClientException;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.model.SoapRequestConfig;
import java.nio.charset.StandardCharsets;

/** Executable tests for automatic request body encoding. */
public final class AutomaticRequestEncodingPlan7Test {

    public static void main(String[] args) {
        jsonPojoUsesTheDependencyFreeCodecWhenJacksonIsAbsent();
        cxfPojoBecomesACompleteSoapEnvelope();
        rawBodiesPassThroughUnchanged();
        cxfPojoRequiresSoapMetadata();
    }

    private static void jsonPojoUsesTheDependencyFreeCodecWhenJacksonIsAbsent() {
        DefaultRequestEncoder encoder = new DefaultRequestEncoder(new BuiltinJsonCodec());
        RequestSpec request = RequestSpec.builder()
            .to("https://example.test/orders")
            .body(new Order("A-17"))
            .build();

        String json = new String(
            encoder.encode(ServiceClient.REST_CLIENT, request), StandardCharsets.UTF_8);

        assertContains(json, "\"id\":\"A-17\"", "JSON body");
    }

    private static void cxfPojoBecomesACompleteSoapEnvelope() {
        DefaultRequestEncoder encoder = new DefaultRequestEncoder(new BuiltinJsonCodec());
        RequestSpec request = RequestSpec.builder()
            .to("https://example.test/orders")
            .body(new Order("A-17"))
            .soap(new SoapRequestConfig(
                "urn:orders", "CreateOrder", "urn:orders:CreateOrder"))
            .build();

        String xml = new String(
            encoder.encode(ServiceClient.APACHE_CXF, request), StandardCharsets.UTF_8);

        assertContains(xml, "Envelope", "SOAP envelope");
        assertContains(xml, "CreateOrder", "SOAP operation");
        assertContains(xml, "A-17", "SOAP body value");
    }

    private static void rawBodiesPassThroughUnchanged() {
        DefaultRequestEncoder encoder = new DefaultRequestEncoder(new BuiltinJsonCodec());
        byte[] raw = "already-encoded".getBytes(StandardCharsets.UTF_8);
        RequestSpec request = RequestSpec.builder()
            .to("https://example.test")
            .body(raw)
            .build();

        byte[] result = encoder.encode(ServiceClient.REST_CLIENT, request);

        if (result == raw || !java.util.Arrays.equals(raw, result)) {
            throw new AssertionError("Raw byte bodies must be copied unchanged");
        }
    }

    private static void cxfPojoRequiresSoapMetadata() {
        DefaultRequestEncoder encoder = new DefaultRequestEncoder(new BuiltinJsonCodec());
        RequestSpec request = RequestSpec.builder()
            .to("https://example.test")
            .body(new Order("A-17"))
            .build();

        assertThrows(UniClientException.class,
            () -> encoder.encode(ServiceClient.APACHE_CXF, request));
    }

    public static final class Order {
        public String id;

        public Order() {
        }

        public Order(String id) {
            this.id = id;
        }
    }

    private static void assertContains(String actual, String expected, String name) {
        if (!actual.contains(expected)) {
            throw new AssertionError(name + ": expected '" + expected + "' in '" + actual + "'");
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

    @FunctionalInterface
    private interface ThrowingAction {
        void run();
    }
}
