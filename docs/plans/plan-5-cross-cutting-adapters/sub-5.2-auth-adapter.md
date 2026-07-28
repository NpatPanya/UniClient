# Sub 5.2 — Auth Header Adapter

**Plan:** 5 — Cross-Cutting Adapters · **Priority: P1** · **Predecessor:** `sub-5.1-ssl-adapter.md`
· **Successor:** `sub-5.3-retry-adapter.md`

## Objective

Turn Sub 1.1's `AuthConfig` (Bearer token or Basic auth credentials) into the correct
`Authorization` header value — one job, reused by every transport adapter.

## What to build

- `AuthHeaderFactory.from(AuthConfig config) -> Optional<Map.Entry<String,String>>` (header
  name + value, or empty if `AuthConfig` specifies no auth).
- Bearer: `"Bearer " + token`.
- Basic: `"Basic " + Base64.getEncoder().encodeToString((user+":"+pass).getBytes(UTF_8))`.
- No network call, no dependency — pure string construction.

## Definition of done

- [ ] No import beyond `java.util.Base64`, `java.nio.charset.StandardCharsets`.
- [ ] Returns `Optional.empty()` cleanly when `AuthConfig` is absent/none — callers don't
      need a null check.
- [ ] Unit test: Bearer produces exact expected header value; Basic produces correctly
      Base64-encoded value; none produces empty Optional.

## Output

`AuthHeaderFactory`, consumed by every Plan 3 adapter when assembling headers from
`RequestSpec`.
