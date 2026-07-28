# Plan 1 — Domain Core & Ports

**Priority: P0 — Blocking.** Nothing in Plans 2–6 can start until this plan is done: it's
the framework-free center that every adapter eventually plugs into.

## Objective

Establish `com.npat.uniclient.core` (name TBD at build time, suggested `com.npat.uniclient.core`)
as a package that imports nothing outside the JDK. It holds the domain model, the ports the
application layer needs from the outside world, the exception vocabulary, and the mechanism
that lets an optional adapter fail loud instead of silently.

## Scope

In scope: domain types, port interfaces, exceptions, the dependency-guard contract.
Out of scope: any concrete adapter, any third-party import, any I/O.

## Definition of done

- [ ] Every file under the core package has zero non-JDK imports.
- [ ] Every port is owned by the core (defined here, implemented later by an adapter) —
      never the reverse.
- [ ] Every type here can be constructed and asserted on in a plain JUnit test with no
      mocking framework required.
- [ ] `Sub 1.4`'s guard mechanism is generic — it doesn't hardcode "CXF" or "Jackson"; it
      takes a fully-qualified class name and a human-readable install hint.

## Sub-plans

1. `sub-1.1-domain-model.md` [P0] — request/response/config value types.
2. `sub-1.2-ports.md` [P0] — `TransportPort`, `PayloadCodecPort`, `DependencyAvailabilityPort`.
3. `sub-1.3-exceptions.md` [P0] — exception hierarchy used by every later plan.
4. `sub-1.4-dependency-guard.md` [P0] — runtime classpath probe + pom.xml scope policy.

Sequencing is strict: 1.1 before 1.2 (ports reference the domain types), 1.2 before 1.3
(exceptions reference port failure modes), 1.3 before 1.4 (the guard throws one of these
exceptions).
