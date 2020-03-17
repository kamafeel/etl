package com.linkage.ftpdrudgery.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.fs.commons.decider.IamDecider;

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
import com.linkage.intf.tools.StringUtils;

/**
 * XmlBean工具类
 * @author run[zhangqi@lianchuang.com]
 * 2:49:23 PM May 11, 2009
 */
public class XmlBeanTools {
	
	private static XmlBeanTools SINGLE = new XmlBeanTools();
	
	private GlobalBean gb;
	
	private static XmlOptions opts;
	
	/** 静态同步锁 */
	private static Object objLock = new Object();
		
	public static synchronized XmlBeanTools getInstance() {

		if (SINGLE == null) {
			SINGLE = new XmlBeanTools();			
		}
		return SINGLE;
	}
	
	private XmlBeanTools(){
		gb = GlobalBean.getInstance();
		//XmlOptions,供解析文件用
		opts = new XmlOptions();	
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(4);
		opts.setCharacterEncoding("GB2312");
	}
		
	/**
	 * 增加TaskConfig
	 * @param tb
	 * @return
	 * @throws XmlException 
	 * @throws IOException 
	 */
	public boolean addTaskConfig(TaskBean tb) throws XmlException, IOException {
				
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpTask);
		File cfBack = new File(gb.getConfigPath() + GlobalBean.Config_BackUpDir
				+ File.separator
				+ GlobalBean.Config_FtpTask
				+ "."
				+ TimeProcessor.getCurrentTime());
		synchronized (objLock) {
			FtpDrudgeryListDocument doc;
			try {
				if(!IamDecider.getInstance().decide(tb)){
					return true;
				}
				doc = FtpDrudgeryListDocument.Factory.parse(cf, opts);
				FtpDrudgery fd = doc.getFtpDrudgeryList().addNewFtpDrudgery();
				//填充
				fd.setId(tb.getId());
				fd.setTaskName(tb.getTaskName());
				fd.setSourceDir(tb.getSourceDir());
				fd.setDestDir(tb.getDestDir());
				
				if(tb.getSourceFtp() != null){
					//设置SourceFtp
					fd.setSourceFtp(tb.getSourceFtp().getId());
					//如果没找到,增加
					if(!this.checkFtpIdExist(tb.getSourceFtp().getId())){						
						this.addFtpInfoConfig(tb.getSourceFtp());
					}
				}
								
				if(tb.getDestFtp() != null){
					//设置DestFtp
					fd.setDestFtp(tb.getDestFtp().getId());
					//如果没找到,增加
					if(!this.checkFtpIdExist(tb.getDestFtp().getId())){
						this.addFtpInfoConfig(tb.getDestFtp());
					}
				}				
				
				fd.setRegExp(tb.getRegExp());
				fd.setIsDelete(tb.isDelete() ? "true" : "false");
				fd.setCheckSleepTime(tb.getCheckSleepTime());
				fd.setCronTrigger(tb.getCronTrigger());
				//System.out.println("是否要备份：" + tb.isBackUp());
				fd.setIsBackUp(tb.isBackUp());
				fd.setBackUpDir(tb.getBackUpDir());
				//add by run 2010.1.4
				fd.setBeforeDay(tb.getBeforeDay());
				fd.setRecordValidDay(tb.getRecordValidDay());
				//插件信息
				fd.setPluginName(tb.getPluginName());
				fd.setPluginPath(tb.getPluginPath());
				fd.setPluginClassPath(tb.getPluginClassPath());
				fd.setPluginId(tb.getPluginId());
				//备份原来配置文件
				MyFileUtil.copyFile(cf, cfBack);
				//保存配置
				doc.save(cf, opts);
			} catch (XmlException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		}		
		return true;		
	}
	
