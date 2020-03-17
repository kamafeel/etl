package com.linkage.ftpdrudgery.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.tools.CommonTool;

/**
 * 任务监听器
 * @author run
 *
 */
public class MyJobListener implements JobListener{
	
	private static Logger logger = LoggerFactory.getLogger(MyJobListener.class);
		
	public String getName() {
		return "FileDisposeJobListener";
	}

	public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
		TaskBean tb = (TaskBean)jobExecutionContext.getJobDetail().getJobDataMap().get(GlobalBean.TaskBean);
		logger.info("任务[{}]执行被否决",CommonTool.getJobName(tb));
	}

	public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
		TaskBean tb = (TaskBean)jobExecutionContext.getJobDetail().getJobDataMap().get(GlobalBean.TaskBean);
		logger.info("任务[{}]开始执行",CommonTool.getJobName(tb));	
	}

	public void jobWasExecuted(JobExecutionContext jobExecutionContext,
			JobExecutionException jobExecutionException) {
		TaskBean tb = (TaskBean)jobExecutionContext.getJobDetail().getJobDataMap().get(GlobalBean.TaskBean);
		logger.info("任务[{}]执行完毕",CommonTool.getJobName(tb));
	}

}
