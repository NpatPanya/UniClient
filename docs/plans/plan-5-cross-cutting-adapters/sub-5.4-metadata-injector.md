# Sub 5.4 — Correlation-ID / Metadata Header Injector

**Plan:** 5 — Cross-Cutting Adapters · **Priority: P2 — Enhancement** · **Predecessor:**
`sub-5.3-retry-adapter.md` · **Successor:** none (last in Plan 5)

## Objective

Deliver "Centralized Metadata Management" from the README — automatic correlation-ID
generation and standard service-metadata headers, applied uniformly without every caller
remembering to set them by hand.

## What to build

- `MetadataHeaderFactory.standardHeaders(RequestSpec spec) -> Map<String,String>` —
  generates/propagates a correlation ID (new UUID if none supplied in `RequestSpec`), adds
  any configured static service metadata (service name/version) as headers.
- Applied at the same point Sub 5.2's auth headers are merged in — one "assemble final
  headers" step in the orchestrator or a shared header-merging helper, not duplicated per
  adapter.

## Definition of done

- [ ] No import beyond `java.util.UUID` and JDK collections.
- [ ] Correlation ID is deterministic-if-supplied (caller can pass their own to propagate
      across service boundaries), auto-generated otherwise.
- [ ] Unit test: no correlation ID supplied → header present with valid UUID format; ID
      supplied → header equals the supplied value unchanged.

## Output

`MetadataHeaderFactory`, completing Plan 5. This is the lowest-priority item in the plan —
safe to defer past initial delivery without blocking the library's core promise.
