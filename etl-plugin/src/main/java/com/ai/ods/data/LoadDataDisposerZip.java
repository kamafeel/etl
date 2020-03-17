package com.ai.ods.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.db.ods.DBOperation_Ods;
import com.linkage.ftpdrudgery.db.ods.bean.OdsPubLog;
import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.file.FileUtil;
import com.linkage.intf.file.ZipFileUtil;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

/**
 * 装载数据
 * @author Run
 *
 */
public class LoadDataDisposerZip {
	
	private static Logger logger = LoggerFactory.getLogger(LoadDataDisposerZip.class);
	/* 文件类型 */
	private HashMap<String,String> fphm;
	/* 数据更替 */
	private HashMap<String,String> rphm;
	/* 业务信息 */
	private HashMap<String,String> bphm;
	/* 预处理命令信息 */
	private HashMap<String,String> psphm;
	/* 文件帐期配置 */
	private HashMap<String,String> fdphm;
	
	private Properties sysp;	
	private OdsPubLog opl;
	
	private String tableName;
	private String schemaName;
	private boolean isGoOn;	
	private Date fileDate;
	private short odsFlag;
	//月表,日表标志
	private int tableType;
	//异常信息
	private String exceptionInfo;
	
	/**
	 * 框架反射调用
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public File[] disposeFile(File[] files) throws Exception{
		//装载Properties
		this.loadProperties();
		for(File f : files){
			//ZIP解压缩处理
			for(File fileUnZip : this.UnZip(f)){
				this.disposeFile(fileUnZip);
			}
			//清空临时处理文件夹
			FileUtil.delDir(f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-".zip".length()));
		}
		return null;
	}
		
	/**
	 * 解压缩ZIP
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private File[] UnZip(File f) throws IOException{
		String unZipFilePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-".zip".length());
		ZipFileUtil.getInstance().unZip(unZipFilePath,f.getAbsolutePath());		
		return new File(unZipFilePath).listFiles();		
	}
	
	/**
	 * 是否要清空表
	 * @param fileName
	 * @return
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 * @throws IOException 
	 */
	private void replace(String fileName) throws NumberFormatException, ParseException, IOException {
		for (Iterator it = rphm.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			//匹配文件名正则表达式
			if (fileName.matches(e.getKey().toString())) {
				String[] subs = e.getValue().toString().split("\\,");
				tableName = subs[2];
				//判断是否是月表
				if(!subs[1].contains("dd")){
					tableType = 1;
				}
				//按照配置分割文件名,获取文件时间
				fileDate = TimeUtils.string2Date(fileName.substring(Integer.parseInt(subs[0]),Integer.parseInt(subs[0]) + subs[1].length()), subs[1]);				
				//文件内容日期与文件名日期规则修正
				for(Iterator it_fd = fdphm.entrySet().iterator();it_fd.hasNext();){
					Map.Entry e_fd = (Map.Entry)it_fd.next();
					if(fileName.matches(e_fd.getKey().toString())){				
						Calendar c = Calendar.getInstance();
						c.setTime(fileDate);
						c.add(Calendar.DAY_OF_MONTH, -(Integer.parseInt(e_fd.getValue().toString())));
						fileDate = c.getTime();
						break;
					}
				}
				
//				//ODS表存储周期
//				String saveCycle = subs.length > 3 ? subs[3] : sysp.getProperty("saveCycle");
//				//清除表标志
//				boolean clear = false;
//				//判断入库日期记录文件是否存在
//				File loadDateRecordFile = new File(sysp.getProperty("recordDir") + subs[2] + ".r");
//				if(!loadDateRecordFile.isFile()){
//					logger.warn("{}入库日期记录文件不存在,程序创建:{}",subs[2],loadDateRecordFile.createNewFile());
//				}
//				String recordDate = FileUtil.readFile(loadDateRecordFile.getAbsolutePath());
//				if(StringUtils.isEmpty(recordDate)){
//					logger.warn("入库记录日期为空,程序写入当前文件时间");
//					logger.warn("入库记录日期为空,程序无法判断当前ODS表:{}的数据时间,所以将清空此表",tableName);
//					clear = true;
//				}else{
//					//文件日期在记录日期之前
//					if(fileDate.before(TimeUtils.string2Date(recordDate, "yyyy-MM-dd"))){
//						logger.error("当前文件日期比入库记录日期早,程序忽略后续步骤");
//						isGoOn =false;
//						return;
//					}else{
//						Date saveMaxCycle = this.getDateAfter(TimeUtils.string2Date(recordDate, "yyyy-MM-dd"), Integer.parseInt(saveCycle));
//						//最大存储时间大于当前文件日期
//						if(saveMaxCycle.after(fileDate)){
//							logger.info("当前文件日期属于当前存储周期内,程序继续执行命令");
//							return;
//						}else{
//							logger.info("文件日期大于ODS表{},最大存储周期,所以将清空此表",tableName);
//							clear = true;							
//						}
//					}															
//				}
//				if(clear){
//					//更新入库日期记录文件
//					logger.info("更新入库日期记录文件");
//					FileUtil.writeStringToFile(loadDateRecordFile,TimeUtils.date2String(fileDate, "yyyy-MM-dd"), false);
//					//清空ODS表
//					String cl = sysp.getProperty("replace").replaceFirst("表名", schemaName+"."+tableName);
//					String[] cmds = {"sh", sysp.getProperty("db2Shell"), sysp.getProperty("db2Name"), 
//							sysp.getProperty("user"), sysp.getProperty("password"),cl};
//					String result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
//					logger.info("DB2清空表:{},处理结果:{}",tableName,result);
//				}
				break;				
			}
		}
	}
	
