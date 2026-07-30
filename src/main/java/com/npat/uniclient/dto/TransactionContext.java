package com.npat.uniclient.dto;


import com.npat.uniclient.exception.GWRejectException;

public class TransactionContext<Q, R> {
    //<ReqtRqt, ReqtRsp, PrvdRqt, PrvdRsp> {
    protected TransactionIdentity transIdentity;
    protected GWRestfulRequest<Q> rqtRequest;
    protected GWRestfulResponse<R> rqtResponse;
    //	protected APIRequest<PrvdRqt> prvdRequest;
//	protected APIResponse<PrvdRsp> prvdResponse;
    protected GWRejectException rejectException;
    protected String remoteIPAddress;
    protected String serverIPAddress;

    public TransactionIdentity getTxnIdentity() {
        return transIdentity;
    }

    public void setTxnIdentity(TransactionIdentity transIdentity) {
        this.transIdentity = transIdentity;
    }

    public GWRestfulRequest<Q> getRqtRequest() {
        return rqtRequest;
    }

    public void setRqtRequest(GWRestfulRequest<Q> rqtRequest) {
        this.rqtRequest = rqtRequest;
    }

    public GWRestfulResponse<R> getRqtResponse() {
        return rqtResponse;
    }

    public void setRqtResponse(GWRestfulResponse<R> rqtResponse) {
        this.rqtResponse = rqtResponse;
    }

    //	public APIRequest<PrvdRqt> getPrvdRequest() {
//		return prvdRequest;
//	}
//	public void setPrvdRequest(APIRequest<PrvdRqt> prvdRequest) {
//		this.prvdRequest = prvdRequest;
//	}
//	public APIResponse<PrvdRsp> getPrvdResponse() {
//		return prvdResponse;
//	}
//	public void setPrvdResponse(APIResponse<PrvdRsp> prvdResponse) {
//		this.prvdResponse = prvdResponse;
//	}
    public GWRejectException getRejectException() {
        return rejectException;
    }

    public void setRejectException(GWRejectException rejectException) {
        this.rejectException = rejectException;
    }

    public String getRemoteIPAddress() {
        return remoteIPAddress;
    }

    public void setRemoteIPAddress(String remoteIPAddress) {
        this.remoteIPAddress = remoteIPAddress;
    }

    public String getServerIPAddress() {
        return serverIPAddress;
    }

    public void setServerIPAddress(String serverIPAddress) {
        this.serverIPAddress = serverIPAddress;
    }

}
