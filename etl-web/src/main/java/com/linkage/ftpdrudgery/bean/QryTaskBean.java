package com.linkage.ftpdrudgery.bean;

public class QryTaskBean implements java.io.Serializable{
	
	private static final long serialVersionUID = 6363835756360762286L;
	private String qryKey;
	private String qry;
	
	public String getQryKey() {
		return qryKey;
	}
	public void setQryKey(String qryKey) {
		this.qryKey = qryKey;
	}
	public String getQry() {
		return qry;
	}
	public void setQry(String qry) {
		this.qry = qry;
	}
}
