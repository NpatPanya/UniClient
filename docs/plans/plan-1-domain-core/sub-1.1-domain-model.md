# Sub 1.1 — Domain Model Types

**Plan:** 1 — Domain Core & Ports · **Priority: P0** · **Predecessor:** none (first in
sequence) · **Successor:** `sub-1.2-ports.md`

## Objective

Define the plain data the core operates on: what a request is, what a response is, and the
cross-cutting config (headers, timeouts, SSL, auth) — all as immutable JDK-only value types.
No behavior beyond validation/construction lives here (SRP: these are data, not orchestrators).

## Inputs

- `com.npat.uniclient.facade.ClientResponse` (currently an empty abstract class) — this
  sub-plan gives it real shape.
- `com.npat.uniclient.Client<T>` interface (`send()`, `response()`) — the new domain types
  must be substitutable as `T`.

## What to build

- `RequestSpec` — target URI/endpoint, HTTP method (or SOAP action), body object (arbitrary
  POJO, matches the "pass any object" README promise), headers map, timeout config, auth
  config, SSL config. Immutable, built only via the Plan 2.2 builder (this sub-plan defines
  the shape, not the builder).
- `ClientResponse` (fill in the existing abstract class, or replace with a final class if an
  abstract base isn't earned yet — flag this as a decision point, don't invent a hierarchy
  that isn't needed) — status/result code, raw body, deserialized body accessor, headers.
- `HeaderSet`, `TimeoutConfig`, `SslConfig`, `AuthConfig` — small immutable value types, one
  concern each.

## Definition of done

- [ ] All types are immutable (final fields, no setters).
- [ ] No type references `ServiceClient`, any adapter, or any transport library — the model
      is protocol-agnostic; protocol-specific shaping happens in adapters (Plan 3).
- [ ] `ClientResponse`'s shape is decided (abstract vs. final) and the reasoning is written
      as a one-line comment in the file — not left implicit.
- [ ] Each type is unit-testable by direct construction, no fakes needed.

## Output

`com.npat.uniclient.core.model` (or equivalent) package containing the above types, ready
for `sub-1.2-ports.md` to reference in port method signatures.
