package com.npat.uniclient.adapter.codec;
import com.fasterxml.jackson.databind.ObjectMapper;
public final class JacksonCodecFactory {private JacksonCodecFactory(){} public static JacksonJsonCodec create(){return new JacksonJsonCodec(new ObjectMapper());} public static JacksonJsonCodec create(ObjectMapper mapper){return new JacksonJsonCodec(mapper);}}