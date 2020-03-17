package com.linkage.ftpdrudgery.console;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import my.fangshu.mp.nieHaha.ICatchU;

import org.apache.xmlbeans.XmlException;
import org.fs.commons.decider.IamDecider;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.ReturnBean;
import com.linkage.ftpdrudgery.bean.SystemBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.db.fd.DBOperation_Fd;
import com.linkage.ftpdrudgery.db.fd.bean.FdHisMonitorKey;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;
import com.linkage.ftpdrudgery.quartz.FileDisposeJob;
import com.linkage.ftpdrudgery.tools.CommonTool;
import com.linkage.ftpdrudgery.tools.MySort;
import com.linkage.ftpdrudgery.tools.XmlBeanTools;
import com.linkage.intf.tools.DESPlus;
import com.linkage.intf.tools.RandomUtils;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;


/**
 * 控制台具体实现类
 * @author run[zhangqi@lianchuang.com]
 * 4:38:45 PM May 8, 2009
 */

public class IFtpdrudgeryConsoleImpl extends UnicastRemoteObject implements IFtpdrudgeryConsole {
	
	private static Logger logger = LoggerFactory.getLogger(IFtpdrudgeryConsoleImpl.class);
	
	private static final long serialVersionUID = -989757340568763841L;
	
	/* 静态同步锁 */
	private static Object Lock = new Object();
	/* XML操作工具类 */
	private XmlBeanTools xbt;
	/* 全局信息载体 */
	private GlobalBean gb;
	
	protected IFtpdrudgeryConsoleImpl() throws RemoteException {
		super();
		xbt = XmlBeanTools.getInstance();
		gb = GlobalBean.getInstance();
	}
	
	/**
	 * 增加任务
	 * @param taskBean
	 * @param save
	 * @return
	 */
	public ReturnBean addTask(TaskBean taskBean, boolean save) throws RemoteException {
		
		logger.debug("增加任务接口被调用,永久保存标志为:{}",save);
		//此操作需要同步
		synchronized (Lock) {
			try {
				if(!IamDecider.getInstance().decide(taskBean)){
					this.buildReturnBean(true, "新增任务成功,任务ID：" + taskBean.getId());
				}
				//设置新建任务ID
				String id = String.valueOf(this.getNewTaskId());
				taskBean.setId(id);
				if(!this.checkPlugin(taskBean)){
					taskBean.setPluginId(this.getPluginId());
				}
				int newFtpInfoId = this.getNewFtpInfoId();
				//设置新来源FTP信息ID
				if(taskBean.getSourceFtp() != null &&
						CommonTool.checkNull(taskBean.getSourceFtp().getId())){
					taskBean.getSourceFtp().setId(String.valueOf(newFtpInfoId));
					newFtpInfoId++;
				}
				//设置新目标FTP信息ID
				if(taskBean.getDestFtp() != null && 
						CommonTool.checkNull(taskBean.getDestFtp().getId())){
					taskBean.getDestFtp().setId(String.valueOf(newFtpInfoId));
				}
				//获取全局调度控制
				Scheduler sch = gb.getScheduler();
				// 细化任务信息
				JobDetail job = new JobDetail(id, Scheduler.DEFAULT_GROUP, FileDisposeJob.class);
				job.getJobDataMap().put(GlobalBean.TaskBean, taskBean);
				// 任务触发器
				CronTrigger trigger = new CronTrigger(id, Scheduler.DEFAULT_GROUP, taskBean.getCronTrigger());
				//装载任务
				sch.scheduleJob(job, trigger);
				//保存改变的Scheduler
				gb.setScheduler(sch);
				//是否保存配置文件
				if(save){
					xbt.addTaskConfig(taskBean);
				}
			} catch (ParseException e) {
				logger.error("增加任务异常", e);
				return this.buildReturnBean(false, "新增任务异常：" + e.toString());
			} catch (SchedulerException e) {
				logger.error("增加任务异常", e);
				return this.buildReturnBean(false, "新增任务异常：" + e.toString());
			} catch (Exception e){
				logger.error("增加任务异常", e);
				return this.buildReturnBean(false, "新增任务异常：" + e.toString());
			}
		}		
		return this.buildReturnBean(true, "新增任务成功,任务ID：" + taskBean.getId());
	}
	
