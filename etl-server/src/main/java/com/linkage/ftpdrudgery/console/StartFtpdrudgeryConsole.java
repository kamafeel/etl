package com.linkage.ftpdrudgery.console;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.SystemBean;

/**
 * 启动控制台服务端
 * @author run[zhangqi@lianchuang.com]
 * 5:41:45 PM May 8, 2009
 */
public class StartFtpdrudgeryConsole {

	private static Logger logger = LoggerFactory.getLogger(StartFtpdrudgeryConsole.class);
	
	/* 单例模式 */
	private static StartFtpdrudgeryConsole SINGLE = new StartFtpdrudgeryConsole();
	
	/**
	 * 单例模式
	 * @return
	 */
	public static synchronized StartFtpdrudgeryConsole getInstance() {

		if (SINGLE == null) {
			SINGLE = new StartFtpdrudgeryConsole();			
		}
		return SINGLE;
	}
	
	private StartFtpdrudgeryConsole(){

		SystemBean sb = GlobalBean.getInstance().getSystemBean();
		logger.info("控制台地址:{}",sb.getUrl());
		try {
			LocateRegistry.createRegistry(sb.getPort());
			IFtpdrudgeryConsole fc = new IFtpdrudgeryConsoleImpl();
			Naming.bind(sb.getUrl(), fc);
		} catch (RemoteException e) {
			logger.error("控制台服务端启动异常", e);
		} catch (MalformedURLException e) {
			logger.error("控制台服务端启动异常", e);
		} catch (AlreadyBoundException e) {
			logger.error("控制台服务端启动异常", e);
		} 
	}
}
