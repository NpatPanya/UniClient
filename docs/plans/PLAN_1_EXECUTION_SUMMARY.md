# Plan 1 — Execution Summary

**Status:** ✅ COMPLETE

Established the framework-free, JDK-only domain core that Plans 2–6 depend on.

## Sub 1.1 — Domain Model Types ✅

**Objective:** Immutable, thread-safe value types for the core.

**Delivered:**

- `HeaderSet` — immutable header map, defensive copy on access, thread-safe for high-throughput
- `TimeoutConfig` — connection/read timeouts + retry policy
- `AuthConfig` — Bearer token or Basic auth (immutable enum-backed)
- `SslConfig` — keystore/truststore paths or platform-default (immutable enum-backed)
- `RequestSpec` — target, method, body (any POJO), headers, config; immutable with fluent builder
  - **Key design:** final class, no subclassing allowed (preserves immutability contract)
  - **Builder:** supports `.to(uri).body(obj).header(k,v).timeout(...).auth(...).ssl(...).build()`
- `ClientResponse` — **final class** (thread-safe, no subclassing)
  - status code, raw body bytes (defensively cloned), headers, content-type
  - helpers: `.isSuccess()`, `.isClientError()`, `.isServerError()`, `.bodyAsString()`
  - builder for test/adapter convenience

**Thread-safety:** All fields final, no setters, defensive copies on mutable returns. Safe for
concurrent reads across threads without locks. Zero race conditions in high-throughput scenarios.

**Files:** 6 classes in `com.npat.uniclient.core.model`

---

## Sub 1.2 — Ports ✅

**Objective:** Port interfaces (owned by core, implemented by adapters).

**Delivered:**

- `TransportPort` — `ClientResponse execute(RequestSpec spec)`
  - One reason to change: how bytes physically go out/come back
  - Throws `ClientTransportException`, `MissingClientDependencyException`
- `PayloadCodecPort` — `byte[] serialize(Object)` + `<T> T deserialize(byte[], Class<T>)`
  - One reason to change: wire-format conversion (JSON, XML, SOAP)
  - Kept separate from `TransportPort` (SRP: two concerns)
  - Throws `PayloadCodecException`, `MissingClientDependencyException`
- `DependencyAvailabilityPort` — `boolean isAvailable(String fullyQualifiedClassName)`
  - One reason to change: how to detect optional runtime dependencies
  - Never throws (safe to call; returns true/false always)

**Design:** Pure interfaces, no default methods yet (minimal abstraction), thread-safe contracts.

**Files:** 3 interfaces in `com.npat.uniclient.core.port`

---

## Sub 1.3 — Core Exception Hierarchy ✅

**Objective:** Consistent, clear failure vocabulary.

**Delivered:**

- `UniClientException` — root, unchecked (extends RuntimeException)
  - No forced checked exceptions on consumers (principle: zero boilerplate)
- `ClientTransportException` — I/O, protocol, timeout, connectivity failure
  - Wraps underlying cause (IOException, SSLException, etc.)
- `PayloadCodecException` — serialization/deserialization failure
  - Wraps underlying cause
- `MissingClientDependencyException` — optional library not on classpath
  - **Standardized message format:** `"APACHE_CXF requires org.apache.cxf:cxf-rt-frontend-jaxws. Add it to pom.xml with <scope>provided</scope>..."`
  - Carries `engineName` and `mavenCoordinate` fields for programmatic access
  - This is "fail loud" in action — replaces cryptic `NoClassDefFoundError` with actionable guidance

**Design:** All inherit from `UniClientException`, immutable, thread-safe.

**Files:** 4 classes in `com.npat.uniclient.core.exception`

---

## Sub 1.4 — Dependency Guard ✅

**Objective:** One reusable mechanism for checking optional dependencies + pom.xml policy.

**Delivered:**

- `ClasspathDependencyAvailability implements DependencyAvailabilityPort`
  - Default implementation using `Class.forName(name, false, classLoader)`
  - Never throws (catches `ClassNotFoundException`, returns false)
  - Thread-safe: `Class.forName` is thread-safe, no mutable state
- `DependencyRequirement` — helper utility
  - `static void require(DependencyAvailabilityPort, className, engineName, mavenCoordinate)`
  - Used by adapter registry (Plan 2 Sub 2.3) before instantiating optional-dependency adapters
  - Throws `MissingClientDependencyException` with standardized message if missing
- **pom.xml Policy** (documented in `docs/OPTIONAL_DEPENDENCIES.md`)
  - JDK-only adapters (HttpURLConnection, JDK HttpClient): **no `<dependency>` entry**
  - Optional-dependency adapters (CXF, Jackson): `<optional>true</optional>` in pom.xml
  - Effect: zero forced dependencies on consumers; optional libraries stay out of transitive closure
  - Consumer chooses to add CXF/Jackson only if they need those engines

**Design:** Reusable, generic (not hardcoding "CXF" or "Jackson"), thread-safe.

**Files:** 2 classes in `com.npat.uniclient.core.support` + 1 policy document

---

## Architecture Compliance

✅ **SRP:** Every type has one reason to change:
- `RequestSpec` — request data shape
- `TimeoutConfig` — timeout values (no retry execution)
- `HeaderSet` — header storage
- `TransportPort` — transport mechanism
- `PayloadCodecPort` — serialization format
- `DependencyAvailabilityPort` — dependency detection

✅ **Hexagonal Boundaries:**
- Core imports: **zero non-JDK imports** ✅
- Ports owned by core, implemented by adapters ✅
- No port references a concrete library ✅

✅ **Thread-Safety for High-Throughput:**
- All value types immutable (final fields, no setters) ✅
- Defensive copies on mutable returns ✅
- No synchronized/locks needed for concurrent reads ✅
- GC-friendly (predictable lifetimes) ✅

✅ **Zero Forced Dependencies:**
- No compile-time dependencies beyond JDK ✅
- Optional adapters guarded via `DependencyRequirement.require()` ✅
- Clear failure messages naming exactly what to install ✅

---

## Ready for Plan 2

All types Sub 1.1–1.4 are in place. Plan 2 (Application Layer) can now:
- Wire ports together in `ClientFacade` ✅
- Implement `AdapterRegistry` mapping `ServiceClient` → `TransportPort` ✅
- Use `DependencyRequirement.require()` before instantiating optional adapters ✅

Plans 3–5 (adapters) can now implement these ports cleanly with zero core changes.

---

## Package Structure

```
com.npat.uniclient.core
  ├── model/
  │   ├── HeaderSet.java
  │   ├── TimeoutConfig.java
  │   ├── AuthConfig.java
  │   ├── SslConfig.java
  │   ├── RequestSpec.java
  │   └── ClientResponse.java
  ├── port/
  │   ├── TransportPort.java
  │   ├── PayloadCodecPort.java
  │   └── DependencyAvailabilityPort.java
  ├── exception/
  │   ├── UniClientException.java
  │   ├── ClientTransportException.java
  │   ├── PayloadCodecException.java
  │   └── MissingClientDependencyException.java
  └── support/
      ├── ClasspathDependencyAvailability.java
      └── DependencyRequirement.java
```

---

## Next: Plan 2 — Application Layer

Ready to proceed? Plan 2 Sub 2.1 will fill in `ClientFacade` and wire these pieces together.
