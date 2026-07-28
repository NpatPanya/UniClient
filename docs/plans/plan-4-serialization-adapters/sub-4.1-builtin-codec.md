# Sub 4.1 — Built-in Minimal POJO↔JSON Codec

**Plan:** 4 — Serialization Adapters · **Priority: P1** · **Predecessor:** none (first in
Plan 4) · **Successor:** `sub-4.2-jackson-codec.md`

## Objective

A zero-dependency `PayloadCodecPort` implementation good enough for the common case: simple
POJOs, `Map`s, primitives, `String` bodies. This is the library's true default — it must work
with nothing else installed, which is the whole point of the "no forced dependencies"
principle.

## What to build

- `BuiltinJsonCodec implements PayloadCodecPort` — reflection-based POJO→JSON serialization
  (walk public getters/fields), JSON→POJO only for simple/flat shapes (document the
  limitation explicitly rather than silently failing on nested generics).
- Clear scope boundary written into the class Javadoc: "handles flat POJOs, maps, lists,
  primitives; for complex/generic/polymorphic types, add Jackson (Sub 4.2) to the
  classpath." This is the honest fallback story the project principles call for.
- Throws `PayloadCodecException` (Sub 1.3) on anything it can't handle, with a message that
  suggests adding Jackson — consistent with `MissingClientDependencyException`'s tone even
  though this isn't technically a missing-dependency failure.

## Definition of done

- [ ] No import beyond `java.*` — this is the one codec that must work with literally
      nothing else on the classpath.
- [ ] Documented limitation list exists (nested generics, polymorphism) so Sub 4.2's value
      proposition is explicit, not implied.
- [ ] Unit tests cover: flat POJO round-trip, `Map<String,Object>` round-trip, and the
      documented failure case producing `PayloadCodecException`.

## Output

`BuiltinJsonCodec`, the default `PayloadCodecPort` implementation used whenever Sub 4.2's
guard reports Jackson unavailable.
