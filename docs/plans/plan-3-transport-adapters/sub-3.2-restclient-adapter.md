# Sub 3.2 — RestClientAdapter (java.net.http.HttpClient)

**Plan:** 3 — Transport Adapters · **Priority: P1** · **Predecessor:**
`sub-3.1-httpurlconnection-adapter.md` · **Successor:** `sub-3.3-apache-cxf-adapter.md`

## Objective

Implement `TransportPort` using the JDK's built-in `java.net.http.HttpClient` (available
since Java 11; this project targets 17, so it's always present — no external REST client
library needed to satisfy `ServiceClient.REST_CLIENT`). This is a deliberate reading of the
"no forced dependencies" principle: the modern JDK HTTP client covers what a typical REST
client library would, without requiring a dependency at all.

## What to build

- `RestClientAdapter implements TransportPort` — builds `HttpRequest` from `RequestSpec`,
  sends via a shared/configurable `HttpClient` instance, maps `HttpResponse<byte[]>` into
  `ClientResponse`.
- Same exception-wrapping rule as Sub 3.1: `IOException`/`InterruptedException` become
  `ClientTransportException`.
- `HttpClient` construction honors `SslConfig`/`TimeoutConfig` via `HttpClient.Builder` —
  again, SSL context itself comes from Plan 5 Sub 5.1, this adapter only consumes it.

## Definition of done

- [ ] No import beyond `java.net.http.*` and `java.*`.
- [ ] `InterruptedException` handling restores the interrupt flag before wrapping/rethrowing
      (standard JDK concurrency hygiene, easy to miss).
- [ ] Testable against a local loopback/mock server, no real external API required.

## Output

`RestClientAdapter`, registrable under `ServiceClient.REST_CLIENT`, no guard check needed
(JDK-only, same as Sub 3.1).
