# Sub 5.1 — SSLContext Adapter

**Plan:** 5 — Cross-Cutting Adapters · **Priority: P1** · **Predecessor:** none (first in
Plan 5) · **Successor:** `sub-5.2-auth-adapter.md`

## Objective

Turn Sub 1.1's `SslConfig` (custom keystore/truststore, or "use platform default") into a
real `javax.net.ssl.SSLContext` that Plan 3's `HttpURLConnectionAdapter` and
`RestClientAdapter` can apply.

## What to build

- `SslContextFactory.from(SslConfig config) -> SSLContext` — loads keystore/truststore from
  the paths/bytes in `SslConfig`, builds `KeyManagerFactory`/`TrustManagerFactory`, returns
  an initialized `SSLContext`. Falls back to `SSLContext.getDefault()` when `SslConfig` asks
  for platform defaults.
- Wraps `KeyStoreException`/`NoSuchAlgorithmException`/etc. into `ClientTransportException`
  (Sub 1.3) — consumers shouldn't need to know the JDK crypto exception zoo.

## Definition of done

- [ ] No import beyond `javax.net.ssl.*`, `java.security.*`.
- [ ] One job only — builds a context, does not apply it to a connection (that's each
      transport adapter's own responsibility, keeping this reusable across both).
- [ ] Unit test: custom truststore in a test resource, assert the built context trusts the
      matching test cert; default config, assert platform default context returned.

## Output

`SslContextFactory`, consumed by Sub 3.1 and Sub 3.2.
