package com.npat.uniclient.adapter;

import com.npat.uniclient.builder.Requests;
import com.npat.uniclient.dto.SOAPRequest;
import com.npat.uniclient.dto.SOAPResponse;
import com.npat.uniclient.exception.MissingDependencyException;
import com.sun.net.httpserver.HttpServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@SuppressWarnings("unchecked")
public final class ApacheCxfSoapAdapterTest {
    private static final String S11 = "http://schemas.xmlsoap.org/soap/envelope/", S12 = "http://www.w3.org/2003/05/soap-envelope";

    public static void main(String[] args) throws Exception {
        if (!cxfPresent()) {
            assertThrows(MissingDependencyException.class, () -> new ApacheCxfSoapAdapter().send(request(S11, "http://example.invalid", false)), "default guard");
            return;
        }
        HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        s.createContext("/soap", e -> {
            String version = e.getRequestHeaders().getFirst("Content-Type").startsWith("application/soap+xml") ? S12 : S11;
            boolean fault = "fault".equals(e.getRequestHeaders().getFirst("X-Fault"));
            String payload = "<s:Envelope xmlns:s=\"" + version + "\"><s:Body>" + (fault ? (S11.equals(version) ? "<s:Fault><faultcode>s:Server</faultcode><faultstring>bad</faultstring></s:Fault>" : "<s:Fault><s:Code><s:Value>s:Receiver</s:Value></s:Code><s:Reason><s:Text xml:lang=\"en\">bad</s:Text></s:Reason></s:Fault>") : "<ok/>") + "</s:Body></s:Envelope>";
            byte[] b = payload.getBytes(StandardCharsets.UTF_8);
            e.sendResponseHeaders(fault ? 500 : 200, b.length);
            e.getResponseBody().write(b);
            e.close();
        });
        s.start();
        try {
            String endpoint = "http://127.0.0.1:" + s.getAddress().getPort() + "/soap";
            SOAPResponse<Element> a = (SOAPResponse<Element>) new ApacheCxfSoapAdapter().send(request(S11, endpoint, false));
            assertEquals(true, a.isSuccess(), "soap11 success");
            assertEquals(200, a.getHttpCode(), "soap11 http code");
            SOAPResponse<Element> b = (SOAPResponse<Element>) new ApacheCxfSoapAdapter().send(request(S12, endpoint, false));
            assertEquals(true, b.isSuccess(), "soap12 success");
            assertEquals(200, b.getHttpCode(), "soap12 http code");
            SOAPResponse<Element> f = (SOAPResponse<Element>) new ApacheCxfSoapAdapter().send(request(S11, endpoint, true));
            assertEquals(false, f.isSuccess(), "fault response");
            assertEquals(500, f.getHttpCode(), "fault http code");
        } finally {
            s.stop(0);
        }
    }

    private static SOAPRequest request(String ns, String endpoint, boolean fault) throws Exception {
        Element e = envelope(ns);
        var b = Requests.soapCxf().endpoint(endpoint).body(e).soapAction("urn:action").connTimeout(Duration.ofSeconds(2)).readTimeout(Duration.ofSeconds(2));
        if (fault) b.header("X-Fault", "fault");
        return b.build();
    }

    private static Element envelope(String ns) throws Exception {
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element e = d.createElementNS(ns, "s:Envelope");
        e.appendChild(d.createElementNS(ns, "s:Body"));
        d.appendChild(e);
        return e;
    }

    private static boolean cxfPresent() {
        try {
            Class.forName("org.apache.cxf.Bus");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void assertThrows(Class<? extends Throwable> x, Run r, String n) {
        try {
            r.run();
        } catch (Throwable e) {
            if (x.isInstance(e)) return;
            throw new AssertionError(n, e);
        }
        throw new AssertionError(n);
    }

    private static void assertEquals(Object x, Object y, String n) {
        if (x == null ? y != null : !x.equals(y)) throw new AssertionError(n);
    }

    @FunctionalInterface
    interface Run {
        void run() throws Exception;
    }
}