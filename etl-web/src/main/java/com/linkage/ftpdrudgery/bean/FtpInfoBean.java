package com.linkage.ftpdrudgery.bean;

/**
 * Ftp信息Bean
 * @author run[zhangqi@lianchuang.com]
 * 3:14:45 PM May 8, 2009
 */
public class FtpInfoBean implements java.io.Serializable {
	
	private static final long serialVersionUID = 3692170544821963476L;
	
	/* ID */
	private String id;
	/* IP */
	private String remoteIP;
	/* 端口 */
	private int port;
	/* 登录账号 */
	private String userName;
	/* 登录密码 */
	private String passWord;
	/* 传输模式 */
	private String transfersType;
	/* 工作模式 */
	private String workType;
	/* 超时时间(毫秒) */
	private int timeout;
	/* 通讯的编码集 */
	private String encoding;
	/* 重试次数 */
	private int retryCount;
	/* 重试间隔时间(毫秒) */
	private long retryInterval;
	/* 最大连接数 */
	private long maxConnect;
	/*是否心跳消息 */
	private boolean isNoOp;
	/*是否断点续传 */
	private boolean isRBD;
	/* 选择状态 */
	private boolean isSelect;
	/* 任务关联 */
	private String relationship;
	
	public String getRemoteIP() {
		return remoteIP;
	}
	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	public String getTransfersType() {
		return transfersType;
	}
	public void setTransfersType(String transfersType) {
		this.transfersType = transfersType;
	}
	public String getWorkType() {
		return workType;
	}
	public void setWorkType(String workType) {
		this.workType = workType;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public int getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	public long getRetryInterval() {
		return retryInterval;
	}
	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	public String getRelationship() {
		return relationship;
	}
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
	public void setMaxConnect(long maxConnect) {
		this.maxConnect = maxConnect;
	}
	public long getMaxConnect() {
		return maxConnect;
	}
	public void setNoOp(boolean isNoOp) {
		this.isNoOp = isNoOp;
	}
	public boolean isNoOp() {
		return isNoOp;
	}
	public void setRBD(boolean isRBD) {
		this.isRBD = isRBD;
	}
	public boolean isRBD() {
		return isRBD;
	}
}
