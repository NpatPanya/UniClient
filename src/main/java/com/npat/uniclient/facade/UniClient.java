package com.npat.uniclient.facade;
import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;
import com.npat.uniclient.port.JsonCodec;
import java.util.Objects;
/** Synchronous transport-agnostic facade over a caller-configured adapter registry. */
public final class UniClient implements ClientFacade {
 private final AdapterRegistry adapterRegistry; private final JsonCodec jsonCodec;
 public UniClient(AdapterRegistry adapterRegistry){this(adapterRegistry,null);}
 public UniClient(AdapterRegistry adapterRegistry,JsonCodec jsonCodec){this.adapterRegistry=Objects.requireNonNull(adapterRegistry,"adapterRegistry");this.jsonCodec=jsonCodec;}
 @Override @SuppressWarnings("unchecked") public <R extends APIResponse<?>> R send(APIRequest<?,R> request){return (R)adapterRegistry.send(Objects.requireNonNull(request,"request"),jsonCodec);}
}