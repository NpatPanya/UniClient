package com.npat.uniclient.adapter.codec;

import com.npat.uniclient.core.exception.PayloadCodecException;
import com.npat.uniclient.core.port.PayloadCodecPort;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JDK-only SOAP 1.1 envelope builder for simple object graphs.
 *
 * <p>The codec is pure and transport-free. It accepts metadata explicitly through
 * {@link SoapEnvelopeMetadata}; it does not invent a namespace, operation, or action. Public
 * fields/getters are rendered as XML child elements, maps use their string keys, and lists use
 * {@code item} elements. XML binding annotations, mixed content, polymorphism, and generic type
 * metadata are intentionally outside this fallback's scope.</p>
 */
public final class SoapEnvelopeCodec implements PayloadCodecPort {
    private static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    private static final int MAX_DEPTH = 32;
    private static final int MAX_BODY_CHARS = 10 * 1024 * 1024;

    private final SoapEnvelopeMetadata metadata;

    public SoapEnvelopeCodec(SoapEnvelopeMetadata metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
    }

    @Override
    public byte[] serialize(Object body) throws PayloadCodecException {
        try {
            Document document = document();
            Element envelope = document.createElementNS(SOAP_ENV_NS, "soap:Envelope");
            envelope.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:soap", SOAP_ENV_NS);
            document.appendChild(envelope);

            Element header = document.createElementNS(SOAP_ENV_NS, "soap:Header");
            envelope.appendChild(header);
            Element action = document.createElementNS(WS_ADDRESSING_NS, "wsa:Action");
            action.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:wsa", WS_ADDRESSING_NS);
            action.setTextContent(metadata.action());
            header.appendChild(action);

            Element soapBody = document.createElementNS(SOAP_ENV_NS, "soap:Body");
            envelope.appendChild(soapBody);
            Element operation = document.createElementNS(
                metadata.namespaceUri(), "m:" + metadata.operationName());
            operation.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                "xmlns:m", metadata.namespaceUri());
            soapBody.appendChild(operation);
            appendValue(document, operation, body, new IdentityHashMap<>(), 0);
            return toBytes(document);
        } catch (PayloadCodecException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new PayloadCodecException("SOAP envelope construction failed", failure);
        }
    }

    @Override
    public <T> T deserialize(byte[] body, Class<T> type) throws PayloadCodecException {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(type, "type");
        if (type == byte[].class) {
            return type.cast(body.clone());
        }
        if (type == String.class) {
            return type.cast(new String(body, StandardCharsets.UTF_8));
        }
        throw new PayloadCodecException(
            "SoapEnvelopeCodec returns raw SOAP bytes/String only; extract the payload and use a typed codec");
    }

    private static Document document() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder().newDocument();
    }

    private static void appendValue(Document document, Element parent, Object value,
                                    IdentityHashMap<Object, Boolean> visiting, int depth)
        throws Exception {
        if (depth > MAX_DEPTH) {
            throw new PayloadCodecException("SOAP body exceeds the maximum nesting depth of " + MAX_DEPTH);
        }
        if (value == null) {
            return;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean
            || value instanceof Character || value instanceof Enum<?>) {
            setText(parent, String.valueOf(value));
            return;
        }
        if (value instanceof byte[] bytes) {
            setText(parent, new String(bytes, StandardCharsets.UTF_8));
            return;
        }
        enter(value, visiting);
        try {
            if (value instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String name = entry.getKey() instanceof String key ? safeXmlName(key) : "item";
                    Element child = document.createElement(name);
                    parent.appendChild(child);
                    appendValue(document, child, entry.getValue(), visiting, depth + 1);
                }
            } else if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    Element child = document.createElement("item");
                    parent.appendChild(child);
                    appendValue(document, child, item, visiting, depth + 1);
                }
            } else if (value.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(value); i++) {
                    Element child = document.createElement("item");
                    parent.appendChild(child);
                    appendValue(document, child, Array.get(value, i), visiting, depth + 1);
                }
            } else {
                Map<String, Object> properties = properties(value);
                if (properties.isEmpty()) {
                    throw new PayloadCodecException(
                        "SOAP fallback supports only POJOs with public fields or getters; add a richer XML codec");
                }
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    Element child = document.createElement(safeXmlName(entry.getKey()));
                    parent.appendChild(child);
                    appendValue(document, child, entry.getValue(), visiting, depth + 1);
                }
            }
        } finally {
            visiting.remove(value);
        }
    }

    private static Map<String, Object> properties(Object value) throws Exception {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (Field field : value.getClass().getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                properties.put(field.getName(), field.get(value));
            }
        }
        for (Method method : value.getClass().getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0
                || method.getReturnType() == Void.TYPE || method.isSynthetic()
                || method.getName().equals("getClass")) {
                continue;
            }
            String property = propertyName(method.getName());
            if (property != null && !properties.containsKey(property)) {
                properties.put(property, method.invoke(value));
            }
        }
        return properties;
    }

    private static String propertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
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

    private static String safeXmlName(String value) {
        if (value.matches("[A-Za-z_][A-Za-z0-9_.-]*")) {
            return value;
        }
        return "item";
    }

    private static void setText(Element element, String value) {
        if (value.length() > MAX_BODY_CHARS) {
            throw new PayloadCodecException("SOAP body exceeds the 10 MiB safety limit");
        }
        element.setTextContent(value);
    }

    private static void enter(Object value, IdentityHashMap<Object, Boolean> visiting) {
        if (visiting.put(value, Boolean.TRUE) != null) {
            throw new PayloadCodecException("SOAP fallback cannot serialize cyclic object graphs");
        }
    }

    private static byte[] toBytes(Document document) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(output));
        return output.toByteArray();
    }
}