	/**
	 * 得到几天后日期
	 * @return
	 */
	private Date getDateAfter(Date d, int day){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, day);
		return c.getTime();
	}
	
	private void updateLogToDB2(){
		opl.setEndTime(new Date());
		opl.setOdsFlag(odsFlag);
		DBOperation_Ods.getInstance().updateOdsPubLog(opl);
	}
	
	private void addLogToDB2(){
		opl = new OdsPubLog();
		opl.setEtlDataCycle(tableType == 0 ? TimeUtils.date2String(fileDate, "yyyyMMdd") : TimeUtils.date2String(fileDate, "yyyyMM"));
		opl.setSchemaName(schemaName);
		opl.setTableName(tableName);
		opl.setStartTime(new Date());		
		DBOperation_Ods.getInstance().addOdsPubLog(opl);
	}
	
	/**
	 * 处理文件
	 * @param f
	 * @return
	 * @throws Exception 
	 */
	private void disposeFile(File f) throws Exception{
		
		//重置变量
		tableType = 0;
		isGoOn = true;
		odsFlag = 0;		
		schemaName = sysp.getProperty("schema");
		exceptionInfo = null;
		
		if(f == null || !f.isFile()){
			logger.error("文件非法:{}",f.getAbsolutePath());
			return;
		}
		
		String fileName = f.getName();
		logger.info("开始处理文件:{}",fileName);
		
		this.replace(fileName);
		
		if(!isGoOn){
			logger.error("后续处理被终止:{}",f.getAbsolutePath());
			return;
		}
				
		//记录数据库
		this.addLogToDB2();
		
		String result = null;
		String postfix = "";
		//解压
		for(Iterator it = fphm.entrySet().iterator();it.hasNext();){
			Map.Entry e = (Map.Entry)it.next();
			if(fileName.endsWith(e.getKey().toString()) || fileName.endsWith(e.getKey().toString().toUpperCase())){
				String[] cmds = {e.getValue().toString(),fileName};				
				result = RunCommand.runProcess(cmds, f.getParent());
				logger.info("开始处理文件:{},处理结果:{}",f.getAbsolutePath(),result);
				postfix = e.getKey().toString();
				break;
			}	
		}
		
		if(StringUtils.isEmpty(postfix)){
			logger.info("文件:{},不需要解压",f.getAbsolutePath());
		}
		
		//解压后文件全路径
		String filePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-postfix.length());
		String preShellFix = "";
		
		if(new File(filePath).length() == 0l){
			logger.error("文件非法:{},文件大小:{}",filePath,f.length());
			//更新ODS_PUB_LOG
			odsFlag = 1;
			this.updateLogToDB2();
			return;
		}
				
		//预处理
		for(Iterator it = psphm.entrySet().iterator();it.hasNext();){
			Map.Entry e = (Map.Entry)it.next();
			if(fileName.matches(e.getKey().toString())){
				//替换动态值
				String cl = e.getValue().toString().replaceFirst("源文件", filePath);
				cl = cl.replaceFirst("文件帐期", tableType == 0 ? TimeUtils.date2String(fileDate, "yyyyMMdd") : TimeUtils.date2String(fileDate, "yyyyMM"));
				cl = cl.replaceFirst("目地文件", filePath + ".pres");
				//拼装命令
				String[] cmds = {"sh", sysp.getProperty("preShell"), cl};	
				result = RunCommand.runProcess(cmds,sysp.getProperty("workDir"));
				logger.info("预处理结果:{}",result);
				//预处理标志
				preShellFix = ".pres";
				break;
			}
		}
		
		//装载数据
		for(Iterator it = bphm.entrySet().iterator();it.hasNext();){
			Map.Entry e = (Map.Entry)it.next();
			if(fileName.matches(e.getKey().toString())){
				result = null;
				//替换动态值
				String cl = e.getValue().toString().replaceFirst("数据文件路径", filePath + preShellFix);
				File loadLogDir = new File(sysp.getProperty("loadLogDir") + tableName + File.separator + TimeUtils.getCurrentTime("yyyyMMdd") + File.separator);
				if(!loadLogDir.isDirectory()){
					loadLogDir.mkdirs();
				}
				cl = cl.replaceFirst("日志文件路径", loadLogDir.getAbsolutePath() + File.separator + fileName + ".log");
				//拼装命令
				String[] cmds = {"sh", sysp.getProperty("db2Shell"), sysp.getProperty("db2Name"), 
						sysp.getProperty("user"), sysp.getProperty("password"),cl};
				result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
				logger.info("DB2开始装载数据:{},处理结果:{}",f.getAbsolutePath(),result);
				break;
			}	
		}
		
		//分析result
		odsFlag = this.analyseResult(result);
		logger.info("{},入库结果:{}",fileName,odsFlag);
		//更新ODS_PUB_LOG
		this.updateLogToDB2();
		
		if(!StringUtils.isEmpty(exceptionInfo)){
			throw new Exception("操作文件[" + fileName + "],Load失败:"+exceptionInfo);
		}
	}
	
	/**
	 * 分析Load文件
	 * @param result
	 * @return
	 */
	private short analyseResult(String result){
		if(StringUtils.isEmpty(result)){
			logger.error("Shell执行结果:NULL,程序程序认为Load操作执行失败");
			exceptionInfo = "Shell执行结果:NULL,程序程序认为Load操作执行失败";
			return 0;
		}
		if(result.contains("SQLSTATE")){
			exceptionInfo = result;
			return 0;
		}
		
		if(StringUtils.countMatches(result, "+00000000") != 10){
			logger.warn("Shell执行结果:SQL Code不全为'+00000000',程序程序认为Load操作执行成功,但需分析Load日志");
		}
		
		int start  = result.indexOf("Summary of Partitioning Agents");
		int end  = result.indexOf("DB20000I  The TERMINATE command completed successfully");
		
		if((start >=0) && (end >= 0)){
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m  = p.matcher(result.substring(start, end));
			List<Long> LoadLogNum = new ArrayList<Long>();
			while (m.find()){
				LoadLogNum.add(Long.parseLong(m.group(1).toString()));				
			}
			if(LoadLogNum.size() < 8){
				logger.error("Shell执行结果 日志分析异常");
				exceptionInfo = "Shell执行结果 日志分析异常:" + result;
				return 0;
			}
			if(((LoadLogNum.get(0).longValue() == 0) || (LoadLogNum.get(2).longValue() == 0)) || (LoadLogNum.get(0).longValue() != LoadLogNum.get(2).longValue())){
				logger.debug("Rows Read:{},Rows Partitioned:{},不相同或者均等于0",LoadLogNum.get(0),LoadLogNum.get(2));
				return 2;
			}
			if(((LoadLogNum.get(3).longValue() ==0) || (LoadLogNum.get(5).longValue() ==0)) || (LoadLogNum.get(3).longValue() != LoadLogNum.get(5).longValue())){
				logger.debug("Number of rows read:{},Number of rows loaded:{},不相同或者均等于0",LoadLogNum.get(3),LoadLogNum.get(5));
				return 2;
			}
			if(((LoadLogNum.get(5).longValue() ==0) || (LoadLogNum.get(8).longValue() ==0)) || (LoadLogNum.get(5).longValue() != LoadLogNum.get(8).longValue())){
				logger.debug("Number of rows loaded:{},Number of rows committed:{},不相同或者均等于0",LoadLogNum.get(5),LoadLogNum.get(8));
				return 2;
			}
		}else{
			logger.error("Shell执行结果 日志分析异常");
			exceptionInfo = "Shell执行结果 日志分析异常:" + result;
			return 0;
		}		
		return 1;
	}
	
	
	private void loadProperties() throws IOException{
		
		//装载系统配置
		InputStream in = this.getClass().getResourceAsStream("system.properties");
		sysp = new Properties();
		sysp.load(in);
		
		in = this.getClass().getResourceAsStream("fileType.properties");		
		Properties p = new Properties();
		p.load(in);
		fphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			fphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("loadPara_Path")));		
		p.load(in);
		bphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			bphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("replace_Path")));		
		p.load(in);
		rphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			rphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("preshell_Path")));		
		p.load(in);
		psphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			psphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("filedate_Path")));		
		p.load(in);
		fdphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			fdphm.put(key, p.getProperty(key));
		}
		p.clear();
	}
	
	public static void main(String[] args) throws Exception {
		new LoadDataDisposerZip().loadProperties();
	}
}
