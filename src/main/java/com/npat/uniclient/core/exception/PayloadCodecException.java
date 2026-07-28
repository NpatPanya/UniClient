package com.npat.uniclient.core.exception;

/**
 * Thrown when a PayloadCodecPort implementation encounters a serialization or deserialization failure.
 *
 * Examples: JSON parsing error, unsupported type, field mismatch, encoding issue.
 * Wraps the underlying cause so callers see a consistent exception vocabulary regardless of
 * which codec (built-in, Jackson, SOAP envelope) is used.
 */
public class PayloadCodecException extends UniClientException {

    public PayloadCodecException(String message) {
        super(message);
    }

    public PayloadCodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayloadCodecException(Throwable cause) {
        super(cause);
    }
}