	/**
	 * 根据任务名称查询任务信息
	 */
	public TaskBean queryTask(String taskName) throws RemoteException {
		logger.debug("{}任务信息被查询", taskName);
		TaskBean tb = null;
		try {
			Scheduler sch = gb.getScheduler();
			JobDetail jd = sch.getJobDetail(taskName, Scheduler.DEFAULT_GROUP);
			tb = (TaskBean)jd.getJobDataMap().get(GlobalBean.TaskBean);
		} catch (SchedulerException e) {
			logger.error("查询{}任务信息接口异常", taskName, e);
		}
		return tb;
	}
	
	/**
	 * 查询任务信息
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public ArrayList<TaskBean> queryTask(String qryKey,int qryType) throws RemoteException {
		logger.debug("查询任务信息接口被调用");
		ArrayList<TaskBean> tbList = new ArrayList<TaskBean>();
		try {
			String[] taskIds = xbt.qryTask(qryKey, qryType);
			for(String taskId : taskIds){
				if(!StringUtils.isEmpty(taskId)){
					tbList.add(this.queryTask(taskId));
				}				
			}
		} catch (Exception e) {
			logger.error("查询任务信息接口异常", e);
		}
		return sortArrayList(tbList);
	}
	
	/**
	 * 获取任务信息队列
	 */
	public ArrayList<TaskBean> returnTaskList() throws RemoteException {
		logger.debug("查询所有任务信息接口被调用");
		ArrayList<TaskBean> tbList = new ArrayList<TaskBean>();
		try {
			Scheduler sch = gb.getScheduler();
			String[] jobNames = sch.getJobNames(Scheduler.DEFAULT_GROUP);
			for(String id : jobNames){
				JobDetail jd = sch.getJobDetail(id, Scheduler.DEFAULT_GROUP);
				TaskBean tb = (TaskBean)jd.getJobDataMap().get(GlobalBean.TaskBean);
				//查询任务状态
				tb.setState(this.getJobState(id));
				tbList.add(tb);
			}
		} catch (SchedulerException e) {
			logger.error("查询所有任务信息接口异常", e);
		}
		return sortArrayList(tbList);
	}
	
	/**
	 * 更新任务信息(异常后回滚)
	 */
	public ReturnBean updateTask(TaskBean taskBean, boolean save) throws RemoteException {
		logger.debug("{}将被更新,永久保存标志为:{}",CommonTool.getJobName(taskBean),save);		
		Scheduler old = null;
		// 此处需要同步
		synchronized (Lock) {
			try {
				if(!IamDecider.getInstance().decide(taskBean)){
					this.buildReturnBean(true, "新增任务成功,任务ID：" + taskBean.getId());
				}
				//在内存中保存原信息,以便发生异常时回滚
				old = gb.getScheduler();
				String id = String.valueOf(taskBean.getId());
				// 先删除任务,但不保存
				ReturnBean rb = this.deleteJob(id, false);
				// 删除任务无异常,增加任务
				if (rb.getReturnCode() == 0) {					
					//设置新来源FTP信息ID
					int newFtpInfoId = this.getNewFtpInfoId();
					if(taskBean.getSourceFtp() != null &&
							CommonTool.checkNull(taskBean.getSourceFtp().getId())){
						taskBean.getSourceFtp().setId(String.valueOf(newFtpInfoId));						
						newFtpInfoId++;
					}
					//设置新目标FTP信息ID
					if(taskBean.getDestFtp() != null &&
							CommonTool.checkNull(taskBean.getDestFtp().getId())){
						taskBean.getDestFtp().setId(String.valueOf(newFtpInfoId));
					}
					JobDetail job = new JobDetail(id, Scheduler.DEFAULT_GROUP, FileDisposeJob.class);
					job.getJobDataMap().put(GlobalBean.TaskBean, taskBean);
					CronTrigger trigger = new CronTrigger(id, Scheduler.DEFAULT_GROUP, taskBean.getCronTrigger());
					Scheduler sch = gb.getScheduler();
					sch.scheduleJob(job, trigger);
					gb.setScheduler(sch);
					// 是否保存配置文件
					if (save) {
						xbt.updateTaskConfig(taskBean);
					}
					return this.buildReturnBean(true, "[" + taskBean.getId() + "]任务更新成功,永久保存标志为:" + save);
				}else{
					return this.buildReturnBean(true, "[" + taskBean.getId() + "]任务更新失败(删除原任务时发生异常),永久保存标志为:" + save);
				}
			} catch (ParseException e) {
				gb.setScheduler(old);
				logger.error("更新任务异常", e);
				return this.buildReturnBean(false, "更新[" + taskBean.getId()+ "]任务失败,已回滚原任务信息,异常：" + e.toString());
			} catch (SchedulerException e) {
				gb.setScheduler(old);
				logger.error("更新任务异常", e);
				return this.buildReturnBean(false, "更新[" + taskBean.getId()+ "]任务失败,已回滚原任务信息,异常：" + e.toString());
			} catch (Exception e) {
				gb.setScheduler(old);
				logger.error("更新任务异常", e);
				return this.buildReturnBean(false, "更新[" + taskBean.getId()+ "]任务失败,已回滚原任务信息,异常：" + e.toString());
			}			
		}	
	}
		
