package com.npat.uniclient.dto;

import com.bbl.gw.common.dto.base.HttpRequestConfig;
import jakarta.ws.rs.core.MultivaluedMap;
//	private String destUrl;			// e.g. "https://api.example./api/v1/order/{orderId}"
//	private String resourcePath;	// e.g. "/api/v1/order/{orderId}"
//	private String httpMethod; 		// e.g. "GET", "POST"
//	private String contentType;  	// e.g. "application/json"

public class RestfulRequest<T> extends APIRequest<T> {

	private HttpRequestConfig config;
	private MultivaluedMap<String, String> pathParams;	// e.g. "orderId" -> "12345"
	private MultivaluedMap<String, String> queryParams;	// e.g. "status" -> "active"

	public RestfulRequest(T payload, HttpRequestConfig config, MultivaluedMap<String, String> pathParams, MultivaluedMap<String, String> queryParams) {
		super(payload);
		this.config = config;
		this.pathParams = pathParams;
		this.queryParams = queryParams;
	}

	public HttpRequestConfig getConfig() {
		return config;
	}

	public void setConfig(HttpRequestConfig config) {
		this.config = config;
	}

	public MultivaluedMap<String, String> getPathParams() {
		return pathParams;
	}

	public void setPathParams(MultivaluedMap<String, String> pathParams) {
		this.pathParams = pathParams;
	}

	public MultivaluedMap<String, String> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(MultivaluedMap<String, String> queryParams) {
		this.queryParams = queryParams;
	}


}
