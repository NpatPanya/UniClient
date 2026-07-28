# Plan 2 — Application Layer (Facade Orchestration)

**Priority: P0 — Blocking.** Plans 3–6 build adapters and polish that this layer wires
together; without it, adapters have nothing to plug into. **Blocked by:** Plan 1.

## Objective

Give `com.npat.uniclient.facade.ClientFacade` its actual body: the use-case that takes a
`RequestSpec`, picks the right `TransportPort` implementation via `ServiceClient`, runs it
through the dependency guard, and returns a `ClientResponse`. This layer defines/consumes
ports from Plan 1 — it does not talk to `HttpURLConnection`, CXF, or Jackson directly. That's
the hexagon rule: dependencies point inward, this layer imports the core, adapters import
this layer's ports, never the reverse.

## Scope

In scope: the orchestrator, the fluent builder consumers use to avoid boilerplate, and the
registry that maps `ServiceClient` enum values to adapter instances (guarded by Sub 1.4).
Out of scope: any adapter's actual transport/serialization logic (Plans 3–4).

## Definition of done

- [ ] `ClientFacade` has zero imports of `java.net.http`, `HttpURLConnection`, or any CXF
      class — only Plan 1 core types and JDK collections.
- [ ] The orchestrator is unit-testable by injecting a fake `TransportPort`, with no real
      network call.
- [ ] `ServiceClient` selection and dependency-guard invocation happen in one place
      (Sub 2.3), not duplicated per call site.

## Sub-plans

1. `sub-2.1-facade-orchestrator.md` [P0] — the use-case itself.
2. `sub-2.2-request-builder.md` [P1] — fluent, zero-boilerplate construction of `RequestSpec`.
3. `sub-2.3-adapter-registry.md` [P0] — `ServiceClient` → adapter, guarded.

Sequencing: 2.1 before 2.3 (the orchestrator's shape determines what the registry needs to
hand it); 2.2 can be drafted in parallel conceptually but is sequenced last for delivery
since it's P1, not blocking, and benefits from 2.1's final method signature being settled
first.