	/**
	 * 删除任务
	 */
	public ReturnBean deleteJob(String jobName, boolean save) throws RemoteException {
		logger.debug("{}将被删除,永久保存标志为:{}",jobName,save);
		//此处需要同步
		synchronized (Lock){
			boolean tag = false;
			try {				
				Scheduler sch = gb.getScheduler();
				tag = sch.deleteJob(jobName, Scheduler.DEFAULT_GROUP);
				gb.setScheduler(sch);
				//是否保存配置文件
				if(save){
					boolean deltag = xbt.delTaskConfig(jobName);
					if(deltag){
						FdPluginInfo fdp = new FdPluginInfo();
						fdp.setPluginId(jobName);
						fdp.setTaskId(jobName);
						DBOperation_Fd.getInstance().delFdPluginInfo(fdp);
					}
				}
			} catch (SchedulerException e) {	
				logger.error("删除任务异常", e);
				return this.buildReturnBean(false, "删除任务[" + jobName + "]异常:" + e.toString());
			} catch (Exception e){
				logger.error("删除任务异常", e);
				return this.buildReturnBean(false, "删除任务[" + jobName + "]异常:" + e.toString());
			}
			if(tag){
				return this.buildReturnBean(tag, "任务" + jobName + "成功删除,永久保存标志为:" + save);
			}else{
				return this.buildReturnBean(tag, "任务" + jobName + "删除失败,永久保存标志为:" + save);
			}
		}	
	}
	
	/**
	 * 拼装ReturnBean
	 * @param tag
	 * @param explain
	 * @return
	 */
	public ReturnBean buildReturnBean(boolean tag, String explain){
		ReturnBean rb = new ReturnBean();
		if(tag){
			rb.setReturnCode(0);
			rb.setReturnInfo(CommonTool.checkNull(explain) ? "操作成功" : explain);
		}else{
			rb.setReturnCode(1);
			rb.setReturnInfo(CommonTool.checkNull(explain) ? "操作失败" : explain);
		}		
		return rb;	
	}
	
	/**
	 * 暂停任务
	 */
	public ReturnBean pauseJob(String jobName) throws RemoteException {
		logger.debug("{}将被暂停",jobName);
		try {			
			Scheduler sch = gb.getScheduler();
			sch.pauseJob(jobName, Scheduler.DEFAULT_GROUP);
			//回写全局调度
			gb.setScheduler(sch);
			return this.buildReturnBean(true, "任务[" + jobName + "]暂停成功");
		} catch (SchedulerException e) {	
			logger.error("暂停任务异常", e);
			return this.buildReturnBean(false, "任务[" + jobName + "]暂停异常:" + e.toString());
		}
	}
	
	/**
	 * 查询任务状态
	 * @param jobName
	 * @return
	 */
	public String getJobState(String id) throws RemoteException {
		//logger.debug("{}将被查询状态",id);
		//默认等于3,即任务异常
		int state = 3;
		String returnState = "错误";
		try {		
			Scheduler sch = gb.getScheduler();
			state = sch.getTriggerState(id, Scheduler.DEFAULT_GROUP);
			returnState = CommonTool.checkNull(this.translateJobState(state)) ?
					returnState : this.translateJobState(state);
		} catch (SchedulerException e) {	
			logger.error("查询任务状态异常", e);
		}
		return returnState;
	}
	
