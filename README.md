# UniClient

UniClient is a synchronous outbound-client library for gateway applications. The caller chooses a protocol-specific builder, configures that protocol completely, and calls one method:

```java
response = uniClient.send(request);
```

It does not expose one mixed builder that contains unrelated HTTP, REST, SOAP, and socket fields.

## Supported protocols

| Builder | Runtime transport | Request and response |
| --- | --- | --- |
| `Requests.httpUrlConnection()` | JDK `HttpURLConnection` | `RestfulRequest<P, T>` -> `RestfulResponse<T>` |
| `Requests.restClient()` | JDK `HttpClient` | `RestfulRequest<P, T>` -> `RestfulResponse<T>` |
| `Requests.soapCxf()` | Apache CXF JAX-WS Dispatch | `SOAPRequest` -> `SOAPResponse<Element>` |
| `Requests.socket()` | Caller-provided `SocketAdapter` | `SocketRequest<P, T>` -> `SocketResponse<T>` |

All request builders require a positive connection timeout and read timeout. The default response limit is 10 MiB; override it with `maxResponseBytes(...)` when necessary.

## Installation

UniClient requires Java 17.

The default HTTP transports require only the library and the Jakarta REST API supplied by the application environment. Enable the `optional-adapters` Maven profile when building this project with Jackson JSON support and the CXF SOAP transport:

```text
mvn -Poptional-adapters test
```

A consuming application that selects SOAP must include CXF JAX-WS plus its HTTP transport and SAAJ implementation. A consuming application that sends DTOs or declares a DTO response type must provide a `JsonCodec`, normally `JacksonJsonCodec` backed by its own shared `ObjectMapper`.

Optional dependencies are lazy: a missing dependency throws `MissingDependencyException` only when the corresponding protocol or JSON conversion is actually selected.

## Compose a client

Register the adapters that your application supports. The caller controls this composition boundary.

```java
import com.npat.uniclient.adapter.HttpUrlConnectionAdapter;
import com.npat.uniclient.adapter.RestClientAdapter;
import com.npat.uniclient.facade.AdapterRegistry;
import com.npat.uniclient.facade.UniClient;

import java.util.List;

UniClient uniClient = new UniClient(new AdapterRegistry(List.of(
    new HttpUrlConnectionAdapter(),
    new RestClientAdapter())));
```

To use automatic JSON conversion, create one shared mapper and pass its codec to the facade:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npat.uniclient.adapter.HttpUrlConnectionAdapter;
import com.npat.uniclient.adapter.RestClientAdapter;
import com.npat.uniclient.adapter.codec.JacksonJsonCodec;
import com.npat.uniclient.facade.AdapterRegistry;
import com.npat.uniclient.facade.UniClient;

import java.util.List;

JacksonJsonCodec json = new JacksonJsonCodec(new ObjectMapper());
UniClient uniClient = new UniClient(new AdapterRegistry(List.of(
    new HttpUrlConnectionAdapter(),
    new RestClientAdapter())), json);
```

Add `new ApacheCxfSoapAdapter()` to that registry only in an application that includes CXF. Add one application-specific `SocketAdapter` to support its socket protocol.

## HTTP and REST

Use `httpUrlConnection()` when the JDK URL-connection transport is desired, and `restClient()` when the JDK `HttpClient` transport is desired. Both use explicit HTTP methods and permit repeated headers through the builder's `header(...)` method.

```java
import com.npat.uniclient.builder.Requests;
import com.npat.uniclient.domain.HTTP_METHOD;
import com.npat.uniclient.dto.RestfulRequest;
import com.npat.uniclient.dto.RestfulResponse;

import java.time.Duration;

RestfulRequest<String, String> request = Requests.httpUrlConnection()
    .body("{\"reference\":\"A-17\"}")
    .endpoint("https://api.example.test/orders")
    .method(HTTP_METHOD.POST)
    .header("Content-Type", "application/json")
    .connTimeout(Duration.ofSeconds(2))
    .readTimeout(Duration.ofSeconds(10))
    .build();

RestfulResponse<String> response = uniClient.send(request);
```

Strings and `byte[]` payloads pass through unchanged. For another DTO type, UniClient encodes the request as JSON and defaults `Content-Type` to `application/json` unless the caller already provided it.

```java
record CreateOrder(String reference) {}
record OrderReply(String id) {}

