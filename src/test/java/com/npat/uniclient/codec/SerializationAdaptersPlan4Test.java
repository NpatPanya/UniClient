package com.npat.uniclient.codec;

import com.npat.uniclient.adapter.codec.BuiltinJsonCodec;
import com.npat.uniclient.adapter.codec.JacksonJsonCodec;
import com.npat.uniclient.adapter.codec.PayloadCodecResolver;
import com.npat.uniclient.adapter.codec.SoapEnvelopeCodec;
import com.npat.uniclient.adapter.codec.SoapEnvelopeMetadata;
import com.npat.uniclient.core.exception.PayloadCodecException;
import com.npat.uniclient.core.port.PayloadCodecPort;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Executable tests for Plan 4 serialization adapters.
 */
public final class SerializationAdaptersPlan4Test {

    public static void main(String[] args) throws Exception {
        builtinCodecRoundTripsAFlatPojo();
        builtinCodecRoundTripsAMap();
        builtinCodecRejectsUnsupportedInputWithGuidance();
        codecResolverSelectsJacksonOnlyWhenAvailable();
        jacksonCodecRoundTripsNestedData();
        soapCodecProducesAParseableEnvelope();
    }

    private static void builtinCodecRoundTripsAFlatPojo() {
        BuiltinJsonCodec codec = new BuiltinJsonCodec();
        Person original = new Person("Ada", 37);

        byte[] json = codec.serialize(original);
        Person decoded = codec.deserialize(json, Person.class);

        assertContains(new String(json, StandardCharsets.UTF_8), "\"name\":\"Ada\"", "POJO JSON");
        assertEquals(original.name, decoded.name, "POJO name");
        assertEquals(original.age, decoded.age, "POJO age");
    }

    private static void builtinCodecRoundTripsAMap() {
        BuiltinJsonCodec codec = new BuiltinJsonCodec();
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("name", "Ada");
        original.put("active", true);
        original.put("score", 9.5);

        byte[] json = codec.serialize(original);
        Map<?, ?> decoded = codec.deserialize(json, Map.class);

        assertEquals(original, decoded, "map");
    }

    private static void builtinCodecRejectsUnsupportedInputWithGuidance() {
        PayloadCodecException failure = assertThrows(
            PayloadCodecException.class, () -> new BuiltinJsonCodec().serialize(new Object()));
        assertContains(failure.getMessage(), "Jackson", "fallback guidance");
    }

    private static void codecResolverSelectsJacksonOnlyWhenAvailable() {
        PayloadCodecPort builtin = new PayloadCodecResolver(className -> false).resolveJson();
        PayloadCodecPort jackson = new PayloadCodecResolver(className -> true).resolveJson();

        if (!(builtin instanceof BuiltinJsonCodec)) {
            throw new AssertionError("Missing Jackson should select BuiltinJsonCodec");
        }
        if (!(jackson instanceof JacksonJsonCodec)) {
            throw new AssertionError("Available Jackson should select JacksonJsonCodec");
        }
    }

    private static void jacksonCodecRoundTripsNestedData() {
        JacksonJsonCodec codec = new JacksonJsonCodec();
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("person", Map.of("name", "Ada"));
        original.put("roles", java.util.List.of("admin", "reviewer"));

        Map<?, ?> decoded = codec.deserialize(codec.serialize(original), Map.class);

        assertEquals(original, decoded, "nested Jackson map");
    }

    private static void soapCodecProducesAParseableEnvelope() throws Exception {
        SoapEnvelopeMetadata metadata = new SoapEnvelopeMetadata(
            "urn:orders", "CreateOrder", "urn:orders:CreateOrder");
        SoapEnvelopeCodec codec = new SoapEnvelopeCodec(metadata);

        byte[] xml = codec.serialize(new Person("Ada & Bob", 37));
        DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        Document document = parserFactory
            .newDocumentBuilder()
            .parse(new ByteArrayInputStream(xml));
        Element envelope = document.getDocumentElement();
        Element body = (Element) envelope.getElementsByTagNameNS(
            "http://schemas.xmlsoap.org/soap/envelope/", "Body").item(0);
        Element operation = (Element) body.getFirstChild();

        assertEquals("Envelope", envelope.getLocalName(), "SOAP envelope");
        assertEquals("Body", body.getLocalName(), "SOAP body");
        assertEquals("CreateOrder", operation.getLocalName(), "SOAP operation");
        assertEquals("urn:orders", operation.getNamespaceURI(), "SOAP namespace");
        assertEquals("urn:orders:CreateOrder", document.getElementsByTagNameNS(
            "http://www.w3.org/2005/08/addressing", "Action").item(0).getTextContent(), "SOAP action");
        assertContains(operation.getTextContent(), "Ada & Bob", "escaped SOAP body value");
    }

    private static <T extends Throwable> T assertThrows(Class<T> type, ThrowingAction action) {
        try {
            action.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) {
                return type.cast(failure);
            }
            throw new AssertionError("Expected " + type.getName() + " but got "
                + failure.getClass().getName(), failure);
        }
        throw new AssertionError("Expected " + type.getName() + " to be thrown");
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertContains(String actual, String expected, String name) {
        if (actual == null || !actual.contains(expected)) {
            throw new AssertionError(name + ": expected '" + expected + "' in '" + actual + "'");
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    public static final class Person {
        public String name;
        public int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
