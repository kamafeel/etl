package com.linkage.ftpdrudgery.wicket;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.linkage.ftpdrudgery.bean.FtpInfoBean;


/**
 * FtpInfoPane
 * @author run[zhangqi@lianchuang.com]
 * 8:14:57 PM May 12, 2009
 */

public class FtpInfoPane extends Panel{
	
	private static final long serialVersionUID = 7321772212582296847L;
		
	public FtpInfoPane(String id, FtpInfoBean fb){
		super(id);
		if(fb == null){
			fb = new FtpInfoBean();
		}
		this.add(new Label("FtpInfoPane.id", fb.getId()));
		this.add(new Label("FtpInfoPane.remoteIP",fb.getRemoteIP()));
		this.add(new Label("FtpInfoPane.port", String.valueOf(fb.getPort())));
		this.add(new Label("FtpInfoPane.userName", fb.getUserName()));
		this.add(new Label("FtpInfoPane.passWord", fb.getPassWord()));
		this.add(new Label("FtpInfoPane.transfersType", fb.getTransfersType()));
		this.add(new Label("FtpInfoPane.workType", fb.getWorkType()));
		this.add(new Label("FtpInfoPane.timeout", String.valueOf(fb.getTimeout())));
		this.add(new Label("FtpInfoPane.encoding", fb.getEncoding()));
		this.add(new Label("FtpInfoPane.retryCount", String.valueOf(fb.getRetryCount())));
		this.add(new Label("FtpInfoPane.retryInterval", String.valueOf(fb.getRetryInterval())));
	}
}
