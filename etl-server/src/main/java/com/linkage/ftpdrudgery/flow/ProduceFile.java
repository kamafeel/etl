package com.linkage.ftpdrudgery.flow;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.fs.commons.decider.IamDecider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.fdenum.TaskStatusEnum;
import com.linkage.ftpdrudgery.tools.CommonTool;
import com.linkage.ftpdrudgery.tools.FTPOperate;
import com.linkage.ftpdrudgery.tools.MyFileUtil;
import com.linkage.ftpdrudgery.tools.PluginManage;
import com.linkage.intf.file.FileUtil;
import com.linkage.intf.tools.StringUtils;

/**
 * 文件由插件生成方式
 * @author run[zhangqi@lianchuang.com]
 * 2:05:07 PM May 26, 2009
 */

public class ProduceFile extends WorkFlow {
	
	private static Logger logger = LoggerFactory.getLogger(ProduceFile.class);
	private Object objLock = new Object();
	public ProduceFile(TaskBean tb,TWMonitorBean tw) {
		super(tb,tw);
	}
		
	/**
	 * 业务逻辑
	 * @throws IOException 
	 */
	public void operationLogic() throws Exception{
		synchronized(objLock) {
			try {
				if(!IamDecider.getInstance().decide(tb)){
					return;
				}
				this.uknowKgMKeastMiKit(5);
				//校验插件信息是否完整
				if(checkPlugin()){
					this.plugin();
				}else{
					CommonTool.setTaskStatusExc(tw, "插件信息不完整,且[数据来源路径]为空");
					logger.error("任务[{}],插件信息不完整,且[数据来源路径]为空",CommonTool.getJobName(tb));
					return;
				}
				//检查插件生成文件的情况
				if(sourceFiles == null || sourceFiles.length == 0){
					
					//如果插件执行未异常,但文件组为空
					if(!tw.getTaskExp().containsKey(TWMonitorBean.EXP_Exception)){
						/* 任务监控 开始*/
						tw.setTaskStatus(TaskStatusEnum.TaskStatus_NoFile.getStatus());
						tw.setTaskExp(TWMonitorBean.EXP_Exception, "插件生成原始文件为空");
						gb.setTaskOfWork(tw);
						/* 任务监控 结束*/
					}
					logger.error("任务[{}]的插件生成原始文件为空",CommonTool.getJobName(tb));
				}else{
					//全量备份
					if(tb.isBackUp()){
						backUpSourceFiles();
					}else{
						logger.info("任务[{}]根据配置文件,不需要备份,程序忽略备份过程",CommonTool.getJobName(tb));
					}
					//插件生成文件后续处理
					if(tb.getDestFtp() == null){
						this.transferFile();
					}else{
						this.UploadFile();
					}
				}
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
	 * 上传文件
	 * @throws IOException 
	 */
	public void UploadFile() throws IOException {
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_PutSourceFile_Upload.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
		//上传插件生成的文件
		long Start = System.currentTimeMillis();		
		ArrayList<File> uploadFiles = new FTPOperate(tb.getDestFtp(),tb.getId())
			.uploadFile(tb.getDestDir(), sourceFiles, getReturnRecord());				
		sourceFiles = arrayList2Array(uploadFiles);		
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb),sourceFiles.length, (End - Start)/1000};
		logger.info("任务[{}]插件生成的{}个文件被上传,耗时={}秒", paramArray);
		
		if(tb.isDelete()){
			MyFileUtil.delFiles(sourceFiles);
			logger.info("成功清除任务[{}]插件生成的文件,{}个",CommonTool.getJobName(tb),sourceFiles.length);
		}
		
		/* 任务监控 开始*/
		tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, "插件生成的" + sourceFiles.length + "个文件被上传,耗时" + (End - Start)/1000 + "秒");
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
	}
	
	/**
	 * 转移文件
	 * @throws IOException 
	 */
	public void transferFile() throws IOException{
		
		/* 任务监控 开始*/
		tw.setTaskStatus(TaskStatusEnum.TaskStatus_PutSourceFile_Transfer.getStatus());
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
		long Start = System.currentTimeMillis();
		if(tb.isDelete()){
			MyFileUtil.cutFiles(sourceFiles, tb.getDestDir());
		}else{
			MyFileUtil.copyFile(sourceFiles, tb.getDestDir());
		}		
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb),sourceFiles.length, (End - Start)/1000};
		logger.info("任务[{}]插件生成的{}个文件被转移,耗时={}秒", paramArray);
		
		/* 任务监控 开始*/
		tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, "插件生成的" + sourceFiles.length + "个文件被转移,耗时" + (End - Start)/1000 + "秒");
		gb.setTaskOfWork(tw);
		/* 任务监控 结束*/
		
	}
	
	
	/**
	 * 由插件生成文件方式
	 * @param tb
	 * @return
	 * @throws Exception 
	 */
	public void plugin(){
		try {
			/* 任务监控 开始*/
			tw.setTaskStatus(TaskStatusEnum.TaskStatus_Plugin_ProduceFile.getStatus());
			gb.setTaskOfWork(tw);
			/* 任务监控 结束*/
			
			long Start = System.currentTimeMillis();
			sourceFiles = new PluginManage(tb).reflectProduceFileMethod();
			//排除不符合正则表达式的文件
			sourceFiles = excludeUnMatches(sourceFiles, super.dynamicRegExp(tb.getRegExp()));
			long End = System.currentTimeMillis();
			
			/* 任务监控 开始*/
			tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Num, sourceFiles == null ? "0" : String.valueOf(sourceFiles.length));
			tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Size, sourceFiles == null ? "0" : FileUtil.formatSize(FileUtil.sizeOfFiles(sourceFiles)));
			tw.setTaskExp(TWMonitorBean.EXP_SourceFile_Names, sourceFiles == null ? "0" : super.appendFileName(sourceFiles));
			tw.setTaskExp(TWMonitorBean.EXP_ResultInfo, "插件生成" + (sourceFiles == null ? "0" : sourceFiles.length) + "个文件,耗时" + (End - Start)/1000 + "秒");
			gb.setTaskOfWork(tw);
			/* 任务监控 结束*/
			
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
			
			Object[] paramArray = {CommonTool.getJobName(tb),tb.getPluginName()};
			logger.error("任务[{}]的插件:{}执行异常", paramArray, e);
			logger.error("插件异常详细信息如下:",e);
		}	
	}

}
