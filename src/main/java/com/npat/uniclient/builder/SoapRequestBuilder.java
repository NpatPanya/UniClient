package com.npat.uniclient.builder;

import com.npat.uniclient.domain.HTTP_METHOD;
import com.npat.uniclient.domain.HttpTransportKind;
import com.npat.uniclient.dto.SOAPRequest;
import com.npat.uniclient.dto.base.HttpRequestConfig;
import com.npat.uniclient.dto.componenet.HeaderConfig;
import com.npat.uniclient.dto.componenet.URLConfig;
import com.npat.uniclient.exception.RequestValidationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.net.ssl.SSLContext;

public final class SoapRequestBuilder extends BaseRequestBuilder<SoapRequestBuilder> {

    private static final String SOAP_11 = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String SOAP_12 = "http://www.w3.org/2003/05/soap-envelope";

    private String endpoint;
    private Element envelope;
    private String soapAction;
    private SSLContext sslContext;
    private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

    public SoapRequestBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public SoapRequestBuilder body(Element envelope) {
        this.envelope = envelope;
        return this;
    }

    public SoapRequestBuilder soapAction(String soapAction) {
        this.soapAction = soapAction;
        return this;
    }

    public SoapRequestBuilder header(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public SoapRequestBuilder sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    @Override
    protected SoapRequestBuilder self() {
        return this;
    }

    public SOAPRequest build() {
        validateTimeoutsAndResponseLimit();
        validateHttpEndpoint(endpoint);
        validateNoAuthorizationConflict(headers);
        validateEnvelope();
        return new SOAPRequest(envelope, new HttpRequestConfig(URLConfig.soap(endpoint), auth, connTimeout, readTimeout,
                HTTP_METHOD.POST, HeaderConfig.of(headers), sslContext, false, maxResponseBytes, HttpTransportKind.SOAP_CXF), soapAction);
    }

    private void validateEnvelope() {
        if (envelope == null || !"Envelope".equals(envelope.getLocalName())
                || !(SOAP_11.equals(envelope.getNamespaceURI()) || SOAP_12.equals(envelope.getNamespaceURI()))) {
            throw new RequestValidationException("SOAP body must be a complete SOAP 1.1 or 1.2 Envelope element");
        }
        for (Node child = envelope.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && "Body".equals(element.getLocalName())
                    && envelope.getNamespaceURI().equals(element.getNamespaceURI())) {
                return;
            }
        }
        throw new RequestValidationException("SOAP Envelope must contain a SOAP Body element");
    }
}