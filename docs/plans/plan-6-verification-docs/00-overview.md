# Plan 6 — Verification & Docs

**Priority: P1.** Not blocking earlier plans (each prior sub-plan already carries its own
unit-test requirement in its definition of done), but required before calling the redesign
"done" — this is the pass that checks the whole assembled library against the project
principles, not just each piece in isolation. **Blocked by:** Plans 1–5.

## Objective

Confirm, at the whole-library level: the core is genuinely swappable/fake-testable, no
adapter accidentally forces a dependency onto consumers, and the README's usage examples
still match the real API surface after the redesign.

## Scope

In scope: an integration-style test pass across layers, a pom.xml audit, and a docs
refresh. Out of scope: new production code — if this plan surfaces a gap, that's a new sub-
plan to add to an earlier plan, not something to patch silently here (per the "flag a gap,
don't invent" rule).

## Definition of done

- [x] `mvn test` passes with zero optional dependencies (CXF, Jackson) present on the
      classpath, proving the zero-dependency default truly works standalone.
- [x] `mvn test` also passes with optional dependencies present, exercising the
      Jackson/CXF code paths.
- [x] README examples 1–4 (from the existing table of contents) each correspond to a working
      code snippet against the final API.

## Sub-plans

1. `sub-6.1-core-unit-tests.md` [P1] — fake-adapter test suite across the core/application
   layers.
2. `sub-6.2-pom-dependency-audit.md` [P1] — confirm every non-JDK dependency is
   optional/provided.
3. `sub-6.3-readme-examples.md` [P2] — refresh README usage examples.

Sequencing: 6.1 before 6.2 (tests need to exist before "run tests with/without optional deps"
is meaningful), 6.2 before 6.3 (docs should describe the dependency story only once it's
been verified, not before).

## Verification record

**PASS (2026-07-29):** The dependency-free default build, explicit optional-adapter build,
consumer dependency-tree simulation, fake-port composition test, and README copy-check are
complete. Earlier-plan gaps around automatic codec/envelope composition are documented in
Sub 6.3 and README rather than changed in this verification-only plan.