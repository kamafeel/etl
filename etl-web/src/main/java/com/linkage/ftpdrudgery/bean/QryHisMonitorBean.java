package com.linkage.ftpdrudgery.bean;

import com.linkage.intf.tools.StringUtils;

public class QryHisMonitorBean implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 719343554828699032L;
	private String qryKey;
	private String qry;
	private String status;
	private String taskDate;
	
	public String getQryKey() {
		return StringUtils.isEmpty(qryKey) ? "Id" : qryKey;
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
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setTaskDate(String taskDate) {
		this.taskDate = taskDate;
	}
	public String getTaskDate() {
		return taskDate;
	}
}
