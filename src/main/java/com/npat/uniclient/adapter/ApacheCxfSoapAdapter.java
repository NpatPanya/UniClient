package com.npat.uniclient.adapter;

import com.npat.uniclient.domain.HttpTransportKind;
import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;
import com.npat.uniclient.dto.SOAPRequest;
import com.npat.uniclient.dto.SOAPResponse;
import com.npat.uniclient.exception.MissingDependencyException;
import com.npat.uniclient.exception.TransportException;
import com.npat.uniclient.port.DependencyAvailability;
import com.npat.uniclient.port.TransportAdapter;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

/** Optional CXF JAX-WS Dispatch transport for complete caller-supplied SOAP envelopes. */
public final class ApacheCxfSoapAdapter implements TransportAdapter {
    private static final String SOAP_11 = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final QName SERVICE_NAME = new QName("urn:uniclient", "UniClientSoapService");
    private static final QName PORT_NAME = new QName("urn:uniclient", "UniClientSoapPort");

    @Override
    public boolean supports(APIRequest<?, ?> request) {
        return request instanceof SOAPRequest soap
                && soap.getConfig().getTransportKind() == HttpTransportKind.SOAP_CXF;
    }

    @Override
    public DependencyAvailability dependencyAvailability() {
        return ApacheCxfSoapAdapter::requireCxf;
    }

    @Override
    public APIResponse<?> send(APIRequest<?, ?> request) {
        requireCxf();
        if (!(request instanceof SOAPRequest soap)) {
            throw new TransportException("CXF SOAP adapter requires SOAPRequest");
        }
        try {
            return invokeCxfDispatch(soap);
        } catch (TransportException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new TransportException("CXF SOAP dispatch failed before a usable response was received", exception);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private SOAPResponse<Element> invokeCxfDispatch(SOAPRequest request) throws Exception {
        ClassLoader loader = ApacheCxfSoapAdapter.class.getClassLoader();
        Class<?> serviceType = Class.forName("jakarta.xml.ws.Service", true, loader);
        Class<?> modeType = Class.forName("jakarta.xml.ws.Service$Mode", true, loader);
        Object service = serviceType.getMethod("create", QName.class).invoke(null, SERVICE_NAME);
        String binding = SOAP_11.equals(request.getPayload().getNamespaceURI())
                ? "http://schemas.xmlsoap.org/wsdl/soap/http"
                : "http://www.w3.org/2003/05/soap/bindings/HTTP/";
        serviceType.getMethod("addPort", QName.class, String.class, String.class)
                .invoke(service, PORT_NAME, binding, request.getConfig().getUrlConfig().getDestURL());
        Object messageMode = Enum.valueOf((Class<Enum>) modeType.asSubclass(Enum.class), "MESSAGE");
        Object dispatch = serviceType.getMethod("createDispatch", QName.class, Class.class, modeType)
                .invoke(service, PORT_NAME, DOMSource.class, messageMode);

        Method requestContextMethod = dispatch.getClass().getMethod("getRequestContext");
        Map<String, Object> context = (Map<String, Object>) requestContextMethod.invoke(dispatch);
        context.put("jakarta.xml.ws.service.endpoint.address", request.getConfig().getUrlConfig().getDestURL());
        context.put("jakarta.xml.ws.client.connectionTimeout", timeoutMillis(request.getConfig().getConnTimeout()));
        context.put("jakarta.xml.ws.client.receiveTimeout", timeoutMillis(request.getConfig().getReadTimeout()));
        context.put("jakarta.xml.ws.http.request.headers", request.getConfig().getHeaderConfig().all());
        if (request.getSoapAction() != null) {
            context.put("jakarta.xml.ws.soap.http.soapaction.use", Boolean.TRUE);
            context.put("jakarta.xml.ws.soap.http.soapaction.uri", request.getSoapAction());
        }

        try {
            Object returned = dispatch.getClass().getMethod("invoke", Object.class)
                    .invoke(dispatch, new DOMSource(request.getPayload()));
            Element envelope = elementFrom((DOMSource) returned);
            SOAPResponse<Element> response = new SOAPResponse<>();
            response.setResponseEntity(envelope);
            response.setHttpCode(responseHttpCode(dispatch));
            boolean fault = hasFault(envelope);
            response.setSuccess(!fault);
            if (fault) {
                response.setFaultString("SOAP Fault");
                response.setErrorMessage("SOAP Fault");
            }
            return response;
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause != null && "jakarta.xml.ws.soap.SOAPFaultException".equals(cause.getClass().getName())) {
                SOAPResponse<Element> response = new SOAPResponse<>();
                response.setSuccess(false);
                response.setHttpCode(responseHttpCode(dispatch));
                response.setFaultString(cause.getMessage());
                response.setErrorMessage(cause.getMessage());
                return response;
            }
            throw exception;
        }
    }

    @SuppressWarnings("unchecked")
    private static Integer responseHttpCode(Object dispatch) throws Exception {
        Map<String, Object> context = (Map<String, Object>) dispatch.getClass()
                .getMethod("getResponseContext")
                .invoke(dispatch);
        Object status = context.get("jakarta.xml.ws.http.response.code");
        return status instanceof Integer code ? code : null;
    }

    private static Element elementFrom(DOMSource source) {
        if (source.getNode() instanceof org.w3c.dom.Document document) {
            return document.getDocumentElement();
        }
        if (source.getNode() instanceof Element element) {
            return element;
        }
        throw new TransportException("CXF SOAP dispatch returned no SOAP envelope element");
    }

    private static boolean hasFault(Element envelope) {
        return envelope.getElementsByTagNameNS(envelope.getNamespaceURI(), "Fault").getLength() > 0;
    }

    private static Long timeoutMillis(Duration duration) {
        return duration.toMillis();
    }

    private static void requireCxf() {
        try {
            Class.forName("org.apache.cxf.Bus", false, ApacheCxfSoapAdapter.class.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new MissingDependencyException("Apache CXF is required for SOAP. Enable the optional-adapters profile.");
        }
    }
}