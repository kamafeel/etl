package com.linkage.ftpdrudgery.bean;

/**
 * 系统基本信息Bean
 * @author run[zhangqi@lianchuang.com]
 * 10:47:16 PM May 8, 2009
 */
public class SystemBean implements java.io.Serializable {
	
	private static final long serialVersionUID = -1030626264292123757L;
	/* 下载文件临时目录 */
	private String downLoadTempDir;	
	/* 存档总目录 */
	private String backUpDir;
	/* 上传临时后缀 */
	private String uploadPostfix;
	/* 控制台URL */
	private String url;
	/* 控制台启动标志 */
	private boolean consoleStart;
	/* 监控记录启动标志 */
	private boolean isRecordMonitor;
	
	public String getDownLoadTempDir() {
		return downLoadTempDir;
	}
	public void setDownLoadTempDir(String downLoadTempDir) {
		this.downLoadTempDir = downLoadTempDir;
	}

	public String getBackUpDir() {
		return backUpDir;
	}
	public void setBackUpDir(String backUpDir) {
		this.backUpDir = backUpDir;
	}
	public String getUploadPostfix() {
		return uploadPostfix;
	}
	public void setUploadPostfix(String uploadPostfix) {
		this.uploadPostfix = uploadPostfix;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isConsoleStart() {
		return consoleStart;
	}
	public void setConsoleStart(boolean consoleStart) {
		this.consoleStart = consoleStart;
	}
	
	/* 控制台端口 */
	public int getPort() {
		String [] step1 = this.getUrl().split(":");	
		String [] step2 = step1[2].split("/");
		return Integer.parseInt(step2[0]);
	}
	public boolean isRecordMonitor() {
		return isRecordMonitor;
	}
	public void setRecordMonitor(boolean isRecordMonitor) {
		this.isRecordMonitor = isRecordMonitor;
	}
}
