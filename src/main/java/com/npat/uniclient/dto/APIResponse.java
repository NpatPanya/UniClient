package com.npat.uniclient.dto;

import java.time.LocalDateTime;

public class APIResponse<T> {
    private boolean success;
    private Integer httpCode;
    private String rspCode;
    private String rspMessage;
    private Exception exception;
    private String errorMessage;
    private T responseEntity;
    private LocalDateTime rspTime;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
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

    public T getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(T responseEntity) {
        this.responseEntity = responseEntity;
    }

    public LocalDateTime getRspTime() {
        return rspTime;
    }

    public void setRspTime(LocalDateTime rspTime) {
        this.rspTime = rspTime;
    }
}