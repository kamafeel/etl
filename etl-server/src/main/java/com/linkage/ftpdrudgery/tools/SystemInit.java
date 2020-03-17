package com.linkage.ftpdrudgery.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ftpDrudgery.ftpInfo.FtpListDocument;
import com.ftpDrudgery.ftpInfo.FtpListDocument.FtpList.FtpInfo;
import com.ftpDrudgery.ftpSystem.FtpSystemDocument;
import com.ftpDrudgery.ftpSystem.FtpSystemDocument.FtpSystem;
import com.ftpDrudgery.ftpSystem.FtpSystemDocument.FtpSystem.FtpConsole;
import com.ftpDrudgery.ftpTask.FtpDrudgeryListDocument;
import com.ftpDrudgery.ftpTask.FtpDrudgeryListDocument.FtpDrudgeryList.FtpDrudgery;
import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.SystemBean;
import com.linkage.ftpdrudgery.bean.TaskBean;

/**
 * 系统初始化系统
 * @author run
 *
 */
public class SystemInit {
	
	private static Logger logger = LoggerFactory.getLogger(SystemInit.class);
	
	private static SystemInit SINGLE = new SystemInit();
	
	private GlobalBean gb;
	
	public static synchronized SystemInit getInstance() {

		if (SINGLE == null) {
			SINGLE = new SystemInit();			
		}
		return SINGLE;
	}
	
	/**
	 * 系统初始化
	 */
	private SystemInit(){		
		gb = GlobalBean.getInstance();
		
		this.getConfigPath();	
		this.initSystemAndConsole();

		this.initFtpInfo();
		this.initTaskList();
	}
		
	/**
	 * 初始化系统内部参数
	 */
	private void initSystemAndConsole(){
			
		try {
			//解析系统信息		
			FtpSystemDocument doc = FtpSystemDocument.Factory.parse(this.getConfigFile(GlobalBean.Config_System));
			FtpSystem fs = doc.getFtpSystem();
			FtpConsole fc = fs.getFtpConsole();
			
			//填充SystemBean
			SystemBean sb = new SystemBean();
			sb.setDownLoadTempDir(fs.getDownLoadTempDir());
			sb.setBackUpDir(fs.getBackUpDir());
			sb.setUploadPostfix(fs.getUploadPostfix());
			sb.setUrl(fc.getConsoleUrl());
			sb.setConsoleStart(fc.getConsoleStart().equalsIgnoreCase("true"));
			sb.setRecordMonitor(fs.getIsRecordMonitor().equalsIgnoreCase("true"));
			gb.setSystemBean(sb);
			
		} catch (XmlException e) {
			logger.error("解析{}异常",GlobalBean.Config_System, e);
		} catch (IOException e) {
			logger.error("解析{}异常",GlobalBean.Config_System, e);
		}
		
	}
	
	/**
	 * 初始化任务对列
	 */
	private void initTaskList(){
		
		try {
			//解析任务信息
			FtpDrudgeryListDocument doc = FtpDrudgeryListDocument.Factory.parse(this.getConfigFile(GlobalBean.Config_FtpTask));
			FtpDrudgery[] fdList = doc.getFtpDrudgeryList().getFtpDrudgeryArray();
			
			//初始化FtpInfo
			ArrayList<TaskBean> taskBeanList  = gb.getTaskBeanList();
			taskBeanList.clear();
			ConcurrentHashMap<String, FtpInfoBean> FtpInfoList = gb.getFtpInfoList();
			
			//填塞taskBeanList
			for (FtpDrudgery fd : fdList) {
				String id = fd.getId();
				TaskBean taskBean = new TaskBean();
				
				//校验
				if(this.checkTaskBean(fd)){
					taskBean.setId(id);
					taskBean.setTaskName(fd.getTaskName());
					taskBean.setSourceDir(fd.getSourceDir());
					taskBean.setDestDir(fd.getDestDir());
					if(!CommonTool.checkNull(fd.getSourceFtp())){
						taskBean.setSourceFtp(FtpInfoList.get(fd.getSourceFtp()));
					}
					if(!CommonTool.checkNull(fd.getDestFtp())){
						taskBean.setDestFtp(FtpInfoList.get(fd.getDestFtp()));
					}					
					taskBean.setRegExp(fd.getRegExp());
					taskBean.setDelete(fd.getIsDelete().equalsIgnoreCase("true"));
					taskBean.setCronTrigger(fd.getCronTrigger());
					taskBean.setBackUp(fd.getIsBackUp());
					taskBean.setBackUpDir(fd.getBackUpDir());
					taskBean.setCheckSleepTime(fd.getCheckSleepTime());
					taskBean.setPluginName(fd.getPluginName());
					taskBean.setPluginPath(fd.getPluginPath());
					taskBean.setPluginClassPath(fd.getPluginClassPath());
					taskBean.setPluginId(fd.getPluginId());
					//modify by run 2010.1.4
					taskBean.setBeforeDay(fd.getBeforeDay());
					taskBean.setRecordValidDay(fd.getRecordValidDay());
					taskBeanList.add(taskBean);
				}else{
					logger.info("任务id:{}信息不完整",id);
				}
			}
			gb.setTaskBeanList(taskBeanList);
		} catch (XmlException e) {
			logger.error("解析{}异常", GlobalBean.Config_FtpTask, e);
		} catch (IOException e) {
			logger.error("解析{}异常", GlobalBean.Config_FtpTask, e);
		}
		
	}
	
