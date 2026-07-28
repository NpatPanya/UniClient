package com.npat.uniclient.adapter.codec;

import com.npat.uniclient.core.exception.PayloadCodecException;
import com.npat.uniclient.core.port.PayloadCodecPort;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Dependency-free JSON codec for flat POJOs, maps, lists, arrays, primitives, and strings.
 *
 * <p>This intentionally conservative fallback does not support nested generic type metadata,
 * polymorphic type metadata, custom annotations, private-constructor object graphs, or arbitrary
 * object identity. Add Jackson (Sub 4.2) for those cases. Deserialization accepts only the target
 * class supplied by the caller and never enables polymorphic class resolution.</p>
 */
public final class BuiltinJsonCodec implements PayloadCodecPort {
    private static final int MAX_JSON_BYTES = 10 * 1024 * 1024;

    @Override
    public byte[] serialize(Object body) throws PayloadCodecException {
        try {
            StringBuilder json = new StringBuilder();
            writeValue(body, json, new IdentityHashMap<>());
            byte[] result = json.toString().getBytes(StandardCharsets.UTF_8);
            if (result.length > MAX_JSON_BYTES) {
                throw new PayloadCodecException("JSON output exceeds the 10 MiB safety limit");
            }
            return result;
        } catch (PayloadCodecException failure) {
            throw failure;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            throw unsupported("Builtin JSON serialization failed", failure);
        }
    }

    @Override
    public <T> T deserialize(byte[] body, Class<T> type) throws PayloadCodecException {
        Objects.requireNonNull(type, "type");
        if (body == null) {
            throw new PayloadCodecException("Cannot deserialize a null JSON body");
        }
        if (body.length > MAX_JSON_BYTES) {
            throw new PayloadCodecException("JSON input exceeds the 10 MiB safety limit");
        }
        if (type == byte[].class) {
            return type.cast(body.clone());
        }
        try {
            Object parsed = new JsonParser(new String(body, StandardCharsets.UTF_8)).parse();
            return type.cast(convert(parsed, type));
        } catch (PayloadCodecException failure) {
            throw failure;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            throw unsupported("Builtin JSON deserialization failed for " + type.getName(), failure);
        }
    }

    private static void writeValue(Object value, StringBuilder json,
                                   IdentityHashMap<Object, Boolean> visiting)
        throws ReflectiveOperationException {
        if (value == null) {
            json.append("null");
        } else if (value instanceof String || value instanceof Character || value instanceof Enum<?>) {
            writeString(String.valueOf(value), json);
        } else if (value instanceof Boolean || value instanceof Integer || value instanceof Long
            || value instanceof Short || value instanceof Byte) {
            json.append(value);
        } else if (value instanceof Float || value instanceof Double) {
            double number = ((Number) value).doubleValue();
            if (!Double.isFinite(number)) {
                throw new PayloadCodecException("JSON cannot represent non-finite numbers");
            }
            json.append(value);
        } else if (value instanceof Number) {
            json.append(value);
        } else if (value instanceof Map<?, ?> map) {
            enter(value, visiting);
            writeMap(map, json, visiting);
            visiting.remove(value);
        } else if (value instanceof Iterable<?> iterable) {
            enter(value, visiting);
            writeIterable(iterable, json, visiting);
            visiting.remove(value);
        } else if (value.getClass().isArray()) {
            enter(value, visiting);
            writeArray(value, json, visiting);
            visiting.remove(value);
        } else {
            enter(value, visiting);
            writePojo(value, json, visiting);
            visiting.remove(value);
        }
    }

