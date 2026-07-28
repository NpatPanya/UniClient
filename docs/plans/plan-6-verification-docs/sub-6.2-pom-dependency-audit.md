# Sub 6.2 — pom.xml Dependency-Scope Audit

**Plan:** 6 — Verification & Docs · **Priority: P1** · **Predecessor:** `sub-6.1-core-unit-tests.md`
· **Successor:** `sub-6.3-readme-examples.md`

## Objective

Confirm every non-JDK dependency introduced across Plans 3–4 (CXF, Jackson) actually follows
Sub 1.4's policy — `<optional>true</optional>`, never a plain compile-scope dependency —
and that a consumer who depends on `uniclient` without those libraries gets a clean build,
not a transitive-dependency surprise.

## What to build

- Manual/scripted check of the final `pom.xml`: every `<dependency>` beyond
  `junit`/test-scope has `<optional>true</optional>`.
- A throwaway consumer-simulation check: a minimal second Maven module (or `mvn dependency:tree`
  inspection) depending on `uniclient` alone, confirming CXF/Jackson do not appear in its
  resolved dependency tree.
- Confirm `mvn test` from Sub 6.1's default (non-integration) suite still passes with those
  optional dependencies temporarily excluded from the test classpath — the real proof the
  "zero forced dependency" principle holds, not just the pom.xml annotation.

## Definition of done

- [x] Every optional dependency in `pom.xml` is marked `<optional>true</optional>`.
- [x] `mvn dependency:tree` from a clean consumer module does not list CXF or Jackson.
- [x] Default test suite passes with optional dependencies excluded from the test classpath.

## Output

Verified `pom.xml`, a short audit note recorded in this file (pass/fail per dependency) for
future reference when a new optional adapter is added.

## Verification record

**PASS (2026-07-29):** CXF and Jackson are declared only inside the explicit
`optional-adapters` Maven profile, and both declarations retain `<optional>true</optional>`.
The default profile compiles without either dependency. `mvn clean test` compiled 30 sources;
`mvn -Poptional-adapters clean test` compiled all 34 production sources.

The consumer simulation passed after installing the dependency-free artifact locally:

```text
mvn install -DskipTests
mvn -f target/plan6-consumer/pom.xml dependency:tree
```

The resulting tree contained only `com.npat.uniclient:uniclient:1.0.0-SNAPSHOT`; it did not
contain `org.apache.cxf` or `com.fasterxml.jackson.core`.