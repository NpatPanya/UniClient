package com.npat.uniclient.dto;

/**
 * DTO that holds unique identifiers for a transaction.
 *
 * @author 2521106332
 * @since 5 พ.ย. 2568
 */
public class TransactionIdentity {
	private String serviceName;
	private String transType;
	private String transCode;
	private String clientId;
	private String providerId;
	private String requestRef;
	private String transDateTime;
	
	public String getTransType() {
		return transType;
	}
	public void setTransType(String transType) {
		this.transType = transType;
	}
	public String getTransCode() {
		return transCode;
	}
	public void setTransCode(String transCode) {
		this.transCode = transCode;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getRequestRef() {
		return requestRef;
	}
	public void setRequestRef(String requestRef) {
		this.requestRef = requestRef;
	}
	public String getTransDateTime() {
		return transDateTime;
	}
	public void setTransDateTime(String transDateTime) {
		this.transDateTime = transDateTime;
	}
	
	@Override
	public String toString() {
		return "TransactionIdentity [serviceName=" + serviceName + ", transType=" + transType + ", transCode="
				+ transCode + ", clientId=" + clientId + ", providerId=" + providerId + ", requestRef=" + requestRef
				+ ", transDateTime=" + transDateTime + "]";
	}
	
}
