package com.npat.uniclient.core.exception;

/**
 * Root exception for the uniclient library.
 * Unchecked (extends RuntimeException) to avoid boilerplate in caller code.
 * This is a library; we don't force checked exceptions on every consumer.
 */
public class UniClientException extends RuntimeException {

    public UniClientException(String message) {
        super(message);
    }

    public UniClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniClientException(Throwable cause) {
        super(cause);
    }
}
