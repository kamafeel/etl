package com.linkage.ftpdrudgery.bean;

/**
 * 控制台结果Bean
 * @author run[zhangqi@lianchuang.com]
 * 12:19:40 AM May 10, 2009
 */
public class ReturnBean implements java.io.Serializable {

	private static final long serialVersionUID = -4130572121236339005L;
	/* 结果编码 */
	private int returnCode;	
	/* 结果信息 */
	private String returnInfo;
	
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public String getReturnInfo() {
		return returnInfo;
	}
	public void setReturnInfo(String returnInfo) {
		this.returnInfo = returnInfo;
	}
	
}
