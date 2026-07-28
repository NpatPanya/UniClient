# Sub 4.3 — SOAP Envelope Codec

**Plan:** 4 — Serialization Adapters · **Priority: P1** · **Predecessor:**
`sub-4.2-jackson-codec.md` · **Successor:** none (last in Plan 4)

## Objective

Auto-construct a SOAP envelope from an arbitrary body object, matching the README's
"SOAP Request with Auto-Envelope Construction" example. Pairs with Plan 3 Sub 3.3's
`ApacheCxfAdapter`, which sends the bytes this codec produces.

## What to build

- `SoapEnvelopeCodec implements PayloadCodecPort` — wraps a body object in a minimal SOAP
  envelope structure (namespace/action metadata comes from `RequestSpec`/`AuthConfig`-style
  config, not hardcoded).
- Decide and document: does this codec require Jackson/JAXB, or does it stay JDK-only via
  `javax.xml`/`jakarta.xml` bindings already implied by pulling in CXF? Flag this as a
  decision point for Sub 3.3's execution — don't silently pick without recording the
  reasoning, since it affects whether this codec needs its own guard check.

## Binding decision

The fallback codec uses JDK `javax.xml` DOM and Transformer APIs, so it adds no JAXB or Jackson dependency. It accepts explicit `SoapEnvelopeMetadata` and renders simple public-field/getter object graphs as escaped XML. Complex XML binding, annotations, polymorphism, and generic type metadata remain outside this fallback; consumers can use a richer codec when needed.

## Definition of done

- [ ] Envelope construction is a pure function of (body, metadata) → bytes — no I/O, no
      transport awareness (SRP boundary with Sub 3.3 stays clean).
- [ ] The JAXB/XML-binding dependency question above is answered and recorded in this file
      before Sub 3.3 is executed, since it may add a second `<optional>true</optional>` pom
      entry alongside CXF.
- [ ] Unit test: simple POJO in, well-formed SOAP envelope XML out, parseable back to
      confirm structure.

## Output

`SoapEnvelopeCodec`, completing Plan 4. Plan 3 Sub 3.3 and this sub-plan are the two halves
of SOAP support — both must be done for `ServiceClient.APACHE_CXF` to be fully usable.
