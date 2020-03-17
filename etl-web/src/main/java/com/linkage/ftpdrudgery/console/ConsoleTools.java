package com.linkage.ftpdrudgery.console;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.ReturnBean;
import com.linkage.ftpdrudgery.bean.SystemBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;

/**
 * 控制台工具
 * @author run[zhangqi@lianchuang.com]
 * 2:23:18 AM May 13, 2009
 */

public class ConsoleTools {
	
	private static Logger logger = Logger.getLogger(ConsoleTools.class.getName());
	
	public static IFtpdrudgeryConsole ic;
			
	/**
	 * 建立连接
	 */
	public static void connectRMI(){
		try {
			ic = null;
			ic = (IFtpdrudgeryConsole)Naming.lookup(GlobalBean.ConsoleUrl);
		} catch (MalformedURLException e) {
			logger.error("链接控制台异常", e);
		} catch (RemoteException e) {
			logger.error("链接控制台异常", e);
		} catch (NotBoundException e) {
			logger.error("链接控制台异常", e);
		}
	}
	
	/**
	 * 建立连接(服务器)
	 */
	public static List<IFtpdrudgeryConsole> connectRMIMulti(){
		List<IFtpdrudgeryConsole> li = new ArrayList<IFtpdrudgeryConsole>();
		for(String consoleUrl : GlobalBean.ConsoleUrlList.split(";")){
			IFtpdrudgeryConsole icMulti = null;
			try {
				icMulti = (IFtpdrudgeryConsole)Naming.lookup(consoleUrl);
			} catch (Exception e) {
			}
			if(icMulti != null){
				li.add(icMulti);
			}			
		}
		return li;
	}
	
	
	/**
	 * 更新任务
	 * @param taskBean
	 * @param save
	 * @return
	 */
	public static ReturnBean updateTask(TaskBean taskBean, boolean save){
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb =  ic.updateTask(taskBean, save);
		} catch (RemoteException e) {
			logger.error("远程调用更新任务异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}
		return rb;
		
	}
	
	/**
	 * 远程调用queryTask
	 */
	public static List<TaskBean> queryTask(String qryKey,int qryType){		
		List<TaskBean> jobs = new ArrayList<TaskBean>();
		try {
			connectRMI();
			jobs = ic.queryTask(qryKey,qryType);
		} catch (RemoteException e) {
			logger.error("远程调用queryTask异常");
			throw new RuntimeException(e);
		}	
		return jobs;
	}
	
	
	/**
	 * 远程新增任务
	 * @param taskBean
	 * @param save
	 * @return
	 */
	public static ReturnBean addTask(TaskBean taskBean, boolean save){
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb =  ic.addTask(taskBean, save);
		} catch (RemoteException e) {
			logger.error("远程调用新增任务异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}
		return rb;
	}
	
	
	/**
	 * 远程调用恢复任务
	 */
	public static ReturnBean resumeJob(String jobName){	
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb =  ic.resumeJob(jobName);
		} catch (RemoteException e) {
			logger.error("远程调用恢复任务异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}
		return rb;
	}
	
	/**
	 * 远程调用强行终止任务
	 */
	public static ReturnBean interruptJob(String jobName){	
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb =  ic.interruptJob(jobName);
		} catch (RemoteException e) {
			logger.error("远程调用强行终止任务异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}
		return rb;
	}
		
	/**
	 * 远程调用暂停任务
	 */
	public static ReturnBean pauseJob(String jobName){	
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb =  ic.pauseJob(jobName);
		} catch (RemoteException e) {
			logger.error("远程调用暂停任务异常");
			throw new RuntimeException(e);
		}
		return rb;
	}
	
	
	/**
	 * 远程调用returnTaskList
	 */
	public static List<TaskBean> returnTaskList(){		
		List<TaskBean> jobs = new ArrayList<TaskBean>();
		try {
			connectRMI();
			jobs = ic.returnTaskList();
		} catch (RemoteException e) {
			logger.error("远程调用returnTaskList异常");
			throw new RuntimeException(e);
		}	
		return jobs;
	}
	
	/**
	 * 远程调用qryTaskList
	 */
	public static List<TaskBean> qryTaskList(String qry,String qryKey){
		List<TaskBean> jobs = new ArrayList<TaskBean>();
		try {
			for(IFtpdrudgeryConsole ic_ : connectRMIMulti()){
				jobs.addAll(ic_.queryTask(qry, Integer.parseInt(qryKey)));
			}
		} catch (RemoteException e) {
			logger.error("远程调用queryTask异常");
			throw new RuntimeException(e);
		}	
		return jobs;
	}
	
	
	/**
	 * 远程调用returnFtpInfoList
	 */
	public static List<FtpInfoBean> returnFtpInfoList(){		
		List<FtpInfoBean> fiList = new ArrayList<FtpInfoBean>();
		try {
			connectRMI();
			fiList = ic.returnFtpInfoList();
		} catch (RemoteException e) {
			logger.error("远程调用returnFtpInfoList异常");
			throw new RuntimeException(e);
		}	
		return fiList;
	}
	
	/**
	 * 远程调用暂停所有任务
	 */
	public static ReturnBean pauseAll(){		
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.pauseAll();
		} catch (RemoteException e) {
			logger.error("远程调用暂停所有任务");
			throw new RuntimeException(e);
		}	
		return rb;
	}
	
	/**
	 * 远程调用恢复所有任务
	 */
	public static ReturnBean resumeAll(){		
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.resumeAll();
		} catch (RemoteException e) {
			logger.error("远程调用恢复所有任务", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}	
		return rb;
	}
	
	/**
	 * 远程调用删除任务
	 */
	public static ReturnBean deleteJob(String jobName, boolean save){		
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.deleteJob(jobName, save);
		} catch (RemoteException e) {
			logger.error("远程调用删除任务", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}	
		return rb;
	}
	
	/**
	 * 远程调用立即执行任务
	 */
	public static ReturnBean triggerJob(String jobName){		
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.triggerJob(jobName);
		} catch (RemoteException e) {
			logger.error("程调用立即执行任务", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}	
		return rb;
	}
	
	/**
	 * 删除FtpInfo
	 */
	public static ReturnBean delFtpInfo(String id){		
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.delFtpInfo(id);
		} catch (RemoteException e) {
			logger.error("删除FtpInfo异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}	
		return rb;
	}
	
	/**
	 * 获取系统信息
	 * @return
	 */
	public static SystemBean getSystemBean(){
		SystemBean sb = new SystemBean();
		try {
			connectRMI();
			sb = ic.getSystemBean();
		} catch (RemoteException e) {
			logger.error("获取系统信息异常");
			throw new RuntimeException(e);
		}	
		return sb;
	}
	
	/**
	 * 更新系统信息
	 * @param sb
	 */
	public static ReturnBean updateSystem(SystemBean sb){
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.updateSystem(sb);
		} catch (RemoteException e) {
			logger.error("更新系统信息异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}	
		return rb;
	}
	
	/**
	 * 返回当前正在运行的任务队列监控信息
	 * @param sb
	 */
	public static ArrayList<TWMonitorBean> getMonitorOfCurrentlyExecutingJobs(){
		ArrayList<TWMonitorBean> al = new ArrayList<TWMonitorBean>();
		try {
			connectRMI();
			al = ic.getMonitorOfCurrentlyExecutingJobs();
		} catch (RemoteException e) {
			logger.error("返回当前正在运行的任务队列监控信息", e);
			throw new RuntimeException(e);
		}	
		return al;
	}
	
	/**
	 * 返回任务队列历史监控信息(DB2)
	 * @param sb
	 */
	public static List<TWMonitorBean> getMonitorOfHistorJobs(String qryKey,String qry, String taskStatus,String taskDate){
		List<TWMonitorBean> list= new ArrayList<TWMonitorBean>();
		try {
			connectRMI();
			list = ic.getMonitorOfHistorJobs(qryKey,qry,taskStatus,taskDate);
		} catch (RemoteException e) {
			logger.error("返回任务队列历史监控信息(DB2)", e);
			throw new RuntimeException(e);
		}	
		return list;
	}
	
	
	public static List<FdPluginInfo> getFdPluginInfoList(FdPluginInfo fdp){
		List<FdPluginInfo> list= new ArrayList<FdPluginInfo>();
		try {
			connectRMI();
			list = ic.getFdPluginInfo(fdp);
		} catch (RemoteException e) {
			logger.error("返回任务插件信息队列异常", e);
			throw new RuntimeException(e);
		}	
		return list;
	}
	
	public static ReturnBean updateFdPluginInfo(FdPluginInfo fdp){
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.updateFdPluginInfo(fdp);
		} catch (RemoteException e) {
			logger.error("更新插件信息异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}	
		return rb;
	}
	
	public static ReturnBean delFdPluginInfo(FdPluginInfo fdp){		
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb = ic.delFdPluginInfo(fdp);
		} catch (RemoteException e) {
			logger.error("远程调用删除插件异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}	
		return rb;
	}
	
	public static ReturnBean addFdPluginInfo(FdPluginInfo fdp){
		ReturnBean rb = new ReturnBean();
		try {
			connectRMI();
			rb =  ic.addFdPluginInfo(fdp);
		} catch (RemoteException e) {
			logger.error("远程调用新增插件信息异常", e);
			rb.setReturnInfo(e.toString());
			return rb;
		}
		return rb;
	}
}
