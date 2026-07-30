package com.npat.uniclient.dto;

import com.bbl.gw.common.dto.base.BaseRequestConfig;


public class SocketRequest<T> extends APIRequest<T> {

    private final BaseRequestConfig config;
    private final String encoding;

    public SocketRequest(T payload, BaseRequestConfig config, String encoding) {
        super(payload);
        this.config = config;
        this.encoding = encoding;
    }

    public BaseRequestConfig getConfig() {
        return config;
    }

    public String getEncoding() {
        return encoding;
    }

    //	private byte[] rawData; 		// raw bytes to send
//	private String encoding; 		// optional (UTF-8, ISO-8859-1)
//	private String remoteHost; 		// IP or hostname
//	private int remotePort; 		// port number
//
//	public byte[] getRawData() {
//		return rawData;
//	}
//	public void setRawData(byte[] rawData) {
//		this.rawData = rawData;
//	}
//	public String getEncoding() {
//		return encoding;
//	}
//	public void setEncoding(String encoding) {
//		this.encoding = encoding;
//	}
//	public String getRemoteHost() {
//		return remoteHost;
//	}
//	public void setRemoteHost(String remoteHost) {
//		this.remoteHost = remoteHost;
//	}
//	public int getRemotePort() {
//		return remotePort;
//	}
//	public void setRemotePort(int remotePort) {
//		this.remotePort = remotePort;
//	}
//
//	@Override
//	public String toString() {
//		return "SocketRequest [rawData=" + Arrays.toString(rawData) + ", encoding=" + encoding + ", remoteHost="
//				+ remoteHost + ", remotePort=" + remotePort + "]";
//	}

}