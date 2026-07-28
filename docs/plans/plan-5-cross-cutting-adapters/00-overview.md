# Plan 5 — Cross-Cutting Adapters

**Priority: P1/P2 (mixed — see sub-plans).** **Blocked by:** Plan 1 (config types), useful
once Plan 3's transport adapters exist to consume these.

## Objective

Fill in the remaining README-promised capabilities that aren't transport or serialization
per se: SSL, auth, resilience, and metadata/correlation headers. Each is a small, single-
concern piece that transport adapters (Plan 3) consume via the config types Plan 1 already
defined (`SslConfig`, `AuthConfig`, `TimeoutConfig`) — these sub-plans turn that config into
actual behavior.

## Scope

In scope: turning `SslConfig`/`AuthConfig` into a real `SSLContext` / real header values,
plus retry policy and correlation-ID injection. Out of scope: any new port (these reuse
`TransportPort`'s existing seams by producing values the adapters already accept).

## Definition of done

- [ ] Each piece here is a pure function/small class with one job — no piece both builds an
      SSL context and sets auth headers.
- [ ] None of these introduce a new third-party dependency (all achievable with
      `javax.net.ssl`, `java.util.Base64`, and JDK scheduling/retry primitives).
- [ ] Every piece is consumed by Plan 3 adapters via existing config types, not by adding new
      parameters to `TransportPort`.

## Sub-plans

1. `sub-5.1-ssl-adapter.md` [P1] — `SslConfig` → `SSLContext`.
2. `sub-5.2-auth-adapter.md` [P1] — `AuthConfig` → `Authorization` header value.
3. `sub-5.3-retry-adapter.md` [P1] — timeout/retry policy wrapper around `TransportPort`.
4. `sub-5.4-metadata-injector.md` [P2] — correlation ID + standard metadata headers.

Sequencing: 5.1 and 5.2 first (both are pure config→value transforms Plan 3 adapters need
directly), then 5.3 (a decorator around `TransportPort`, needs 5.1/5.2's adapters proven
stable first), then 5.4 (lowest priority, additive only).
