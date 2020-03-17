package com.linkage.ftpdrudgery.quartz;

import java.text.ParseException;
import java.util.ArrayList;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.TaskBean;

/**
 * 调度管理者
 * @author run
 *
 */

public class QuartzManager {
	
	private static Logger logger = LoggerFactory.getLogger(QuartzManager.class);
	
	private static QuartzManager SINGLE = new QuartzManager();
	
	/**
	 * 单例模式
	 * @return
	 */
	public static synchronized QuartzManager getInstance() {

		if (SINGLE == null) {
			SINGLE = new QuartzManager();			
		}
		return SINGLE;
	}
	
	private QuartzManager(){
		this.run();
	}
	
	private void run(){
		
		//启动任务调度工厂
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler;
		try {
			scheduler = schedulerFactory.getScheduler();
			
			GlobalBean gb = GlobalBean.getInstance();
			ArrayList<TaskBean> tb = gb.getTaskBeanList();
			MyJobListener ftpTaskJobListener = new MyJobListener();
			for (TaskBean t : tb) {
				String jobName = String.valueOf(t.getId());
				// 细化任务信息
				JobDetail job = new JobDetail(jobName, Scheduler.DEFAULT_GROUP,
						FileDisposeJob.class);
				job.getJobDataMap().put(GlobalBean.TaskBean, t);
				// 任务触发器
				CronTrigger trigger = new CronTrigger(jobName,
						Scheduler.DEFAULT_GROUP, t.getCronTrigger());
				scheduler.scheduleJob(job, trigger);
			}
			// 加入全局Job监听,会占用资源,可以注释掉
			scheduler.addGlobalJobListener(ftpTaskJobListener);
			// 共享调度管理器对象Scheduler
			gb.setScheduler(scheduler);
			// 任务调度启动
			scheduler.start();
		} catch (SchedulerException e) {
			logger.error("任务调度器启动异常",e);
		} catch (ParseException e) {
			logger.error("任务调度器启动异常",e);
		}
	}

	public static void main(String[] args) throws Exception {
		QuartzManager.getInstance();
	}
}
