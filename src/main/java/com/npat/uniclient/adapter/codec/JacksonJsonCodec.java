package com.npat.uniclient.adapter.codec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npat.uniclient.domain.ResponseType;
import com.npat.uniclient.dto.RestfulResponse;
import com.npat.uniclient.port.JsonCodec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
public final class JacksonJsonCodec implements JsonCodec {
 private final ObjectMapper mapper;
 public JacksonJsonCodec(ObjectMapper mapper){this.mapper=Objects.requireNonNull(mapper,"mapper");}
 public byte[] encode(Object value){try{if(value instanceof byte[] b)return b;if(value instanceof String s)return s.getBytes(StandardCharsets.UTF_8);return mapper.writeValueAsBytes(value);}catch(Exception e){throw new IllegalArgumentException("Unable to encode JSON request payload",e);}}
 public <T>T decode(String payload,ResponseType<T> type){try{return mapper.readValue(payload,mapper.constructType(type.type()));}catch(Exception e){throw new IllegalArgumentException("Unable to decode JSON payload",e);}}
 public <T> RestfulResponse<T> decodeResponseEnvelope(String raw,ResponseType<T> type){RestfulResponse<T> r=new RestfulResponse<>();r.setRawPayload(raw);try{JsonNode root=mapper.readTree(raw);r.setRspCode(text(root,"rspCode"));r.setRspMessage(text(root,"rspMessage"));r.setErrorMessage(text(root,"errorMessage"));JsonNode entity=root.get("responseEntity");if(entity!=null&&!entity.isNull())r.setResponseEntity(mapper.readerFor(mapper.constructType(type.type())).readValue(entity));}catch(Exception e){r.setErrorMessage("Unable to decode upstream JSON response");}return r;}
 private static String text(JsonNode root,String n){JsonNode v=root.get(n);return v==null||v.isNull()?null:v.asText();}
}