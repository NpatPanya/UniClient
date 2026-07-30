package com.npat.uniclient.facade;

import com.npat.uniclient.dto.APIRequest;
import com.npat.uniclient.dto.APIResponse;

public interface ClientFacade {

    <R extends APIResponse<?>> R send(APIRequest<?, R> request);
}