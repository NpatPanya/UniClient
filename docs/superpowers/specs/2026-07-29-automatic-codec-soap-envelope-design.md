# Automatic Request Encoding and CXF SOAP Envelope Design

## Goal

Close the documented Plan 6 serialization gap by making the standard facade automatically
encode logical request bodies before transport execution, including constructing a complete SOAP
1.1 envelope for CXF requests.

## Decisions

- Keep `TransportPort` byte-oriented. Transports remain responsible only for sending bytes.
- Add a core-owned `RequestEncoderPort`; the adapter layer implements it with JSON and SOAP
  codecs.
- Preserve existing `byte[]` and `String` bodies as already-serialized wire bodies.
- Encode non-wire POJOs as JSON for REST/HttpURLConnection and as SOAP for Apache CXF.
- Require explicit SOAP namespace, operation name, and action metadata because those values
  cannot be safely inferred from an arbitrary POJO or URI.
- Store SOAP operation metadata in the core request model, not in an adapter package.
- Keep the existing `ClientFacade(AdapterResolver)` constructor behavior-compatible and add an
  encoder-injecting constructor. The standard composition factory will use automatic encoding.
- CXF continues to receive a complete envelope through its existing `Dispatch<SOAPMessage>`
  boundary; CXF itself does not become a serializer.

## Data flow

```text
logical RequestSpec body
        |
        v
RequestEncoderPort
  REST/HTTP -> JSON bytes
  CXF       -> SOAP envelope bytes
        |
        v
RequestSpec with serialized body
        |
        v
RetryingTransportPort -> concrete transport -> network
```

## Error behavior

- A CXF POJO request without SOAP metadata fails with `UniClientException` before transport
  invocation.
- Codec failures remain `PayloadCodecException`.
- Existing raw `byte[]` and `String` requests do not require SOAP or JSON metadata.

## Compatibility

The existing transport port and raw-body behavior remain valid. Automatic encoding is available
through the new `ClientFacade(AdapterResolver, RequestEncoderPort)` constructor and the standard
client composition helper.
