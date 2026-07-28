# Sub 6.1 — Core Unit Tests via Fake Adapters

**Plan:** 6 — Verification & Docs · **Priority: P1.** **Predecessor:** none (first in
Plan 6) · **Successor:** `sub-6.2-pom-dependency-audit.md`

## Objective

Prove, at the whole-library level, the `coding-architecture-standards` checklist item: "the
core is unit-testable with a fake adapter, no real framework/DB/network required." Each
sub-plan in Plans 1–5 already specified its own unit tests; this sub-plan is the pass that
checks they compose — that `ClientFacade` end-to-end, with every port faked, needs zero real
I/O.

## What to build

- A test suite exercising `ClientFacade` (Sub 2.1) with hand-written fakes for
  `TransportPort` and `PayloadCodecPort` — no real HTTP, no real CXF, no Jackson on the test
  classpath for this suite specifically.
- A separate, explicitly-tagged integration suite (e.g. JUnit `@Tag("integration")`) for the
  cases that do need something real: Sub 3.3's CXF adapter against a SOAP mock, Sub 3.1/3.2
  against a loopback HTTP server. Keeps `mvn test` fast and dependency-free by default;
  integration tests run as a separate, explicit step.

## Definition of done

- [x] Default `mvn test` run touches no network, no real CXF, no real Jackson — confirms the
      hexagonal boundary actually holds, not just in theory.
- [x] Every port from Sub 1.2 has at least one fake implementation shared across tests
      (avoid five different ad-hoc fakes for the same interface — SRP applies to test code
      too: one fake, one job).
- [x] Any gap found here (a port that turns out to need a real dependency to fake
      convincingly) is written up as a flagged gap referencing the specific sub-plan that
      needs revisiting — not silently patched.

## Output

Core/application-layer test suite, `@Tag("integration")` suite for the adapters that
genuinely need it.

## Verification record

**PASS (2026-07-29):** `CoreApplicationPlan6Test` uses one hand-written fake for each core
port: `TransportPort`, `PayloadCodecPort`, and `DependencyAvailabilityPort`. The default
Maven profile excludes the optional adapter sources and real-I/O adapter tests; the direct
core test run uses only `target/classes` and `target/test-classes` and performs no network,
CXF, or Jackson work.

The existing loopback HTTP and optional CXF/Jackson executable suites remain explicit adapter
verification and are compiled under `-Poptional-adapters`.