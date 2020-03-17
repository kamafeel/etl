package com.linkage.ftpdrudgery.bean;

import org.apache.wicket.IClusterable;


/**
 * 任务信息Bean
 * @author run[zhangqi@lianchuang.com]
 * 3:14:06 PM May 8, 2009
 */
public class TaskBean implements java.io.Serializable, IClusterable {
	
	private static final long serialVersionUID = -799464765630525558L;
	
	/*任务ID*/	 
	private String id;
	/*任务名称*/	 	
	private String taskName;	
	/*提供方FtpInfo对象 */
	private FtpInfoBean sourceFtp;	
	/*使用方FtpInfo对象*/	 	
	private FtpInfoBean destFtp;	
	/*提供方下载路径*/	 
	private String sourceDir;	
	/*使用方上传路径*/
	private String destDir;	
	/*提供方下载正则匹配*/
	private String regExp;			
	/*是否下载后删除 */
	private boolean isDelete;		
	/*任务调度时间信息 */
	private String cronTrigger;	
	/*备份目录 */
	private String backUpDir;	
	/*检验文件大小变化休眠时间 */
	private String checkSleepTime;
	/*任务状态*/
	private String state;	
	/* 更新后是否保存*/
	private boolean isSave;	
	/*控制任务时候,时候被选择  */
	private boolean isSelect;	
	/*任务上次操作方式*/
	private String lastAction;	
	/* 插件名称*/
	private String pluginName;	
	/*插件路径*/
	private String pluginPath;	
	/* Class路径 */
	private String pluginClassPath;
	/* 插件配置ID */
	private String pluginId;
	/* 是否要备份原始 */
	private boolean IsBackUp;
	/* 前*天时间限制 */
	private String beforeDay;
	private String recordValidDay;
	
	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public String getCronTrigger() {
		return cronTrigger;
	}

	public void setCronTrigger(String cronTrigger) {
		this.cronTrigger = cronTrigger;
	}

	public FtpInfoBean getSourceFtp() {
		return sourceFtp;
	}

	public void setSourceFtp(FtpInfoBean sourceFtp) {
		this.sourceFtp = sourceFtp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FtpInfoBean getDestFtp() {
		return destFtp;
	}

	public void setDestFtp(FtpInfoBean destFtp) {
		this.destFtp = destFtp;
	}

	public String getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public String getDestDir() {
		return destDir;
	}

	public void setDestDir(String destDir) {
		this.destDir = destDir;
	}

	public String getRegExp() {
		return regExp;
	}

	public void setRegExp(String regExp) {
		this.regExp = regExp;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}


	public String getCheckSleepTime() {
		return checkSleepTime;
	}

	public void setCheckSleepTime(String checkSleepTime) {
		this.checkSleepTime = checkSleepTime;
	}

	public boolean isSave() {
		return isSave;
	}

	public void setSave(boolean isSave) {
		this.isSave = isSave;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isSelect() {
		return isSelect;
	}

	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	public String getLastAction() {
		return lastAction;
	}

	public void setLastAction(String lastAction) {
		this.lastAction = lastAction;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getPluginPath() {
		return pluginPath;
	}

	public void setPluginPath(String pluginPath) {
		this.pluginPath = pluginPath;
	}

	public String getPluginClassPath() {
		return pluginClassPath;
	}

	public void setPluginClassPath(String pluginClassPath) {
		this.pluginClassPath = pluginClassPath;
	}

	public String getBackUpDir() {
		return backUpDir;
	}

	public void setBackUpDir(String backUpDir) {
		this.backUpDir = backUpDir;
	}
	
	@Override
	public String toString()
	{
		return taskName;
	}

	public boolean isBackUp() {
		return IsBackUp;
	}

	public void setBackUp(boolean isBackUp) {
		IsBackUp = isBackUp;
	}

	public String getBeforeDay() {
		return beforeDay;
	}

	public void setBeforeDay(String beforeDay) {
		this.beforeDay = beforeDay;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setRecordValidDay(String recordValidDay) {
		this.recordValidDay = recordValidDay;
	}

	public String getRecordValidDay() {
		return recordValidDay;
	}
}
