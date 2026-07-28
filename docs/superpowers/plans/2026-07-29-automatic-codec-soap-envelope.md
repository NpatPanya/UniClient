# Automatic Codec and SOAP Envelope Implementation Plan

> **For agentic workers:** Use inline execution with `executing-plans` to implement this plan task-by-task.

**Goal:** Automatically encode POJO request bodies as JSON or complete SOAP envelopes before transport execution.

**Architecture:** The core owns a `RequestEncoderPort` and SOAP request metadata model. Adapter code implements the port using the existing codecs. `ClientFacade` orchestrates encoding, then passes serialized bytes through the existing `TransportPort` and retry decorator.

**Tech Stack:** Java 17, Maven, JDK XML/JSON fallback codecs, optional Jackson and Apache CXF.

## Global Constraints

- Preserve existing `TransportPort` and `PayloadCodecPort` signatures.
- Preserve raw `byte[]` and `String` request-body behavior.
- Do not make core/application code import CXF, Jackson, or adapter codec classes.
- CXF POJO requests require explicit namespace, operation name, and action metadata.
- Default Maven profile must remain dependency-free; `optional-adapters` verifies optional paths.
- Follow TDD: each production change starts with a failing executable assertion.

### Task 1: Add SOAP Request Configuration

**Files:**
- Create: `src/main/java/com/npat/uniclient/core/model/SoapRequestConfig.java`
- Modify: `src/main/java/com/npat/uniclient/core/model/RequestSpec.java`
- Test: `src/test/java/com/npat/uniclient/facade/ApplicationLayerPlan2Test.java`

- [x] Add immutable validated `SoapRequestConfig` with `namespaceUri()`, `operationName()`, and `action()` accessors.
- [x] Add nullable SOAP configuration storage and `Builder.soap(SoapRequestConfig)` to `RequestSpec`.
- [x] Add `RequestSpec.withBody(Object)` preserving every existing field.
- [x] Add assertions for SOAP metadata retention and body replacement.
- [x] Run the focused test and confirm it fails before implementation because the new API is absent.

### Task 2: Add the Encoding Port and Adapter

**Files:**
- Create: `src/main/java/com/npat/uniclient/core/port/RequestEncoderPort.java`
- Create: `src/main/java/com/npat/uniclient/adapter/codec/DefaultRequestEncoder.java`
- Test: `src/test/java/com/npat/uniclient/codec/SerializationAdaptersPlan4Test.java`

- [x] Define `byte[] encode(ServiceClient engine, RequestSpec request)` on the core port.
- [x] Implement raw `byte[]`/`String` pass-through.
- [x] Use `PayloadCodecResolver.resolveJson()` for REST and HttpURLConnection POJOs.
- [x] Build `SoapEnvelopeMetadata` from `SoapRequestConfig` and use `SoapEnvelopeCodec` for CXF POJOs.
- [x] Throw a clear `UniClientException` when a CXF POJO has no SOAP metadata.
- [x] Add failing assertions for JSON output, SOAP envelope structure, pass-through bodies, and missing metadata.
- [x] Implement the adapter and rerun the focused executable test until green.

### Task 3: Integrate Encoding into the Facade

**Files:**
- Modify: `src/main/java/com/npat/uniclient/facade/ClientFacade.java`
- Modify: `src/test/java/com/npat/uniclient/facade/ApplicationLayerPlan2Test.java`
- Create: `src/test/java/com/npat/uniclient/plan7/AutomaticEncodingPlan7Test.java`

- [x] Add `ClientFacade(AdapterResolver, RequestEncoderPort)` while preserving the existing constructor.
- [x] Encode first, create `spec.withBody(encodedBytes)`, then resolve and execute the transport.
- [x] Add a fake encoder and fake transport assertion proving the transport receives bytes and retains request configuration.
- [x] Add an end-to-end fake test proving a POJO REST body is encoded without network I/O.
- [x] Run default-profile tests and direct assertions.

### Task 4: Add Standard Automatic Composition and Documentation

**Files:**
- Create: `src/main/java/com/npat/uniclient/adapter/StandardClientFactory.java`
- Modify: `README.md`
- Modify: `docs/plans/plan-6-verification-docs/sub-6.3-readme-examples.md`
- Test: `src/test/java/com/npat/uniclient/plan6/ReadmeExamplesPlan6Test.java`

- [x] Add a standard factory that assembles `AdapterRegistry`, `DefaultRequestEncoder`, and `ClientFacade`.
- [x] Update examples to pass POJOs for REST and CXF and provide SOAP metadata explicitly.
- [x] Remove the documented automatic-encoding gap once verified.
- [x] Add a README compile-check using the standard factory.

### Task 5: Verify and Commit

- [x] Run `mvn clean test` without optional dependencies.
- [x] Run `mvn -Poptional-adapters clean test` with CXF/Jackson.
- [x] Run all executable Plan 2–7 assertion classes directly.
- [x] Run `git diff --check` and inspect the final status.
- [x] Commit the implementation as `feat: automatically encode facade request bodies`.
