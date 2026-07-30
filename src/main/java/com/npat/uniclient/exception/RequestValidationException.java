package com.npat.uniclient.exception;

public final class RequestValidationException extends RuntimeException {
    public RequestValidationException(String message) {
        super(message);
    }
}