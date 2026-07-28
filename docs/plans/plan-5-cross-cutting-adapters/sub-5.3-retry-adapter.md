# Sub 5.3 — Retry/Timeout Resilience Adapter

**Plan:** 5 — Cross-Cutting Adapters · **Priority: P1** · **Predecessor:** `sub-5.2-auth-adapter.md`
· **Successor:** `sub-5.4-metadata-injector.md`

## Objective

Deliver the README's "Built-in Resilience" promise (timeouts, automatic retries) without
touching any individual transport adapter's internals — implemented as a decorator around
`TransportPort`, not baked into each adapter.

## What to build

- `RetryingTransportPort implements TransportPort` — wraps another `TransportPort`
  (constructor injection), applies a retry policy (max attempts, backoff) sourced from
  `RequestSpec`'s `TimeoutConfig`/retry settings, catches `ClientTransportException` and
  retries up to the configured limit before rethrowing the last failure.
- Registered in Sub 2.3's `AdapterRegistry` as a wrapper applied to every resolved adapter,
  not a separate `ServiceClient` value — every engine gets retry behavior for free, decided
  in one place.

## Definition of done

- [ ] Decorator pattern — this class holds a `TransportPort` reference, does not reimplement
      any transport logic itself (SRP: retry policy vs. transport mechanics stay separate).
- [ ] No busy-waiting — backoff uses `Thread.sleep` or a scheduled executor, and honors
      interruption.
- [ ] Unit test: fake inner `TransportPort` that fails N times then succeeds, assert the
      decorator retries exactly the configured number of times and returns the eventual
      success; fake that always fails, assert it rethrows after the max attempts.

## Output

`RetryingTransportPort`, wired into Sub 2.3's registry as the standard wrapper around every
resolved adapter.
