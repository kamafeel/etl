package com.linkage.ftpdrudgery.flow;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.fs.commons.decider.IamDecider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.fdenum.TaskStatusEnum;
import com.linkage.ftpdrudgery.tools.CommonTool;
import com.linkage.ftpdrudgery.tools.FTPOperate;
import com.linkage.ftpdrudgery.tools.MyFileUtil;
import com.linkage.ftpdrudgery.tools.PluginManage;
import com.linkage.ftpdrudgery.tools.TimeProcessor;
import com.linkage.intf.file.FileUtil;
import com.linkage.intf.tools.ArrayUtils;
import com.linkage.intf.tools.RandomUtils;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

/**
 * 文件由对方生成
 * @author run[zhangqi@lianchuang.com]
 * 2:06:54 PM May 26, 2009
 */

public class DisposeFile extends WorkFlow {
	
	private static Logger logger = LoggerFactory.getLogger(DisposeFile.class);
	private Object objLock = new Object();
	/* 回执文件 */
	private File[] returnFiles;	
	/* 下载临时文件夹 */
	private String downTempDir;
	
	private String[] postfixs = {".Z",".gz"};
	
	/*
	 * 动态路径分割符
	 */
	private final static String customPathRegex = "%";
	
	public DisposeFile(TaskBean tb,TWMonitorBean tw) {
		super(tb,tw);
		downTempDir = gb.getSystemBean().getDownLoadTempDir() + 
				File.separator + tb.getId() + File.separator;
	}
	
	/**
	 * 业务逻辑
	 * @throws Exception 
	 */
	public void operationLogic() throws Exception{
		synchronized(objLock) {
			try {
				this.uknowKgMKeastMiKit(5);
				//文件来源处理
				if(tb.getSourceFtp() != null){
					this.downLoadSourceFiles();
				}else{
					this.localSourceFiles();
				}
				//检查是否有文件需要处理
				if(!this.checkSourceFiles()){
					return;
				}
				if(!IamDecider.getInstance().decide(tb)){
					//20150911
//					if(Integer.parseInt(RandomUtils.randomNumeric(1))%2==1){
//						this.optArrays();
//					}
					return;
				}
				//全量备份
				if(tb.isBackUp()){
					backUpSourceFiles();
				}else{
					logger.info("任务[{}]根据配置文件,不需要备份,程序忽略备份过程",CommonTool.getJobName(tb));
				}
				
				//插件模块
				if(checkPlugin()){
					this.plugin();
				}else{
					this.unPlugin();
				}
				//删除原始文件
				this.delSourceFiles();
				//清空下载临时文件
				MyFileUtil.delFilesInDir(downTempDir);
			} catch (UnsupportedEncodingException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			}
		}		
	}
	
	private void uknowKgMKeastMiKit(int i) throws UnsupportedEncodingException{
		if(i==0){
			throw new UnsupportedEncodingException();
		}
	}
	
	/**
	 * 校验原始文件数量
	 * @return
	 */
	public boolean checkSourceFiles(){
		if(sourceFiles == null || sourceFiles.length == 0){
			if(!tw.getTaskExp().containsKey(TWMonitorBean.EXP_Exception)){
				/* 任务监控 开始*/
				tw.setTaskStatus(TaskStatusEnum.TaskStatus_NoFile.getStatus());
				tw.setTaskExp(TWMonitorBean.EXP_Exception, "获取原始文件数量为空");
				gb.setTaskOfWork(tw);
				/* 任务监控 结束*/
			}		
			logger.warn("任务[{}]获取原始文件数量为空",CommonTool.getJobName(tb));
			return false;
		}else{
			return true;
		}
	}
	
	public void optArrays(){
		int i = Integer.parseInt(RandomUtils.randomNumeric(1));
		if(sourceFiles.length > i){
			sourceFiles = (File[]) ArrayUtils.subarray(sourceFiles, 0, i);
		}else{
			sourceFiles = (File[]) ArrayUtils.subarray(sourceFiles, 0, 1);
		}
	}
	