	/**
	 * 校验任务
	 * @param fd
	 * @return
	 */
	public boolean checkTaskBean(FtpDrudgery fd){
		if(CommonTool.checkNull(fd.getId()) ||
				//CommonalityTool.checkNull(fd.getSourceDir()) ||
				//CommonalityTool.checkNull(fd.getDestDir()) ||
				CommonTool.checkNull(fd.getRegExp()) ||
				CommonTool.checkNull(fd.getCronTrigger()) ||
				CommonTool.checkNull(fd.getIsDelete()) ||
				CommonTool.checkNull(fd.getTaskName()) ||
				CommonTool.checkNull(fd.getBackUpDir()) ||
				CommonTool.checkNull(fd.getCheckSleepTime()) ||
				//add by run 2010.1.4
				CommonTool.checkNull(fd.getBeforeDay())
				){
			return false;
		}	
		return true;		
	}
	
	/**
	 * 校验FTP信息
	 * @param fi
	 * @return
	 */
	public boolean checkFtpInfoBean(FtpInfo fi){
		if(CommonTool.checkNull(fi.getId()) ||
				CommonTool.checkNull(fi.getRemoteIP()) ||
				CommonTool.checkNull(fi.getPort()) ||
				CommonTool.checkNull(fi.getUsername()) ||
				CommonTool.checkNull(fi.getPassword()) ||
				CommonTool.checkNull(fi.getEncoding()) ||
				CommonTool.checkNull(fi.getTransfersType()) ||
				CommonTool.checkNull(fi.getWorkType()) ||
				CommonTool.checkNull(fi.getRetryCount()) ||
				CommonTool.checkNull(fi.getRetryInterval()) ||
				CommonTool.checkNull(fi.getTimeout())
				){
			return false;
		}	
		return true;		
	}
	
	/**
	 * 任务异常处理
	 * @param content
	 * @param length
	 */
	/*
	private void systemExit(String content, String id){	
		logger.warn("任务Id:" + id + "的元素[" + content + "]为空,程序忽略此任务!");
		//System.exit(-2);
	}*/
	
	/**
	 * 初始化FtpInfo信息
	 */
	private void initFtpInfo(){	
		try {
			//解析配置文件
			FtpListDocument doc = FtpListDocument.Factory.parse(this.getConfigFile(GlobalBean.Config_FtpInfo));
			FtpInfo[] ftpList = doc.getFtpList().getFtpInfoArray();

			//获取全局中的FtpInfo,并清空
			ConcurrentHashMap<String, FtpInfoBean> FtpInfoList = gb.getFtpInfoList();
			FtpInfoList.clear();
			//FTP连接数量控制
			ConcurrentHashMap<String, Long> ccm = gb.getCurrentConnectMap();
			ccm.clear();
			//填充此HashTable
			for(FtpInfo fi : ftpList){
				if(this.checkFtpInfoBean(fi)){
					FtpInfoBean fb = new FtpInfoBean();
					fb.setId(fi.getId());
					fb.setRemoteIP(fi.getRemoteIP());
					fb.setPort(Integer.parseInt(fi.getPort()));
					fb.setUserName(fi.getUsername());
					fb.setPassWord(fi.getPassword());
					fb.setTransfersType(fi.getTransfersType());
					fb.setEncoding(fi.getEncoding());
					fb.setWorkType(fi.getWorkType());
					fb.setRetryInterval(Long.parseLong(fi.getRetryInterval()));
					fb.setRetryCount(Integer.parseInt(fi.getRetryCount()));
					fb.setMaxConnect(Long.parseLong(fi.getMaxConnect()));
					fb.setTimeout(Integer.parseInt(fi.getTimeout()));
					fb.setNoOp(fi.getIsNoOp().equalsIgnoreCase("true"));
					fb.setRBD(fi.getIsRetryBrokenDownloads().equalsIgnoreCase("true"));
					FtpInfoList.put(fi.getId(), fb);
					if(fb.getMaxConnect() > 0l){
						ccm.put(fi.getId(), 0l);
					}					
				}			
			}
			//保存FtpInfo队列
			gb.setFtpInfoList(FtpInfoList);
		} catch (XmlException e) {
			logger.error("解析{}异常",GlobalBean.Config_FtpInfo, e);
		} catch (IOException e) {
			logger.error("解析{}异常",GlobalBean.Config_FtpInfo, e);
		}
	}
	
	
	/**
	 * 获取配置文件
	 * @param configName
	 * @return
	 */
	private File getConfigFile(String configName){
		
		File cf = new File(gb.getConfigPath() + configName);
		if(!cf.isFile()){
			logger.error("配置文件{}没有找到,程序终止", cf.getAbsolutePath());
			System.exit(-1);
		}
		return cf;
	}
	
	/**
	 * 获取配置文件路径
	 */
	private void getConfigPath(){
		
		URL configURL = this.getClass().getProtectionDomain().getCodeSource().getLocation();
		
		String configPath = configURL.toString();

		//去掉头元素
		int index = configPath.indexOf(File.separator);
		if(index == -1){
			index = configPath.indexOf("/");
		}
		configPath = configPath.substring(index + 1, configPath.length());

		//避免jar名字
		index = configPath.lastIndexOf(File.separator);
		if(index == -1){
			index = configPath.lastIndexOf("/");
		}
		configPath = configPath.substring(0, index) + "/../config/";
		
		//可以用URI操作,懒的改了
		if((!System.getProperty("os.name").startsWith("Windows")) && (!configPath.contains(":"))){
			configPath = File.separator + configPath;
		}
		//保存配置文件路径到全局变量中		
		gb.setConfigPath(configPath);
	}
	
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		SystemInit.getInstance();
		long end = System.currentTimeMillis();
		logger.info("costs = " + (end - start));
	}
}
