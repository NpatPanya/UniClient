# UniClient

UniClient provides one application-facing facade for swappable HTTP and SOAP transports. The
core depends on small ports (`TransportPort`, `PayloadCodecPort`, and
`DependencyAvailabilityPort`); concrete adapters are supplied at the composition boundary.

## Features

- JDK-only HTTP transports: `HttpURLConnectionAdapter` and `RestClientAdapter`.
- Optional Apache CXF transport: `ApacheCxfAdapter`.
- Dependency-free JSON and SOAP fallback codecs, with optional Jackson JSON support.
- Shared authentication, SSL, correlation metadata, timeouts, and retries.
- Consistent `ClientTransportException`, `PayloadCodecException`, and
  `MissingClientDependencyException` errors.

## Installation

The library coordinates are:

```xml
<dependency>
    <groupId>com.npat.uniclient</groupId>
    <artifactId>uniclient</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

CXF and Jackson are optional. Add them explicitly only when those adapters are needed:

```xml
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>4.2.2</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.22.0</version>
</dependency>
```

For this checkout, `mvn test` verifies the dependency-free profile. Run
`mvn -Poptional-adapters test` to compile and verify the optional CXF/Jackson paths.

## Configuration

`RequestSpec` is the immutable request value. The registry requires one factory for each
`ServiceClient` value and applies the standard retry decorator when resolving a client:

```java
DependencyAvailabilityPort availability = new ClasspathDependencyAvailability();
AdapterRegistry registry = new AdapterRegistry(
    availability,
    StandardAdapterFactories.create());
ClientFacade client = new ClientFacade(registry);
```

`StandardAdapterFactories` includes CXF, so use it with the optional-adapters profile or with
CXF explicitly available. A consumer that only needs JDK transports can register those two
factories directly and provide a never-used placeholder for `APACHE_CXF`.

## Usage Examples

### 1. Standard REST JSON Request

The built-in codec has no third-party runtime dependency. Transport adapters accept serialized
`byte[]` or `String` bodies, so serialization is an explicit composition step:

```java
public final class Order {
    public String id;

    public Order() {
    }

    public Order(String id) {
        this.id = id;
    }
}

PayloadCodecPort json = new BuiltinJsonCodec();
byte[] body = json.serialize(new Order("A-17"));
RequestSpec request = RequestSpec.builder()
    .to("https://api.example.test/orders")
    .httpMethod("POST")
    .header("Content-Type", "application/json")
    .body(body)
    .build();

ClientResponse response = client.execute(request, ServiceClient.REST_CLIENT);
```

### 2. SOAP Request with Envelope Construction

Add CXF explicitly before selecting `APACHE_CXF`. The fallback SOAP codec creates a SOAP 1.1
envelope from explicit operation metadata:

```java
PayloadCodecPort soap = new SoapEnvelopeCodec(new SoapEnvelopeMetadata(
    "urn:orders", "CreateOrder", "urn:orders:CreateOrder"));
byte[] envelope = soap.serialize(new Order("A-17"));
RequestSpec request = RequestSpec.builder()
    .to("https://soap.example.test/orders")
    .header("Content-Type", "text/xml; charset=utf-8")
    .body(envelope)
    .build();

ClientResponse response = client.execute(request, ServiceClient.APACHE_CXF);
```

### 3. Lightweight HttpURLConnection with Custom SSL Context

A custom truststore can be selected per request:

```java
SslConfig ssl = SslConfig.custom(
    null,
    null,
    "C:/certs/orders-truststore.p12",
    "changeit");
RequestSpec request = RequestSpec.builder()
    .to("https://api.example.test/health")
    .httpMethod("GET")
    .ssl(ssl)
    .body("")
    .build();

ClientResponse response = client.execute(request, ServiceClient.HTTPURLCONNECTION);
```

### 4. Custom API Metadata and Headers

Caller-provided correlation IDs are propagated; otherwise UniClient generates a UUID. Auth and
custom headers are merged at the transport boundary:

```java
RequestSpec request = RequestSpec.builder()
    .to("https://api.example.test/orders")
    .header("X-Tenant", "acme")
    .header("X-Correlation-ID", "trace-123")
    .auth(AuthConfig.bearer("token-value"))
    .body("{}")
    .build();

ClientResponse response = client.execute(request, ServiceClient.REST_CLIENT);
```

For static service metadata, inject a configured `RequestHeaderAssembler` into a transport:

```java
TransportPort transport = new RestClientAdapter(
    null,
    new RequestHeaderAssembler(new MetadataHeaderFactory("orders", "1.0")));
ClientResponse response = new ClientFacade(engine -> transport)
    .execute(request, ServiceClient.REST_CLIENT);
```

## Extensibility & Custom Clients

Implement `TransportPort` and register its factory in `AdapterRegistry`. The registry remains
the only engine-selection point:

```java
TransportPort custom = spec -> ClientResponse.builder()
    .statusCode(200)
    .body("custom response")
    .build();

EnumMap<ServiceClient, Supplier<TransportPort>> factories = new EnumMap<>(ServiceClient.class);
for (ServiceClient engine : ServiceClient.values()) {
    factories.put(engine, () -> custom);
}
AdapterRegistry registry = new AdapterRegistry(
    new ClasspathDependencyAvailability(), factories);
ClientResponse response = new ClientFacade(registry)
    .execute(request, ServiceClient.REST_CLIENT);
```

## Error Handling

Catch the exception that matches the failing boundary:

- `ClientTransportException`: I/O, timeout, protocol, SSL, or connectivity failure.
- `PayloadCodecException`: serialization, deserialization, malformed input, or unsupported type.
- `MissingClientDependencyException`: an optional engine such as CXF was selected without its
  explicit dependency.

All three extend `UniClientException`. The original cause is retained when there is an
underlying library or I/O failure.

## Verification

The default build is dependency-free:

```text
mvn clean test
```

The optional adapter build is explicit:

```text
mvn -Poptional-adapters clean test
```

Executable assertion suites are also available under `src/test/java`. The Plan 6 core suite
uses hand-written fakes and performs no network, CXF, or Jackson work.

## Known API Gaps

The current redesign keeps serialization as a separate `PayloadCodecPort`; `ClientFacade` does
not automatically select a codec from `ServiceClient`. Likewise, SOAP envelope construction is
explicit through `SoapEnvelopeCodec` before transport execution. These are documented gaps from
the earlier aspirational README wording and were not silently changed in the verification-only
Plan 6.