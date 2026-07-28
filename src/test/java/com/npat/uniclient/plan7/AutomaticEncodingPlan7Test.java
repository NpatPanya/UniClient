package com.npat.uniclient.plan7;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.RequestEncoderPort;
import com.npat.uniclient.core.port.TransportPort;
import com.npat.uniclient.facade.ClientFacade;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/** Executable tests for facade-level automatic request encoding. */
public final class AutomaticEncodingPlan7Test {

    public static void main(String[] args) {
        facadeEncodesBeforeCallingTheTransport();
    }

    private static void facadeEncodesBeforeCallingTheTransport() {
        AtomicReference<RequestSpec> sent = new AtomicReference<>();
        TransportPort transport = spec -> {
            sent.set(spec);
            return ClientResponse.builder().statusCode(202).body("accepted").build();
        };
        RequestEncoderPort encoder = (engine, request) ->
            "encoded-body".getBytes(StandardCharsets.UTF_8);
        RequestSpec request = RequestSpec.builder()
            .to("https://example.test/orders")
            .body(new Object())
            .header("X-Test", "preserved")
            .build();

        ClientResponse response = new ClientFacade(engine -> transport, encoder)
            .execute(request, ServiceClient.REST_CLIENT);

        if (response.statusCode() != 202) {
            throw new AssertionError("Unexpected fake response status");
        }
        RequestSpec wire = sent.get();
        if (!(wire.body() instanceof byte[] body)
            || !"encoded-body".equals(new String(body, StandardCharsets.UTF_8))) {
            throw new AssertionError("Facade did not pass encoded bytes to the transport");
        }
        if (!"preserved".equals(wire.headers().get("X-Test"))) {
            throw new AssertionError("Facade did not preserve request headers");
        }
    }
}
