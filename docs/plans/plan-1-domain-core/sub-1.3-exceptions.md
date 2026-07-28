# Sub 1.3 — Core Exception Hierarchy

**Plan:** 1 — Domain Core & Ports · **Priority: P0** · **Predecessor:** `sub-1.2-ports.md` ·
**Successor:** `sub-1.4-dependency-guard.md`

## Objective

Give every port a defined failure mode. This is what makes "throw a clear error when a
dependency is missing" (a stated non-negotiable project principle) an actual typed exception
instead of an ad-hoc `RuntimeException` invented per-adapter later.

## What to build

- `UniClientException` — root, unchecked (this is a library; forcing checked exceptions on
  every caller is boilerplate the README explicitly promises to avoid).
- `ClientTransportException` — thrown by `TransportPort` implementations on I/O/protocol
  failure. Carries the underlying cause.
- `PayloadCodecException` — thrown by `PayloadCodecPort` implementations on
  serialize/deserialize failure.
- `MissingClientDependencyException` — thrown when `DependencyAvailabilityPort` reports a
  required optional library isn't on the classpath. Message must name the missing
  dependency and the Maven coordinate/instruction to add it (this is the exact behavior the
  project principles call for: "throw this client service cannot be used, need to install
  dependency").

## Definition of done

- [ ] All exceptions extend `UniClientException`, which extends `RuntimeException`.
- [ ] `MissingClientDependencyException`'s message format is decided now (e.g.
      `"<engine> requires <dependency>. Add it: <maven coordinate>. See docs/..."`) so every
      later adapter that throws it (Sub 3.3, Sub 4.2) is consistent without re-deciding
      wording each time.
- [ ] No exception here references a concrete adapter or third-party type.

## Output

`com.npat.uniclient.core.exception` package, ready for `sub-1.4-dependency-guard.md` to
consume `MissingClientDependencyException` in its default implementation.
