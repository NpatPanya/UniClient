package com.npat.uniclient.adapter.soap;

import com.npat.uniclient.adapter.TransportBody;
import com.npat.uniclient.adapter.crosscutting.RequestHeaderAssembler;
import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.HeaderSet;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.port.TransportPort;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.xml.namespace.QName;

/**
 * Optional SOAP transport adapter backed by Apache CXF's JAX-WS Dispatch API.
 *
 * <p>The registry performs the CXF classpath guard before this class is constructed. This
 * adapter consumes a complete SOAP envelope as bytes or String and does not serialize POJOs.</p>
 */
public final class ApacheCxfAdapter implements TransportPort {
    private static final String SERVICE_NAMESPACE = "urn:npat:uniclient";
    private static final QName SERVICE_NAME = new QName(SERVICE_NAMESPACE, "UniClientService");
    private static final QName PORT_NAME = new QName(SERVICE_NAMESPACE, "UniClientPort");

    private final Function<java.net.URI, Dispatch<SOAPMessage>> dispatchFactory;

    /**
     * Creates an adapter that builds a message-mode CXF dispatch for each target endpoint.
     */
    public ApacheCxfAdapter() {
        this(ApacheCxfAdapter::createDispatch);
    }

    /**
     * Creates an adapter with an injectable dispatch factory for isolated tests.
     */
    public ApacheCxfAdapter(Function<java.net.URI, Dispatch<SOAPMessage>> dispatchFactory) {
        this.dispatchFactory = Objects.requireNonNull(dispatchFactory, "dispatchFactory");
    }

    @Override
    public ClientResponse execute(RequestSpec spec) throws ClientTransportException {
        byte[] body = TransportBody.toBytes(spec.body());
        try {
            Dispatch<SOAPMessage> dispatch = dispatchFactory.apply(spec.target());
            configureHeaders(dispatch, new RequestHeaderAssembler().assemble(spec));
            SOAPMessage request = MessageFactory.newInstance().createMessage(
                null, new ByteArrayInputStream(body));
            request.saveChanges();
            SOAPMessage response = dispatch.invoke(request);
            if (response == null) {
                throw new ClientTransportException("Apache CXF returned an empty SOAP response");
            }

            ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
            response.writeTo(responseBytes);
            return ClientResponse.builder()
                .statusCode(200)
                .body(responseBytes.toByteArray())
                .headers(HeaderSet.empty())
                .contentType(contentType(response))
                .build();
        } catch (ClientTransportException failure) {
            throw failure;
        } catch (SOAPException | IOException | WebServiceException failure) {
            throw new ClientTransportException(
                "Apache CXF transport failed for " + spec.target(), failure);
        }
    }

    private static Dispatch<SOAPMessage> createDispatch(java.net.URI endpoint) {
        try {
            Service service = Service.create(SERVICE_NAME);
            service.addPort(PORT_NAME, SOAPBinding.SOAP11HTTP_BINDING, endpoint.toString());
            return service.createDispatch(PORT_NAME, SOAPMessage.class, Service.Mode.MESSAGE);
        } catch (WebServiceException failure) {
            throw new ClientTransportException(
                "Apache CXF could not create a SOAP dispatch for " + endpoint, failure);
        }
    }

    private static void configureHeaders(Dispatch<SOAPMessage> dispatch, Map<String, String> headers) {
        Map<String, List<String>> requestHeaders = new HashMap<>();
        headers.forEach((name, value) -> requestHeaders.put(name, new ArrayList<>(List.of(value))));
        dispatch.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
    }

    private static String contentType(SOAPMessage message) {
        String[] values = message.getMimeHeaders().getHeader("Content-Type");
        return values.length == 0 ? null : values[0];
    }
}
