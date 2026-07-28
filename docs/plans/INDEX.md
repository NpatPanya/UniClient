# Global API Client Facade — Design Plan Index

Governs the redesign of `uniclient` into a hexagonal (ports & adapters) library, per
project principles: SRP everywhere, framework-free core, zero forced runtime dependencies
(optional client libs are probed at runtime — missing ones fail loud with an install
instruction, never a silent NoClassDefFoundError).

This file is the only place the entire sequence is visible end-to-end. It stays a table of
contents — full detail lives in each plan's own files. Execution walks the notation below
top to bottom, one `Sub` at a time, stopping for explicit user go-ahead between each
(plan-mode-protocol Rule 4). Nothing has been executed yet — this session only produced the
plan.

```
Project #1 — Global API Client Facade (uniclient) — Hexagonal Redesign

Plan 1. Domain Core & Ports                                    [P0 — blocking]
  -> Sub 1.1  Domain model types (RequestSpec, Headers, SslConfig, AuthConfig)   [P0]
  -> Sub 1.2  Ports (TransportPort, PayloadCodecPort, DependencyAvailabilityPort) [P0]
  -> Sub 1.3  Core exception hierarchy                                          [P0]
  -> Sub 1.4  Dependency-guard mechanism + pom.xml optional-dependency policy   [P0]

Plan 2. Application Layer — Facade Orchestration                [P0 — blocking]
  -> Sub 2.1  ClientFacade use-case orchestrator (strategy dispatch)            [P0]
  -> Sub 2.2  Fluent zero-boilerplate request builder                          [P1]
  -> Sub 2.3  Adapter registry/resolver wired to the dependency guard          [P0]

Plan 3. Transport Adapters                                      [P1 — required]
  -> Sub 3.1  HttpURLConnectionAdapter (JDK-only, always-available default)     [P1]
  -> Sub 3.2  RestClientAdapter on java.net.http.HttpClient (JDK-only)          [P1]
  -> Sub 3.3  ApacheCxfAdapter for SOAP (optional dep, reflection-guarded)      [P1]

Plan 4. Serialization Adapters                                  [P1 — required]
  -> Sub 4.1  Built-in minimal POJO<->JSON codec (zero dependency)              [P1]
  -> Sub 4.2  Optional Jackson codec (classpath-probed, used when present)      [P2]
  -> Sub 4.3  SOAP envelope codec (pairs with Sub 3.3)                         [P1]

Plan 5. Cross-Cutting Adapters                                   [P1/P2]
  -> Sub 5.1  SSLContext adapter (custom key/trust store)                      [P1]
  -> Sub 5.2  Auth header adapter (Bearer, Basic)                              [P1]
  -> Sub 5.3  Retry/timeout resilience adapter                                 [P1]
  -> Sub 5.4  Correlation-ID / metadata header injector                        [P2]

Plan 6. Verification & Docs                                     [P1]
  -> Sub 6.1  Unit tests: core exercised via fake adapters, no framework/network [P1]
  -> Sub 6.2  pom.xml dependency-scope audit (optional/provided, no forced deps) [P1]
  -> Sub 6.3  README/usage examples updated to match the real API              [P2]
```

## Priority legend

- **P0 — Blocking**: nothing else can proceed until this is done.
- **P1 — Required**: needed for the stated goal, not blocking other P1s.
- **P2 — Enhancement**: improves the result; the goal is met without it.

## Plan directory map

```
docs/plans/
  INDEX.md                              <- this file
  plan-1-domain-core/
    00-overview.md
    sub-1.1-domain-model.md
    sub-1.2-ports.md
    sub-1.3-exceptions.md
    sub-1.4-dependency-guard.md
  plan-2-application-layer/
    00-overview.md
    sub-2.1-facade-orchestrator.md
    sub-2.2-request-builder.md
    sub-2.3-adapter-registry.md
  plan-3-transport-adapters/
    00-overview.md
    sub-3.1-httpurlconnection-adapter.md
    sub-3.2-restclient-adapter.md
    sub-3.3-apache-cxf-adapter.md
  plan-4-serialization-adapters/
    00-overview.md
    sub-4.1-builtin-codec.md
    sub-4.2-jackson-codec.md
    sub-4.3-soap-envelope-codec.md
  plan-5-cross-cutting-adapters/
    00-overview.md
    sub-5.1-ssl-adapter.md
    sub-5.2-auth-adapter.md
    sub-5.3-retry-adapter.md
    sub-5.4-metadata-injector.md
  plan-6-verification-docs/
    00-overview.md
    sub-6.1-core-unit-tests.md
    sub-6.2-pom-dependency-audit.md
    sub-6.3-readme-examples.md
```

## Current state vs. this plan

Existing skeleton (already on disk, untouched by this planning pass):

- `com.npat.uniclient.Client<T>` — generic send/response interface. Plan 2 wraps this
  behind the orchestrator rather than replacing it.
- `com.npat.uniclient.ServiceClient` — enum `{REST_CLIENT, APACHE_CXF, HTTPURLCONNECTION}`.
  Becomes the strategy key Plan 2 Sub 2.3 dispatches on; each value maps 1:1 to a Plan 3
  adapter.
- `com.npat.uniclient.facade.ClientFacade` — empty abstract class. Plan 2 Sub 2.1 gives it
  its orchestration body.
- `com.npat.uniclient.facade.ClientResponse` — empty abstract class. Plan 1 Sub 1.1 defines
  its shape as a domain type.
- `pom.xml` — Java 17, zero dependencies declared. Plan 1 Sub 1.4 establishes the
  optional/provided-scope policy that Plans 3–4 follow; Plan 6 Sub 6.2 audits it at the end.

## Next step

Sub 1.1 is first in the sequence. Per Rule 4, execution does not begin until you say go —
this index and the sub-plan files below are the deliverable of this planning pass.
