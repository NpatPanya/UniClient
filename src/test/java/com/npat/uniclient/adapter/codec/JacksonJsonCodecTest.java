package com.npat.uniclient.adapter.codec;

import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.dto.RestfulResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class JacksonJsonCodecTest {
    private JacksonJsonCodecTest() {
    }

    public static void main(String[] args) {
        JacksonJsonCodec codec = JacksonCodecFactory.create();
        assertEquals("raw", new String(codec.encode("raw"), StandardCharsets.UTF_8), "string pass-through");
        assertEquals("{\"name\":\"Ada\"}", new String(codec.encode(new Item("Ada")), StandardCharsets.UTF_8), "DTO encoding");
        RestfulResponse<Item> decoded = codec.decodeResponseEnvelope("{\"rspCode\":\"00\",\"rspMessage\":\"ok\",\"responseEntity\":{\"name\":\"Ada\"}}", ResponseType.of(Item.class));
        assertEquals("00", decoded.getRspCode(), "response code");
        assertEquals("Ada", decoded.getResponseEntity().name, "DTO decoding");
        RestfulResponse<List<Item>> generic = codec.decodeResponseEnvelope("{\"responseEntity\":[{\"name\":\"Ada\"}]}", new ResponseType<List<Item>>() {
        });
        assertEquals("Ada", generic.getResponseEntity().get(0).name, "generic decoding");
        RestfulResponse<Item> malformed = codec.decodeResponseEnvelope("not-json", ResponseType.of(Item.class));
        assertEquals("not-json", malformed.getRawPayload(), "raw preserved on decode failure");
        if (malformed.getErrorMessage() == null) throw new AssertionError("decode error must be retained");
    }

    public static final class Item {
        public String name;

        public Item() {
        }

        public Item(String name) {
            this.name = name;
        }
    }

    static void assertEquals(Object e, Object a, String l) {
        if (e == null ? a != null : !e.equals(a)) throw new AssertionError(l + " expected=" + e + " actual=" + a);
    }
}