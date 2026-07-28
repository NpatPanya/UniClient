package com.npat.uniclient.plan6;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.adapter.StandardAdapterFactories;
import com.npat.uniclient.adapter.codec.BuiltinJsonCodec;
import com.npat.uniclient.adapter.codec.SoapEnvelopeCodec;
import com.npat.uniclient.adapter.codec.SoapEnvelopeMetadata;
import com.npat.uniclient.adapter.crosscutting.MetadataHeaderFactory;
import com.npat.uniclient.adapter.crosscutting.RequestHeaderAssembler;
import com.npat.uniclient.adapter.http.HttpURLConnectionAdapter;
import com.npat.uniclient.adapter.rest.RestClientAdapter;
import com.npat.uniclient.core.model.AuthConfig;
import com.npat.uniclient.core.model.ClientResponse;
import com.npat.uniclient.core.model.RequestSpec;
import com.npat.uniclient.core.model.SslConfig;
import com.npat.uniclient.core.port.DependencyAvailabilityPort;
import com.npat.uniclient.core.port.PayloadCodecPort;
import com.npat.uniclient.core.port.TransportPort;
import com.npat.uniclient.core.support.ClasspathDependencyAvailability;
import com.npat.uniclient.facade.AdapterRegistry;
import com.npat.uniclient.facade.ClientFacade;
import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * Compile-only coverage for the code patterns published in README.md.
 */
public final class ReadmeExamplesPlan6Test {

    public static void main(String[] args) {
        RequestSpec rest = standardRestJsonRequest();
        RequestSpec soap = soapRequest();
        RequestSpec ssl = httpURLConnectionRequest();
        RequestSpec metadata = metadataRequest();
        ClientFacade client = standardClient();
        if (rest == null || soap == null || ssl == null || metadata == null || client == null) {
            throw new AssertionError("README example setup returned null");
        }
        extensibleRegistry();
    }

    private static RequestSpec standardRestJsonRequest() {
        PayloadCodecPort json = new BuiltinJsonCodec();
        byte[] body = json.serialize(new Order("A-17"));
        return RequestSpec.builder()
            .to("https://api.example.test/orders")
            .httpMethod("POST")
            .header("Content-Type", "application/json")
            .body(body)
            .build();
    }

    private static RequestSpec soapRequest() {
        PayloadCodecPort soap = new SoapEnvelopeCodec(new SoapEnvelopeMetadata(
            "urn:orders", "CreateOrder", "urn:orders:CreateOrder"));
        byte[] envelope = soap.serialize(new Order("A-17"));
        return RequestSpec.builder()
            .to("https://soap.example.test/orders")
            .header("Content-Type", "text/xml; charset=utf-8")
            .body(envelope)
            .build();
    }

    private static RequestSpec httpURLConnectionRequest() {
        SslConfig ssl = SslConfig.custom(null, null, "C:/certs/orders-truststore.p12", "changeit");
        return RequestSpec.builder()
            .to("https://api.example.test/health")
            .httpMethod("GET")
            .ssl(ssl)
            .body("")
            .build();
    }

    private static RequestSpec metadataRequest() {
        return RequestSpec.builder()
            .to("https://api.example.test/orders")
            .header("X-Tenant", "acme")
            .header("X-Correlation-ID", "trace-123")
            .auth(AuthConfig.bearer("token-value"))
            .body("{}")
            .build();
    }

    private static ClientFacade standardClient() {
        DependencyAvailabilityPort availability = new ClasspathDependencyAvailability();
        AdapterRegistry registry = new AdapterRegistry(
            availability, StandardAdapterFactories.create());
        return new ClientFacade(registry);
    }

    private static void extensibleRegistry() {
        TransportPort custom = spec -> ClientResponse.builder()
            .statusCode(200)
            .body("custom response")
            .build();
        EnumMap<ServiceClient, Supplier<TransportPort>> factories = new EnumMap<>(ServiceClient.class);
        for (ServiceClient engine : ServiceClient.values()) {
            factories.put(engine, () -> custom);
        }
        new AdapterRegistry(new ClasspathDependencyAvailability(), factories);

        TransportPort metadataTransport = new RestClientAdapter(
            null,
            new RequestHeaderAssembler(new MetadataHeaderFactory("orders", "1.0")));
        if (metadataTransport == null || new HttpURLConnectionAdapter() == null) {
            throw new AssertionError("README transport setup failed");
        }
    }

    public static final class Order {
        public String id;

        public Order() {
        }

        public Order(String id) {
            this.id = id;
        }
    }
}
