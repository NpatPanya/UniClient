package com.npat.uniclient.builder;

import com.npat.uniclient.domain.HTTP_METHOD;
import com.npat.uniclient.domain.SocketFormat;
import com.npat.uniclient.dto.componenet.AuthConfig;
import com.npat.uniclient.exception.RequestValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executable public-contract assertions for protocol-specific request builders.
 */
public final class ProtocolBuilderContractTest {

    private static final long TEN_MIB = 10L * 1024L * 1024L;

    private ProtocolBuilderContractTest() {
    }

    public static void main(String[] args) throws Exception {
        exposesOnlyDirectProtocolFactories();
        validatesHttpAndRestBuilderBoundaries();
        preservesHttpRequestDefaultsAndExplicitAuthentication();
        validatesSoapEnvelopeWithoutSerializingIt();
        validatesSocketTargetAndNeutralFormat();
    }

    private static void exposesOnlyDirectProtocolFactories() throws Exception {
        assertEquals("HttpUrlConnectionRequestBuilder", factory("httpUrlConnection").getClass().getSimpleName(),
                "HttpURLConnection factory");
        assertEquals("RestRequestBuilder", factory("restClient").getClass().getSimpleName(),
                "RestClient factory");
        assertEquals("SoapRequestBuilder", factory("soapCxf").getClass().getSimpleName(),
                "SOAP CXF factory");
        assertEquals("SocketRequestBuilder", factory("socket").getClass().getSimpleName(),
                "socket factory");
    }

    private static void validatesHttpAndRestBuilderBoundaries() throws Exception {
        Object builder = factory("restClient");
        call(builder, "endpoint", "https://upstream.example/orders");
        call(builder, "body", Map.of("orderId", "A-1"));
        call(builder, "method", HTTP_METHOD.POST);
        assertThrows(RequestValidationException.class, () -> call(builder, "build"),
                "missing timeouts must be rejected");

        call(builder, "connTimeout", Duration.ofSeconds(1));
        call(builder, "readTimeout", Duration.ofSeconds(2));
        assertDoesNotThrow(() -> call(builder, "build"), "valid REST request must build");

        Object invalidEndpoint = factory("httpUrlConnection");
        call(invalidEndpoint, "endpoint", "/relative");
        call(invalidEndpoint, "body", "request");
        call(invalidEndpoint, "method", HTTP_METHOD.POST);
        call(invalidEndpoint, "connTimeout", Duration.ofSeconds(1));
        call(invalidEndpoint, "readTimeout", Duration.ofSeconds(1));
        assertThrows(RequestValidationException.class, () -> call(invalidEndpoint, "build"),
                "relative HTTP endpoint must be rejected");

        Object missingMethod = factory("httpUrlConnection");
        call(missingMethod, "endpoint", "https://upstream.example/orders");
        call(missingMethod, "body", "request");
        call(missingMethod, "connTimeout", Duration.ofSeconds(1));
        call(missingMethod, "readTimeout", Duration.ofSeconds(1));
        assertThrows(RequestValidationException.class, () -> call(missingMethod, "build"),
                "HTTP method must be explicit");
    }

    private static void preservesHttpRequestDefaultsAndExplicitAuthentication() throws Exception {
        Object builder = factory("restClient");
        call(builder, "endpoint", "https://upstream.example/orders");
        call(builder, "body", new LinkedHashMap<>(Map.of("orderId", "A-1")));
        call(builder, "responseType", String.class);
        call(builder, "method", HTTP_METHOD.POST);
        call(builder, "connTimeout", Duration.ofSeconds(1));
        call(builder, "readTimeout", Duration.ofSeconds(2));
        Object request = call(builder, "build");
        Object config = call(request, "getConfig");

        assertEquals(TEN_MIB, call(config, "getMaxResponseBytes"), "default response limit");
        Object headers = call(config, "getHeaderConfig");
        assertEquals("application/json", call(headers, "getFirst", "Content-Type"),
                "DTO bodies default to JSON content type");

        Object conflict = factory("restClient");
        call(conflict, "endpoint", "https://upstream.example/orders");
        call(conflict, "body", "request");
        call(conflict, "method", HTTP_METHOD.POST);
        call(conflict, "connTimeout", Duration.ofSeconds(1));
        call(conflict, "readTimeout", Duration.ofSeconds(1));
        call(conflict, "auth", AuthConfig.bearer("do-not-log-this-token"));
        call(conflict, "header", "Authorization", "Bearer caller-value");
        assertThrows(RequestValidationException.class, () -> call(conflict, "build"),
                "structured and explicit authorization cannot be combined");
    }

    private static void validatesSoapEnvelopeWithoutSerializingIt() throws Exception {
        Element envelope = envelope("http://schemas.xmlsoap.org/soap/envelope/");
        Object builder = factory("soapCxf");
        call(builder, "endpoint", "https://upstream.example/soap");
        call(builder, "body", envelope);
        call(builder, "soapAction", "urn:submit");
        call(builder, "connTimeout", Duration.ofSeconds(1));
        call(builder, "readTimeout", Duration.ofSeconds(2));
        Object request = call(builder, "build");
        assertSame(envelope, call(request, "getPayload"), "SOAP envelope must remain caller-owned element");

        Object invalid = factory("soapCxf");
        call(invalid, "endpoint", "https://upstream.example/soap");
        call(invalid, "body", envelope("urn:not-soap"));
        call(invalid, "connTimeout", Duration.ofSeconds(1));
        call(invalid, "readTimeout", Duration.ofSeconds(1));
        assertThrows(RequestValidationException.class, () -> call(invalid, "build"),
                "unsupported SOAP namespace must be rejected");
    }

    private static void validatesSocketTargetAndNeutralFormat() throws Exception {
        Object builder = factory("socket");
        call(builder, "host", "127.0.0.1");
        call(builder, "port", 65536);
        call(builder, "body", Map.of("mti", "0200"));
        call(builder, "format", SocketFormat.of("iso8583"));
        call(builder, "connTimeout", Duration.ofSeconds(1));
        call(builder, "readTimeout", Duration.ofSeconds(2));
        assertThrows(RequestValidationException.class, () -> call(builder, "build"),
                "socket port above 65535 must be rejected");

        call(builder, "port", 9000);
        Object request = call(builder, "build");
        assertEquals("iso8583", call(request, "getFormat").getClass().getMethod("identifier").invoke(call(request, "getFormat")),
                "socket format must be retained without adapter-specific types");
        assertEquals(null, call(request, "getResponseType"), "socket raw-byte fallback has no response type");
    }

    private static Object factory(String name) throws Exception {
        return Requests.class.getMethod(name).invoke(null);
    }

    private static Object call(Object target, String name, Object... arguments) throws Exception {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == arguments.length) {
                try {
                    return method.invoke(target, arguments);
                } catch (InvocationTargetException exception) {
                    throw rethrow(exception.getCause());
                }
            }
        }
        throw new AssertionError("Missing public method " + target.getClass().getSimpleName() + "." + name);
    }

    private static Element envelope(String namespace) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element envelope = document.createElementNS(namespace, "soap:Envelope");
        envelope.appendChild(document.createElementNS(namespace, "soap:Body"));
        document.appendChild(envelope);
        return envelope;
    }

    private static RuntimeException rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new RuntimeException(throwable);
    }

    private static void assertDoesNotThrow(ThrowingRunnable action, String message) {
        try {
            action.run();
        } catch (Exception exception) {
            throw new AssertionError(message, exception);
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

    private static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}