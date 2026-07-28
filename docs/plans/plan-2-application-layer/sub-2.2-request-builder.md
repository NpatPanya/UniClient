# Sub 2.2 — Fluent Zero-Boilerplate Request Builder

**Plan:** 2 — Application Layer · **Priority: P1** · **Predecessor:** `sub-2.1-facade-orchestrator.md`
(sequenced after so the builder targets a settled `RequestSpec`/`execute` shape) ·
**Successor:** `sub-2.3-adapter-registry.md`

## Objective

This is the piece that delivers the README's "Zero-Boilerplate Payload Handling" and
"pass any POJO/DTO directly as a body" promises. One reason to change: how a `RequestSpec`
gets assembled from scattered calls into one fluent chain.

## What to build

- `RequestSpec.builder()` (or a dedicated `RequestSpecBuilder`) exposing chained setters:
  `.to(uri)`, `.body(Object)`, `.header(k, v)` / `.headers(Map)`, `.timeout(TimeoutConfig)`,
  `.auth(AuthConfig)`, `.ssl(SslConfig)`, `.build()`.
- `.build()` validates required fields (target destination, at minimum) and throws a Plan
  1.3 exception (not a raw `IllegalStateException`) on missing required state, so failures
  are consistent with the rest of the library.
- No serialization happens in the builder — it stores the raw body object; serialization is
  a `PayloadCodecPort` concern (Plan 4), invoked later by the transport adapter.

## Definition of done

- [ ] Builder has no third-party imports — pure JDK, matches Plan 1's core constraint even
      though this class technically lives in the application layer.
- [ ] Building a spec with just a destination and a body requires exactly one chained
      expression — no separate config objects the caller has to assemble by hand first
      (this is the literal "zero-boilerplate" bar).
- [ ] Unit test: build a spec, assert every field lands where expected; build with a missing
      required field, assert the correct core exception type.

## Output

`RequestSpecBuilder` (or static factory on `RequestSpec`), the primary consumer-facing entry
point alongside `ClientFacade`.
