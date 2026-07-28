# Sub 2.3 — Adapter Registry / Resolver

**Plan:** 2 — Application Layer · **Priority: P0** · **Predecessor:** `sub-2.1-facade-orchestrator.md`
· **Successor:** none (last in Plan 2; Plan 3 supplies the adapters this registers)

## Objective

Own the one mapping from `ServiceClient` enum value to a concrete `TransportPort`
implementation, and be the single call site for Sub 1.4's dependency guard. One reason to
change: which engine a `ServiceClient` value resolves to. This is the Strategy-pattern
dispatch point the README's architecture section promises.

## What to build

- `AdapterRegistry.resolve(ServiceClient engine) -> TransportPort`, implemented as either a
  `switch` or an `EnumMap<ServiceClient, Supplier<TransportPort>>` (prefer the map — easier
  to extend without touching a growing switch, same SRP reasoning as avoiding branchy code
  in the orchestrator).
- For engines backed by an optional dependency (`APACHE_CXF` today), call
  `requireAvailable(...)` (Sub 1.4) *before* constructing the adapter, so the failure is a
  clean `MissingClientDependencyException`, never a `NoClassDefFoundError` surfacing from
  deep inside adapter construction.
- `HTTPURLCONNECTION` is the documented safe default — always resolvable, no guard needed,
  since it's JDK-only (Sub 3.1).

## Definition of done

- [ ] Every `ServiceClient` enum value has exactly one registry entry — adding a new engine
      later means adding one entry here, not touching `ClientFacade`.
- [ ] Guard check happens before adapter instantiation for every non-JDK-only engine.
- [ ] Unit test: fake `DependencyAvailabilityPort` returning false for `APACHE_CXF`, assert
      `resolve(APACHE_CXF)` throws `MissingClientDependencyException` with the CXF Maven
      coordinate in the message; assert `resolve(HTTPURLCONNECTION)` never touches the guard.

## Output

`AdapterRegistry`, the last piece Plan 2 needs. Closes out Plan 2 — Plan 3 can now begin
supplying real adapters for this registry to hand out.