    private static void writeMap(Map<?, ?> map, StringBuilder json,
                                 IdentityHashMap<Object, Boolean> visiting)
        throws ReflectiveOperationException {
        json.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new PayloadCodecException("Builtin JSON maps require String keys; add Jackson for other keys");
            }
            if (!first) {
                json.append(',');
            }
            first = false;
            writeString(key, json);
            json.append(':');
            writeValue(entry.getValue(), json, visiting);
        }
        json.append('}');
    }

    private static void writeIterable(Iterable<?> iterable, StringBuilder json,
                                      IdentityHashMap<Object, Boolean> visiting)
        throws ReflectiveOperationException {
        json.append('[');
        boolean first = true;
        for (Object value : iterable) {
            if (!first) {
                json.append(',');
            }
            first = false;
            writeValue(value, json, visiting);
        }
        json.append(']');
    }

    private static void writeArray(Object array, StringBuilder json,
                                   IdentityHashMap<Object, Boolean> visiting)
        throws ReflectiveOperationException {
        json.append('[');
        for (int i = 0; i < Array.getLength(array); i++) {
            if (i > 0) {
                json.append(',');
            }
            writeValue(Array.get(array, i), json, visiting);
        }
        json.append(']');
    }

    private static void writePojo(Object value, StringBuilder json,
                                  IdentityHashMap<Object, Boolean> visiting)
        throws ReflectiveOperationException {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (Field field : value.getClass().getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                properties.put(field.getName(), field.get(value));
            }
        }
        for (Method method : value.getClass().getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0
                || method.getReturnType() == Void.TYPE || method.isSynthetic()) {
                continue;
            }
            String property = propertyName(method.getName());
            if (property != null && !properties.containsKey(property)) {
                properties.put(property, method.invoke(value));
            }
        }
        if (properties.isEmpty()) {
            throw unsupported("Builtin JSON supports only POJOs with public fields or getters", null);
        }
        writeMap(properties, json, visiting);
    }

    private static String propertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3
            && !methodName.equals("getClass")) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return decapitalize(methodName.substring(2));
        }
        return null;
    }

    private static String decapitalize(String value) {
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static void writeString(String value, StringBuilder json) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        json.append('"');
    }

    private static void enter(Object value, IdentityHashMap<Object, Boolean> visiting) {
        if (visiting.put(value, Boolean.TRUE) != null) {
            throw unsupported("Builtin JSON cannot serialize cyclic object graphs; add Jackson", null);
        }
    }

    private static Object convert(Object value, Class<?> type) throws ReflectiveOperationException {
        if (value == null) {
            if (type.isPrimitive()) {
                throw new PayloadCodecException("JSON null cannot populate primitive " + type.getName());
            }
            return null;
        }
        if (type == Object.class || type.isInstance(value)) {
            return value;
        }
        if (type == String.class) {
            return String.valueOf(value);
        }
        if (type == Boolean.class || type == Boolean.TYPE) {
            if (value instanceof Boolean) return value;
        }
        if (Number.class.isAssignableFrom(box(type)) || type.isPrimitive()) {
            if (value instanceof Number number) return numberValue(number, type);
        }
        if (type.isEnum() && value instanceof String text) {
            return enumValue(type, text);
        }
        if (Map.class.isAssignableFrom(type) && value instanceof Map<?, ?>) {
            return value;
        }
        if (Collection.class.isAssignableFrom(type) && value instanceof Collection<?>) {
            return value;
        }
        if (type.isArray() && value instanceof List<?> list) {
            Object array = Array.newInstance(type.getComponentType(), list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convert(list.get(i), type.getComponentType()));
            }
            return array;
        }
        if (!(value instanceof Map<?, ?> properties)) {
            throw new PayloadCodecException("JSON value cannot populate " + type.getName());
        }
        Constructor<?> constructor = type.getDeclaredConstructor();
        if (!Modifier.isPublic(constructor.getModifiers()) || !Modifier.isPublic(type.getModifiers())) {
            throw new PayloadCodecException("Builtin JSON requires a public no-arg target type; add Jackson for "
                + type.getName());
        }
        Object instance = constructor.newInstance();
        for (Field field : type.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && properties.containsKey(field.getName())) {
                field.set(instance, convert(properties.get(field.getName()), field.getType()));
            }
        }
        for (Method method : type.getMethods()) {
            if (method.getName().startsWith("set") && method.getName().length() > 3
                && method.getParameterCount() == 1 && Modifier.isPublic(method.getModifiers())) {
                String property = decapitalize(method.getName().substring(3));
                if (properties.containsKey(property)) {
                    method.invoke(instance, convert(properties.get(property), method.getParameterTypes()[0]));
                }
            }
        }
        return instance;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object enumValue(Class<?> type, String value) {
        return Enum.valueOf((Class<? extends Enum>) type, value);
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == Integer.TYPE) return Integer.class;
        if (type == Long.TYPE) return Long.class;
        if (type == Short.TYPE) return Short.class;
        if (type == Byte.TYPE) return Byte.class;
        if (type == Double.TYPE) return Double.class;
        if (type == Float.TYPE) return Float.class;
        return type;
    }

    private static Object numberValue(Number value, Class<?> type) {
        if (type == Integer.class || type == Integer.TYPE) return value.intValue();
        if (type == Long.class || type == Long.TYPE) return value.longValue();
        if (type == Short.class || type == Short.TYPE) return value.shortValue();
        if (type == Byte.class || type == Byte.TYPE) return value.byteValue();
        if (type == Double.class || type == Double.TYPE) return value.doubleValue();
        if (type == Float.class || type == Float.TYPE) return value.floatValue();
        return value;
    }

    private static PayloadCodecException unsupported(String message, Throwable cause) {
        String guidance = message + ". Add Jackson for nested generics, annotations, or polymorphic types.";
        return cause == null ? new PayloadCodecException(guidance)
            : new PayloadCodecException(guidance, cause);
    }

    private static final class JsonParser {
        private final String input;
        private int index;

        private JsonParser(String input) {
            this.input = input;
        }

        private Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != input.length()) {
                throw new PayloadCodecException("Unexpected JSON content at index " + index);
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= input.length()) throw invalid();
            return switch (input.charAt(index)) {
                case '"' -> parseString();
                case '{' -> parseObject();
                case '[' -> parseArray();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> result = new LinkedHashMap<>();
            index++;
            skipWhitespace();
            if (consume('}')) return result;
            while (true) {
                skipWhitespace();
                if (index >= input.length() || input.charAt(index) != '"') throw invalid();
                String key = parseString();
                skipWhitespace();
                require(':');
                result.put(key, parseValue());
                skipWhitespace();
                if (consume('}')) return result;
                require(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> result = new ArrayList<>();
            index++;
            skipWhitespace();
            if (consume(']')) return result;
            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (consume(']')) return result;
                require(',');
            }
        }

        private String parseString() {
            require('"');
            StringBuilder result = new StringBuilder();
            while (index < input.length()) {
                char character = input.charAt(index++);
                if (character == '"') return result.toString();
                if (character == '\\') {
                    if (index >= input.length()) throw invalid();
                    char escaped = input.charAt(index++);
                    switch (escaped) {
                        case '"', '\\', '/' -> result.append(escaped);
                        case 'b' -> result.append('\b');
                        case 'f' -> result.append('\f');
                        case 'n' -> result.append('\n');
                        case 'r' -> result.append('\r');
                        case 't' -> result.append('\t');
                        case 'u' -> result.append((char) Integer.parseInt(input.substring(index, index + 4), 16));
                        default -> throw invalid();
                    }
                    if (escaped == 'u') index += 4;
                } else {
                    result.append(character);
                }
            }
            throw invalid();
        }

        private Object parseNumber() {
            int start = index;
            while (index < input.length() && "-+0123456789.eE".indexOf(input.charAt(index)) >= 0) index++;
            String number = input.substring(start, index);
            try {
                if (number.indexOf('.') >= 0 || number.indexOf('e') >= 0 || number.indexOf('E') >= 0) {
                    return Double.parseDouble(number);
                }
                long value = Long.parseLong(number);
                return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE ? (int) value : value;
            } catch (NumberFormatException failure) {
                throw new PayloadCodecException("Invalid JSON number", failure);
            }
        }

        private Object parseLiteral(String literal, Object value) {
            if (!input.startsWith(literal, index)) throw invalid();
            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) index++;
        }

        private boolean consume(char expected) {
            if (index < input.length() && input.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void require(char expected) {
            if (!consume(expected)) throw invalid();
        }

        private PayloadCodecException invalid() {
            return new PayloadCodecException("Malformed JSON at index " + index);
        }
    }
}
