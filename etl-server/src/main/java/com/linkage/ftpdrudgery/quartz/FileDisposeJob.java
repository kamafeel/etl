package com.linkage.ftpdrudgery.quartz;

import java.io.IOException;
import java.util.Date;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.db.fd.DBOperation_Fd;
import com.linkage.ftpdrudgery.fdenum.TaskStatusEnum;
import com.linkage.ftpdrudgery.flow.DisposeFile;
import com.linkage.ftpdrudgery.flow.ProduceFile;
import com.linkage.ftpdrudgery.tools.CommonTool;
/**
 * 文件处理job
 * 
 * @author run
 * 
 */

public class FileDisposeJob implements InterruptableJob {

	private static Logger logger = LoggerFactory.getLogger(FileDisposeJob.class);
	private GlobalBean gb;
	private TaskBean tb;
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		tb = (TaskBean) context.getJobDetail().getJobDataMap().get(GlobalBean.TaskBean);
		String jobName = CommonTool.getJobName(tb);
		gb = GlobalBean.getInstance();
		TWMonitorBean twb = null;
		try {
			/* 避免任务重叠执行 */
			if (gb.getTWMonitorBean(tb.getId()) != null) {
				logger.warn("任务[{}]正在运行中...本次跳出,等待下一此触发",jobName);
			}else{
				/* 增加任务状态Bean */
				twb = new TWMonitorBean();
				twb.setId(tb.getId());
				twb.setTaskName(tb.getTaskName());
				twb.setTaskStatus(TaskStatusEnum.TaskStatus_Start.getStatus());
				twb.setStartTime(new Date());				
				gb.setTaskOfWork(twb);
				this.executeflow(tb,twb);
				/* 不包含异常状态,任务状态置为完成 */
				if(!twb.getTaskExp().containsKey(TWMonitorBean.EXP_Exception)){
					twb.setTaskStatus(TaskStatusEnum.TaskStatus_Complete.getStatus());
				}
			}
		} catch (Exception e) {
			logger.error("任务[{}]执行失败:{}", jobName, e);
			logger.error("信息如下:",e);	
			CommonTool.setTaskStatusExc(twb, "任务执行异常:" + e.getMessage());			
		} finally {
			if(gb.getSystemBean().isRecordMonitor() && twb != null){
				logger.warn("任务[{}]需要记录任务状态",jobName);
				try {
					DBOperation_Fd.getInstance().addTWMonitorBean(twb, 100);
				} catch (Exception ex) {
					logger.error("记录任务日志异常:{}",ex.toString());
				}				
			}else{
				logger.warn("任务[{}]不需要记录任务状态,程序忽略",tb.getId());
			}
			//注销任务锁
			if(twb != null){
				gb.removeTWMonitorBean(tb.getId());
			}			
		}
	}
		
	/**
	 * 文件处理方法,2中方式基本定型,就没比要再去使用工厂模式了
	 * 
	 * @param tb,twb
	 * @throws IOException 
	 */
	public void executeflow(TaskBean tb,TWMonitorBean twb) throws Exception {
		// 源路径为空,程序以插件生产文件方式处理
		if (CommonTool.checkNull(tb.getSourceDir())) {
			new ProduceFile(tb,twb).operationLogic();
		} else {
			new DisposeFile(tb,twb).operationLogic();
		}
	}

	public void interrupt() throws UnableToInterruptJobException {
		
		TWMonitorBean twb = gb.getTWMonitorBean(tb.getId());
		
		if(gb.getSystemBean().isRecordMonitor() && twb != null){
			logger.warn("任务[{}]需要记录任务状态",tb.getId());
			CommonTool.setTaskStatusExc(twb, "任务被强行终止");
			try {
				DBOperation_Fd.getInstance().addTWMonitorBean(twb, 100);
			} catch (Exception e) {
				logger.error("记录任务日志异常:{}",e.toString());
			}				
		}else{
			logger.warn("任务[{}]不需要记录任务状态,程序忽略",tb.getId());
		}
		
		//注销任务锁
		gb.removeTWMonitorBean(tb.getId());

		//注销可终止的任务ID
		try {
			gb.removeInterruptTask(tb.getId());
		} catch (IOException e) {
			logger.error("注销可终止的任务ID[{}]异常:",tb.getId(),e);
		}	
	}
}
