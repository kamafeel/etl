package com.linkage.ftpdrudgery.db.fd.bean;

import com.linkage.ftpdrudgery.bean.TaskBean;

public class FdPluginInfo implements java.io.Serializable{
	
	private static final long serialVersionUID = -2282891041391959777L;
	private String pluginId;
	private Integer dateOffset;
	private String dB2Statement;
	private String perShell;
	private String db2Environment;
	private String pluginInfo;
	private TaskBean choiceTaskBean;
	private String taskId;
	
	public String getPluginId() {
		return pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getPerShell() {
		return perShell;
	}
	public void setPerShell(String perShell) {
		this.perShell = perShell;
	}
	public String getPluginInfo() {
		return pluginInfo;
	}
	public void setPluginInfo(String pluginInfo) {
		this.pluginInfo = pluginInfo;
	}
	public Integer getDateOffset() {
		return dateOffset;
	}
	public void setDateOffset(Integer dateOffset) {
		this.dateOffset = dateOffset;
	}
	public String getdB2Statement() {
		return dB2Statement;
	}
	public void setdB2Statement(String dB2Statement) {
		this.dB2Statement = dB2Statement;
	}
	public String getDb2Environment() {
		return db2Environment;
	}
	public void setDb2Environment(String db2Environment) {
		this.db2Environment = db2Environment;
	}
	public void setChoiceTaskBean(TaskBean choiceTaskBean) {
		this.choiceTaskBean = choiceTaskBean;
	}
	public TaskBean getChoiceTaskBean() {
		return choiceTaskBean;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getTaskId() {
		return taskId;
	}	  
}
