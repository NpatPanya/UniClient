package com.npat.uniclient.dto;

import com.bbl.gw.common.dto.base.HttpRequestConfig;

public class SOAPRequest <T> extends APIRequest <T> {

	private final HttpRequestConfig config;
	private final String soapAction;

	public SOAPRequest(T payload, HttpRequestConfig config, String soapAction) {
		super(payload);
		this.config = config;
		this.soapAction = soapAction;
	}

	public HttpRequestConfig getConfig() {
		return config;
	}

	public String getSoapAction() {
		return soapAction;
	}



	//	private String soapAction;
//	private String soapHeader; 	// serialized header XML
//	private String soapBody; 	// serialized body XML
//
//	public String getSoapAction() {
//		return soapAction;
//	}
//	public void setSoapAction(String soapAction) {
//		this.soapAction = soapAction;
//	}
//	public String getSoapHeader() {
//		return soapHeader;
//	}
//	public void setSoapHeader(String soapHeader) {
//		this.soapHeader = soapHeader;
//	}
//	public String getSoapBody() {
//		return soapBody;
//	}
//	public void setSoapBody(String soapBody) {
//		this.soapBody = soapBody;
//	}
//
//	@Override
//	public String toString() {
//		return "SOAPRequest [soapAction=" + soapAction + ", soapHeader=" + soapHeader + ", soapBody=" + soapBody + "]";
//	}
	
}
