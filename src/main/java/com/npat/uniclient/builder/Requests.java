package com.npat.uniclient.builder;

public final class Requests {

    private Requests() {
    }

    public static HttpUrlConnectionRequestBuilder<Object, String> httpUrlConnection() {
        return new HttpUrlConnectionRequestBuilder<>();
    }

    public static RestRequestBuilder<Object, String> restClient() {
        return new RestRequestBuilder<>();
    }

    public static SoapRequestBuilder soapCxf() {
        return new SoapRequestBuilder();
    }

    public static SocketRequestBuilder<Object, byte[]> socket() {
        return new SocketRequestBuilder<>();
    }
}