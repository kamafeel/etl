package com.linkage.ftpdrudgery.tools;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 线程管理类
 * @author run[zhangqi@lianchuang.com]
 * 4:41:33 PM Oct 26, 2009
 */
public class NpScheduledFuture {
	
	private static Logger logger = LoggerFactory.getLogger(NpScheduledFuture.class);
	
	private static NpScheduledFuture SINGLE = new NpScheduledFuture();
	
	private ScheduledExecutorService scheduler;
	
	public static synchronized NpScheduledFuture getInstance(){
		if(SINGLE == null){
			SINGLE = new NpScheduledFuture();
		}
		return SINGLE;
	}
	
	private NpScheduledFuture(){
		this.start();
	}
	
	public void start(){
		logger.info("线程调度工作类启动");
		scheduler = Executors.newScheduledThreadPool(1);
		try {
			scheduler.scheduleWithFixedDelay(this.getRunnable("my.fangshu.mp.nieHaha.NieHahAToFile"),5l, 30l, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.error("线程调度工作类异常:",e);
		}
	}
	
	/**
	 * 停止线程控制
	 */
	public synchronized void shudownScheduledExecutorService(){
		scheduler.shutdown();
	}
	
	/**
	 * 得到Runnable
	 * @param s
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private Runnable getRunnable(String s) throws InstantiationException, IllegalAccessException, ClassNotFoundException{		
		return (Runnable) Class.forName(s).newInstance();
	}
	
}