	/**
	 * 翻译任务状态
	 * @param state
	 * @return
	 */
	public String translateJobState(int state){	
		/*
		 *  STATE_BLOCKED 4 阻塞
			STATE_COMPLETE 2 完成
			STATE_ERROR 3 错误
			STATE_NONE -1 指定查询的[任务]不存在
			STATE_NORMAL 0 正常
			STATE_PAUSED 1 暂停
		 */
		String stateS = "";
		switch (state) {	
			case -1:
				stateS = "指定查询的任务ID[" + state + "]不存在";
				break;
			case 0:
				stateS = "正常";
				break;
			case 1:
				stateS = "暂停";
				break;
			case 2:
				stateS = "完成";
				break;
			case 3:
				stateS = "错误";
				break;
			case 4:
				stateS = "阻塞";
				break;
		}	
		return stateS;
	}
	
	/**
	 * 恢复任务
	 */
	public ReturnBean resumeJob(String jobName) throws RemoteException {
		logger.debug("{}将恢复运行状态",jobName);
		try {			
			Scheduler sch = gb.getScheduler();
			sch.resumeJob(jobName, Scheduler.DEFAULT_GROUP);
			//回写全局调度
			gb.setScheduler(sch);
			return this.buildReturnBean(true, "任务[" + jobName + "]成功恢复运行状态");
		} catch (SchedulerException e) {	
			logger.error("恢复任务异常", e);
			return this.buildReturnBean(false, "任务[" + jobName + "]恢复运行状态异常:" + e.toString());
		}
	}
	
	/**
	 * 强行终止任务
	 */
	public ReturnBean interruptJob(String jobName) throws RemoteException {
		logger.debug("{}将被强行终止",jobName);
		try {
			if(!gb.getITTable().containsKey(jobName)){
				return this.buildReturnBean(false, "任务[" + jobName + "]此时状态不能被强行终止");
			}
			Scheduler sch = gb.getScheduler();
			sch.interrupt(jobName, Scheduler.DEFAULT_GROUP);
			//回写全局调度
			gb.setScheduler(sch);
			return this.buildReturnBean(true, "任务[" + jobName + "]将被强行终止");
		} catch (SchedulerException e) {	
			logger.error("强行终止任务异常", e);
			return this.buildReturnBean(false, "任务[" + jobName + "]将被强行终止时异常:" + e.toString());
		}
	}
	
	/**
	 * 返回当前正在执行的任务(未使用)
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<TaskBean> getCurrentlyExecutingJobs() throws RemoteException {
		logger.debug("查询当前正在运行任务队列接口被调用");
		ArrayList<TaskBean> tbList = new ArrayList<TaskBean>();
		try {		
			Scheduler sch = gb.getScheduler();
			List<JobExecutionContext> cej = sch.getCurrentlyExecutingJobs();
			for(JobExecutionContext jec : cej){
				JobDetail jd = jec.getJobDetail();
				tbList.add((TaskBean)jd.getJobDataMap().get(GlobalBean.TaskBean));
			}
		} catch (SchedulerException e) {
			logger.error("查询当前正在运行任务队列异常", e);
		}
		return sortArrayList(tbList);
	}
	
	/**
	 * 获取新建FtpInfo的ID
	 */
	public int getNewFtpInfoId() throws RemoteException {
		int[] upInt = new int[0];
		try {
			upInt = xbt.getFtpInfoIdList();
		} catch (IOException e) {
			logger.error("操作XML异常",e);
		} catch (XmlException e) {
			logger.error("操作XML异常",e);
		}
		if(upInt.length == 0){
			return 0;
		}else{
			//升序排序
			Arrays.sort(upInt);
			return upInt[upInt.length -1] + 1;
		}		
	}	
	
	/**
	 * 获取新建Task的ID
	 */
	public int getNewTaskId() throws RemoteException {
		int[] upInt = new int[0];
		try {
			upInt = xbt.getTaskIdList();
		} catch (XmlException e) {
			logger.error("操作XML异常",e);
		} catch (IOException e) {
			logger.error("操作XML异常",e);
		}
		if(upInt.length == 0){
			return 0;
		}else{
			//升序排序
			Arrays.sort(upInt);
			return upInt[upInt.length -1] + 1;
		}	
	}
	
