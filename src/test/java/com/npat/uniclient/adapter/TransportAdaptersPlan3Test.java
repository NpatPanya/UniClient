package com.npat.uniclient.adapter;

import com.npat.uniclient.adapter.http.HttpURLConnectionAdapter;
import com.npat.uniclient.adapter.rest.RestClientAdapter;
import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.TransportPort;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JDK-only executable tests for the Plan 3 JDK transport adapters.
 */
public final class TransportAdaptersPlan3Test {

    public static void main(String[] args) throws Exception {
        httpURLConnectionAdapterMovesBytesAndMapsTheResponse();
        restClientAdapterMovesBytesAndMapsTheResponse();
        adaptersRejectUnserializedObjects();
    }

    private static void httpURLConnectionAdapterMovesBytesAndMapsTheResponse() throws Exception {
        assertRoundTrip(new HttpURLConnectionAdapter());
    }

    private static void restClientAdapterMovesBytesAndMapsTheResponse() throws Exception {
        assertRoundTrip(new RestClientAdapter());
    }

    private static void adaptersRejectUnserializedObjects() {
        RequestSpec spec = RequestSpec.builder()
            .to("http://127.0.0.1:1")
            .body(new Object())
            .build();

        assertThrows(ClientTransportException.class, () -> new HttpURLConnectionAdapter().execute(spec));
        assertThrows(ClientTransportException.class, () -> new RestClientAdapter().execute(spec));
    }

    private static void assertRoundTrip(TransportPort adapter) throws Exception {
        AtomicReference<Throwable> serverFailure = new AtomicReference<>();
        try (ServerSocket server = new ServerSocket(0)) {
            Thread serverThread = new Thread(() -> serve(server, serverFailure), "plan-3-loopback-server");
            serverThread.start();

            RequestSpec spec = RequestSpec.builder()
                .to(URI.create("http://127.0.0.1:" + server.getLocalPort() + "/echo"))
                .httpMethod("POST")
                .header("X-Test", "plan-3")
                .body("request-body")
                .build();

            ClientResponse response = adapter.execute(spec);
            serverThread.join(5000);

            if (serverFailure.get() != null) {
                throw new AssertionError("Loopback server failed", serverFailure.get());
            }
            assertEquals(201, response.statusCode(), "status");
            assertEquals("response-body", response.bodyAsString(), "body");
            assertEquals("plan-3", headerValue(response, "X-Test-Response"), "header");
            assertEquals("text/plain", response.contentType(), "content type");
        }
    }

    private static void serve(ServerSocket server, AtomicReference<Throwable> failure) {
        try (Socket socket = server.accept()) {
            socket.setSoTimeout(5000);
            InputStream input = socket.getInputStream();
            byte[] request = readRequest(input);
            String requestText = new String(request, StandardCharsets.UTF_8);
            assertContains(requestText, "POST /echo", "request line");
            assertContains(requestText, "X-Test: plan-3", "request header");
            assertContains(requestText, "request-body", "request body");

            byte[] responseBody = "response-body".getBytes(StandardCharsets.UTF_8);
            OutputStream output = socket.getOutputStream();
            output.write(("HTTP/1.1 201 Created\r\n"
                + "Content-Type: text/plain\r\n"
                + "X-Test-Response: plan-3\r\n"
                + "Content-Length: " + responseBody.length + "\r\n"
                + "Connection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            output.write(responseBody);
            output.flush();
        } catch (Throwable error) {
            failure.set(error);
        }
    }

    private static byte[] readRequest(InputStream input) throws IOException {
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        int previous = -1;
        int current;
        while ((current = input.read()) != -1) {
            request.write(current);
            if (previous == '\r' && current == '\n' && endsWith(request, "\r\n\r\n")) {
                break;
            }
            previous = current;
        }
        String headers = request.toString(StandardCharsets.UTF_8);
        int contentLength = contentLength(headers);
        for (int i = 0; i < contentLength; i++) {
            request.write(input.read());
        }
        return request.toByteArray();
    }

    private static int contentLength(String headers) {
        for (String line : headers.split("\\r\\n")) {
            if (line.regionMatches(true, 0, "Content-Length:", 0, "Content-Length:".length())) {
                return Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
            }
        }
        return 0;
    }

    private static boolean endsWith(ByteArrayOutputStream output, String suffix) {
        byte[] bytes = output.toByteArray();
        byte[] expected = suffix.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < expected.length) {
            return false;
        }
        for (int i = 1; i <= expected.length; i++) {
            if (bytes[bytes.length - i] != expected[expected.length - i]) {
                return false;
            }
        }
        return true;
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

    private static String headerValue(ClientResponse response, String expectedName) {
        return response.headers().all().entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(expectedName))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse("");
    }

    private static void assertContains(String actual, String expected, String name) {
        if (!actual.contains(expected)) {
            throw new AssertionError(name + ": expected '" + expected + "' in '" + actual + "'");
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}
