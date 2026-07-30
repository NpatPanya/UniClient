package com.npat.uniclient.dto;

/**
 * Executable contract assertions for the framework-free response core.
 */
public final class CoreResponseContractTest {

    private CoreResponseContractTest() {
    }

    public static void main(String[] args) throws Exception {
        preservesTypedEntitySeparatelyFromRawHttpPayload();
        keepsHttpCodeNullableForSocketResponses();
        exposesImmutableSocketFormatAndTypedExceptions();
    }

    private static void preservesTypedEntitySeparatelyFromRawHttpPayload() throws Exception {
        RestfulResponse<String> response = new RestfulResponse<>();

        invoke(response, "setResponseEntity", Object.class, "decoded-order");
        invoke(response, "setRawPayload", String.class, "{\"responseEntity\":\"decoded-order\"}");

        assertEquals("decoded-order", invoke(response, "getResponseEntity"),
                "decoded response entity must remain available independently");
        assertEquals("{\"responseEntity\":\"decoded-order\"}", invoke(response, "getRawPayload"),
                "original HTTP text must remain available independently");
    }

    private static void keepsHttpCodeNullableForSocketResponses() throws Exception {
        SocketResponse<byte[]> response = new SocketResponse<>();

        assertEquals(null, invoke(response, "getHttpCode"),
                "socket responses must expose no HTTP status");
    }

    private static void exposesImmutableSocketFormatAndTypedExceptions() throws Exception {
        Class<?> socketFormat = requiredClass("com.npat.uniclient.domain.SocketFormat");
        Object format = socketFormat.getMethod("of", String.class).invoke(null, "iso8583");

        assertEquals("iso8583", socketFormat.getMethod("identifier").invoke(format),
                "socket format must retain its immutable identifier");

        Class<?> responseType = requiredClass("com.npat.uniclient.domain.ResponseType");
        Object typedString = responseType.getMethod("of", Class.class).invoke(null, String.class);
        assertEquals(String.class, responseType.getMethod("type").invoke(typedString),
                "simple response type must retain its declared class");
        assertThrows(IllegalArgumentException.class,
                () -> socketFormat.getMethod("of", String.class).invoke(null, " "),
                "blank socket format must be rejected");

        requiredClass("com.npat.uniclient.exception.RequestValidationException");
        requiredClass("com.npat.uniclient.exception.MissingDependencyException");
        requiredClass("com.npat.uniclient.exception.TransportException");
    }

    private static Object invoke(Object target, String method, Class<?> parameterType, Object argument) throws Exception {
        return target.getClass().getMethod(method, parameterType).invoke(target, argument);
    }

    private static Object invoke(Object target, String method) throws Exception {
        return target.getClass().getMethod(method).invoke(target);
    }

    private static Class<?> requiredClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Missing required type: " + name, exception);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, ThrowingRunnable action, String message) {
        try {
            action.run();
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
            if (expectedType.isInstance(cause)) {
                return;
            }
            throw new AssertionError(message + "; expected " + expectedType.getName()
                    + " but received " + cause.getClass().getName(), cause);
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
        void run() throws Exception;
    }
}
