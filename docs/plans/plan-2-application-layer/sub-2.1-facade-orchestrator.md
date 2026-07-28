# Sub 2.1 — ClientFacade Use-Case Orchestrator

**Plan:** 2 — Application Layer · **Priority: P0** · **Predecessor:** none (first in Plan 2,
blocked only by all of Plan 1) · **Successor:** `sub-2.3-adapter-registry.md`

## Objective

Fill in `com.npat.uniclient.facade.ClientFacade` (currently an empty abstract class) with
the one job it has: given a `RequestSpec` and a `ServiceClient` choice, get a `ClientResponse`
back. One reason to change: the orchestration sequence itself (validate → resolve adapter →
execute → map result). Not: how any given transport works.

## Inputs

- `RequestSpec`, `ClientResponse` (Sub 1.1)
- `TransportPort`, `PayloadCodecPort` (Sub 1.2)
- `com.npat.uniclient.ServiceClient` enum (existing) and `com.npat.uniclient.Client<T>`
  interface (existing) — decide whether `ClientFacade` implements `Client<ClientResponse>`
  or composes it; flag this as a decision point rather than guessing silently.

## What to build

- `ClientFacade.execute(RequestSpec spec, ServiceClient engine)` (or constructor-bound
  engine, matching however `Client<T>.send()/response()` is meant to be used) that:
  1. Resolves a `TransportPort` for `engine` (delegates to Sub 2.3's registry — does not
     `new` up adapters itself).
  2. Calls `transportPort.execute(spec)`.
  3. Lets `ClientTransportException` / `MissingClientDependencyException` propagate
     unwrapped (callers need the real cause, not a generic failure).
- Keep this class free of `if (engine == APACHE_CXF) { ... }`-style branching — that
  branching belongs in the registry (Sub 2.3), not the orchestrator (SRP: orchestration vs.
  adapter selection are different concerns).

## Definition of done

- [ ] `ClientFacade` has one public entry point for "run a request," not several
      near-duplicate overloads.
- [ ] Unit test: fake `TransportPort` returning a canned `ClientResponse`, fake registry
      returning that fake port, assert `ClientFacade` returns it unchanged — zero real I/O.
- [ ] No `ServiceClient`-specific logic lives in this file.

## Output

Populated `ClientFacade` class, ready for `sub-2.3-adapter-registry.md` to supply the
adapter-resolution collaborator it calls.
