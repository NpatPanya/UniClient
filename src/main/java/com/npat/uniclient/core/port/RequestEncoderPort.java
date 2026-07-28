package com.npat.uniclient.core.port;

import com.npat.uniclient.ServiceClient;
import com.npat.uniclient.core.model.RequestSpec;

/**
 * Port for converting a logical request body into transport-ready bytes.
 */
public interface RequestEncoderPort {
    byte[] encode(ServiceClient engine, RequestSpec request);
}
