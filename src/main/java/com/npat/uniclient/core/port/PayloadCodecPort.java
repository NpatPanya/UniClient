package com.npat.uniclient.core.port;

import com.npat.uniclient.core.exception.PayloadCodecException;

/**
 * Port for serializing request bodies and deserializing response bodies.
 * One reason to change: wire-format conversion (JSON, XML, SOAP, etc).
 *
 * Implementations: BuiltinJsonCodec, JacksonJsonCodec, SoapEnvelopeCodec.
 * Kept separate from TransportPort on purpose (SRP): a transport engine and a serializer
 * are two different concerns.
 *
 * Thread-safety: implementations must be thread-safe for high-throughput scenarios.
 */
public interface PayloadCodecPort {

    /**
     * Serializes a request body object into bytes.
     *
     * @param body the request body (any POJO, may be null)
     * @return serialized bytes (empty array if body is null), never null
     * @throws PayloadCodecException if serialization fails (unsupported type, etc)
     * @throws com.npat.uniclient.core.exception.MissingClientDependencyException if a required
     *         optional codec library is not on the classpath (e.g. Jackson for complex POJOs)
     */
    byte[] serialize(Object body) throws PayloadCodecException;

    /**
     * Deserializes response bytes into a typed object.
     *
     * @param body the response body bytes (may be empty)
     * @param type the target class to deserialize into
     * @param <T> the target type
     * @return deserialized object
     * @throws PayloadCodecException if deserialization fails (malformed data, type mismatch, etc)
     * @throws com.npat.uniclient.core.exception.MissingClientDependencyException if a required
     *         optional codec library is not on the classpath
     */
    <T> T deserialize(byte[] body, Class<T> type) throws PayloadCodecException;
}
