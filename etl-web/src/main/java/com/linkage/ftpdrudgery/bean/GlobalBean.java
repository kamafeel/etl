package com.linkage.ftpdrudgery.bean;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.wicket.authorization.strategies.role.Roles;


/**
 * 全局信息
 * @author run[zhangqi@lianchuang.com]
 * 2:41:59 PM May 22, 2009
 */
public class GlobalBean {
	
	private static Logger logger = Logger.getLogger(GlobalBean.class.getName());
	private static GlobalBean SINGLE = new GlobalBean();
	
	/*任务操作记录 */
	public static Hashtable<String, String> TaskLastAction;
	
	public static String ConsoleUrl;
	public static String ConsoleUrlList;
	
	public static String Encode;
	public ScheduledExecutorService scheduler;
	
	private static final String tClass = "com.linkage.ftpdrudgery.tools.PwRunnable";
	
	private HashMap<String,String> userPH;
	private HashMap<String,String> userDB;
	private HashMap<String,String> userRoles;
	/**
	 * 单例模式
	 * @return
	 */
	public static synchronized GlobalBean getInstance() {

		if (SINGLE == null) {
			SINGLE = new GlobalBean();			
		}
		return SINGLE;
	}
	
	private GlobalBean(){
		TaskLastAction = new Hashtable<String, String>();
		scheduler = Executors.newScheduledThreadPool(1);
		userDB = new HashMap<String,String>();
		userRoles = new HashMap<String,String>();
		userPH = new HashMap<String,String>();
		userPH.put("zhangqi", "13908062905");
		userPH.put("linxq", "15828432565");
		userPH.put("lcy", "13880089524");
		userRoles.put("zhangqi", Roles.ADMIN);
		userRoles.put("linxq", Roles.ADMIN);
		userRoles.put("lcy", Roles.ADMIN);
	}
	
	public void setTemporaryAccount(){
		userDB.put("admin", "123");
		userRoles.put("admin", Roles.ADMIN);
	}
	
	public void clearTemporaryAccount(){
		userDB.remove("admin");
		userRoles.remove("admin");
	}
	
	public static void setTaskLastAction(String id, String lastAction){
		TaskLastAction.put(id, lastAction);
	}
	
	public void setUserDB(String user, String pw){
		userDB.put(user, pw);
	}
	
	public HashMap<String,String> getUserDB(){
		return userDB;
	}
	
	public HashMap<String,String> getUserPH(){
		return userPH;
	}
	
	public HashMap<String,String> getUserRoles(){
		return userRoles;
	}
	
	public void setUserDB(){
		try {
			logger.info("Scheduler is Start!");
			scheduler.scheduleWithFixedDelay(getRunnable(tClass), 0, 86400, TimeUnit.SECONDS);
		}catch (Exception e) {
			logger.error("启动密码处理线程失败:", e);
		}
	}
	
	private Runnable getRunnable(String s) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return (Runnable) Class.forName(s).newInstance();
	}
}
