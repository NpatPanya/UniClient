package com.npat.uniclient.adapter;

import com.npat.uniclient.builder.Requests;
import com.npat.uniclient.domain.HTTP_METHOD;
import com.npat.uniclient.dto.RestfulRequest;
import com.npat.uniclient.dto.RestfulResponse;
import com.npat.uniclient.exception.TransportException;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Executable integration contracts for the JDK HTTP transport adapters. */
public final class JdkHttpAdaptersTest {
    private JdkHttpAdaptersTest() { }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            String reply = exchange.getRequestMethod() + ":" + exchange.getRequestHeaders().getFirst("X-Request")
                    + ":" + new String(requestBody, StandardCharsets.UTF_8);
            byte[] body = reply.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/redirect", exchange -> { exchange.getResponseHeaders().add("Location", "/echo"); exchange.sendResponseHeaders(302, -1); exchange.close(); });
        server.createContext("/not-found", exchange -> {
            byte[] body = "missing".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/oversize", exchange -> {
            byte[] body = "12345".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            transmitsMethodHeaderAndBodyThroughHttpUrlConnection(base);
            returnsReceivedFourOhFourThroughRestClient(base);
            returnsRedirectWithoutFollowingIt(base);
            rejectsResponseBeyondConfiguredLimit(base);
        } finally {
            server.stop(0);
        }
    }

    private static void transmitsMethodHeaderAndBodyThroughHttpUrlConnection(String base) {
        RestfulRequest<String, String> request = Requests.httpUrlConnection()
                .body("payload").responseType(String.class).endpoint(base + "/echo")
                .method(HTTP_METHOD.POST).header("X-Request", "present")
                .connTimeout(Duration.ofSeconds(2)).readTimeout(Duration.ofSeconds(2)).build();
        RestfulResponse<?> response = new HttpUrlConnectionAdapter().send(request);
        assertEquals(201, response.getHttpCode(), "status");
        assertEquals(true, response.isSuccess(), "2xx success");
        assertEquals("POST:present:payload", response.getRawPayload(), "request transmission");
    }

    private static void returnsReceivedFourOhFourThroughRestClient(String base) {
        RestfulRequest<String, String> request = Requests.restClient()
                .body("payload").endpoint(base + "/not-found").method(HTTP_METHOD.GET)
                .connTimeout(Duration.ofSeconds(2)).readTimeout(Duration.ofSeconds(2)).build();
        RestfulResponse<?> response = new RestClientAdapter().send(request);
        assertEquals(404, response.getHttpCode(), "received error status");
        assertEquals(false, response.isSuccess(), "4xx is not protocol success");
        assertEquals("missing", response.getRawPayload(), "received error body");
    }

    private static void returnsRedirectWithoutFollowingIt(String base) {
        RestfulRequest<String, String> request = Requests.restClient().body("payload").endpoint(base + "/redirect").method(HTTP_METHOD.GET)
                .connTimeout(Duration.ofSeconds(2)).readTimeout(Duration.ofSeconds(2)).build();
        RestfulResponse<?> response = new RestClientAdapter().send(request);
        assertEquals(302, response.getHttpCode(), "redirect must be returned by default");
    }

    private static void rejectsResponseBeyondConfiguredLimit(String base) {
        RestfulRequest<String, String> request = Requests.httpUrlConnection()
                .body("payload").endpoint(base + "/oversize").method(HTTP_METHOD.GET)
                .connTimeout(Duration.ofSeconds(2)).readTimeout(Duration.ofSeconds(2)).maxResponseBytes(4).build();
        assertThrows(TransportException.class, () -> new HttpUrlConnectionAdapter().send(request), "response limit");
    }

    private static void assertThrows(Class<? extends Throwable> expected, ThrowingRunnable action, String label) {
        try { action.run(); } catch (Throwable throwable) { if (expected.isInstance(throwable)) return; throw new AssertionError(label, throwable); }
        throw new AssertionError(label + " did not throw");
    }
    private static void assertEquals(Object expected, Object actual, String label) {
        if (expected == null ? actual != null : !expected.equals(actual)) throw new AssertionError(label + "; expected=" + expected + ", actual=" + actual);
    }
    @FunctionalInterface private interface ThrowingRunnable { void run() throws Exception; }
}