	/**
	 * 暂停所有的任务
	 */
	public ReturnBean pauseAll() throws RemoteException {
		logger.debug("暂停所有的任务接口被调用");
		try {			
			Scheduler sch = gb.getScheduler();
			sch.pauseAll();
			//回写全局调度
			gb.setScheduler(sch);
			return this.buildReturnBean(true, "成功暂停所有的任务");
		} catch (SchedulerException e) {	
			logger.error("暂停所有的任务异常", e);
			return this.buildReturnBean(false, "暂停所有的任务异常:" + e.toString());
		}
	}
	
	/**
	 * 恢复所有的任务
	 */
	public ReturnBean resumeAll() throws RemoteException {
		logger.debug("恢复所有的任务接口被调用");
		try {			
			Scheduler sch = gb.getScheduler();
			sch.resumeAll();
			//回写全局调度
			gb.setScheduler(sch);
			return this.buildReturnBean(true, "成功恢复所有的任务");
		} catch (SchedulerException e) {	
			logger.error("恢复所有的任务异常", e);
			return this.buildReturnBean(false, "恢复所有的任务异常:" + e.toString());
		}
	}
	
	/**
	 * 返回现有的FtpInfo List
	 */
	public ArrayList<FtpInfoBean> returnFtpInfoList() throws RemoteException {
		ArrayList<FtpInfoBean> tbList = new ArrayList<FtpInfoBean>();
		try {
			tbList = xbt.getFtpInfoList();
		} catch (IOException e) {
			logger.error("操作XML异常",e);
		} catch (XmlException e) {
			logger.error("操作XML异常",e);
		}		
		return tbList;
	}
	
	/**
	 * 立即执行任务
	 */
	public ReturnBean triggerJob(String jobName) throws RemoteException {
		logger.debug("{}将被立即执行",jobName);
		try {
			Scheduler sch = gb.getScheduler();
			sch.triggerJob(jobName, Scheduler.DEFAULT_GROUP);
			//回写全局调度
			gb.setScheduler(sch);
			return this.buildReturnBean(true, "任务[" + jobName + "]立即执行成功");
		} catch (SchedulerException e) {	
			logger.error("立即执行任务异常", e);
			return this.buildReturnBean(false, "任务[" + jobName + "]立即执行异常:" + e.toString());
		}
	}
	
	/**
	 * 删除FtpInfo
	 * @param id
	 * @return
	 * @throws RemoteException
	 */
	public ReturnBean delFtpInfo(String id) throws RemoteException {
		logger.debug("FtpInfoId:{}将被删除",id);
		try {			
			boolean delTag = xbt.delFtpInfoConfig(id);
			return this.buildReturnBean(delTag, "Ftp配置[" + id + "]删除成功");
		} catch (Exception e) {
			logger.error("删除FtpInfo异常", e);
			return this.buildReturnBean(false, "Ftp配置[" + id + "]删除失败:" + e.toString());
		}
	}
	
	/**
	 * 更新系统信息
	 */
	public ReturnBean updateSystem(SystemBean sb) throws RemoteException {
		logger.debug("更新系统设置接口被调用");
		try {			
			boolean delTag = xbt.updateSystem(sb);
			return this.buildReturnBean(delTag, "系统更新成功");
		} catch (Exception e) {
			logger.error("删除FtpInfo异常", e);
			return this.buildReturnBean(false, "系统更新异常:" + e.toString());
		}
	}
	
	/**
	 * 获取系统信息
	 */
	public SystemBean getSystemBean() throws RemoteException {
		logger.debug("系统信息查询接口被调用");
		SystemBean sb = new SystemBean();
		try {
		sb = gb.getSystemBean();
		} catch (Exception e) {
			logger.error("系统信息查询异常", e);
		}
		return sb;
	}
	
	/**
	 * 对ArrayList<TaskBean> 更具Id大小排序
	 * @param tbl
	 * @return
	 */
	public ArrayList<TaskBean> sortArrayList(ArrayList<TaskBean> tbl){
		Collections.sort(tbl, new MySort());
		return tbl;
	}
	
