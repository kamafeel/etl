package com.linkage.ftpdrudgery.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.fdenum.TaskStatusEnum;
import com.linkage.ftpdrudgery.flow.WorkFlow;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;


/**
 * FTP工具(支持断点续传,多线程访问此类不支持单例)
 * @author run
 *
 */
public class FTPOperate {
	
	private static Logger logger = LoggerFactory.getLogger(FTPOperate.class);
	
	/*
	 * FTPClient实例
	 */
	private FTPClient ftpClient; 
	
	/*
	 * FTP主机IP
	 */
	private String remoteIP;
	
	/*
	 * FTP主机端口
	 */
	private int port;
	
	/*
	 * 用户名
	 */
	private String username;
	
	/*
	 * 密码
	 */
	private String password;
	
	/*
	 * 超时时间(单位：毫秒)
	 */
	private int timeout;
	
	/*
	 *传输方式 
	 */	
	private String transfersType;
	
	/*
	 *工作方式 
	 */	
	private String workType;
	
	/*
	 * 通讯的编码集
	 */
	private String encoding;
	
	/*
	 * 当连接获取失败时重试的次数
	 */
	private int retryCount;

	/*
	 * 重试的时间的间隔
	 */
	private long retryInterval;
	
	/*
	 * 上传临时后缀
	 */
	private String uploadPostfix;
	
	/*
	 * 文件操作buffer大小
	 */
	private final static int bufSize = 1024 * 20;
	
	/*
	 * 连接状态 
	 */
	private boolean isConnection;
	
	/*
	 * 随机访问对象
	 */
	private RandomAccessFile raf;
	
	/*
	 * 输入流
	 */
	private InputStream is;
	
	/*
	 * 输出流
	 */
	private OutputStream os;
	
	/*
	 * 任务ID 
	 */
	private String taskId;
	
	/*
	 * 任务ID 
	 */
	private String exceptionInfo;
	
	/*
	 * 动态路径分割符
	 */
	private final static String customPathRegex = "%";
	
	/*
	 * 心跳线程
	 */
	//private FutureTask<Boolean> sendNoOpFutureTask;
	//private Callable<Boolean> sendNoOpCallable;
	private long maxConnect;
	private String id;
	private boolean isNoOp;
	private boolean isRBD;
	
	public FTPOperate(FtpInfoBean fb,String taskId){	
		this.uploadPostfix = GlobalBean.getInstance().getSystemBean().getUploadPostfix();
		this.id = fb.getId();
		this.taskId = taskId;		
		this.remoteIP = fb.getRemoteIP();
		this.port = fb.getPort();
		this.username = fb.getUserName();
		this.password = fb.getPassWord();
		this.transfersType = fb.getTransfersType();
		this.workType = fb.getWorkType();
		this.timeout = fb.getTimeout();
		this.encoding = fb.getEncoding();
		this.retryCount = fb.getRetryCount();
		this.retryInterval = fb.getRetryInterval();
		this.maxConnect = fb.getMaxConnect();
		this.isNoOp = fb.isNoOp();
		this.isRBD=fb.isRBD();
	}

