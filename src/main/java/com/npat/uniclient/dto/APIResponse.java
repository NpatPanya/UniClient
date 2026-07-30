package com.npat.uniclient.dto;

import java.time.LocalDateTime;

/**
 * API Response DTO for capturing response data.
 *
 * @author 2521106332
 * @since 5 พ.ย. 2568
 */
public class APIResponse <T> {
	protected boolean isSuccess;
	protected String rspCode;
	protected String rspMessage;
	protected Exception exception;
	protected String errorMessage;
	
	protected String rspPayload;
	protected T rspEntity;
	protected LocalDateTime rspTime;
	
	public boolean isSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getRspCode() {
		return rspCode;
	}
	public void setRspCode(String rspCode) {
		this.rspCode = rspCode;
	}
	public String getRspMessage() {
		return rspMessage;
	}
	public void setRspMessage(String rspMessage) {
		this.rspMessage = rspMessage;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getRspPayload() {
		return rspPayload;
	}
	public void setRspPayload(String rspPayload) {
		this.rspPayload = rspPayload;
	}
	public T getRspEntity() {
		return rspEntity;
	}
	public void setRspEntity(T rspEntity) {
		this.rspEntity = rspEntity;
	}
	public LocalDateTime getRspTime() {
		return rspTime;
	}
	public void setRspTime(LocalDateTime rspTime) {
		this.rspTime = rspTime;
	}
	
}