	/**
	 * 返回当前正在运行的任务队列监控信息
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList<TWMonitorBean> getMonitorOfCurrentlyExecutingJobs() throws RemoteException{
		ArrayList<TWMonitorBean> al = new ArrayList<TWMonitorBean>();
		ConcurrentHashMap<String, TWMonitorBean> ht = gb.getTWTable();
		for (Iterator it = ht.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			TWMonitorBean twb = (TWMonitorBean)e.getValue();
			twb.setCastTime(TimeUtils.formatLong(new Date().getTime()-twb.getStartTime().getTime()));
			al.add((TWMonitorBean)e.getValue());
		}		
		return al;		
	}
	
	/**
	 * 返回任务队列历史监控信息(DB)
	 * @param qry
	 * @return
	 * @throws RemoteException
	 */
	public List<TWMonitorBean> getMonitorOfHistorJobs(String qryKey,String qry,String taskStatus,String taskDate) throws RemoteException{
		FdHisMonitorKey fmk = new FdHisMonitorKey();
		fmk.setTaskStatus(taskStatus);
		fmk.setTaskDate(taskDate);
		if(!StringUtils.isEmpty(qry) && !StringUtils.isEmpty(qryKey)){
			fmk.setSelectKey(qryKey);			
			if("SnId".equalsIgnoreCase(qryKey)){
				fmk.setSnId(qry);
			}else if("Id".equalsIgnoreCase(qryKey)){
				fmk.setTaskId(qry);
			}else if("Name".equalsIgnoreCase(qryKey)){
				fmk.setTaskName("%"+qry+"%");
			}
		}		
		return DBOperation_Fd.getInstance().getTWMonitorBean(fmk);		
	}

	public void setHumP(long l, int i) throws RemoteException {
		IamDecider id = IamDecider.getInstance();
		id.setMaxTime(l);
		id.setMaxDay(i);	
	}

	public List<String> getHump() throws RemoteException {
		IamDecider id = IamDecider.getInstance();
		List<String> l = new ArrayList<String>();
		l.add(String.valueOf(id.getNowTime()));
		l.add(String.valueOf(id.getFirstMaxDay()));
		return l;
	}

	public void setIDKey(String md5,String sha) throws RemoteException {
		IamDecider id = IamDecider.getInstance();
		id.setMd5Hex(md5);
		id.setShaHex(sha);		
	}

	public void setTk(String tnk) throws RemoteException {
		IamDecider id = IamDecider.getInstance();
		id.setConStr(tnk);
	}
	
//	public List<String> getSMSInfo() throws RemoteException {
//		List<String> l = new ArrayList<String>();
//		List<String> lf = ICatchU.getInstance().getFileContent();
//		if(lf != null){
//			l.addAll(lf);
//		}		
//		l.addAll(Arrays.asList(ICatchU.getInstance().toArray()));
//		return l;
//	}
//
//	public void setKeyWord(String keyWord,String edsKey) throws RemoteException {
//		ICatchU.getInstance().setKeyWord(keyWord);
//		ICatchU.getInstance().setEdsKey(edsKey);
//	}

