# Sub 1.4 — Dependency-Guard Mechanism + pom.xml Policy

**Plan:** 1 — Domain Core & Ports · **Priority: P0** · **Predecessor:** `sub-1.3-exceptions.md`
· **Successor:** none (last in Plan 1; Plan 2 Sub 2.3 consumes this)

## Objective

Turn the project's hardest constraint — "don't rely on any dependencies; if an engine needs
a client library and it's missing, fail loud, don't fail to install" — into one reusable
mechanism, plus the pom.xml convention every later adapter's dependency must follow.

## What to build

**Guard implementation:**
- `ClasspathDependencyAvailability implements DependencyAvailabilityPort` — default,
  reflection-based (`Class.forName(name, false, callerClassLoader)` wrapped in
  try/catch `ClassNotFoundException` returning false, never throwing from the probe itself).
- A small helper the adapter registry (Sub 2.3) calls before instantiating an
  optional-dependency adapter: `requireAvailable(className, humanEngineName, mavenCoordinate)`
  → throws `MissingClientDependencyException` with the standardized message from Sub 1.3 if
  absent, returns silently if present.

**pom.xml policy (documented here, applied starting Plan 3):**
- Any adapter wrapping a library beyond the JDK (Apache CXF now; anything else later)
  declares that dependency with `<scope>provided</scope>` (or `<optional>true</optional>`
  for a library, `provided` for an app-consumed one — decide based on whether `uniclient`
  ships as a library dependency of other services, which it does per the README, so
  `<optional>true</optional>` is the correct choice: it keeps the dependency out of
  consumers' transitive closure unless *they* also declare it).
- JDK-only adapters (HttpURLConnection, `java.net.http.HttpClient`) declare no dependency at
  all — they're always available, which is why `ServiceClient.HTTPURLCONNECTION` is the safe
  default when the guard would otherwise reject a request.

## Definition of done

- [ ] Guard never throws from the availability check itself — only `requireAvailable(...)`
      throws, and only `MissingClientDependencyException`.
- [ ] Policy above is written into this file (done) and cross-referenced from
      `plan-3-transport-adapters/sub-3.3-apache-cxf-adapter.md` and
      `plan-4-serialization-adapters/sub-4.2-jackson-codec.md` when those are executed.
- [ ] A fake `DependencyAvailabilityPort` that always returns `false` is enough to unit-test
      that `requireAvailable` throws with the right message — no real missing dependency
      needed to test the failure path.

## Output

`com.npat.uniclient.core.support.ClasspathDependencyAvailability` (or equivalent) plus this
recorded pom.xml convention. Closes out Plan 1 — Plan 2 can now begin.
