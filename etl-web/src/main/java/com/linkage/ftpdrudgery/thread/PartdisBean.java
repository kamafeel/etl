package com.linkage.ftpdrudgery.thread;

import java.util.ArrayList;
import java.util.List;

public class PartdisBean {
	
	private static PartdisBean SINGLE = new PartdisBean();
	
	private List<String> timeNum;
	
	private String partPath;
	private String backPath;
	private int sendNum;
	private int restartNum;
	
	public static synchronized PartdisBean getInstance() {

		if (SINGLE == null) {
			SINGLE = new PartdisBean();			
		}
		return SINGLE;
	}
	
	public PartdisBean(){
		setTimeNum(new ArrayList<String>());
		sendNum = 0;
		restartNum = 0;
	}

	public void setTimeNum(List<String> timeNum) {
		this.timeNum = timeNum;
	}

	public List<String> getTimeNum() {
		return timeNum;
	}

	public void setPartPath(String partPath) {
		this.partPath = partPath;
	}

	public String getPartPath() {
		return partPath;
	}

	public void setBackPath(String backPath) {
		this.backPath = backPath;
	}

	public String getBackPath() {
		return backPath;
	}

	public void setSendNum(int sendNum) {
		this.sendNum = sendNum;
	}

	public int getSendNum() {
		return sendNum;
	}

	public void setRestartNum(int restartNum) {
		this.restartNum = restartNum;
	}

	public int getRestartNum() {
		return restartNum;
	}

}