	public void initFDPMap() throws RemoteException {
		logger.debug("插件信息重新加载被调用");
		List<FdPluginInfo> ll = DBOperation_Fd.getInstance().getFdPluginInfo(null);
		GlobalBean.getInstance().getFdpMap().clear();
		for(FdPluginInfo fdp : ll){
			try {
				fdp.setDateOffset(fdp.getDateOffset());
				fdp.setTaskId(fdp.getTaskId());
				if(!StringUtils.isEmpty(fdp.getdB2Statement())){
					fdp.setdB2Statement(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdp.getdB2Statement()));
				}
				if(!StringUtils.isEmpty(fdp.getPerShell())){
					fdp.setPerShell(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdp.getPerShell()));
				}
				fdp.setDb2Environment(fdp.getDb2Environment());
				fdp.setPluginInfo(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdp.getPluginInfo()));
				GlobalBean.getInstance().setFdpMap(fdp.getPluginId(), fdp);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}		
	}

	public ReturnBean delFdPluginInfo(FdPluginInfo fdp) throws RemoteException {
		logger.debug("删除插件配置信息被调用");
		try {			
			int result = DBOperation_Fd.getInstance().delFdPluginInfo(fdp);
			if(result==1 && fdp.getChoiceTaskBean() != null){
				TaskBean tb = fdp.getChoiceTaskBean();
				tb.setPluginId(null);
				ReturnBean rb = this.updateTask(tb,true);
				return this.buildReturnBean(result==1, "删除插件配置信息成功;" + rb.getReturnInfo());
			}
			return this.buildReturnBean(result==1, "删除插件配置信息成功");
		} catch (Exception e) {
			logger.error("删除插件配置信息异常", e);
			return this.buildReturnBean(false, "删除插件配置信息异常:" + e.toString());
		} finally{
			this.initFDPMap();
		}
	}

	public ReturnBean updateFdPluginInfo(FdPluginInfo fdp) throws RemoteException {
		logger.debug("更新插件配置信息被调用");
		try {
			if(!StringUtils.isEmpty(fdp.getdB2Statement())){
				fdp.setdB2Statement(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).encrypt(fdp.getdB2Statement()));
			}
			if(!StringUtils.isEmpty(fdp.getPerShell())){
				fdp.setPerShell(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).encrypt(fdp.getPerShell()));
			}		
			fdp.setPluginInfo(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).encrypt(fdp.getPluginInfo()));
			int result = DBOperation_Fd.getInstance().updateFdPluginInfo(fdp);
			return this.buildReturnBean(result==1, "更新插件配置信息成功");
		} catch (Exception e) {
			logger.error("更新插件配置信息异常", e);
			return this.buildReturnBean(false, "更新插件配置信息异常:" + e.toString());
		} finally{
			this.initFDPMap();
		}
	}

	public List<FdPluginInfo> getFdPluginInfo(FdPluginInfo fdp) throws RemoteException {
		logger.debug("查询插件配置信息被调用");
		List<FdPluginInfo> ll = DBOperation_Fd.getInstance().getFdPluginInfo(fdp);
		List<FdPluginInfo> llr = new ArrayList<FdPluginInfo>();
		for(FdPluginInfo fdpr : ll){
			try {
				if(!StringUtils.isEmpty(fdpr.getdB2Statement())){
					fdpr.setdB2Statement(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdpr.getdB2Statement()));
				}
				if(!StringUtils.isEmpty(fdpr.getPerShell())){
					fdpr.setPerShell(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdpr.getPerShell()));
				}				
				fdpr.setPluginInfo(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdpr.getPluginInfo()));
				if(!StringUtils.isEmpty(fdpr.getTaskId())){
					TaskBean tb = this.queryTask(fdpr.getTaskId());
					if(tb != null){
						fdpr.setChoiceTaskBean(tb);
					}
				}				
				llr.add(fdpr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return llr;
	}

	public ReturnBean addFdPluginInfo(FdPluginInfo fdp) throws RemoteException {
		logger.debug("增加插件配置信息被调用");
		try {			
			fdp.setPluginId(TimeUtils.getCurrentTime("yyyyMMdd") + RandomUtils.randomNumeric(5));
			fdp.setTaskId(fdp.getChoiceTaskBean().getId());
			if(!StringUtils.isEmpty(fdp.getdB2Statement())){
				fdp.setdB2Statement(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).encrypt(fdp.getdB2Statement()));
			}
			if(!StringUtils.isEmpty(fdp.getPerShell())){
				fdp.setPerShell(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).encrypt(fdp.getPerShell()));
			}
			fdp.setPluginInfo(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).encrypt(fdp.getPluginInfo()));
			int result = DBOperation_Fd.getInstance().addFdPluginInfo(fdp);
			if(result==1 && fdp.getChoiceTaskBean() != null){
				TaskBean tb = fdp.getChoiceTaskBean();
				tb.setPluginId(fdp.getPluginId());
				ReturnBean rb = this.updateTask(tb,true);
				return this.buildReturnBean(result==1, "更新插件配置信息成功;" + rb.getReturnInfo());
			}		
			return this.buildReturnBean(result==1, "增加插件配置信息成功");
		} catch (Exception e) {
			logger.error("增加插件配置信息异常", e);
			return this.buildReturnBean(false, "增加插件配置信息异常:" + e.toString());
		} finally{
			this.initFDPMap();
		}
	}
	
	/**
	 * 验证插件信息时候完整
	 * @param tb
	 * @return
	 * @throws Exception 
	 */
	public boolean checkPlugin(TaskBean taskBean){
		if(CommonTool.checkNull(taskBean.getPluginName()) ||
				CommonTool.checkNull(taskBean.getPluginPath()) ||
				CommonTool.checkNull(taskBean.getPluginClassPath())){
			return false;
		}
		return true;
	}
	
	public String getPluginId(){
		return TimeUtils.getCurrentTime("yyyyMMdd") + RandomUtils.randomNumeric(5);
	}
}
