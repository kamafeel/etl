package com.linkage.ftpdrudgery.console;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.ReturnBean;
import com.linkage.ftpdrudgery.bean.SystemBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;


/**
 * FTP苦工控制台接口
 * @author run[zhangqi@lianchuang.com]
 * 4:23:01 PM May 8, 2009
 */
public interface IFtpdrudgeryConsole extends Remote{
	
	/**
	 * 返回调度器中任务队列
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<TaskBean> returnTaskList() throws RemoteException;
	
	/**
	 * 查询任务
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<TaskBean> queryTask(String qryKey,int qryType) throws RemoteException;
	
	/**
	 * 返回配置文件中已有的Ftp配置
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<FtpInfoBean> returnFtpInfoList() throws RemoteException;
	
	/**
	 * 返回当前正在运行的任务队列
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<TaskBean> getCurrentlyExecutingJobs() throws RemoteException;
	
	/**
	 * 增加任务
	 * @param taskBean
	 * @param save
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean addTask(TaskBean taskBean, boolean save) throws RemoteException;
	
	/**
	 * 更新任务
	 * @param taskBean
	 * @param save
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean updateTask(TaskBean taskBean, boolean save) throws RemoteException;
	
	/**
	 * 查询任务
	 * @param taskName
	 * @return
	 * @throws RemoteException
	 */
	public TaskBean queryTask(String taskName) throws RemoteException;
	
	/**
	 * 删除任务
	 * @param jobName
	 * @param save
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean deleteJob(String jobName, boolean save) throws RemoteException;
	
	/**
	 * 强行终止任务
	 * @param jobName
	 * @param save
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean interruptJob(String jobName) throws RemoteException;
	
	/**
	 * 暂停任务
	 * @param jobName
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean pauseJob(String jobName) throws RemoteException;
	
	/**
	 * 暂停所有任务
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean pauseAll() throws RemoteException;
	
	/**
	 * 恢复任务
	 * @param jobName
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean resumeJob(String jobName) throws RemoteException;
	
	/**
	 * 恢复所有任务
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean resumeAll() throws RemoteException;
	
	/**
	 * 立即执行任务
	 * @param jobName
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean triggerJob(String jobName) throws RemoteException;
	
	/**
	 * 删除FtpInfo
	 * @param id
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean delFtpInfo(String id) throws RemoteException;
	
	/**
	 * 更新系统信息
	 * @param sb
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean updateSystem(SystemBean sb) throws RemoteException;
	
	/**
	 * 获取系统信息
	 * @return
	 * @throws RemoteException
	 */
	public SystemBean getSystemBean() throws RemoteException;
	
	/**
	 * 返回当前正在运行的任务队列监控信息
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<TWMonitorBean> getMonitorOfCurrentlyExecutingJobs() throws RemoteException;
	
	/**
	 * 返回任务队列历史监控信息(DB)
	 * @param qry
	 * @return
	 * @throws RemoteException
	 */
	public List<TWMonitorBean> getMonitorOfHistorJobs(String qryKey,String qry,String taskStatus,String taskDate) throws RemoteException;
	
	public void setHumP(long l,int i) throws RemoteException;
	
	public List<String> getHump() throws RemoteException;
	
	public void setIDKey(String md5,String sha) throws RemoteException;
	
	public void setTk(String tnk) throws RemoteException;
	//public List<String> getSMSInfo() throws RemoteException;
	//public void setKeyWord(String keyWord,String edsKey) throws RemoteException;
	public void initFDPMap() throws RemoteException;
	public ReturnBean delFdPluginInfo(FdPluginInfo fdp) throws RemoteException;
	public ReturnBean updateFdPluginInfo(FdPluginInfo fdp) throws RemoteException;
	public List<FdPluginInfo> getFdPluginInfo(FdPluginInfo fdp) throws RemoteException;
	public ReturnBean addFdPluginInfo(FdPluginInfo fdp) throws RemoteException;
	
}
