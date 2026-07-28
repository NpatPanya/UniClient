package com.npat.uniclient.adapter.http;

import com.npat.uniclient.adapter.TransportBody;
import com.npat.uniclient.adapter.crosscutting.RequestHeaderAssembler;
import com.npat.uniclient.adapter.crosscutting.SslContextFactory;
import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.HeaderSet;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.TransportPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDK-only transport adapter backed by {@link HttpURLConnection}.
 */
public final class HttpURLConnectionAdapter implements TransportPort {
    private final RequestHeaderAssembler headerAssembler;

    public HttpURLConnectionAdapter() {
        this(new RequestHeaderAssembler());
    }

    public HttpURLConnectionAdapter(RequestHeaderAssembler headerAssembler) {
        this.headerAssembler = Objects.requireNonNull(headerAssembler, "headerAssembler");
    }

    @Override
    public ClientResponse execute(RequestSpec spec) throws ClientTransportException {
        byte[] requestBody = TransportBody.toBytes(spec.body());
        HttpURLConnection connection = null;
        try {
            URLConnection rawConnection = spec.target().toURL().openConnection();
            connection = (HttpURLConnection) rawConnection;
            connection.setRequestMethod(spec.httpMethod());
            connection.setConnectTimeout(timeoutMillis(spec.timeout().connectTimeout()));
            connection.setReadTimeout(timeoutMillis(spec.timeout().readTimeout()));
            if (connection instanceof HttpsURLConnection https) {
                https.setSSLSocketFactory(SslContextFactory.from(spec.ssl()).getSocketFactory());
            }
            for (Map.Entry<String, String> header : headerAssembler.assemble(spec).entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            if (requestBody.length > 0) {
                connection.setDoOutput(true);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBody);
                }
            }

            int statusCode = connection.getResponseCode();
            InputStream responseStream = statusCode >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
            byte[] responseBody = responseStream == null
                ? new byte[0]
                : responseStream.readAllBytes();
            return ClientResponse.builder()
                .statusCode(statusCode)
                .body(responseBody)
                .headers(headers(connection))
                .contentType(connection.getContentType())
                .build();
        } catch (IOException | ClassCastException failure) {
            throw new ClientTransportException(
                "HttpURLConnection transport failed for " + spec.target(), failure);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HeaderSet headers(HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                headers.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return HeaderSet.of(headers);
    }

    private static int timeoutMillis(java.time.Duration timeout) {
        long millis = timeout.toMillis();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, millis));
    }
}
