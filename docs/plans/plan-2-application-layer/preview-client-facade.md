# Preview — ClientFacade Shape (illustrative, not yet executed)

This is a sketch to review the design from `sub-2.1-facade-orchestrator.md`,
`sub-2.2-request-builder.md`, and `sub-2.3-adapter-registry.md` before any sub-plan is
actually executed. Nothing here has been written to `src/` — per Rule 4 of
`plan-mode-protocol`, real execution starts only once you approve Sub 1.1.

## Supporting types (from Plan 1, sketched for context)

```java
// core/model/RequestSpec.java
public final class RequestSpec {
    private final URI target;
    private final Object body;
    private final Map<String, String> headers;
    private final TimeoutConfig timeout;
    private final AuthConfig auth;
    private final SslConfig ssl;

    private RequestSpec(Builder b) {
        this.target = b.target;
        this.body = b.body;
        this.headers = Map.copyOf(b.headers);
        this.timeout = b.timeout;
        this.auth = b.auth;
        this.ssl = b.ssl;
    }

    public static Builder builder() { return new Builder(); }

    public URI target() { return target; }
    public Object body() { return body; }
    public Map<String, String> headers() { return headers; }
    public TimeoutConfig timeout() { return timeout; }
    public AuthConfig auth() { return auth; }
    public SslConfig ssl() { return ssl; }

    public static final class Builder {
        private URI target;
        private Object body;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private TimeoutConfig timeout = TimeoutConfig.defaults();
        private AuthConfig auth = AuthConfig.none();
        private SslConfig ssl = SslConfig.platformDefault();

        public Builder to(String uri) { this.target = URI.create(uri); return this; }
        public Builder body(Object body) { this.body = body; return this; }
        public Builder header(String k, String v) { this.headers.put(k, v); return this; }
        public Builder timeout(TimeoutConfig t) { this.timeout = t; return this; }
        public Builder auth(AuthConfig a) { this.auth = a; return this; }
        public Builder ssl(SslConfig s) { this.ssl = s; return this; }

        public RequestSpec build() {
            if (target == null) {
                throw new UniClientException("RequestSpec requires a target destination — call .to(uri) before .build()");
            }
            return new RequestSpec(this);
        }
    }
}
```

```java
// core/model/ClientResponse.java
public final class ClientResponse {
    private final int statusCode;
    private final byte[] rawBody;
    private final Map<String, String> headers;
    private final PayloadCodecPort codec;

    ClientResponse(int statusCode, byte[] rawBody, Map<String, String> headers, PayloadCodecPort codec) {
        this.statusCode = statusCode;
        this.rawBody = rawBody;
        this.headers = headers;
        this.codec = codec;
    }

    public int statusCode() { return statusCode; }
    public String bodyAsString() { return new String(rawBody, StandardCharsets.UTF_8); }
    public <T> T as(Class<T> type) { return codec.deserialize(rawBody, type); }
    public Map<String, String> headers() { return headers; }
}
```

```java
// core/port/TransportPort.java
public interface TransportPort {
    ClientResponse execute(RequestSpec spec) throws ClientTransportException;
}
```

## The facade itself (Sub 2.1)

One job: hand a `RequestSpec` + `ServiceClient` choice to whatever `TransportPort` the
registry resolves, and return what comes back. No `if (engine == APACHE_CXF)` branching
lives here — that's the registry's job.

```java
// facade/ClientFacade.java
public final class ClientFacade {
    private final AdapterRegistry registry;

    public ClientFacade(AdapterRegistry registry) {
        this.registry = registry;
    }

    public ClientResponse execute(RequestSpec spec, ServiceClient engine) {
        TransportPort transport = registry.resolve(engine); // throws MissingClientDependencyException if unavailable
        return transport.execute(spec);                     // ClientTransportException propagates unwrapped
    }
}
```

## The adapter registry (Sub 2.3)

Owns the one `ServiceClient -> TransportPort` mapping and the dependency guard from
`sub-1.4-dependency-guard.md`.

```java
// facade/AdapterRegistry.java
public final class AdapterRegistry {
    private final DependencyAvailabilityPort availability;
    private final Map<ServiceClient, Supplier<TransportPort>> factories = new EnumMap<>(ServiceClient.class);

    public AdapterRegistry(DependencyAvailabilityPort availability) {
        this.availability = availability;
        factories.put(ServiceClient.HTTPURLCONNECTION, HttpURLConnectionAdapter::new);
        factories.put(ServiceClient.REST_CLIENT, RestClientAdapter::new);
        factories.put(ServiceClient.APACHE_CXF, () -> {
            requireAvailable("org.apache.cxf.endpoint.Client", "APACHE_CXF",
                "org.apache.cxf:cxf-rt-frontend-jaxws:optional");
            return new ApacheCxfAdapter();
        });
    }

    public TransportPort resolve(ServiceClient engine) {
        return factories.get(engine).get();
    }

    private void requireAvailable(String className, String engineName, String mavenCoordinate) {
        if (!availability.isAvailable(className)) {
            throw new MissingClientDependencyException(engineName, mavenCoordinate);
        }
    }
}
```

## Consumer-facing usage (what the README examples will look like)

```java
ClientFacade facade = new ClientFacade(new AdapterRegistry(new ClasspathDependencyAvailability()));

RequestSpec spec = RequestSpec.builder()
    .to("https://api.example.com/orders")
    .body(new CreateOrderRequest("SKU-1", 2))
    .header("X-Api-Key", "secret")
    .build();

ClientResponse response = facade.execute(spec, ServiceClient.REST_CLIENT);
OrderConfirmation confirmation = response.as(OrderConfirmation.class);
```

Swap `ServiceClient.REST_CLIENT` for `ServiceClient.APACHE_CXF` and nothing else changes —
if CXF isn't on the classpath, `facade.execute(...)` throws
`MissingClientDependencyException("APACHE_CXF requires org.apache.cxf:cxf-rt-frontend-jaxws. Add it: ...")`
instead of a cryptic `NoClassDefFoundError`.

## What's not shown here

`HttpURLConnectionAdapter`, `RestClientAdapter`, `ApacheCxfAdapter`, the codecs, and the
config value types (`TimeoutConfig`, `AuthConfig`, `SslConfig`) are Plan 3/4/1 territory —
left as method-signature stubs above on purpose so this preview stays focused on the facade
shape you asked to see.

## Decision points this preview surfaces

- `ClientResponse` is a `final` class here, not the `abstract` one currently on disk — matches
  the "decide and record" flag in `sub-1.1-domain-model.md`. Worth confirming before Sub 1.1
  executes for real.
- `ClientFacade` does **not** implement the existing `Client<T>` interface in this sketch —
  it exposes `execute(spec, engine)` instead of `send()`/`response()`. Also flagged as a
  decision point in `sub-2.1-facade-orchestrator.md`; happy to sketch the `Client<T>`-
  implementing variant instead if you'd rather keep that interface.