	/**
	 * 连接FTP
	 * @throws Exception 
	 * @throws SocketException
	 * @throws IOException
	 */
	private void connection() throws Exception{
		if(GlobalBean.getInstance().getCurrentConnectMap().containsKey(id)){
			long l = GlobalBean.getInstance().getCurrentConnectMap().get(id).longValue();
			if(l + 1l >= maxConnect){
				Object[] paramArray = {String.valueOf(l),remoteIP,String.valueOf(maxConnect)}; 
				logger.info("当前连接数:{}已经超过FTP:{}的最大连接数:{},程序不执行,程序休眠60秒后再试",paramArray);
				Thread.sleep(60000l);
				this.connection();
			}
		}
		ftpClient = new FTPClient();
		isConnection = false;
		int count = 0;
		Exception ex = null;
        while(count < retryCount && !isConnection){
        	count++;
        	try {				
	    		//Socket,毫秒(初次连接超时判断)
	    		//ftpClient.setDefaultTimeout(timeout);
        		ftpClient.setConnectTimeout(timeout);
	    		//数据读取超时,毫秒
	            ftpClient.setDataTimeout(timeout);
	            //connect()方法使用后,连接超时判断
	            //ftpClient.setSoTimeout(timeout);	            	            
	        	ftpClient.setControlEncoding(encoding);
	        	//add by run 2012-02-02
	        	ftpClient.setControlKeepAliveTimeout(12);
	        	ftpClient.setControlKeepAliveReplyTimeout(10000);
	        	//add by run 2009-08-18 内部字节缓存
	        	//ftpClient.setBufferSize(bufSize);
	        	//ftpClient.setReceiveBufferSize(bufSize);
	        	//ftpClient.setSendBufferSize(bufSize);	        	
	        	//logger.info(ftpClient.getStatus());
	        	//工作模式
	        	if("PASV".equals(workType)){
	        		//FtpServer防火墙模式
        			ftpClient.enterLocalPassiveMode();
        		}else{
        			//FtpServer为裸机
        			ftpClient.enterLocalActiveMode();
        		}
	        	
	        	ftpClient.connect(remoteIP, port);
				//ftpClient.setDefaultPort(port);
	    		ftpClient.login(username,password);
	        	
	        	//logger.info("DataConnectionMode: " + ftpClient.getDataConnectionMode());
	        	//ZIP,RAR等二进制文件格式的传输模式
	        	if("BINARY".equals(transfersType)){
	        		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
	        	//文本格式
	        	}else{
	        		ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
	        	}
	        		        	
	        	int reply = ftpClient.getReplyCode();
	        	//命令是否完成(reply >= 200 && reply < 300)
                if (!FTPReply.isPositiveCompletion(reply)) {
                	Object[] paramArray = {remoteIP, port, username, password, reply, ftpClient.getReplyString().trim(), count};
                	logger.warn("\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n登录失败,服务器返回:\n1.ReplyCode:{}\n2.ReplyString:{},Try:{}",paramArray);
    				try {
    					Thread.sleep(retryInterval);
    				} catch (InterruptedException ex1) {
    				}               	
                }else{
                	//+1
                	if(GlobalBean.getInstance().getCurrentConnectMap().containsKey(id)){
                		long l = GlobalBean.getInstance().getCurrentConnectMap().get(id).longValue();
                		GlobalBean.getInstance().getCurrentConnectMap().put(id, ++l);
                		//logger.info("add------------------------------{}",GlobalBean.getInstance().getCurrentConnectMap().get(id).toString());
                	}            		
                	isConnection = true;               	
                }
			} catch (SocketException e) {
				ex = e;
				Object[] paramArray = {remoteIP, port, username, password, count, e.toString()};
				logger.error("\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n不能连接,Try:{}\n异常:{}", paramArray);
				try {
					Thread.sleep(retryInterval);
				} catch (InterruptedException ex1) {
				}
			} catch (IOException e) {
				ex = e;
				Object[] paramArray = {remoteIP, port, username, password, count, e.toString()};
				logger.error("\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n不能连接,Try:{}\n异常:{}", paramArray);
				try {
					Thread.sleep(retryInterval);
				} catch (InterruptedException ex1) {
				}
			}
        }
        
        if(!isConnection){
        	throw ex;
        }
    }
	
	/**
	 * 关闭连接
	 * @throws IOException
	 */
	private void close() {
		//-1
		if(GlobalBean.getInstance().getCurrentConnectMap().containsKey(id)){
			long cru = GlobalBean.getInstance().getCurrentConnectMap().get(id).longValue();
			//logger.info("Minus-1-------------------------------{}",cru);
			if(cru > 0){
				GlobalBean.getInstance().getCurrentConnectMap().put(id, --cru);
			}
			//logger.info("Minus-2-------------------------------{}",GlobalBean.getInstance().getCurrentConnectMap().get(id).toString());
		}
		if(isNoOp){
			this.cancelsendNoOpFutureTask();
		}				
        if (ftpClient != null && isConnection && ftpClient.isConnected())
			try {				
				ftpClient.disconnect();
				isConnection = false;
				//GC回收
				ftpClient = null;
			} catch (IOException e) {
				logger.error("关闭Ftp连接异常:", e);
			}        	 
    }
	
