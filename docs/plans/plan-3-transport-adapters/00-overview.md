# Plan 3 — Transport Adapters

**Priority: P1 — Required.** Needed for the library to actually send anything; doesn't block
other P1 plans. **Blocked by:** Plan 2 (needs `TransportPort` and the registry to plug into).

## Objective

One adapter per `ServiceClient` enum value, each implementing `TransportPort` from Plan 1.
Each adapter's only job is: take a `RequestSpec`, talk to one specific transport mechanism,
return a `ClientResponse`. Adapters depend on the core; the core never depends on them
(hexagonal dependency direction).

## Scope

In scope: the three transport engines named in the existing `ServiceClient` enum and the
README (HttpURLConnection, a REST client, SOAP via Apache CXF). Out of scope: payload
serialization (Plan 4 — a `PayloadCodecPort` is injected into these adapters, not
reimplemented inside them).

## Definition of done

- [ ] Each adapter implements `TransportPort` and nothing else public — no adapter also
      implements `PayloadCodecPort` (SRP: transport vs. serialization stay separate even
      though both are needed to fully handle a request).
- [ ] `HttpURLConnectionAdapter` and `RestClientAdapter` (Sub 3.1, 3.2) compile and run with
      zero non-JDK dependencies.
- [ ] `ApacheCxfAdapter` (Sub 3.3) is only ever constructed after Sub 1.4's guard has
      confirmed CXF is present — enforced by Sub 2.3, not re-checked here.

## Sub-plans

1. `sub-3.1-httpurlconnection-adapter.md` [P1] — JDK-only, always-available default.
2. `sub-3.2-restclient-adapter.md` [P1] — `java.net.http.HttpClient`, JDK-only.
3. `sub-3.3-apache-cxf-adapter.md` [P1] — SOAP via Apache CXF, optional dependency.

Sequencing: 3.1 first (it's the safe default and the simplest — good proof of the port
contract before adding complexity), then 3.2, then 3.3 (the one with the dependency-guard
interaction, so it benefits from the pattern being proven twice already).
