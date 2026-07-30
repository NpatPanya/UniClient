package com.npat.uniclient.dto;

import java.util.Arrays;

public class SocketResponse <T> extends APIResponse <T> {
    private byte[] rawData;               // raw bytes received
    private String encoding;			  // optional (UTF-8, ISO-8859-1)
    
	public byte[] getRawData() {
		return rawData;
	}
	public void setRawData(byte[] rawData) {
		this.rawData = rawData;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	@Override
	public String toString() {
		return "SocketResponse [rawData=" + Arrays.toString(rawData) + ", encoding=" + encoding + "]";
	}
    
}