RestfulRequest<CreateOrder, OrderReply> request = Requests.restClient()
    .body(new CreateOrder("A-17"))
    .responseType(OrderReply.class)
    .endpoint("https://api.example.test/orders")
    .method(HTTP_METHOD.POST)
    .connTimeout(Duration.ofSeconds(2))
    .readTimeout(Duration.ofSeconds(10))
    .build();

RestfulResponse<OrderReply> response = uniClient.send(request);
OrderReply reply = response.getResponseEntity();
```

Without `responseType(...)`, a REST response remains available as `rawPayload`; callers can parse it themselves. `ResponseType<T>` is also available for parameterized response entities.

## SOAP with CXF

`soapCxf()` accepts only a complete SOAP 1.1 or SOAP 1.2 `org.w3c.dom.Element` envelope containing a SOAP Body. UniClient does not build raw XML, create a SOAP operation, or convert a SOAP response to a DTO. The caller creates the envelope, including any SOAP headers and a body marshalled from its CXF-generated/JAXB request object; CXF sends that exact envelope at runtime in JAX-WS Dispatch message mode.

```java
import com.npat.uniclient.adapter.ApacheCxfSoapAdapter;
import com.npat.uniclient.builder.Requests;
import com.npat.uniclient.dto.SOAPRequest;
import com.npat.uniclient.dto.SOAPResponse;
import com.npat.uniclient.facade.AdapterRegistry;
import com.npat.uniclient.facade.UniClient;
import org.w3c.dom.Element;

import java.time.Duration;
import java.util.List;

// Construct a complete SOAP Envelope Element. The caller owns CXF-generated
// request objects, JAXB marshalling, SOAP headers, and the SOAP Body.
Element completeEnvelope = buildEnvelopeFromGeneratedCxfRequest();

UniClient soapClient = new UniClient(new AdapterRegistry(List.of(
    new ApacheCxfSoapAdapter())));

SOAPRequest request = Requests.soapCxf()
    .endpoint("https://soap.example.test/orders")
    .body(completeEnvelope)
    .soapAction("urn:orders:submit")
    .connTimeout(Duration.ofSeconds(2))
    .readTimeout(Duration.ofSeconds(15))
    .build();

SOAPResponse<Element> response = soapClient.send(request);
Element receivedEnvelope = response.getResponseEntity();
```

SOAP responses retain the received envelope element. SOAP faults return `success == false`, with `httpCode`, `faultString`, and `errorMessage` populated when CXF provides them. A missing CXF dependency throws `MissingDependencyException` when this request is sent; it does not affect HTTP-only or socket-only applications.

## Socket extension point

UniClient intentionally has no built-in socket wire protocol. Supply one `SocketAdapter` that translates the neutral request fields - host, port, `SocketFormat`, payload, and timeouts - to the socket library used by your gateway.

```java
import com.npat.uniclient.builder.Requests;
import com.npat.uniclient.domain.SocketFormat;
import com.npat.uniclient.dto.SocketRequest;
import com.npat.uniclient.dto.SocketResponse;

import java.time.Duration;

SocketRequest<String, String> request = Requests.socket()
    .body("ping")
    .responseType(String.class)
    .host("127.0.0.1")
    .port(9000)
    .format(SocketFormat.of("gateway-frame-v1"))
    .connTimeout(Duration.ofSeconds(2))
    .readTimeout(Duration.ofSeconds(10))
    .build();

SocketResponse<String> response = uniClient.send(request);
```

## Responses and failures

Every response extends `APIResponse<T>` and has these gateway-oriented fields:

- `success`
- `httpCode`  -  `null` for socket responses
- `rspCode` and `rspMessage`  -  values extracted from a response body when available
- `errorMessage`  -  an upstream error message or API-level failure detail
- `responseEntity`  -  the typed response payload, SOAP envelope, or socket payload

HTTP non-2xx responses and SOAP faults are returned as responses so a gateway can persist their details. Invalid request configuration, a missing selected optional dependency, and transport failures before a usable response exists throw typed unchecked exceptions:

- `RequestValidationException`
- `MissingDependencyException`
- `TransportException`

## Verification

```text
mvn test
mvn -Poptional-adapters test
```

The executable examples in `src/test/java/com/npat/uniclient/FacadeUsageExamplesTest.java` demonstrate all four builders. The CXF executable adapter test additionally verifies SOAP 1.1, SOAP 1.2, SOAP Fault handling, status propagation, and the lazy missing-CXF guard.
