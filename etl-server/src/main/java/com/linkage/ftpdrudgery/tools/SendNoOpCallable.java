package com.linkage.ftpdrudgery.tools;

import java.util.concurrent.Callable;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;


public class SendNoOpCallable implements Callable<Boolean>{
	
	private static Logger logger = LoggerFactory.getLogger(SendNoOpCallable.class);

	private FTPClient ftpClient;
	//重试间隔
	private long sleepTime = 15000l;	
	/*
	 * 任务ID 
	 */
	private String taskId;
	
	public SendNoOpCallable(FTPClient ftpClient,String taskId){
		this.ftpClient = ftpClient;
		this.taskId = taskId;
	}
	
	public Boolean call() throws Exception {
		Boolean b = true;
		int i = 1;
		while(i < 5000 && GlobalBean.getInstance().NoOp_ThreadMap.containsKey(taskId)){
			i++;
			try {
				if(ftpClient != null && ftpClient.isConnected()){			
					b = ftpClient.sendNoOp();					
					logger.debug("FTP{} Send NOOP Command,Result:{}",ftpClient.getRemoteAddress().getHostAddress(),b);
				}
				Thread.sleep(sleepTime);
			} catch (Exception e) {
				logger.warn("SendNoOpCallable线程异常:{}",e.toString());
			}
		}		
		return b;
	}

}
