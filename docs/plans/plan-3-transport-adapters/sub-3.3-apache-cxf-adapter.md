# Sub 3.3 — ApacheCxfAdapter (SOAP)

**Plan:** 3 — Transport Adapters · **Priority: P1** · **Predecessor:**
`sub-3.2-restclient-adapter.md` · **Successor:** none (last in Plan 3; Plan 4 provides the
SOAP envelope codec this adapter pairs with)

## Objective

Implement `TransportPort` for SOAP using Apache CXF, matching `ServiceClient.APACHE_CXF`.
This is the one transport adapter that carries an actual optional dependency, so it's the
proving ground for the whole "fail loud, don't force-install" principle.

## Inputs

- Sub 1.4's `requireAvailable(className, engineName, mavenCoordinate)` guard — **called by
  the registry (Sub 2.3) before this class is ever constructed**, not inside this class. By
  the time `ApacheCxfAdapter`'s constructor runs, CXF is confirmed present.
- Sub 4.3's SOAP envelope codec (parallel plan; this adapter consumes envelope bytes the
  same way Sub 3.1/3.2 consume already-serialized bytes — it does not build the envelope
  itself).

## What to build

- `ApacheCxfAdapter implements TransportPort` — wraps CXF's dynamic client / dispatch API to
  send a pre-built SOAP envelope and return the raw response body, mapped to
  `ClientResponse`.
- pom.xml: add `org.apache.cxf:cxf-rt-frontend-jaxws` (or the minimal CXF artifact actually
  needed) with `<optional>true</optional>`, per the Sub 1.4 policy.
- Wrap CXF-specific exceptions into `ClientTransportException`, same as the other adapters —
  callers of `TransportPort` see one exception vocabulary regardless of engine.

## Definition of done

- [ ] `pom.xml` entry uses `<optional>true</optional>` — verified by Sub 6.2's audit.
- [ ] This class is never referenced by name anywhere in Plan 1 or Plan 2 code (only by
      registry lookup) — so the core and application layer still compile even if this file
      is deleted, proving the optionality is real, not nominal.
- [ ] Manual/integration test plan noted (SOAP mock endpoint) since a full CXF round-trip
      test is heavier than the other adapters' unit tests — acceptable to defer to
      Sub 6.1 as an integration-tagged test, not a blocker for this sub-plan's completion.

## Output

`ApacheCxfAdapter` + updated `pom.xml`, registrable under `ServiceClient.APACHE_CXF`, guard
check enforced by Sub 2.3. Closes out Plan 3.