	/**
	 * 查询FtpId是否存在于配置文件中
	 * @param id
	 * @return
	 * @throws XmlException 
	 * @throws IOException 
	 */
	public boolean checkFtpIdExist(String id) throws IOException, XmlException {
		
		int[] temp = this.getFtpInfoIdList();		
		//排序
		Arrays.sort(temp);
		//2分查找发
		int index = Arrays.binarySearch(temp, Integer.parseInt(id));
		
		if(index < 0){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 增加FtpInfo
	 * @param configName
	 * @param tb
	 * @return
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public boolean addFtpInfoConfig(FtpInfoBean fiBean) throws IOException, XmlException {
		
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpInfo);
		File cfBack = new File(gb.getConfigPath() + GlobalBean.Config_BackUpDir
				+ File.separator
				+ GlobalBean.Config_FtpInfo
				+ "."
				+ TimeProcessor.getCurrentTime());
		synchronized (objLock) {
			FtpListDocument doc;
			try {
				doc = FtpListDocument.Factory.parse(cf, opts);
				FtpInfo fi = doc.getFtpList().addNewFtpInfo();
				//填充
				fi.setId(fiBean.getId());
				fi.setRemoteIP(fiBean.getRemoteIP());
				fi.setPort(String.valueOf(fiBean.getPort()));
				fi.setUsername(fiBean.getUserName());
				fi.setPassword(fiBean.getPassWord());
				fi.setTransfersType(fiBean.getTransfersType());
				fi.setWorkType(fiBean.getWorkType());
				fi.setTimeout(String.valueOf(fiBean.getTimeout()));
				fi.setEncoding(fiBean.getEncoding());
				fi.setRetryCount(String.valueOf(fiBean.getRetryCount()));
				fi.setRetryInterval(String.valueOf(fiBean.getRetryInterval()));
				fi.setMaxConnect(String.valueOf(fiBean.getMaxConnect()));
				fi.setIsNoOp(fiBean.isNoOp() ? "true" : "false");
				fi.setIsRetryBrokenDownloads(fiBean.isRBD() ? "true" : "false");
				if(fiBean.getMaxConnect() > 0l){
					gb.getCurrentConnectMap().put(fi.getId(), 0l);
				}
				//备份原来配置文件
				MyFileUtil.copyFile(cf, cfBack);
				//保存配置
				doc.save(cf, opts);			
			} catch (XmlException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		}			
		return true;		
	}
	
	/**
	 * 删除TaskConfig
	 * @param tb
	 * @return
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public boolean delTaskConfig(String id) throws IOException, XmlException {
		
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpTask);
		File cfBack = new File(gb.getConfigPath() + GlobalBean.Config_BackUpDir
				+ File.separator
				+ GlobalBean.Config_FtpTask
				+ "."
				+ TimeProcessor.getCurrentTime());
		synchronized (objLock) {
			FtpDrudgeryListDocument doc;
			try {
				doc = FtpDrudgeryListDocument.Factory.parse(cf, opts);
				FtpDrudgery[] fdList = doc.getFtpDrudgeryList().getFtpDrudgeryArray();
				for(FtpDrudgery fd : fdList){
					if(!CommonTool.checkNull(fd.getId()) && fd.getId().equalsIgnoreCase(id)){
						fd.setNil();
						break;
					}
				}
				//备份原来配置文件
				MyFileUtil.copyFile(cf, cfBack);
				//保存配置
				doc.save(cf, opts);				
			} catch (XmlException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		}			
		return true;		
	}
	
	/**
	 * 删除FtpInfo
	 * @param configName
	 * @param tb
	 * @return
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public boolean delFtpInfoConfig(String id) throws IOException, XmlException {
		
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpInfo);
		File cfBack = new File(gb.getConfigPath() + GlobalBean.Config_BackUpDir
				+ File.separator
				+ GlobalBean.Config_FtpInfo
				+ "."
				+ TimeProcessor.getCurrentTime());
		
		synchronized (objLock) {
			FtpListDocument doc;
			try {
				doc = FtpListDocument.Factory.parse(cf, opts);
				FtpInfo[] fiList = doc.getFtpList().getFtpInfoArray();
				
				for(FtpInfo fi : fiList){
					if(!CommonTool.checkNull(fi.getId()) && fi.getId().equalsIgnoreCase(id)){
						fi.setNil();
						break;
					}
				}
				if(gb.getCurrentConnectMap().contains(id)){
					gb.getCurrentConnectMap().remove(id);
				}
				//备份原来配置文件
				MyFileUtil.copyFile(cf, cfBack);
				//保存配置
				doc.save(cf, opts);
			} catch (XmlException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		}				
		return true;
	}
	
	/**
	 * 更新TaskConfig
	 * @param tb
	 * @return
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public boolean updateTaskConfig(TaskBean tb) throws IOException, XmlException {
		
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpTask);
		File cfBack = new File(gb.getConfigPath() + GlobalBean.Config_BackUpDir
				+ File.separator
				+ GlobalBean.Config_FtpTask
				+ "."
				+ TimeProcessor.getCurrentTime());
		
		synchronized (objLock) {
			FtpDrudgeryListDocument doc;
			try {
				/**
				if(!IamDecider.getInstance().decide(tb)){
					return true;
				}
				**/
				doc = FtpDrudgeryListDocument.Factory.parse(cf, opts);
				FtpDrudgery[] fdList = doc.getFtpDrudgeryList().getFtpDrudgeryArray();
				//更新
				for(FtpDrudgery fd : fdList){
					if(!CommonTool.checkNull(fd.getId()) && fd.getId().equalsIgnoreCase(tb.getId())){
						/**
						fd.setId(tb.getId());
						fd.setTaskName(tb.getTaskName());
						fd.setSourceDir(tb.getSourceDir());
						fd.setDestDir(tb.getDestDir());				
						//设置SourceFtp
						if(tb.getSourceFtp() != null){
							fd.setSourceFtp(tb.getSourceFtp().getId());
							//如果没找到,增加,找到更新	
							if(!this.checkFtpIdExist(tb.getSourceFtp().getId())){
								this.addFtpInfoConfig(tb.getSourceFtp());
							}else{
								this.updateFtpInfoConfig(tb.getSourceFtp());
							}
						}else{
							fd.setSourceFtp(null);
						}
										
						//设置DestFtp
						if(tb.getDestFtp() != null){
							fd.setDestFtp(tb.getDestFtp().getId());
							//如果没找到,增加	
							if(!this.checkFtpIdExist(tb.getDestFtp().getId())){
								this.addFtpInfoConfig(tb.getDestFtp());
							}else{
								this.updateFtpInfoConfig(tb.getDestFtp());
							}
						}else{
							fd.setDestFtp(null);
						}
											
						fd.setRegExp(tb.getRegExp());
						fd.setIsDelete(tb.isDelete() ? "true" : "false");
						fd.setCheckSleepTime(tb.getCheckSleepTime());
						fd.setCronTrigger(tb.getCronTrigger());
						fd.setIsBackUp(tb.isBackUp());
						fd.setBackUpDir(tb.getBackUpDir());
						//add by run 2010.1.4
						fd.setBeforeDay(tb.getBeforeDay());
						fd.setRecordValidDay(tb.getRecordValidDay());
						//插件内容
						fd.setPluginName(tb.getPluginName());
						fd.setPluginPath(tb.getPluginPath());
						fd.setPluginClassPath(tb.getPluginClassPath());
						**/
						fd.setPluginId(tb.getPluginId());
					}
				}
				//备份原来配置文件
				MyFileUtil.copyFile(cf, cfBack);
				//保存配置
				doc.save(cf, opts);				
			} catch (XmlException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		}		
		return true;		
	}
	
