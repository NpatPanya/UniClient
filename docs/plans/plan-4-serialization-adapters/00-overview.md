# Plan 4 — Serialization Adapters

**Priority: P1 — Required.** Needed so adapters have real bytes to send; doesn't block other
P1 plans. **Blocked by:** Plan 1 (needs `PayloadCodecPort`); can build in parallel with
Plan 3 conceptually, but sequenced after per the index's strict-order rule.

## Objective

One (or more) implementation of `PayloadCodecPort` per wire format the library supports:
JSON for REST engines, SOAP envelope XML for the CXF engine. This is where "pass any
POJO/DTO directly as a body" actually gets fulfilled without forcing a dependency on every
consumer.

## Scope

In scope: JSON codec (built-in, no dependency) and an optional Jackson-backed codec used
only when Jackson happens to be on the consumer's classpath. Out of scope: transport
(Plan 3), SSL/auth/headers (Plan 5).

## Definition of done

- [ ] `PayloadCodecPort` implementations have no transport-layer knowledge — a codec never
      opens a connection.
- [ ] The built-in codec (Sub 4.1) handles the common case (simple POJOs, maps, primitives)
      with zero dependency, so the library works out of the box even with nothing else on
      the classpath.
- [ ] The optional Jackson codec (Sub 4.2) is selected automatically when available, via the
      same `DependencyAvailabilityPort` used elsewhere — not a separate ad-hoc check.

## Sub-plans

1. `sub-4.1-builtin-codec.md` [P1] — zero-dependency default.
2. `sub-4.2-jackson-codec.md` [P2] — optional, classpath-probed, richer POJO support.
3. `sub-4.3-soap-envelope-codec.md` [P1] — pairs with Plan 3 Sub 3.3.

Sequencing: 4.1 before 4.2 (Jackson codec is explicitly a fallback-replacement for the
built-in one, so the contract must exist first), 4.2 before 4.3 (SOAP envelope construction
can reuse the JSON codec's body-to-string conventions where applicable, e.g. header/body
separation logic).
