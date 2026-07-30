package com.npat.uniclient.adapter;

import com.npat.uniclient.domain.HttpTransportKind;
import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.RestfulRequest;
import com.npat.uniclient.dto.RestfulResponse;
import com.npat.uniclient.dto.base.HttpRequestConfig;
import com.npat.uniclient.exception.TransportException;
import com.npat.uniclient.port.TransportAdapter;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class HttpUrlConnectionAdapter implements TransportAdapter {
    private final com.npat.uniclient.port.JsonCodec jsonCodec;

    public HttpUrlConnectionAdapter() {
        this(null);
    }

    public HttpUrlConnectionAdapter(com.npat.uniclient.port.JsonCodec jsonCodec) {
        this.jsonCodec = jsonCodec;
    }

    @Override
    public boolean supports(APIRequest<?, ?> request) {
        return request instanceof RestfulRequest<?, ?> rest && rest.getConfig().getTransportKind() == HttpTransportKind.HTTP_URL_CONNECTION;
    }

    @Override
    public RestfulResponse<?> send(APIRequest<?, ?> request) {
        return send(request, jsonCodec);
    }

    @Override
    public RestfulResponse<?> send(APIRequest<?, ?> request, com.npat.uniclient.port.JsonCodec jsonCodec) {
        if (!(request instanceof RestfulRequest<?, ?> rest))
            throw new TransportException("HttpURLConnection adapter requires a REST request");
        HttpRequestConfig config = rest.getConfig();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(config.getUrlConfig().getDestURL()).openConnection();
            connection.setRequestMethod(config.getHttpMethod().name());
            connection.setConnectTimeout(millis(config.getConnTimeout()));
            connection.setReadTimeout(millis(config.getReadTimeout()));
            connection.setInstanceFollowRedirects(config.isFollowRedirects());
            config.getHeaderConfig().all().forEach((name, values) -> values.forEach(value -> connection.addRequestProperty(name, value)));
            applyAuth(connection, config);
            byte[] body = body(rest.getPayload(), jsonCodec);
            if (body.length > 0) {
                connection.setDoOutput(true);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                }
            }
            int status = connection.getResponseCode();
            return HttpResponseMapper.map(status, connection.getHeaderFields(), status >= 400 ? connection.getErrorStream() : connection.getInputStream(), config.getMaxResponseBytes(), rest.getResponseType(), jsonCodec);
        } catch (TransportException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new TransportException("HTTP request failed before a usable response was received", exception);
        }
    }

    private static void applyAuth(HttpURLConnection connection, HttpRequestConfig config) {
        var auth = config.getAuthentication();
        if (auth == null || auth.getType() == com.npat.uniclient.dto.componenet.AuthConfig.AuthType.NONE) return;
        String value = auth.getType() == com.npat.uniclient.dto.componenet.AuthConfig.AuthType.BEARER ? "Bearer " + auth.getToken() : "Basic " + java.util.Base64.getEncoder().encodeToString((auth.getUsername() + ":" + auth.getPassword()).getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", value);
    }

    private static byte[] body(Object payload, com.npat.uniclient.port.JsonCodec jsonCodec) {
        if (payload == null) return new byte[0];
        if (payload instanceof byte[] bytes) return bytes;
        if (payload instanceof String text) return text.getBytes(StandardCharsets.UTF_8);
        if (jsonCodec == null)
            throw new com.npat.uniclient.exception.MissingDependencyException("Jackson JSON support is required for a DTO request payload. Enable the optional-adapters profile.");
        return jsonCodec.encode(payload);
    }

    private static int millis(Duration duration) {
        return Math.toIntExact(Math.min(Integer.MAX_VALUE, duration.toMillis()));
    }
}