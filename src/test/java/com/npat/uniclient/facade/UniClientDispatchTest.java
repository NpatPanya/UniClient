package com.npat.uniclient.facade;

import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;
import com.npat.uniclient.dto.RestfulResponse;
import com.npat.uniclient.exception.MissingDependencyException;
import com.npat.uniclient.port.DependencyAvailability;
import com.npat.uniclient.port.TransportAdapter;

import java.util.List;

/**
 * Executable contracts for typed facade dispatch.
 */
public final class UniClientDispatchTest {

    private UniClientDispatchTest() {
    }

    public static void main(String[] args) {
        dispatchesToTheMatchingAdapterExactlyOnceWithTypedResponse();
        rejectsMissingSelectedDependencyBeforeAdapterInvocation();
        passesConfiguredJsonCodecOnlyToTheSelectedAdapter();
    }

    private static void dispatchesToTheMatchingAdapterExactlyOnceWithTypedResponse() {
        CountingAdapter adapter = new CountingAdapter(DependencyAvailability.available());
        UniClient client = new UniClient(new AdapterRegistry(List.of(adapter)));

        RestfulResponse<String> response = client.send(new TestRequest("request-body"));

        assertEquals(1, adapter.sendCount, "adapter invocation count");
        assertEquals("response-body", response.getResponseEntity(), "typed response entity");
    }

    private static void rejectsMissingSelectedDependencyBeforeAdapterInvocation() {
        CountingAdapter adapter = new CountingAdapter(
                DependencyAvailability.missing("Enable the selected optional integration."));
        UniClient client = new UniClient(new AdapterRegistry(List.of(adapter)));

        assertThrows(MissingDependencyException.class, () -> client.send(new TestRequest("request-body")),
                "missing dependency must fail before execution");
        assertEquals(0, adapter.sendCount, "unavailable adapter must not be invoked");
    }

    private static void passesConfiguredJsonCodecOnlyToTheSelectedAdapter() {
        CountingAdapter adapter = new CountingAdapter(DependencyAvailability.available());
        com.npat.uniclient.port.JsonCodec codec = new com.npat.uniclient.port.JsonCodec() {
            public byte[] encode(Object value) {
                return new byte[0];
            }

            public <T> T decode(String payload, com.npat.uniclient.domain.ResponseType<T> type) {
                return null;
            }

            public <T> RestfulResponse<T> decodeResponseEnvelope(String payload, com.npat.uniclient.domain.ResponseType<T> type) {
                return new RestfulResponse<>();
            }
        };
        new UniClient(new AdapterRegistry(List.of(adapter)), codec).send(new TestRequest("request-body"));
        if (adapter.receivedCodec != codec)
            throw new AssertionError("facade must pass its configured codec to the selected adapter");
    }

    private static final class TestRequest extends APIRequest<String, RestfulResponse<String>> {
        private TestRequest(String body) {
            super(body, null);
        }
    }

    private static final class CountingAdapter implements TransportAdapter {
        private final DependencyAvailability availability;
        private int sendCount;
        private com.npat.uniclient.port.JsonCodec receivedCodec;

        private CountingAdapter(DependencyAvailability availability) {
            this.availability = availability;
        }

        @Override
        public boolean supports(APIRequest<?, ?> request) {
            return request instanceof TestRequest;
        }

        @Override
        public APIResponse<?> send(APIRequest<?, ?> request) {
            sendCount++;
            RestfulResponse<String> response = new RestfulResponse<>();
            response.setResponseEntity("response-body");
            response.setSuccess(true);
            return response;
        }

        @Override
        public APIResponse<?> send(APIRequest<?, ?> request, com.npat.uniclient.port.JsonCodec jsonCodec) {
            receivedCodec = jsonCodec;
            return send(request);
        }

        @Override
        public DependencyAvailability dependencyAvailability() {
            return availability;
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, ThrowingRunnable action, String message) {
        try {
            action.run();
        } catch (Throwable throwable) {
            if (expectedType.isInstance(throwable)) {
                return;
            }
            throw new AssertionError(message + "; expected " + expectedType.getName()
                    + " but received " + throwable.getClass().getName(), throwable);
        }
        throw new AssertionError(message + "; expected " + expectedType.getName());
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + "; expected=" + expected + ", actual=" + actual);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}