	/**
	 * 关闭IO
	 * @throws IOException
	 */
	public void closeIO() throws IOException{
		//-1
		if(GlobalBean.getInstance().getCurrentConnectMap().containsKey(id)){
			long cru = GlobalBean.getInstance().getCurrentConnectMap().get(id).longValue();
			if(cru > 0){
				GlobalBean.getInstance().getCurrentConnectMap().put(id, --cru);
			}
			//logger.info("Minus-------------------------------{}",GlobalBean.getInstance().getCurrentConnectMap().get(id).toString());
		}
		if(isNoOp){
			this.cancelsendNoOpFutureTask();
		}		 
		if (is != null){
			//logger.debug("FTP下载文件输入流即将被关闭");
			is.close();
			is = null;
		}
		if(raf != null){
			//logger.debug("FTP上传文件随机访问对象即将被关闭");
			raf.close(); 
			raf = null;
		}
		if(os != null){
			//logger.debug("FTP上传文件输出流即将被关闭");
			os.close();
			os = null;
		}
		Object[] paramArray = {is,raf,os}; 
		logger.debug("任务被强行终止，FTP输入流:{},随机访问对象:{},FTP输出流:{}",paramArray);
	}
	
	
	private void startSendNoOp(){
		//避免重复启动
		if(!GlobalBean.getInstance().NoOp_ThreadMap.containsKey(taskId)){
			Callable<Boolean> sendNoOpCallable = new SendNoOpCallable(ftpClient,taskId);
			FutureTask<Boolean> sendNoOpFutureTask = new FutureTask<Boolean>(sendNoOpCallable);
			GlobalBean.getInstance().NoOp_ThreadMap.put(taskId, sendNoOpFutureTask);
			new Thread(sendNoOpFutureTask).start();
		}	
	}
	
	
	private void cancelsendNoOpFutureTask(){
		 //logger.debug("FTP关闭心跳线程");
		if(GlobalBean.getInstance().NoOp_ThreadMap.get(taskId) != null){
			FutureTask<Boolean> sendNoOpFutureTask = GlobalBean.getInstance().NoOp_ThreadMap.get(taskId);
			if(sendNoOpFutureTask.isCancelled() || sendNoOpFutureTask.isDone()){
				logger.debug("任务{}心跳线程已经终止",taskId);
			}else{
				sendNoOpFutureTask.cancel(true);
				GlobalBean.getInstance().NoOp_ThreadMap.remove(taskId);
			}
			
		}
//		if(sendNoOpFutureTask != null && !sendNoOpFutureTask.isCancelled()){
//			 //logger.debug("FTP关闭心跳线程");
//			 sendNoOpFutureTask.cancel(true);
//			 sendNoOpFutureTask = null;
//		}	
	}
	
	
	/**
	 * 下载文件
	 * @param tb
	 * @param tempDir
	 * @param records
	 * @return
	 */
	public ArrayList<File> downLoadFiles(TaskBean tb, String tempDir, ArrayList<String> records) {
		if("0".equalsIgnoreCase(tb.getBeforeDay())){
			return this.downLoadFiles_General(tb,tempDir,records);
		}else{
			return this.downLoadFiles_FileTime(tb,tempDir,records);
		}
	}
	
	
	/**
	 * 下载文件(检查FTP文件时间戳)
	 * @param tb
	 * @param tempDir
	 * @param records
	 * @return
	 */
	public ArrayList<File> downLoadFiles_FileTime(TaskBean tb, String tempDir, ArrayList<String> records) {
		
		//增加任务可终止标志
		GlobalBean.getInstance().setInterruptTask(taskId, this);
		exceptionInfo = null;
		
		ArrayList<File> downFileNames = new ArrayList<File>();
		try {
			this.connection();
			//add by run 2012-02-02
			if(isNoOp){
				this.startSendNoOp();
			}			
			if (ftpClient.changeWorkingDirectory(this.dynamicSourceDir(tb.getSourceDir()))) {
				//取当前时间前N天
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_MONTH, -(Integer.parseInt(tb.getBeforeDay())));
				
				FTPFile[] ftpFiles = ftpClient.listFiles();
				if (ftpFiles != null) {
					for (int i = 0; i < ftpFiles.length; i++) {
						//匹配正则表达式						
						if(ftpFiles[i].getName().matches(this.dynamicRegExp(tb.getRegExp()))){
							//记录文件包含此文件							
							if (!WorkFlow.isExistInRecord(ftpFiles[i].getName(),records)) {
								//FTP原始文件时间戳大于当前时间前N天
								if(ftpFiles[i].getTimestamp().after(c)){
									//检验文件大小在checkSleepTime/1000秒钟内会不会变化
									if(!StringUtils.isEmpty(tb.getCheckSleepTime()) && Long.parseLong(tb.getCheckSleepTime()) > 0){
										this.CheckFileIsUploadOver(ftpFiles[i].getName(), tb.getCheckSleepTime());
									}								
									
									File file = new File(tempDir + File.separator + ftpFiles[i].getName());
									
									//判断断点续传,2013-12-23//
									if(!isRBD && file.isFile()){
										file.delete();
										logger.warn("先删除{}文件,屏蔽其断点续传!",ftpFiles[i].getName());
									}
									//屏蔽断点续传,2013-12-23//
									
									if (!file.exists()) {
										new File(file.getParent()).mkdirs();
										file.createNewFile();
									}
									long pos = file.length();
									//随机存取文件
									raf = new RandomAccessFile(file,"rw");
									raf.seek(pos);
									ftpClient.setRestartOffset(pos);

									is = ftpClient.retrieveFileStream(ftpFiles[i].getName());
									if (is == null) {//这个问题还不知道怎么解决
										raf.close();
										Object[] paramArray = {ftpFiles[i].getName(),remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString()};
										logger.error("获取文件{}输入流异常,\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}",paramArray);
									} else {
										byte[] buf = new byte[bufSize];
										int len = 0;

										while ((len = is.read(buf)) != -1) {
											raf.write(buf, 0, len);
										}
										//关闭IO
										is.close();
										raf.close();
										//心跳连接,避免断掉
										//ftpClient.sendNoOp();
										if (ftpClient.completePendingCommand()) {
											logger.debug("{}成功下载!",ftpFiles[i].getName());
											downFileNames.add(file);
										} else {
											Object[] paramArray = {ftpFiles[i].getName(),remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString(), "RETR"};
											logger.error("文件{}下载失败,\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}\nFtp Command{}",paramArray);
										}
									}
								}else{
									String checkDate = new StringBuilder(c.get(Calendar.YEAR))
													.append("-")
													.append(c.get(Calendar.MONTH) + 1)
													.append("-")
													.append(c.get(Calendar.DATE)).toString();
									logger.warn("文件{},最后修改时间在{}时间之前,程序将不下载",ftpFiles[i].getName(),checkDate);
								}
							} else {
								//logger.warn("记录文件中已经包含此文件{},程序将不下载",ftpFiles[i].getName());						
							}
						}else{
							//logger.warn("文件{}不匹配正则表达式{},将不会下载", ftpFiles[i].getName(), tb.getRegExp());
						}						
					}
				} else {
					logger.warn("远程目录{}中没有文件,Ftp Command{}",tb.getSourceDir(),"NLST");
				}
			} else {
				Object[] paramArray = {tb.getSourceDir(), remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString(), "CWD"};
				logger.error("远程目录{}不能进入或者无法连接远程FTP服务,\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}\nFtp Command{}",paramArray);
				exceptionInfo = ftpClient.getReplyString();
			}
			ftpClient.logout();
			return downFileNames;			
		} catch (SocketTimeoutException e) {
			Object[] paramArray = {downFileNames.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经下载的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return downFileNames;
		} catch (IOException e) {
			Object[] paramArray = {downFileNames.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经下载的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return downFileNames;
		} catch (Exception e) {
			Object[] paramArray = {downFileNames.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经下载的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return downFileNames;
		} finally{
			this.close();
			//注销可终止任务
			GlobalBean.getInstance().getITTable().remove(taskId);
			//记录错误信息
			if(!StringUtils.isEmpty(exceptionInfo)){
				TWMonitorBean tw = GlobalBean.getInstance().getTWMonitorBean(taskId);
				if(tw != null){
					tw.setTaskStatus(TaskStatusEnum.TaskStatus_Exception.getStatus());
					tw.setTaskExp(TWMonitorBean.EXP_Exception, exceptionInfo);
					GlobalBean.getInstance().setTaskOfWork(tw);
				}
			}
			
		}				
	}
	
	/**
	 * 下载文件(普通)
	 * @param tb
	 * @param tempDir
	 * @param records
	 * @return
	 */
	public ArrayList<File> downLoadFiles_General(TaskBean tb, String tempDir, ArrayList<String> records) {
		
		//增加任务可终止标志
		GlobalBean.getInstance().setInterruptTask(taskId, this);
		exceptionInfo = null;
		
		ArrayList<File> downFileNames = new ArrayList<File>();
		try {
			this.connection();
			//add by run 2012-02-02
			if(isNoOp){
				this.startSendNoOp();
			}
			if (ftpClient.changeWorkingDirectory(this.dynamicSourceDir(tb.getSourceDir()))) {
				String[] names = ftpClient.listNames();
				if (names != null) {
					for (int i = 0; i < names.length; i++) {
						//匹配正则表达式
						if(names[i].matches(this.dynamicRegExp(tb.getRegExp()))){
							//记录文件包含此文件							
							if (!WorkFlow.isExistInRecord(names[i],records)) {								
								//检验文件大小在checkSleepTime/1000秒钟内会不会变化
								if(!StringUtils.isEmpty(tb.getCheckSleepTime()) && Long.parseLong(tb.getCheckSleepTime()) > 0){
									this.CheckFileIsUploadOver(names[i], tb.getCheckSleepTime());
								}
								
								File file = new File(tempDir + File.separator + names[i]);
								
								//判断断点续传,2013-12-23//
								if(!isRBD && file.isFile()){
									file.delete();
									logger.warn("先删除{}文件,屏蔽其断点续传!",names[i]);
								}
								//屏蔽断点续传,2013-12-23//
								
								if (!file.exists()) {
									new File(file.getParent()).mkdirs();
									file.createNewFile();
								}
								long pos = file.length();
								//随机存取文件
								raf = new RandomAccessFile(file,"rw");
								raf.seek(pos);
								ftpClient.setRestartOffset(pos);

								is = ftpClient.retrieveFileStream(names[i]);
								if (is == null) {//这个问题还不知道怎么解决
									raf.close();
									Object[] paramArray = {names[i],remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString()};
									logger.error("获取文件{}输入流异常,\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}",paramArray);
								} else {
									byte[] buf = new byte[bufSize];
									int len = 0;

									while ((len = is.read(buf)) != -1) {
										raf.write(buf, 0, len);
									}

									//关闭IO
									is.close();
									raf.close();
									//心跳连接,避免断掉
									//ftpClient.sendNoOp();
									if (ftpClient.completePendingCommand()) {
										logger.debug("{}成功下载!",names[i]);
										downFileNames.add(file);
									} else {
										Object[] paramArray = {names[i],remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString(), "RETR"};
										logger.error("文件{}下载失败,\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}\nFtp Command{}",paramArray);
									}
								}								
							} else {
								//logger.warn("记录文件中已经包含此文件{},程序将不下载",names[i]);						
							}
						}else{
							//logger.warn("文件{}不匹配正则表达式{},将不会下载", names[i], tb.getRegExp());
						}						
					}
				} else {
					logger.warn("远程目录{}中没有文件,Ftp Command{}",tb.getSourceDir(),"NLST");
				}
			} else {
				Object[] paramArray = {tb.getSourceDir(), remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString(), "CWD"};
				logger.error("远程目录{}不能进入或者无法连接远程FTP服务,\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}\nFtp Command{}",paramArray);
				exceptionInfo = ftpClient.getReplyString();
			}
			ftpClient.logout();
			return downFileNames;			
		} catch (SocketTimeoutException e) {
			Object[] paramArray = {downFileNames.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经下载的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return downFileNames;
		} catch (IOException e) {
			Object[] paramArray = {downFileNames.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经下载的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return downFileNames;
		} catch (Exception e) {
			Object[] paramArray = {downFileNames.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经下载的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:downLoadFiles\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return downFileNames;
		} finally{
			this.close();
			//注销可终止任务
			GlobalBean.getInstance().getITTable().remove(taskId);
			//记录错误信息
			if(!StringUtils.isEmpty(exceptionInfo)){
				TWMonitorBean tw = GlobalBean.getInstance().getTWMonitorBean(taskId);
				if(tw != null){
					tw.setTaskStatus(TaskStatusEnum.TaskStatus_Exception.getStatus());
					tw.setTaskExp(TWMonitorBean.EXP_Exception, exceptionInfo);
					GlobalBean.getInstance().setTaskOfWork(tw);
				}
				
			}
		}				
	}
		
	/**
	 * 删除FTP服务器上的文件
	 * @param delFileInDir
	 * @param fileNames
	 * @throws Exception 
	 */
	public ArrayList<String> delRemoteFile(String sourceDir, ArrayList<String> fileNames) {
		
		ArrayList<String> delRemoteFiles = new ArrayList<String>();
		if(fileNames == null || fileNames.size() == 0 ){
			return delRemoteFiles;
		}
		try {		
			this.connection();
			if (ftpClient.changeWorkingDirectory(sourceDir)) {
				for(String fileName : fileNames){
					if (!ftpClient.deleteFile(fileName)) {
						Object[] paramArray = {fileName,remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString()};
						logger.error("文件{}删除失败,\n动作:delRemoteFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}",paramArray);
					} else {
						delRemoteFiles.add(fileName);
						logger.debug("远程文件{}被成功删除:",fileName);
					}
				}
			} else {
				Object[] paramArray = {sourceDir, remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString()};
				logger.error("远程目录{}不能进入或者无法连接远程FTP服务,\n动作:delRemoteFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}",paramArray);
			}
			ftpClient.logout();
			return delRemoteFiles;
		} catch (SocketTimeoutException e) {
			Object[] paramArray = {delRemoteFiles.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经删除的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:delRemoteFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			return delRemoteFiles;
		} catch (IOException e) {
			Object[] paramArray = {delRemoteFiles.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经删除的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:delRemoteFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			return delRemoteFiles;
		} catch (Exception e) {
			Object[] paramArray = {delRemoteFiles.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经删除的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:delRemoteFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			return delRemoteFiles;
		} finally{
			this.close();
		}
		
	}
	
	
	/**
	* 获取服务器上的文件大小
	* 
	* @param fileName
	* @return
	*/
	private long getFileSize(String fileName) {
		try {
			StringBuffer command = new StringBuffer("size ");
			command.append(fileName);
			ftpClient.sendCommand(command.toString());
			if (ftpClient.getReplyCode() == 213) {
				String replyText = ftpClient.getReplyString().substring(4).trim();
				return Long.parseLong(replyText);
			} else {
				return 0;
			}
		} catch (Exception e) {
			Object[] paramArray = {fileName, remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString(),e.toString()};
			logger.error("获取文件{}大小异常,\n动作:getFileSize\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 检验文件是否是对方处理完毕文件
	 * 检验休眠时间,单位:毫秒(FTP取文件时,检验文件是否大小发生变化,值为0表示不校验)
	 * @param names
	 * @param checkSleepTime
	 */
	private void CheckFileIsUploadOver(String names, String checkSleepTime){		
		try {
			if(CommonTool.checkNull(checkSleepTime) || "0".equalsIgnoreCase(checkSleepTime)){
				return;
			}
			long sleepTime = Long.parseLong(checkSleepTime);
			long startSize = this.getFileSize(names);
			Thread.sleep(sleepTime);
			long endSize = this.getFileSize(names);
			if(startSize != endSize){
				Object[] paramArray = {names, sleepTime/1000, startSize, endSize};
				logger.warn("远程文件{}在{}秒内大小不一致,上次检查大小{},本次检查大小{},程序递归校验,直到在设定的检查时间间隔内大小保持一致",paramArray);
				this.CheckFileIsUploadOver(names, checkSleepTime);
			}		
		} catch (NumberFormatException e) {
			logger.error("检查远程文件是否是对方处理完毕文件异常,CheckSleepTime:{}\n异常信息:{}",checkSleepTime,e.toString());
			e.printStackTrace();
			return;
		} catch (Exception e) {
			logger.error("检查文件是否是对方处理完毕文件异常", e);
			e.printStackTrace();
		}		
	}
	
	/**
	 * 列出需要上传的文件已经在Ftp服务器上存在的
	 * @param remoteFiles
	 * @param file
	 * @return
	 */
	private FTPFile existsFile(FTPFile[] remoteFiles, File file) {
        for (int i = 0; i < remoteFiles.length; i++) {
            if (file.getName().equals(remoteFiles[i].getName())) {
                return remoteFiles[i];
            }
        }
        return null;
    }
	
	/**
	 * 创建远程目录
	 * @param destDir
	 * @return
	 * @throws IOException
	 * @throws FdException 
	 */
	private String isMakeDir(String destDir) throws IOException, FdException{
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
			boolean cd = ftpClient.makeDirectory(sb.toString());
			logger.info("创建远程目录{},{}",sb.toString(),cd);
			return sb.toString();
		}
		return destDir;
	}
	
	
	/**
	 * 动态原始目录
	 * @param destDir
	 * @return
	 * @throws IOException
	 * @throws FdException 
	 */
	private String dynamicSourceDir(String sourceDir) throws IOException, FdException{
		if(sourceDir.contains(customPathRegex)){
			List<String> cp = Arrays.asList(sourceDir.split(customPathRegex));
			List<String> dd = new ArrayList<String>(cp.size());
			StringBuilder sb = new StringBuilder();
			
			for(String s : cp){
				dd.add(this.customPath(s));
			}
			
			for(String s : dd){
				sb.append(s);
			}
			return sb.toString();
		}
		return sourceDir;
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
		if(s.startsWith("yyyy-MM-dd")){
			if(s.contains("_")){
				return TimeUtils.date2String(this.getDateBefore(new Date(), Integer.parseInt(s.split("_")[1])), "yyyy-MM-dd");
			}else if(s.contains("+")){
				return TimeUtils.date2String(this.getDateBefore(new Date(), -Integer.parseInt(s.split("+")[1])), "yyyy-MM-dd");
			}else{
				return TimeUtils.getCurrentTime("yyyy-MM-dd");
			}		
		}
		return s;
	}
	
	
	/**
	 * 动态正则表达式
	 * @param destDir
	 * @return
	 * @throws IOException
	 * @throws FdException 
	 */
	private String dynamicRegExp(String sourceDir) throws IOException, FdException{
		if(sourceDir.contains(customPathRegex)){
			List<String> cp = Arrays.asList(sourceDir.split(customPathRegex));
			List<String> dd = new ArrayList<String>(cp.size());
			StringBuilder sb = new StringBuilder();
			
			for(String s : cp){
				dd.add(this.customRegExp(s));
			}
			
			for(String s : dd){
				sb.append(s);
			}
			return sb.toString();
		}
		return sourceDir;
	}
	
	/**
	 * 自定义正则表达式
	 * @param s
	 * @return
	 */
	private String customRegExp(String s){
		if(s.startsWith("yyyyMMdd")){
			if(s.contains("-")){
				return TimeUtils.date2String(this.getDateBefore(new Date(), Integer.parseInt(s.split("-")[1])), "yyyyMMdd");
			}else if(s.contains("+")){
				return TimeUtils.date2String(this.getDateBefore(new Date(), -Integer.parseInt(s.split("+")[1])), "yyyyMMdd");
			}else{
				return TimeUtils.getCurrentTime("yyyyMMdd");
			}		
		}
		return s;
	}
	
	/**
	 * 上传文件
	 * @param destDir
	 * @param localFiles
	 * @param recordFile
	 * @return
	 */
	public ArrayList<File> uploadFile(String destDir, File[] localFiles, File recordFile) {
		
		//增加任务可终止标志
		GlobalBean.getInstance().setInterruptTask(taskId, this);
		exceptionInfo = null;
		
		ArrayList<File> uploadFiles = new ArrayList<File>();		
		try {
			this.connection();
			//add by run 2012-02-02
			if(isNoOp){
				this.startSendNoOp();
			}			
			//动态创建远程目录
			destDir = this.isMakeDir(destDir);
			if (ftpClient.changeWorkingDirectory(destDir)) {
				for(File lf : localFiles){
					if (lf.isFile()) {							
							// 先得到当前文件
							//ftpClient.initiateListParsing();
							FTPFile[] remoteFiles = ftpClient.listFiles();
	
							os = ftpClient.storeFileStream(lf.getName() + uploadPostfix);
							if (os != null) {
								raf = new RandomAccessFile(lf, "rw");
	
								// 如果远程文件存在,并且小于当前文件大小
								FTPFile remoteFile = existsFile(remoteFiles, lf);
								if (remoteFile != null && raf.length() >= remoteFile.getSize()) {
									raf.seek(remoteFile.getSize());
								}
	
								byte[] buf = new byte[bufSize];
								int len = 0;
	
								while ((len = raf.read(buf)) != -1) {
									os.write(buf, 0, len);
									os.flush();
								}
								
								//关闭IO
								raf.close();
								os.close();
								//心跳连接,避免断掉
								//ftpClient.sendNoOp();
								if (ftpClient.completePendingCommand()) {
									uploadFiles.add(lf);
									logger.debug("本地文件{}成功上传",lf.getName());
								} else {
									Object[] paramArray = {lf.getName(),remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString()};
									logger.error("本地文件{}上传失败,\n动作:uploadFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}",paramArray);
								}
								//临时后缀
								ftpClient.rename(lf.getName() + uploadPostfix, lf.getName());
								//记录成功上传的文件名称,一个一个的记录,避免相互干扰
								if(recordFile != null){
									MyFileUtil.StringToFile(recordFile, lf.getName()
											+ GlobalBean.Record_Delimiter, true);
								}						
							} else {
								Object[] paramArray = {lf.getName(),remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString()};
								logger.error("上传文件{}远程数据流异常,\n动作:uploadFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}",paramArray);
							}
						
					} else {
						logger.error("本地文件{},不存在",lf.getAbsolutePath());
					}					
				}
			} else {
				Object[] paramArray = {destDir, remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString()};
				logger.error("远程目录{}不能进入或者无法连接远程FTP服务,\n动作:uploadFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\nReplyCode:{}\nReplyString:{}",paramArray);
				exceptionInfo = ftpClient.getReplyString();
			}
			ftpClient.logout();
			return uploadFiles;			
		} catch (SocketTimeoutException e) {
			Object[] paramArray = {uploadFiles.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经上传的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:uploadFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return uploadFiles;
		} catch (IOException e) {
			Object[] paramArray = {uploadFiles.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经上传的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:uploadFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return uploadFiles;
		} catch (Exception e) {
			Object[] paramArray = {uploadFiles.size(), remoteIP, port, username, password, e.toString()};
			logger.error("\n程序将强制返回已经上传的文件列表{},防止由于网络的问题使处理逻辑限入低效率的循环\n动作:uploadFile\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n异常信息:{}",paramArray);
			e.printStackTrace();
			exceptionInfo = e.toString();
			return uploadFiles;
		} finally{
			this.close();
			//注销可终止任务
			GlobalBean.getInstance().getITTable().remove(taskId);
			//记录错误信息
			if(!StringUtils.isEmpty(exceptionInfo)){
				TWMonitorBean tw = GlobalBean.getInstance().getTWMonitorBean(taskId);
				if(tw != null){
					tw.setTaskStatus(TaskStatusEnum.TaskStatus_Exception.getStatus());
					tw.setTaskExp(TWMonitorBean.EXP_Exception, exceptionInfo);
					GlobalBean.getInstance().setTaskOfWork(tw);
				}
			}
		}		
	}
	
	/**
	 * 错误码打印
	 */
	public void getErrorReplyCode() {
		Object[] paramArray = {remoteIP, port, username, password, ftpClient.getReplyCode(), ftpClient.getReplyString().trim()};
    	logger.error("\nServerIp:{}\nServerPort:{}\nUser|PassWord:{}|{}\n操作失败,服务器返回:\n1.ReplyCode:{}\n2.ReplyString:{}",paramArray);
	}
	
	
	/**
	 * 单元测试方法
	 * 
	 * @param args
	 * @throws IOException
	 * @throws FdException 
	 */
	public static void main(String[] args) throws IOException, FdException {
	}
}
