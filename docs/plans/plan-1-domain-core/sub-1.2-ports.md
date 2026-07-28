# Sub 1.2 — Ports

**Plan:** 1 — Domain Core & Ports · **Priority: P0** · **Predecessor:** `sub-1.1-domain-model.md`
· **Successor:** `sub-1.3-exceptions.md`

## Objective

Define the interfaces the application layer (Plan 2) needs from the outside world. These are
owned by the core — adapters (Plans 3–5) implement them, the core never imports an adapter.
This is the hexagon boundary; get it right here and every later plan is just filling in a
box.

## Inputs

`sub-1.1-domain-model.md` types (`RequestSpec`, `ClientResponse`, etc.) as method
signatures' parameter/return types.

## What to build

- `TransportPort` — one method, roughly `ClientResponse execute(RequestSpec spec)`. One
  reason to change: how a request physically goes out and a response comes back. Every
  transport adapter (HttpURLConnection, JDK HttpClient, Apache CXF) implements this and only
  this.
- `PayloadCodecPort` — `String/bytes serialize(Object body)` and
  `<T> T deserialize(bytes, Class<T>)`. One reason to change: wire-format conversion. Kept
  separate from `TransportPort` on purpose — a transport engine and a serializer are two
  different concerns (SRP smell check: don't let one class do both).
- `DependencyAvailabilityPort` — `boolean isAvailable(String fullyQualifiedClassName)` (or
  similar). One reason to change: how the library detects an optional runtime dependency.
  Plan 1.4 provides the default (reflection-based) implementation of this port.

## Definition of done

- [ ] Each port has exactly one method-group serving exactly one concern (no
      `sendAndSerialize`-style merged methods).
- [ ] No port references a concrete library (no `HttpURLConnection`, no `javax.xml.ws`
      anywhere in this package).
- [ ] Ports are Java interfaces, not abstract classes, unless default-method sharing is a
      proven need (don't invent inheritance not yet required).
- [ ] A hand-written fake implementation of each port is enough to unit-test Plan 2's
      orchestrator with zero real I/O.

## Output

`com.npat.uniclient.core.port` package with the three interfaces above, ready for
`sub-1.3-exceptions.md` to define the failure vocabulary these ports raise.
