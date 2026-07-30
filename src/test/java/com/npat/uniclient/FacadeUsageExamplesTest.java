package com.npat.uniclient;

import com.npat.uniclient.builder.Requests;
import com.npat.uniclient.domain.HTTP_METHOD;
import com.npat.uniclient.domain.SocketFormat;
import com.npat.uniclient.dto.RestfulRequest;
import com.npat.uniclient.dto.SOAPRequest;
import com.npat.uniclient.dto.SocketRequest;
import com.npat.uniclient.dto.SocketResponse;
import com.npat.uniclient.facade.AdapterRegistry;
import com.npat.uniclient.facade.UniClient;
import com.npat.uniclient.port.SocketAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.time.Duration;
import java.util.List;

/**
 * Executable, non-network examples for every direct protocol builder.
 */
public final class FacadeUsageExamplesTest {
    public static void main(String[] args) throws Exception {
        httpUrlConnectionExample();
        restClientExample();
        soapEnvelopeExample();
        socketSpiExample();
    }

    static void httpUrlConnectionExample() {
        RestfulRequest<String, String> r = Requests.httpUrlConnection().body("raw").endpoint("https://example.test/http").method(HTTP_METHOD.POST).connTimeout(Duration.ofSeconds(1)).readTimeout(Duration.ofSeconds(1)).build();
        if (r.getPayload() == null) throw new AssertionError();
    }

    static void restClientExample() {
        RestfulRequest<Order, OrderReply> r = Requests.restClient().body(new Order("A-1")).responseType(OrderReply.class).endpoint("https://example.test/rest").method(HTTP_METHOD.POST).connTimeout(Duration.ofSeconds(1)).readTimeout(Duration.ofSeconds(1)).build();
        if (r.getResponseType() == null) throw new AssertionError();
    }

    static void soapEnvelopeExample() throws Exception {
        Element e = envelope();
        SOAPRequest r = Requests.soapCxf().endpoint("https://example.test/soap").body(e).soapAction("urn:submit").connTimeout(Duration.ofSeconds(1)).readTimeout(Duration.ofSeconds(1)).build();
        if (r.getPayload() != e) throw new AssertionError();
    }

    static void socketSpiExample() {
        SocketRequest<Order, SocketReply> r = Requests.socket().body(new Order("A-1")).responseType(SocketReply.class).host("127.0.0.1").port(9000).format(SocketFormat.of("example-format")).connTimeout(Duration.ofSeconds(1)).readTimeout(Duration.ofSeconds(1)).build();
        SocketResponse<SocketReply> response = new UniClient(new AdapterRegistry(List.of(new ExampleSocketAdapter()))).send(r);
        if (!"A-1".equals(response.getResponseEntity().reference)) throw new AssertionError();
    }

    static Element envelope() throws Exception {
        String ns = "http://schemas.xmlsoap.org/soap/envelope/";
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element e = d.createElementNS(ns, "soap:Envelope");
        e.appendChild(d.createElementNS(ns, "soap:Body"));
        d.appendChild(e);
        return e;
    }

    static final class ExampleSocketAdapter implements SocketAdapter {
        public SocketResponse<?> send(SocketRequest<?, ?> r) {
            SocketResponse<SocketReply> x = new SocketResponse<>();
            x.setSuccess(true);
            x.setResponseEntity(new SocketReply("A-1"));
            return x;
        }
    }

    record Order(String reference) {
    }

    record OrderReply(String reference) {
    }

    record SocketReply(String reference) {
    }
}