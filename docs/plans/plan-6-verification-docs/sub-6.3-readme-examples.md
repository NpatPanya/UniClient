# Sub 6.3 — README/Usage Examples Refresh

**Plan:** 6 — Verification & Docs · **Priority: P2 — Enhancement** · **Predecessor:**
`sub-6.2-pom-dependency-audit.md` · **Successor:** none (last sub-plan in the whole project)

## Objective

The existing `README.md` already promises four usage examples (per its table of contents:
standard REST JSON, SOAP with auto-envelope, lightweight HttpURLConnection with custom SSL,
custom metadata/headers) but the body content for those sections isn't written yet. This
sub-plan writes them against the *real*, post-redesign API — not the aspirational API the
README currently only gestures at.

## What to build

- Example 1: `ServiceClient.REST_CLIENT` + `RequestSpecBuilder` (Sub 2.2) + a plain POJO
  body, showing the built-in JSON codec (Sub 4.1) handling it with zero extra config.
- Example 2: `ServiceClient.APACHE_CXF` + a body object, showing auto-envelope construction
  (Sub 4.3) and the CXF optional-dependency install instruction inline as a comment.
- Example 3: `ServiceClient.HTTPURLCONNECTION` + `SslConfig` with a custom truststore
  (Sub 5.1).
- Example 4: any engine + custom headers/correlation ID (Sub 5.2/5.4).
- "Extensibility & Custom Clients" section: document that a consumer can implement
  `TransportPort` themselves and register it, since Plan 2's registry is the only place
  engine selection happens — this is the concrete extensibility story the README's ToC
  already promises a section for.

## Definition of done

- [x] Every code snippet in the README actually compiles against the final API (copy-tested,
      not hand-typed from memory).
- [x] "Error Handling" section documents the Sub 1.3 exception vocabulary
      (`ClientTransportException`, `PayloadCodecException`, `MissingClientDependencyException`)
      so consumers know what to catch.

## Output

Completed `README.md`, closing out Plan 6 and the whole Project #1 sequence.

## Verification record

**PASS (2026-07-29):** `README.md` now documents the final public API, dependency profiles,
four copy-oriented examples, extensibility, error vocabulary, and verification commands.

**Resolved in follow-up fix (2026-07-29):** the standard facade now accepts an injected
`RequestEncoderPort`, and `StandardClientFactory` wires automatic JSON and SOAP encoding.
`SoapRequestConfig` makes the SOAP namespace, operation, and action explicit; CXF receives the
complete envelope generated before transport execution.