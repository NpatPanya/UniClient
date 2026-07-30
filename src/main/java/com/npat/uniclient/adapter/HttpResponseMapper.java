package com.npat.uniclient.adapter;

import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.dto.RestfulResponse;
import com.npat.uniclient.exception.MissingDependencyException;
import com.npat.uniclient.exception.TransportException;
import com.npat.uniclient.port.JsonCodec;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

final class HttpResponseMapper {
    private HttpResponseMapper() {
    }

    @SuppressWarnings("unchecked")
    static RestfulResponse<?> map(int status, Map<String, List<String>> headers, InputStream stream, long limit, ResponseType<?> type, JsonCodec codec) {
        byte[] bytes = readBounded(stream, limit);
        String raw = new String(bytes, StandardCharsets.UTF_8);
        RestfulResponse<?> response;
        if (needsJson(type)) {
            if (codec == null)
                throw new MissingDependencyException("Jackson JSON support is required for the declared response type. Enable the optional-adapters profile.");
            response = codec.decodeResponseEnvelope(raw, (ResponseType<Object>) type);
        } else {
            RestfulResponse<String> r = new RestfulResponse<>();
            r.setRawPayload(raw);
            r.setResponseEntity(raw);
            response = r;
        }
        MultivaluedMap<String, String> copied = new MultivaluedHashMap<>();
        headers.forEach((n, v) -> {
            if (n != null) copied.put(n, v);
        });
        response.setHttpCode(status);
        response.setSuccess(status >= 200 && status < 300);
        response.setHttpHeaders(copied);
        response.setContentType(first(headers, "Content-Type"));
        response.setRawPayload(raw);
        return response;
    }

    static boolean needsJson(ResponseType<?> type) {
        return type != null && type.type() != String.class && type.type() != byte[].class;
    }

    private static String first(Map<String, List<String>> h, String n) {
        return h.entrySet().stream().filter(e -> e.getKey() != null && n.equalsIgnoreCase(e.getKey())).flatMap(e -> e.getValue().stream()).findFirst().orElse(null);
    }

    private static byte[] readBounded(InputStream in, long limit) {
        try (in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[8192];
            int r;
            long total = 0;
            while ((r = in.read(b)) != -1) {
                total += r;
                if (total > limit)
                    throw new TransportException("Upstream response exceeded the configured response limit");
                out.write(b, 0, r);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new TransportException("Unable to read upstream response", e);
        }
    }
}