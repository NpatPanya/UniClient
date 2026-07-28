package com.npat.uniclient.adapter.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npat.uniclient.core.exception.PayloadCodecException;
import com.npat.uniclient.core.port.PayloadCodecPort;
import java.io.IOException;
import java.util.Objects;

/**
 * Optional Jackson-backed JSON codec for nested generics, annotations, and richer POJOs.
 *
 * <p>The mapper is treated as immutable after construction. Jackson's documented
 * {@code writeValueAsBytes} and {@code readValue} operations are used directly:
 * https://github.com/FasterXML/jackson-databind</p>
 */
public final class JacksonJsonCodec implements PayloadCodecPort {
    private static final int MAX_JSON_BYTES = 10 * 1024 * 1024;
    private final ObjectMapper mapper;

    public JacksonJsonCodec() {
        this(new ObjectMapper());
    }

    public JacksonJsonCodec(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public byte[] serialize(Object body) throws PayloadCodecException {
        try {
            byte[] result = mapper.writeValueAsBytes(body);
            if (result.length > MAX_JSON_BYTES) {
                throw new PayloadCodecException("JSON output exceeds the 10 MiB safety limit");
            }
            return result;
        } catch (PayloadCodecException failure) {
            throw failure;
        } catch (JsonProcessingException failure) {
            throw new PayloadCodecException("Jackson JSON serialization failed", failure);
        }
    }

    @Override
    public <T> T deserialize(byte[] body, Class<T> type) throws PayloadCodecException {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(type, "type");
        if (body.length > MAX_JSON_BYTES) {
            throw new PayloadCodecException("JSON input exceeds the 10 MiB safety limit");
        }
        try {
            return mapper.readValue(body, type);
        } catch (IOException failure) {
            throw new PayloadCodecException(
                "Jackson JSON deserialization failed for " + type.getName(), failure);
        }
    }
}
