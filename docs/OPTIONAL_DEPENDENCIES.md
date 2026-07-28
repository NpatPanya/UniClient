# Optional Dependencies Policy

**Principle:** The `uniclient` library has zero forced runtime dependencies. Optional adapters
(e.g., Apache CXF for SOAP, Jackson for rich JSON handling) are declared with
`<optional>true</optional>` in `pom.xml`, keeping them out of consumers' transitive closure
unless the consumer explicitly adds them.

## pom.xml Scoping Rules

### JDK-only adapters (always available, no entry needed)
- `HttpURLConnectionAdapter` — uses `java.net.HttpURLConnection`
- `RestClientAdapter` — uses `java.net.http.HttpClient` (Java 11+)

These adapters have **no `<dependency>` entry** in `pom.xml` — they are always available, so
`ServiceClient.HTTPURLCONNECTION` is the safe fallback when an optional-dependency adapter is
unavailable.

### Optional-dependency adapters (declared with `<optional>true</optional>`)
- `ApacheCxfAdapter` — requires Apache CXF (`org.apache.cxf:cxf-rt-frontend-jaxws`)
- `JacksonJsonCodec` — requires Jackson (`com.fasterxml.jackson.core:jackson-databind`)

These are declared as:
```xml
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>3.5.0</version><!-- or current version -->
    <optional>true</optional>
</dependency>
```

**Effect:** A consumer depending on `uniclient` does not automatically pull in CXF or Jackson.
If the consumer wants to use `ServiceClient.APACHE_CXF` or the `JacksonJsonCodec`, they must
explicitly add the dependency themselves.

## Failure Behavior

If a consumer tries to use an optional adapter without the library installed:

```java
ClientFacade facade = new ClientFacade(registry);
facade.execute(spec, ServiceClient.APACHE_CXF);
// → MissingClientDependencyException: "APACHE_CXF requires org.apache.cxf:cxf-rt-frontend-jaxws.
//   Add it to pom.xml with <scope>provided</scope> or ensure it's on the classpath."
```

This is "fail loud" in action — cryptic `NoClassDefFoundError` is replaced with a clear,
actionable message naming the missing library and how to install it.

## Adding a New Optional Adapter

When a new Plan 3–4 sub-plan adds an adapter for an optional library:

1. **In pom.xml:** add a `<dependency>` entry with `<optional>true</optional>`.
2. **In the adapter registry** (Plan 2 Sub 2.3): before instantiating the adapter, call
   `DependencyRequirement.require(availability, className, engineName, mavenCoordinate)`.
3. **In tests:** add an integration test (tagged `@Tag("integration")`) that exercises the
   adapter with the library present. The default unit-test suite runs without it, proving
   zero forced dependencies.

## For Library Consumers

If you use `uniclient` and want to enable a specific optional adapter:

```xml
<!-- In your pom.xml, alongside uniclient -->
<dependency>
    <groupId>com.npat.uniclient</groupId>
    <artifactId>uniclient</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Add this only if you want SOAP support -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>3.5.0</version>
</dependency>

<!-- Or this, only if you want rich JSON handling -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

Then use them:
```java
ClientFacade facade = new ClientFacade(new AdapterRegistry(new ClasspathDependencyAvailability()));
facade.execute(soapSpec, ServiceClient.APACHE_CXF); // Works because you added CXF
```

No surprises, no hidden transitive dependencies, no forced installations.
