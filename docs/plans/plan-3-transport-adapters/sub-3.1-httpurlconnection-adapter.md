# Sub 3.1 — HttpURLConnectionAdapter

**Plan:** 3 — Transport Adapters · **Priority: P1** · **Predecessor:** none (first in Plan 3)
· **Successor:** `sub-3.2-restclient-adapter.md`

## Objective

Implement `TransportPort` using `java.net.HttpURLConnection`. This is the library's
always-available fallback — zero dependency, works on any JDK, matches
`ServiceClient.HTTPURLCONNECTION`.

## What to build

- `HttpURLConnectionAdapter implements TransportPort` — opens the connection from
  `RequestSpec`'s target, applies headers/timeouts/SSL context (via the config objects from
  Sub 1.1; SSL context itself is *supplied* by Plan 5 Sub 5.1, not built here), writes the
  body (already-serialized bytes/string handed to it — this adapter does not call a codec
  directly; the registry or facade wires codec output in, keeping this adapter's one job as
  "move bytes over this specific transport").
- Wraps any `IOException` into `ClientTransportException` (Sub 1.3) before it leaves the
  adapter — callers of `TransportPort` should never see a raw `java.io` exception type.

## Definition of done

- [ ] No import beyond `java.*`/`javax.net.ssl.*`.
- [ ] Every checked `IOException` path is caught and rethrown as `ClientTransportException`.
- [ ] Testable against a local loopback/mock server (or a fake `HttpURLConnection` via
      `URLStreamHandler`) without hitting a real external API.

## Output

`HttpURLConnectionAdapter`, registrable in Sub 2.3's `AdapterRegistry` under
`ServiceClient.HTTPURLCONNECTION`, no guard check needed.
