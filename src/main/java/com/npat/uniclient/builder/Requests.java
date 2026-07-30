package com.npat.uniclient.builder;

public final class Requests {

    private Requests() {
    }

    public static RestRequestBuilder rest() {
        return new RestRequestBuilder();
    }

    public static SoapRequestBuilder soap() {
        return new SoapRequestBuilder();
    }

    public static SocketRequestBuilder socket() {
        return new SocketRequestBuilder();
    }
}