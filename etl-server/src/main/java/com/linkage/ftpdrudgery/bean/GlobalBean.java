package com.linkage.ftpdrudgery.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.quartz.Scheduler;

import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;
import com.linkage.ftpdrudgery.tools.FTPOperate;


/**
 * 全局Bean
 * @author run[zhangqi@lianchuang.com]
 * 3:14:32 PM May 8, 2009
 */
public class GlobalBean {
	/* 单例模式 */
	private static GlobalBean SINGLE = new GlobalBean();

	public static final String Config_FtpInfo = "FtpInfo.xml";
	
	public static final String Config_FtpTask = "FtpTask.xml";
	
	public static final String Config_System = "System.xml";
	/* 配置文件备份文件夹名 */
	public static final String Config_BackUpDir = "ConfigBackUp";		
	/* 记录分割符 */
	public static final String Record_Delimiter = ",";
	/* 记录文件存放文件夹 */
	public static final String Record_FileDir = "Record";	
	/* 记录原始文件后缀 */
	public static final String Source_Record_Postfix = ".sr";
	/* 记录回执文件后缀 */
	public static final String Return_Record_Postfix = ".rr";
	/* Map标识 */
	public static final String TaskBean = "TaskBean";
	
	/* 原始文件备份文件夹 */
	public static final String OrigDir = "Orig";
	/* 回执文件备份文件夹 */
	public static final String ReturnDir = "Return";
	
	/* 配置文件路径 */
	private String configPath;
	
	/* 系统参数Bean */
	private SystemBean systemBean;
	
	/* 共享调度管理器对象Scheduler */
	private Scheduler scheduler;
	
	/* 运行任务队列监控 */
	private ConcurrentHashMap<String,TWMonitorBean> TWTable;
	
	/* 可终止任务队列 */
	private ConcurrentHashMap<String,FTPOperate> ITTable;

	/* 全局FTP配置信息 */
	private ConcurrentHashMap<String, FtpInfoBean> FtpInfoList;
	
	/* 全局FTP配置信息 */
	private ConcurrentHashMap<String, Long> currentConnectMap;
	
	/* 任务队列 */
	private ArrayList<TaskBean> taskBeanList;
	
	/* FTP Noop命令异步线程Map */
	public ConcurrentHashMap<String,FutureTask<Boolean>> NoOp_ThreadMap;
	
	/* 插件Map */
	private HashMap<String,FdPluginInfo> fdpMap;
	
	/* 密钥 */
	private String DESPlusKey;
	
	/* 插件Map */
	private HashMap<String,String> compressConmandMap;
	
	private ConcurrentHashMap<String,String> newDateGis;
	
	private boolean gnLoad;
	private boolean oprNewDateGisMap;
	private boolean oprPartFile;
	private ExecutorService threadPool;
	
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
		this.TWTable = new ConcurrentHashMap<String,TWMonitorBean>(10);
		this.ITTable = new ConcurrentHashMap<String,FTPOperate>(10);
		this.taskBeanList = new ArrayList<TaskBean>();
		this.FtpInfoList = new ConcurrentHashMap<String, FtpInfoBean>();
		this.currentConnectMap = new ConcurrentHashMap<String, Long>();
		this.NoOp_ThreadMap = new ConcurrentHashMap<String,FutureTask<Boolean>>();
		this.fdpMap = new HashMap<String,FdPluginInfo>();
		this.compressConmandMap = new HashMap<String,String>();
		this.DESPlusKey = "FD2";
		this.initCompressConmandMap();
		this.initNewDateGis();
		this.setGnLoad(false);
		this.setOprNewDateGisMap(false);
		this.setOprPartFile(false);
		threadPool = Executors.newFixedThreadPool(40);
	}
	
	private void initNewDateGis(){
		setNewDateGis(new ConcurrentHashMap<String, String>());
		newDateGis.put("A", "0");
		newDateGis.put("GN", "0");
	}
	
	private void initCompressConmandMap(){
		compressConmandMap.put(".gz", "gunzip");
		compressConmandMap.put(".z", "uncompress");
	}
	
	public Scheduler getScheduler() {
		return scheduler;
	}

	public synchronized void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public ArrayList<TaskBean> getTaskBeanList() {
		return taskBeanList;
	}

	public void setTaskBeanList(ArrayList<TaskBean> taskBeanList) {
		this.taskBeanList = taskBeanList;
	}

	public SystemBean getSystemBean() {
		return systemBean;
	}

	public void setSystemBean(SystemBean systemBean) {
		this.systemBean = systemBean;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public ConcurrentHashMap<String, FtpInfoBean> getFtpInfoList() {
		return FtpInfoList;
	}

	public void setFtpInfoList(ConcurrentHashMap<String, FtpInfoBean> ftpInfoList) {
		FtpInfoList = ftpInfoList;
	}

	public void setTaskOfWork(TWMonitorBean twBean) {
		this.TWTable.put(twBean.getId(), twBean);		
	}

	public TWMonitorBean getTWMonitorBean(String id) {
		return TWTable.get(id);
	}
	
	public void removeTWMonitorBean(String id) {
		TWTable.remove(id);
	}

	public ConcurrentHashMap<String, TWMonitorBean> getTWTable() {
		return TWTable;
	}
	
	public ConcurrentHashMap<String, FTPOperate> getITTable() {
		return ITTable;
	}
	
	public void setInterruptTask(String taskId,FTPOperate fo) {
		this.ITTable.put(taskId, fo);		
	}
	
	public void removeInterruptTask(String taskId) throws IOException {
		if(this.ITTable.containsKey(taskId)){
			this.ITTable.get(taskId).closeIO();
			this.ITTable.remove(taskId);
		}				
	}

	public void setFdpMap(String pluginId,FdPluginInfo fdp) {
		this.fdpMap.put(pluginId, fdp);
	}

	public HashMap<String,FdPluginInfo> getFdpMap() {
		return fdpMap;
	}

	public void setDESPlusKey(String dESPlusKey) {
		DESPlusKey = dESPlusKey;
	}

	public String getDESPlusKey() {
		return DESPlusKey;
	}

	public HashMap<String,String> getCompressConmandMap() {
		return compressConmandMap;
	}

	public ConcurrentHashMap<String, Long> getCurrentConnectMap() {
		return currentConnectMap;
	}

	public void setCurrentConnectMap(
			ConcurrentHashMap<String, Long> currentConnectMap) {
		this.currentConnectMap = currentConnectMap;
	}

	public void setGnLoad(boolean gnLoad) {
		this.gnLoad = gnLoad;
	}

	public boolean isGnLoad() {
		return gnLoad;
	}

	public void setNewDateGis(ConcurrentHashMap<String,String> newDateGis) {
		this.newDateGis = newDateGis;
	}

	public ConcurrentHashMap<String,String> getNewDateGis() {
		return newDateGis;
	}

	public void setOprNewDateGisMap(boolean oprNewDateGisMap) {
		this.oprNewDateGisMap = oprNewDateGisMap;
	}

	public boolean isOprNewDateGisMap() {
		return oprNewDateGisMap;
	}

	public void setOprPartFile(boolean oprPartFile) {
		this.oprPartFile = oprPartFile;
	}

	public boolean isOprPartFile() {
		return oprPartFile;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	public ExecutorService getThreadPool() {
		return threadPool;
	}
}
