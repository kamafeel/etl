package com.ai.ods.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;
import com.linkage.ftpdrudgery.db.ods.DBOperation_Ods;
import com.linkage.ftpdrudgery.db.ods.bean.OdsPubLog;
import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.file.FileUtil;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

/**
 * 装载数据_表增量
 * @author Run
 *
 */
public class LoadAddDisposer{
	
	private static Logger logger = LoggerFactory.getLogger(LoadAddDisposer.class);
	
	private Properties sysp;
	private FdPluginInfo fdp;	
	private OdsPubLog opl;	
	private String tableName;
	private String newTableName;
	private Date fileDate;
	private short odsFlag;
	//月表,日表标志
	private int tableType;
	//异常信息
	private String exceptionInfo;
	private boolean isOdsPubLog;
	private boolean isCreate;
	//存储HASH
	private String hashKey;
	private String tableSpace;
	
	/**
	 * 框架反射调用
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public File[] disposeFile(String pluginId, File[] files) throws Exception{
		fdp = GlobalBean.getInstance().getFdpMap().get(pluginId);
		this.loadProperties();
		for(File f : files){
			this.disposeFlowFile(f);
		}
		return null;
	}
	
	private void updateLogToOdsPubLog(){
		if(isOdsPubLog){
			opl.setEndTime(new Date());
			opl.setOdsFlag(odsFlag);
			DBOperation_Ods.getInstance().updateOdsPubLog(opl);
		}		
	}
	
	private void addLogToOdsPubLog(){
		if(isOdsPubLog){
			opl = new OdsPubLog();
			opl.setEtlDataCycle(tableType == 0 ? TimeUtils.date2String(fileDate, "yyyyMMdd") : TimeUtils.date2String(fileDate, "yyyyMM"));
			opl.setSchemaName(tableName.split("\\.")[0]);
			opl.setTableName(tableName.split("\\.")[1]);
			opl.setStartTime(new Date());		
			DBOperation_Ods.getInstance().addOdsPubLog(opl);
		}		
	}
	
	private void isCreateSus(String result) throws Exception{
		if(StringUtils.isEmpty(result) || result.contains("SQLSTATE")){
			this.updateLogToOdsPubLog();
			exceptionInfo = result;
		}
	}
	
	private void analysePluginInfo(String fileName) throws Exception{
		if(StringUtils.isEmpty(fdp.getPluginInfo())){
			throw new Exception("插件ID[" + fdp.getPluginId() + "]不包含插件信息");
		}
		String[] subs = fdp.getPluginInfo().split("\\,");
		tableName = subs[2];
		hashKey = subs[3];
		tableSpace = subs[4];
		//判断是否是月表
		if(!subs[1].contains("dd")){
			tableType = 1;
		}
		//按照配置分割文件名,获取文件时间
		fileDate = TimeUtils.string2Date(fileName.substring(Integer.parseInt(subs[0]),Integer.parseInt(subs[0]) + subs[1].length()), subs[1]);
		//是否要记录到OdsPubLog
		//isOdsPubLog = !(subs.length > 5);
		isOdsPubLog =subs[subs.length-1].equalsIgnoreCase("Y_OPL");
		if(fdp.getDateOffset() != 0){
			Calendar c = Calendar.getInstance();
			c.setTime(fileDate);
			c.add(Calendar.DAY_OF_MONTH, - fdp.getDateOffset());
			fileDate = c.getTime();
		}
		//判断入库日期记录文件是否存在
		File loadDateRecordFile = new File(sysp.getProperty("recordDir") + subs[2] + ".r");
		if(!loadDateRecordFile.isFile()){
			logger.warn("{}入库日期记录文件不存在,程序创建:{}",subs[2],loadDateRecordFile.createNewFile());
		}
		String recordDate = FileUtil.readFile(loadDateRecordFile.getAbsolutePath());
		if(TimeUtils.date2String(fileDate, "yyyy-MM-dd").equalsIgnoreCase(recordDate)){
			isCreate = false;
		}
		//更新入库日期记录文件
		logger.info("更新入库日期记录文件");
		FileUtil.writeStringToFile(loadDateRecordFile,TimeUtils.date2String(fileDate, "yyyy-MM-dd"), false);
		newTableName = tableName+"_"+TimeUtils.date2String(fileDate, "yyyyMMdd");
	}
	
	/**
	 * 处理文件
	 * @param f
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	private void disposeFlowFile(File f) throws Exception{
		
		//重置变量
		tableType = 0;
		odsFlag = 0;		
		exceptionInfo = null;
		isOdsPubLog = false;
		isCreate = true;
		
		if(f == null || !f.isFile()){
			logger.error("文件非法:{}",f.getAbsolutePath());
			return;
		}		
		String fileName = f.getName();
		logger.info("开始处理文件:{}",fileName);		
		this.analysePluginInfo(fileName);
				
		//记录数据库
		this.addLogToOdsPubLog();
		
		String result = null;
		String postfix = "";
		//创建表
		if(isCreate){
			logger.info("程序开始执行创建表:{}",newTableName);
			//拼装Shell参数
			String[] cmdc = {"sh", sysp.getProperty("db2Create"), fdp.getDb2Environment().split("\\,")[0], 
					fdp.getDb2Environment().split("\\,")[1], fdp.getDb2Environment().split("\\,")[2],newTableName,tableName,tableSpace,hashKey};
			result = RunCommand.runProcess(cmdc, sysp.getProperty("workDir"));
			logger.info("程序创建表:{},处理结果:{}",newTableName,result);	
			
			//验证创建表是否成功,不成功抛出异常
			this.isCreateSus(result);
			if(!StringUtils.isEmpty(exceptionInfo)){
				throw new Exception("操作文件[" + fileName + "],创建表失败:"+exceptionInfo);
			}
		}
		//解压
		for(Iterator it = GlobalBean.getInstance().getCompressConmandMap().entrySet().iterator();it.hasNext();){
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
			this.updateLogToOdsPubLog();
			return;
		}
		
		//预处理
		if(!StringUtils.isEmpty(fdp.getPerShell())){
			//替换动态值
			String cl = fdp.getPerShell().toString().replaceFirst("源文件", filePath);
			cl = cl.replaceFirst("文件帐期", tableType == 0 ? TimeUtils.date2String(fileDate, "yyyyMMdd") : TimeUtils.date2String(fileDate, "yyyyMM"));
			cl = cl.replaceFirst("目地文件", filePath + ".pres");
			//拼装命令
			String[] cmds = {"sh", sysp.getProperty("preShell"), cl};	
			result = RunCommand.runProcess(cmds,sysp.getProperty("workDir"));
			logger.info("预处理结果:{}",result);
			//预处理标志
			preShellFix = ".pres";
		}
		
		//装载数据
		if(!StringUtils.isEmpty(fdp.getdB2Statement())){
			if(StringUtils.isEmpty(fdp.getDb2Environment())){
				throw new Exception("插件ID[" + fdp.getPluginId() + "]不包含执行环境信息");
			}
			if(fdp.getDb2Environment().split("\\,").length != 3){
				throw new Exception("插件ID[" + fdp.getPluginId() + "]执行环境信息不正确");
			}
			
			//开始装置数据
			result = null;
			//替换动态值
			String cl = fdp.getdB2Statement().replaceFirst("数据文件路径", filePath + preShellFix);
			File loadLogDir = new File(sysp.getProperty("loadLogDir") + tableName + File.separator + TimeUtils.getCurrentTime("yyyyMMdd") + File.separator);
			//拼装命令_表增量
			cl = cl.replaceFirst("日志文件路径", loadLogDir.getAbsolutePath() + File.separator + fileName + ".log");
			cl = cl.replaceAll(tableName, newTableName);
			//load日志文件
			loadLogDir = new File(sysp.getProperty("loadLogDir") + newTableName + File.separator + TimeUtils.getCurrentTime("yyyyMMdd") + File.separator);
			if(!loadLogDir.isDirectory()){
				loadLogDir.mkdirs();
			}
			//拼装命令
			String[] cmds = {"sh", sysp.getProperty("db2Shell"), fdp.getDb2Environment().split("\\,")[0], 
					fdp.getDb2Environment().split("\\,")[1], fdp.getDb2Environment().split("\\,")[2],cl};
			result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
			logger.info("DB2开始装载数据:{},处理结果:{}",f.getAbsolutePath(),result);
			
		}
		
		//分析result
		odsFlag = this.analyseResult(result);
		logger.info("{},入库结果:{}",fileName,odsFlag);
		//更新ODS_PUB_LOG
		this.updateLogToOdsPubLog();
		this.recordImp(fileName);
		if(!StringUtils.isEmpty(exceptionInfo)){
			throw new Exception("操作文件[" + fileName + "],Load失败:"+exceptionInfo);
		}
	}
	
	private void recordImp(String fileName){
		File impFile = new File(sysp.getProperty("recordImp")+newTableName);
		if(!impFile.isFile()){
			try {
				impFile.createNewFile();
				FileUtil.writeStringToFile(impFile, "1:正常,0:错误,2:记录加载不完整\r\n", true);
			} catch (IOException e) {
				logger.error("创建重要文件记录{},异常",impFile.getName(),e);
			}
		}
		FileUtil.writeStringToFile(impFile, fileName+","+odsFlag+"\r\n", true);
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
	}
	
}
