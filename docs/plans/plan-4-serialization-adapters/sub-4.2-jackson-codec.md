# Sub 4.2 — Optional Jackson Codec

**Plan:** 4 — Serialization Adapters · **Priority: P2 — Enhancement** · **Predecessor:**
`sub-4.1-builtin-codec.md` · **Successor:** `sub-4.3-soap-envelope-codec.md`

## Objective

Richer JSON handling (nested generics, polymorphism, annotations) when Jackson happens to
already be on the consumer's classpath — never installed by this library itself.

## What to build

- `JacksonJsonCodec implements PayloadCodecPort` — thin wrapper over `ObjectMapper`.
- pom.xml: `com.fasterxml.jackson.core:jackson-databind` with `<optional>true</optional>`,
  per Sub 1.4's policy.
- Selection logic (in Sub 2.3's registry, or a small `PayloadCodecPort` resolver alongside
  it): probe `DependencyAvailabilityPort` for Jackson's `ObjectMapper` class; use
  `JacksonJsonCodec` if present, `BuiltinJsonCodec` (Sub 4.1) otherwise. This is a graceful
  upgrade, not a hard requirement — unlike Sub 3.3's CXF adapter, absence of Jackson is never
  an error, it just means the built-in codec handles the request.

## Definition of done

- [ ] `pom.xml` entry uses `<optional>true</optional>` — verified by Sub 6.2.
- [ ] Codec selection is automatic and silent when Jackson is absent (falls back, does not
      throw) — contrast explicitly with Sub 3.3 where absence *does* throw, since a missing
      transport engine has no fallback but a missing JSON library does.
- [ ] Unit test: with a fake `DependencyAvailabilityPort` reporting Jackson present, resolver
      picks `JacksonJsonCodec`; reporting absent, resolver picks `BuiltinJsonCodec`.

## Output

`JacksonJsonCodec` + selection logic + updated `pom.xml`.
