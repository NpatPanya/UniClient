package com.npat.uniclient.dto;

import com.npat.uniclient.dto.base.HttpRequestConfig;
import org.w3c.dom.Element;

public final class SOAPRequest extends APIRequest<Element, SOAPResponse<Element>> {

    private final HttpRequestConfig config;
    private final String soapAction;

    public SOAPRequest(Element envelope, HttpRequestConfig config, String soapAction) {
        super(envelope, null);
        this.config = config;
        this.soapAction = soapAction;
    }

    public HttpRequestConfig getConfig() {
        return config;
    }

    public String getSoapAction() {
        return soapAction;
    }
}