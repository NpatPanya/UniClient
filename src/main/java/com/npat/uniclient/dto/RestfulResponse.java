package com.npat.uniclient.dto;

import jakarta.ws.rs.core.MultivaluedMap;

public class RestfulResponse <B> extends APIResponse <B> {
	private int httpStatus;		// e.g. 200, 404, 500
	private String contentType;	// e.g. "application/json"
	private MultivaluedMap<String, String> httpHeaders;
	
	public int getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public MultivaluedMap<String, String> getHttpHeaders() {
		return httpHeaders;
	}
	public void setHttpHeaders(MultivaluedMap<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}
	
	@Override
	public String toString() {
		return "RestfulResponse [httpStatus=" + httpStatus + ", contentType=" + contentType + ", httpHeaders="
				+ httpHeaders + "]";
	}
	
}