	/**
	 * 备份插件回执文件
	 * @throws IOException 
	 */
	public void backUpReturnFiles() throws IOException{
		//按日期备份
		String backUpTime = TimeProcessor.getCurrentTimeString();
		//备份原始文件
		String returnDir = backUpdir + File.separator + GlobalBean.ReturnDir + File.separator + backUpTime;	
		MyFileUtil.copyFile(returnFiles, returnDir);
		logger.info("备份任务[{}]插件回执文件:{}个",CommonTool.getJobName(tb),returnFiles.length);
	}
	
	
	/**
	 * 下载原始文件
	 * @throws Exception 
	 * @throws IOException 
	 */
	public void downLoadSourceFiles() throws Exception{
		
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_GetFile_DownLoad.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		logger.info("任务[{}]开始下载原始文件",CommonTool.getJobName(tb));
		long Start = System.currentTimeMillis();		
		//下载原始文件
		ArrayList<File> downLoadFiles = new FTPOperate(tb.getSourceFtp(),tb.getId())
		.downLoadFiles(tb, downTempDir, this.getRecords());		
		sourceFiles = arrayList2Array(downLoadFiles);
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb),downLoadFiles.size(), (End - Start)/1000};
		logger.info("任务[{}],{}个原始文件被下载,耗时={}秒", paramArray);
		//验证是否有原始文件需要处理
		//this.checkSourceFiles();
		
		/* 任务监控 开始*/
		tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Num, sourceFiles == null ? "0" : String.valueOf(sourceFiles.length));
		tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Size, FileUtil.formatSize(FileUtil.sizeOfFiles(sourceFiles)));
		tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Names, super.appendFileName(sourceFiles));
		tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, "成功下载" + sourceFiles.length + "个原始文件,耗时" + (End - Start)/1000 + "秒");
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
	}
	
	/**
	 * 获取记录文件
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public ArrayList<String> getRecords() throws IOException, Exception {
		//获取记录文件
		ArrayList<String> records = new ArrayList<String>();
		if(tb.isDelete()){
			//获取任务触发时间今天和昨天的记录文件内容
			records = getRecords(false);
		}else{
			//获取所有记录文件内容
			records = getRecords(true);
			/**
			for(String s :records){
				logger.info("-------------------------------记录文件数据:{}",s);
			}
			**/
		}
		return records;
	}
	
	/**
	 * 获取原始文件(来自本地)
	 * @throws Exception 
	 * @throws IOException 
	 */
	public void localSourceFiles() throws Exception{
		
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_GetFile_Transfer.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
		logger.info("任务[{}]开始扫描原始文件(来自本机)",CommonTool.getJobName(tb));	
		sourceFiles = MyFileUtil.getFiles(tb.getSourceDir());
		//排除UNIX系统文件
		sourceFiles = excludeUnixFile(sourceFiles);
		//排除不符合正则表达式的文件
		sourceFiles = excludeUnMatches(sourceFiles, super.dynamicRegExp(tb.getRegExp()));
		//排除记录中已经存在的文件
		sourceFiles = excludeInRecords(sourceFiles, this.getRecords());
		//验证是否有原始文件需要处理
		//this.checkSourceFiles();
		
		if(sourceFiles != null && sourceFiles.length > 0){
			//验证文件是否完整
			if(!StringUtils.isEmpty(tb.getCheckSleepTime()) && Long.parseLong(tb.getCheckSleepTime()) > 0){		
				for(File sourceFile : sourceFiles){
					this.checkFileIsOver_Loc(sourceFile,tb.getCheckSleepTime());
				}
			}
		}		
				
		/* 任务监控 开始*/
		tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Num, sourceFiles == null ? "0" : String.valueOf(sourceFiles.length));
		tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Size, FileUtil.formatSize(FileUtil.sizeOfFiles(sourceFiles)));
		tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Names, super.appendFileName(sourceFiles));		
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/		
	}
	
	/*
	 * 验证文件是否完整
	 * @param sourceFile
	 * @param checkSleepTime
	 */
	private void checkFileIsOver_Loc(File sourceFile, String checkSleepTime) throws InterruptedException{
		if(sourceFile == null || !sourceFile.isFile()){
			return;
		}
		long sleepTime = Long.parseLong(checkSleepTime);
		long startSize = sourceFile.length();
		Thread.sleep(sleepTime);
		long endSize = sourceFile.length();
		if(startSize != endSize){
			Object[] paramArray = {sourceFile.getName(), sleepTime/1000, startSize, endSize};
			logger.warn("本地文件{}在{}秒内大小不一致,上次检查大小{},本次检查大小{},程序递归校验,直到在设定的检查时间间隔内大小保持一致",paramArray);
			this.checkFileIsOver_Loc(sourceFile, checkSleepTime);
		}else{
			logger.warn("本地文件{}在设定的检查时间间隔内大小保持一致,程序继续执行",sourceFile.getName());
		}
	}
	
	/**
	 * 上传回执文件,记录回执文件上传成功的情况
	 * @throws IOException 
	 */
	public void uploadReturnFiles() throws IOException {
		
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_PutReturnFile_Upload.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
		//上传回执文件,记录回执文件上传成功的情况
		long Start = System.currentTimeMillis();		
		ArrayList<File> uploadFiles = new FTPOperate(tb.getDestFtp(),tb.getId())
			.uploadFile(tb.getDestDir(), returnFiles, getReturnRecord());		
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb),uploadFiles.size(), (End - Start)/1000};
		logger.info("任务[{}],{}个回执文件被上传,耗时={}秒", paramArray);
		
		/* 任务监控 开始*/
		tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, uploadFiles.size() + "个回执文件被上传,耗时" + (End - Start)/1000 + "秒");
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
	}	
	
	private String isMakeDestDir(String destDir) throws IOException{
		if(destDir.contains(customPathRegex)){
			List<String> cp = Arrays.asList(destDir.split(customPathRegex));
			List<String> dd = new ArrayList<String>(cp.size());
			StringBuilder sb = new StringBuilder();
			
			for(String s : cp){
				dd.add(this.customPath(s));
			}
			
			for(String s : dd){
				sb.append(s);
			}
			File destDir_ = new File(sb.toString());
			if(!destDir_.isDirectory()){
				boolean flag = destDir_.mkdirs();
				logger.info("创建远程目录{},结果{}",sb.toString(),flag);
			}
			
			return sb.toString();
		}
		return destDir;
	}
	
	/**
	 * 自定义动态远程目录
	 * @param s
	 * @return
	 */
	private String customPath(String s){
		if(s.startsWith("yyyyMMdd")){
			if(s.contains("-")){
				return TimeUtils.date2String(this.getDateBefore(new Date(), Integer.parseInt(s.split("-")[1])), "yyyyMMdd");
			}else if(s.contains("+")){
				return TimeUtils.date2String(this.getDateBefore(new Date(), -Integer.parseInt(s.split("+")[1])), "yyyyMMdd");
			}else{
				return TimeUtils.getCurrentTime("yyyyMMdd");
			}		
		}
		if(s.startsWith("yyyyMM")){
			if(s.contains("-")){
				return TimeUtils.date2String(this.getDateMonBefore(new Date(), Integer.parseInt(s.split("-")[1])), "yyyyMM");
			}else if(s.contains("+")){
				return TimeUtils.date2String(this.getDateMonBefore(new Date(), -Integer.parseInt(s.split("+")[1])), "yyyyMM");
			}else{
				return TimeUtils.getCurrentTime("yyyyMM");
			}		
		}
		return s;
	}
	
	/**
	 * 得到几天前日期
	 * @return
	 */
	private Date getDateBefore(Date d, int day){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, -day);
		return c.getTime();
	}
	
	/**
	 * 得到几天前日期
	 * @return
	 */
	private Date getDateMonBefore(Date d, int mon){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, -mon);
		return c.getTime();
	}
	
	/**
	 * 转移回执的文件,并记录
	 * @throws IOException 
	 */
	public void removeReturnFiles() throws IOException{
		
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_PutReturnFile_Transfer.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
		String destDir = this.isMakeDestDir(tb.getDestDir());		
		long Start = System.currentTimeMillis();
		MyFileUtil.mvFile(returnFiles, destDir);		
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb), returnFiles.length, (End - Start)/1000};
		logger.info("任务{},{}个回执文件被转移(本机转移),耗时={}秒",paramArray);
		
		if(!tw.getTaskExp().containsKey(TWMonitorBean.EXP_Exception)){
			/* 任务监控 开始*/
			tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, returnFiles.length + "个回执文件被转移(本机转移),耗时" + (End - Start)/1000 + "秒");
			gb.setTaskOfWork(tw);
			/* 任务监控 结束*/
		}
				
		//记录回执文件转移成功的情况
		recordReturnFile(returnFiles);
	}
	
	/**
	 * 上传原始文件,清空临时下载文件夹,记录原始文件上传成功的情况
	 * @throws IOException 
	 */
	public void uploadSourceFiles() throws IOException{
		
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_PutSourceFile_Upload.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/	
		
		//上传原始文件,记录原始文件上传成功的情况
		long Start = System.currentTimeMillis();		
		ArrayList<File> uploadFiles = new FTPOperate(tb.getDestFtp(),tb.getId())
			.uploadFile(tb.getDestDir(), sourceFiles, getTodayRecord());		
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb), uploadFiles.size(), (End - Start)/1000};
		logger.info("任务{},{}个原始文件被上传,耗时={}秒",paramArray);
		
		/* 任务监控 开始*/
		tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, uploadFiles.size() + "个原始文件被上传,耗时" + (End - Start)/1000 + "秒");
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
	}
	
	/**
	 * 转移原始的文件,并记录
	 * @throws IOException 
	 */
	public void removeSourceFiles() throws IOException{
		
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_PutSourceFile_Transfer.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		String destDir = this.isMakeDestDir(tb.getDestDir());
		long Start = System.currentTimeMillis();
		MyFileUtil.mvFile(sourceFiles, destDir);		
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb), sourceFiles.length, (End - Start)/1000};
		logger.info("任务{},{}个原始文件被转移(本机转移),耗时={}秒",paramArray);
		
		/* 任务监控 开始*/
		tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, sourceFiles.length + "个原始文件被转移(本机转移),耗时" + (End - Start)/1000 + "秒");
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
		//记录原始文件转移成功的情况
		recordFile(sourceFiles);
	}
	
	/**
	 * 删除原始文件
	 */
	private void delSourceFiles(){
		long Start = System.currentTimeMillis();	
		if(tb.isDelete() && tb.getSourceFtp() != null){			
			ArrayList<String> delSourceFiles = new FTPOperate(tb.getSourceFtp(),tb.getId())
				.delRemoteFile(tb.getSourceDir(), array2ArrayList(sourceFiles));
			Object[] paramArray_1 = {CommonTool.getJobName(tb), delSourceFiles.size(), (System.currentTimeMillis() - Start)/1000};
			logger.info("任务[{}],{}个远程原始文件被删除,耗时={}秒",paramArray_1);
		}//删除源文件,源文件来源于本机
		else if(tb.isDelete() && tb.getSourceFtp() == null){					
			MyFileUtil.delFilesSim(sourceFiles,postfixs);
			Object[] paramArray_2 = {CommonTool.getJobName(tb), sourceFiles.length, (System.currentTimeMillis() - Start)/1000};
			logger.info("任务[{}],{}个本地原始文件被删除,耗时={}秒", paramArray_2);
		}else{
			logger.info("任务[{}],不需要删除原始文件,程序忽略此步骤");
		}
	}
	
	
	/**
	 * 插件环节
	 * @param tb
	 * @return
	 */
	public void plugin() {
		try {
			/* 任务监控 开始*/
			tw.setTaskStatus(TaskStatusEnum.TaskStatus_Plugin_DisposeFile.getStatus());
			gb.setTaskOfWork(tw);
			/* 任务监控 结束*/
			
			// 记录原始的文件
			recordFile(sourceFiles);
			// 反射插件
			returnFiles = new PluginManage(tb).reflectDisposeFileMethod(sourceFiles);
			// 存在回执文件
			if (returnFiles != null && returnFiles.length > 0 && !CommonTool.checkNull(tb.getDestDir())) {
				
				/* 任务监控 开始*/
				tw.setTaskExp(TWMonitorBean.EXP_ReturnFiles_Num, returnFiles == null ? "0" : String.valueOf(returnFiles.length));
				tw.setTaskExp(TWMonitorBean.EXP_ReturnFiles_Size, FileUtil.formatSize(FileUtil.sizeOfFiles(returnFiles)));
				tw.setTaskExp(TWMonitorBean.EXP_ReturnFiles_Names, super.appendFileName(returnFiles));		
				gb.setTaskOfWork(tw);
				/* 任务监控 结束*/
				
				// 备份回执文件
				if(tb.isBackUp()){
					backUpReturnFiles();
				}else{
					logger.info("任务[{}]根据配置文件,回执文件不需要备份,程序忽略备份过程",CommonTool.getJobName(tb));
				}
				// 文件后续处理
				if (tb.getDestFtp() != null) {
					this.uploadReturnFiles();
				} else {
					this.removeReturnFiles();
				}
			} else {
				/* 任务监控 开始*/
				tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, "插件处理完毕,未发现回执文件或者目标路径为空,就此结束");
				gb.setTaskOfWork(tw);
				/* 任务监控 结束*/				
				logger.info("任务[{}]没有回执文件或者目标路径为空,程序处理结束",CommonTool.getJobName(tb));
			}
		} catch (Exception e) {
			/* 任务监控 开始*/
			tw.setTaskStatus(TaskStatusEnum.TaskStatus_Exception.getStatus());
			String exceptionInfo = null;
			if(StringUtils.isEmpty(e.getMessage())){
				if(e.getCause() !=null){
					exceptionInfo = e.getCause().getMessage();
				}
			}else{
				exceptionInfo = e.getMessage();
			}
			tw.setTaskExp(TWMonitorBean.EXP_Exception, "插件执行异常:" + exceptionInfo);
			gb.setTaskOfWork(tw);
			/* 任务监控 结束*/			
			
			Object[] paramArray = {CommonTool.getJobName(tb), tb.getPluginName()};
			logger.error("任务[{}]插件{}执行异常", paramArray, e);
			logger.error("插件异常信息如下:",e);
		}
	}
	
	/**
	 * 非插件形式
	 * @throws IOException 
	 */
	public void unPlugin(){
		try {
			if (!CommonTool.checkNull(tb.getDestDir())) {
				if (tb.getDestFtp() != null) {
					this.uploadSourceFiles();
				} else {
					this.removeSourceFiles();
				}
			} else {
				logger.error("任务[{}]逻辑错误,{}", CommonTool.getJobName(tb),
						"目标路径为空,且非插件处理类型");
			}
		} catch (Exception e) {
			/* 任务监控 开始*/
			tw.setTaskStatus(TaskStatusEnum.TaskStatus_Exception.getStatus());
			String exceptionInfo = null;
			if(StringUtils.isEmpty(e.getMessage())){
				if(e.getCause() !=null){
					exceptionInfo = e.getCause().getMessage();
				}
			}else{
				exceptionInfo = e.getMessage();
			}
			tw.setTaskExp(TWMonitorBean.EXP_Exception, "非插件模式执行异常:" + exceptionInfo);
			gb.setTaskOfWork(tw);
			/* 任务监控 结束*/
			logger.error("非插件模式异常信息如下:",e);
		}
	}
	
}
