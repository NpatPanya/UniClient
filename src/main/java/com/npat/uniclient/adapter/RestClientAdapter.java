package com.npat.uniclient.adapter;

import com.npat.uniclient.domain.HttpTransportKind;
import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;
import com.npat.uniclient.dto.RestfulRequest;
import com.npat.uniclient.dto.RestfulResponse;
import com.npat.uniclient.dto.base.HttpRequestConfig;
import com.npat.uniclient.exception.TransportException;
import com.npat.uniclient.port.TransportAdapter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class RestClientAdapter implements TransportAdapter {
 private final com.npat.uniclient.port.JsonCodec jsonCodec;
 public RestClientAdapter(){this(null);}
 public RestClientAdapter(com.npat.uniclient.port.JsonCodec jsonCodec){this.jsonCodec=jsonCodec;}
 @Override public boolean supports(APIRequest<?,?> request){return request instanceof RestfulRequest<?,?> rest && rest.getConfig().getTransportKind()==HttpTransportKind.REST_CLIENT;}
 @Override public RestfulResponse<?> send(APIRequest<?,?> request){return send(request,jsonCodec);}
 @Override public RestfulResponse<?> send(APIRequest<?,?> request, com.npat.uniclient.port.JsonCodec jsonCodec){
  if(!(request instanceof RestfulRequest<?,?> rest))throw new TransportException("RestClient adapter requires a REST request"); HttpRequestConfig c=rest.getConfig();
  try { HttpClient.Builder cb=HttpClient.newBuilder().connectTimeout(c.getConnTimeout()).followRedirects(c.isFollowRedirects()?HttpClient.Redirect.NORMAL:HttpClient.Redirect.NEVER); if(c.getSslContext()!=null)cb.sslContext(c.getSslContext()); HttpRequest.Builder rb=HttpRequest.newBuilder(URI.create(c.getUrlConfig().getDestURL())).timeout(c.getReadTimeout()); c.getHeaderConfig().all().forEach((n,v)->v.forEach(x->rb.header(n,x))); applyAuth(rb, c); Object p=rest.getPayload(); HttpRequest.BodyPublisher body=p==null?HttpRequest.BodyPublishers.noBody():p instanceof byte[] b?HttpRequest.BodyPublishers.ofByteArray(b):p instanceof String s?HttpRequest.BodyPublishers.ofString(s,StandardCharsets.UTF_8):throwPayload(p,jsonCodec); rb.method(c.getHttpMethod().name(),body); HttpResponse<java.io.InputStream> response=cb.build().send(rb.build(),HttpResponse.BodyHandlers.ofInputStream()); return HttpResponseMapper.map(response.statusCode(),response.headers().map(),response.body(),c.getMaxResponseBytes(),rest.getResponseType(),jsonCodec);
  } catch(TransportException e){throw e;}catch(Exception e){throw new TransportException("REST request failed before a usable response was received",e);}
 }
  private static void applyAuth(HttpRequest.Builder builder, HttpRequestConfig config) { var auth=config.getAuthentication(); if(auth==null||auth.getType()==com.npat.uniclient.dto.componenet.AuthConfig.AuthType.NONE)return; String value=auth.getType()==com.npat.uniclient.dto.componenet.AuthConfig.AuthType.BEARER?"Bearer "+auth.getToken():"Basic "+java.util.Base64.getEncoder().encodeToString((auth.getUsername()+":"+auth.getPassword()).getBytes(StandardCharsets.UTF_8)); builder.header("Authorization", value); }
 private static HttpRequest.BodyPublisher throwPayload(Object payload, com.npat.uniclient.port.JsonCodec jsonCodec){if(jsonCodec==null)throw new com.npat.uniclient.exception.MissingDependencyException("Jackson JSON support is required for a DTO request payload. Enable the optional-adapters profile.");return HttpRequest.BodyPublishers.ofByteArray(jsonCodec.encode(payload));}
}