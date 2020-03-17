package com.linkage.ftpdrudgery.bean;

public class ServerBean implements java.io.Serializable {
	
	private static final long serialVersionUID = -5329328792348775022L;
	
	private String serverURL;
	private String serverInfo;
	
	public String getServerURL() {
		return serverURL;
	}
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
	public String getServerInfo() {
		return serverInfo;
	}
	public void setServerInfo(String serverInfo) {
		this.serverInfo = serverInfo;
	}
}