	/**
	 * 更新FtpInfo
	 * @param configName
	 * @param tb
	 * @return
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public boolean updateFtpInfoConfig(FtpInfoBean fiBean) throws IOException, XmlException {
		
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpInfo);
		File cfBack = new File(gb.getConfigPath() + GlobalBean.Config_BackUpDir
				+ File.separator
				+ GlobalBean.Config_FtpInfo
				+ "."
				+ TimeProcessor.getCurrentTime());
		
		synchronized (objLock) {
			FtpListDocument doc;
			try {
				doc = FtpListDocument.Factory.parse(cf, opts);
				FtpInfo[] fiList = doc.getFtpList().getFtpInfoArray();
				//更新
				for(FtpInfo fi : fiList){
					if(!CommonTool.checkNull(fi.getId()) && fi.getId().equalsIgnoreCase(fiBean.getId())){
						fi.setId(fiBean.getId());
						fi.setRemoteIP(fiBean.getRemoteIP());
						fi.setPort(String.valueOf(fiBean.getPort()));
						fi.setUsername(fiBean.getUserName());
						fi.setPassword(fiBean.getPassWord());
						fi.setTransfersType(fiBean.getTransfersType());
						fi.setWorkType(fiBean.getWorkType());
						fi.setTimeout(String.valueOf(fiBean.getTimeout()));
						fi.setEncoding(fiBean.getEncoding());
						fi.setRetryCount(String.valueOf(fiBean.getRetryCount()));
						fi.setRetryInterval(String.valueOf(fiBean.getRetryInterval()));
						fi.setMaxConnect(String.valueOf(fiBean.getMaxConnect()));
						fi.setIsNoOp(fiBean.isNoOp() ? "true" : "false");
						fi.setIsRetryBrokenDownloads(fiBean.isRBD() ? "true" : "false");
						if(fiBean.getMaxConnect() > 0l){
							gb.getCurrentConnectMap().put(fi.getId(), 0l);
						}
						break;			
					}
				}
				//备份原来配置文件
				MyFileUtil.copyFile(cf, cfBack);
				//保存配置
				doc.save(cf, opts);
			} catch (XmlException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		}			
		return true;		
	}
	
	/**
	 * 获取当前FtpInfo_ID数组
	 * @return
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public int[] getFtpInfoIdList() throws IOException, XmlException{			
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpInfo);
		FtpListDocument doc;
		try {
			doc = FtpListDocument.Factory.parse(cf, opts);
			FtpInfo[] fiList = doc.getFtpList().getFtpInfoArray();
			int[] idList = new int[fiList.length];
			int num = 0;
			for(FtpInfo fi : fiList){
				if(!CommonTool.checkNull(fi.getId())){
					idList[num] = Integer.parseInt(fi.getId());
					num++;
				}		
			}
			return idList;
		} catch (XmlException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * 获取当前Task_ID数组
	 * @return
	 * @throws XmlException 
	 * @throws IOException 
	 */
	public int[] getTaskIdList() throws XmlException, IOException {

		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpTask);
		FtpDrudgeryListDocument doc;
		try {
			doc = FtpDrudgeryListDocument.Factory.parse(cf, opts);
			FtpDrudgery[] fdList = doc.getFtpDrudgeryList().getFtpDrudgeryArray();
			// 更新
			int[] idList = new int[fdList.length];
			int num = 0;
			for (FtpDrudgery fd : fdList) {
				if (!CommonTool.checkNull(fd.getId())) {
					idList[num] = Integer.parseInt(fd.getId());
					num++;
				}
			}
			return idList;
		} catch (XmlException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * 查询任务
	 * 0 任务名称
	 * 1 正则表达式
	 * 2 备份目录
	 * 3 插件ID为NULL
	 * @return
	 * @throws XmlException 
	 * @throws IOException 
	 */
	public String[] qryTask(String qryKey,int qryType) throws XmlException, IOException {

		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpTask);
		FtpDrudgeryListDocument doc;
		try {
			doc = FtpDrudgeryListDocument.Factory.parse(cf, opts);
			FtpDrudgery[] fdList = doc.getFtpDrudgeryList().getFtpDrudgeryArray();
			// 更新
			String[] idList = new String[fdList.length];
			int num = 0;
			for (FtpDrudgery fd : fdList) {
				if (!CommonTool.checkNull(fd.getId())) {
					if (qryType == 0) {
						if (fd.getTaskName().contains(qryKey)) {
							idList[num] = fd.getId();
							num++;
						}
					} else if (qryType == 1) {
						if (fd.getRegExp().contains(qryKey)) {
							idList[num] = fd.getId();
							num++;
						}
					} else if (qryType == 2) {
						if (fd.getBackUpDir().contains(qryKey) 
								|| fd.getBackUpDir().contains(qryKey.toLowerCase())
								|| fd.getBackUpDir().contains(qryKey.toUpperCase())) {
							idList[num] = fd.getId();
							num++;
						}
					} else if (qryType == 3) {
						if(StringUtils.isEmpty(qryKey)){
							if (StringUtils.isEmpty(fd.getPluginId())) {
								idList[num] = fd.getId();
								num++;
							}
						}else if(!StringUtils.isEmpty(fd.getPluginId()) && fd.getPluginId().equalsIgnoreCase(qryKey)) {
							idList[num] = fd.getId();
							num++;
						}
					} else if (qryType == 4) {
						if (fd.getId().contains(qryKey)) {
							idList[num] = fd.getId();
							num++;
						}
					}
					
				}
			}
			return idList;
		} catch (XmlException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * 返回FtpInfo列表
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public ArrayList<FtpInfoBean> getFtpInfoList() throws IOException, XmlException{
		
		ArrayList<FtpInfoBean> tbList = new ArrayList<FtpInfoBean>();
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_FtpInfo);
		FtpListDocument doc;
		try {
			doc = FtpListDocument.Factory.parse(cf, opts);
			FtpInfo[] fiList = doc.getFtpList().getFtpInfoArray();
			for(FtpInfo fi : fiList){
				if(!CommonTool.checkNull(fi.getId())){
					FtpInfoBean fb = new FtpInfoBean();
					fb.setId(fi.getId());
					fb.setRemoteIP(fi.getRemoteIP());
					fb.setPort(Integer.parseInt(fi.getPort()));
					fb.setUserName(fi.getUsername());
					fb.setPassWord(fi.getPassword());
					fb.setEncoding(fi.getEncoding());
					fb.setRetryCount(Integer.parseInt(fi.getRetryCount()));
					fb.setRetryInterval(Long.parseLong(fi.getRetryInterval()));
					fb.setMaxConnect(Long.parseLong(fi.getMaxConnect()));
					fb.setWorkType(fi.getWorkType());
					fb.setTransfersType(fi.getTransfersType());
					fb.setTimeout(Integer.parseInt(fi.getTimeout()));
					fb.setNoOp(fi.getIsNoOp().equalsIgnoreCase("true"));
					fb.setRBD(fi.getIsRetryBrokenDownloads().equalsIgnoreCase("true"));
					tbList.add(fb);
				}			
			}
		} catch (XmlException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return tbList;
	}
	
	/**
	 * 更新系统配置
	 * @return
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public boolean updateSystem(SystemBean sb) throws IOException, XmlException{
		
		File cf = new File(gb.getConfigPath() + GlobalBean.Config_System);
		File cfBack = new File(gb.getConfigPath() + GlobalBean.Config_BackUpDir
				+ File.separator
				+ GlobalBean.Config_System
				+ "."
				+ TimeProcessor.getCurrentTime());
		synchronized (objLock) {
			FtpSystemDocument doc;
			try {
				doc = FtpSystemDocument.Factory.parse(cf, opts);
				FtpSystem fs = doc.getFtpSystem();
				//填充
				fs.setBackUpDir(sb.getBackUpDir());
				fs.setDownLoadTempDir(sb.getDownLoadTempDir());
				fs.setUploadPostfix(sb.getUploadPostfix());
				fs.setIsRecordMonitor(new Boolean(sb.isRecordMonitor()).toString());
				
				FtpConsole fc = fs.getFtpConsole();
				fc.setConsoleStart(new Boolean(sb.isConsoleStart()).toString());
				fc.setConsoleUrl(sb.getUrl());
				//备份原来配置文件
				MyFileUtil.copyFile(cf, cfBack);
				//保存配置
				doc.save(cf, opts);
			} catch (XmlException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		}
		return true;
	}
	
}
