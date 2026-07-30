package com.npat.uniclient.domain;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Captures an HTTP response-entity type, including parameterized types.
 *
 * @param <T> response entity type
 */
public abstract class ResponseType<T> {

    private final Type type;

    protected ResponseType() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException("ResponseType requires a concrete type parameter");
        }
        this.type = parameterizedType.getActualTypeArguments()[0];
    }

    private ResponseType(Type type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public static <T> ResponseType<T> of(Class<T> type) {
        return new ResponseType<>(type) {
        };
    }

    public final Type type() {
        return type;
    }
}