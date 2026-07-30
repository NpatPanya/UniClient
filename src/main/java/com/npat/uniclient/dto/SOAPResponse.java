package com.npat.uniclient.dto;

public class SOAPResponse<T> extends APIResponse<T> {
    private String soapHeader;    // serialized header XML
    private String soapBody;    // serialized body XML
    private String faultCode;    // fault code
    private String faultString; // fault description

    public String getSoapHeader() {
        return soapHeader;
    }

    public void setSoapHeader(String soapHeader) {
        this.soapHeader = soapHeader;
    }

    public String getSoapBody() {
        return soapBody;
    }

    public void setSoapBody(String soapBody) {
        this.soapBody = soapBody;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    @Override
    public String toString() {
        return "SOAPResponse [soapHeader=" + soapHeader + ", soapBody=" + soapBody + ", faultCode=" + faultCode
                + ", faultString=" + faultString + "]";
    }

}
