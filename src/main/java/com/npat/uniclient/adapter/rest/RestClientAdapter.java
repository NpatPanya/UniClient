package com.npat.uniclient.adapter.rest;

import com.npat.uniclient.adapter.TransportBody;
import com.npat.uniclient.adapter.crosscutting.RequestHeaderAssembler;
import com.npat.uniclient.adapter.crosscutting.SslContextFactory;
import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.HeaderSet;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.TransportPort;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JDK-only REST transport adapter backed by {@link HttpClient}.
 */
public final class RestClientAdapter implements TransportPort {
    private final HttpClient configuredClient;
    private final RequestHeaderAssembler headerAssembler;

    public RestClientAdapter() {
        this(null, new RequestHeaderAssembler());
    }

    public RestClientAdapter(HttpClient client) {
        this(client, new RequestHeaderAssembler());
    }

    public RestClientAdapter(HttpClient client, RequestHeaderAssembler headerAssembler) {
        this.configuredClient = client;
        this.headerAssembler = Objects.requireNonNull(headerAssembler, "headerAssembler");
    }

    @Override
    public ClientResponse execute(RequestSpec spec) throws ClientTransportException {
        byte[] requestBody = TransportBody.toBytes(spec.body());
        HttpRequest.BodyPublisher publisher = requestBody.length == 0
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofByteArray(requestBody);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(spec.target())
            .timeout(spec.timeout().readTimeout())
            .method(spec.httpMethod(), publisher);
        for (Map.Entry<String, String> header : headerAssembler.assemble(spec).entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }

        try {
            HttpResponse<byte[]> response = clientFor(spec).send(
                requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
            return ClientResponse.builder()
                .statusCode(response.statusCode())
                .body(response.body())
                .headers(headers(response))
                .contentType(response.headers().firstValue("Content-Type").orElse(null))
                .build();
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new ClientTransportException(
                "HttpClient transport interrupted for " + spec.target(), failure);
        } catch (IOException | IllegalArgumentException failure) {
            throw new ClientTransportException(
                "HttpClient transport failed for " + spec.target(), failure);
        }
    }

    private HttpClient clientFor(RequestSpec spec) {
        if (configuredClient != null) {
            return configuredClient;
        }
        return HttpClient.newBuilder()
            .connectTimeout(spec.timeout().connectTimeout())
            .sslContext(SslContextFactory.from(spec.ssl()))
            .build();
    }

    private static HeaderSet headers(HttpResponse<byte[]> response) {
        Map<String, String> headers = new HashMap<>();
        response.headers().map().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headers.put(name, values.get(0));
            }
        });
        return HeaderSet.of(headers);
    }
}
