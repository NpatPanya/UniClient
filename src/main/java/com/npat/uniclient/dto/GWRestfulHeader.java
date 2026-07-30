package com.npat.uniclient.dto;



/**
 * GW Restful Header DTO according to AYGW specification.
 * Refers to {@linkplain GW_HTTP_HEADER} for header details. 
 *
 * @author 2521106332
 * @since 5 พ.ย. 2568
 */
public class GWRestfulHeader {
	private String requestRef;
    private String transmitDateTime;
    private String gatewayReply;
    
	public String getRequestRef() {
		return requestRef;
	}
	public void setRequestRef(String requestRef) {
		this.requestRef = requestRef;
	}
	public String getTransmitDateTime() {
		return transmitDateTime;
	}
	public void setTransmitDateTime(String transmitDateTime) {
		this.transmitDateTime = transmitDateTime;
	}
	public String getGatewayReply() {
		return gatewayReply;
	}
	public void setGatewayReply(String gatewayReply) {
		this.gatewayReply = gatewayReply;
	}
	
	@Override
	public String toString() {
		return "GWRestfulHeader [requestRef=" + requestRef + ", transmitDateTime=" + transmitDateTime
				+ ", gatewayReply=" + gatewayReply + "]";
	}
    
}
