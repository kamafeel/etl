package com.linkage.ftpdrudgery.bean;

import java.util.Date;
import java.util.HashMap;

/**
 * 
 * @author Run
 *
 */
public class TWMonitorBean implements java.io.Serializable{

	private static final long serialVersionUID = 5544865609296316249L;
	
	/*任务ID*/	 
	private String id;
	/*任务名称*/	 	
	private String taskName;		
	/*任务状态*/	 
	private String taskStatus;	
	/*任务开始时间*/
	private Date startTime;	
	/*任务结束时间*/
	private Date endTime;			
	/*任务耗时 */
	private String castTime;
	/*任务额外信息 */
	private HashMap<String,String> taskExp;
	
	public static final String EXP_SourceFile_Num = "EXP_SourceFile_Num";
	public static final String EXP_SourceFile_Size = "EXP_SourceFile_Size";
	public static final String EXP_SourceFile_Names = "EXP_SourceFile_Names";
	
	public static final String EXP_ReturnFiles_Num = "EXP_ReturnFiles_Num";
	public static final String EXP_ReturnFiles_Size = "EXP_ReturnFiles_Size";
	public static final String EXP_ReturnFiles_Names = "EXP_ReturnFiles_Names";
	
	/*处理结果信息*/
	public static final String EXP_ResultInfo = "EXP_ResultInfo";
	public static final String EXP_Exception = "EXP_Exception";
	
	public TWMonitorBean(){
		taskExp = new HashMap<String,String>(10);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getCastTime() {
		return castTime;
	}
	public void setCastTime(String castTime) {
		this.castTime = castTime;
	}
	public HashMap<String, String> getTaskExp() {
		return taskExp;
	}
	public void setTaskExp(String key, String value) {
		this.taskExp.remove(key);
		this.taskExp.put(key, value);
	}
}
