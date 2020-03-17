package com.linkage.ftpdrudgery.flow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.tools.CommonTool;
import com.linkage.ftpdrudgery.tools.FdException;
import com.linkage.ftpdrudgery.tools.MyFileUtil;
import com.linkage.ftpdrudgery.tools.RecordFileFilter;
import com.linkage.ftpdrudgery.tools.TimeProcessor;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;


/**
 * 工作流抽象类
 * @author run[zhangqi@lianchuang.com]
 * 10:50:05 AM May 26, 2009
 */
public abstract class WorkFlow {
	
	private static Logger logger = LoggerFactory.getLogger(WorkFlow.class);
	
	public TaskBean tb;
	
	public TWMonitorBean tw;
	
	public GlobalBean gb;	
	/* 存档ROOT目录 */
	public String backUpdir;	
	/* 记录文件目录 */
	public String recordDir;
	/* 源文件目录 */
	public File[] sourceFiles;	
	/*
	 * 动态路径分割符
	 */
	private final static String customPathRegex = "%";
	
	/**
	 * 构造函数
	 * @param tb
	 */
	public WorkFlow(TaskBean tb,TWMonitorBean tw) {
		this.tb = tb;
		this.tw = tw;
		this.gb = GlobalBean.getInstance();
		this.backUpdir = gb.getSystemBean().getBackUpDir() + File.separator + tb.getBackUpDir();
		this.recordDir = backUpdir + File.separator + GlobalBean.Record_FileDir;
		this.makeDir();
	}
	
	/**
	 * 备份原始文件
	 * @throws IOException 
	 */
	public void backUpSourceFiles() throws IOException{
				
		if(sourceFiles == null || sourceFiles.length == 0){
			logger.warn("任务[{}]无原始文件需要备份",CommonTool.getJobName(tb));
			return;
		}
		long Start = System.currentTimeMillis();
		//按日期备份
		String backUpTime = TimeProcessor.getCurrentTimeString();
		//备份原始文件
		String origDir = backUpdir + File.separator + GlobalBean.OrigDir + File.separator + backUpTime;	
		MyFileUtil.copyFile(sourceFiles, origDir);
		long End = System.currentTimeMillis();
		Object[] paramArray = {CommonTool.getJobName(tb),sourceFiles.length, (End - Start)/1000};
		logger.info("备份任务[{}]原始文件:{}个,耗时={}秒", paramArray);
	}
	
	/**
	 * 建立一些必要的文件夹
	 */
	public void makeDir(){
		MyFileUtil.makeDirs(backUpdir);
		MyFileUtil.makeDirs(recordDir);
	}
	
	/**
	 * 拼装数据为ArrayList
	 * @param files
	 * @return
	 */
	public ArrayList<String> array2ArrayList(File[] files){
		
		ArrayList<String> al = new ArrayList<String>();
		if(files == null || files.length == 0){
			return al;
		}

		for(File f : files){
			al.add(f.getName());
		}
		
		return al;
	}
			
	/**
	 * 拼装ArrayList为数组
	 * @return
	 */
	public File[] arrayList2Array(ArrayList<File> filesList){
		
		if(filesList == null || filesList.size() == 0){
			return new File[0];
		}			
		File[] files = new File[filesList.size()];
		int i = 0;
		for(File f : filesList){
			files[i] = f;
			i++;
		}		
		return files;
	}
	
	/**
	 * 排除UNIX系统文件
	 * @param al
	 * @return
	 */
	public File[] excludeUnixFile(File[] sourceFiles){
		
		ArrayList<File> al = new ArrayList<File>();		
		if(sourceFiles == null || sourceFiles.length == 0){
			return null;
		}
		
		for(File f : sourceFiles){
			if(!f.getName().matches("[.]{1}.+")){
				al.add(f);
			}else{
				logger.debug("文件名[{}]不符合正则表达式[.]{1}.+",f.getName());
			}
		}
		return this.arrayList2Array(al);
	}
	
	/**
	 * 剔除不符合正则表达式的文件
	 * @param al
	 * @return
	 */
	public File[] excludeUnMatches(File[] sourceFiles, String regExp){
		
		ArrayList<File> al = new ArrayList<File>();
		if(sourceFiles == null || sourceFiles.length == 0){
			return null;
		}
		
		for(File f : sourceFiles){
			if(f.getName().matches(regExp)){
				al.add(f);
			}else{
				logger.debug("文件名[{}]不符合正则表达式{}",f.getName(),regExp);
			}
		}	
		return this.arrayList2Array(al);
	}
	
	/**
	 * 剔除记录文件中已有的文件名
	 * @param al
	 * @param records
	 * @return
	 */
	public File[] excludeInRecords(File[] sourceFiles, ArrayList<String> records){
		
		ArrayList<File> al = new ArrayList<File>();
		if(sourceFiles == null || sourceFiles.length == 0){
			return null;
		}
		
		for(File f : sourceFiles){			
			if(!WorkFlow.isExistInRecord(f.getName(), records)){
				al.add(f);
			}else{
				logger.warn("文件名[{}]存在于记录文件中,程序将不做处理",f.getName());
			}
		}
	
		return this.arrayList2Array(al);
	}
	
	
	
	
	/**
	 * 获取记录
	 * @param recorddir
	 * @param all
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public ArrayList<String> getRecords(boolean all) throws IOException, Exception{
		logger.info("-------------------------------开始获取记录文件");
		if(all){
			if(!StringUtils.isEmpty(tb.getRecordValidDay()) && Integer.parseInt(tb.getRecordValidDay()) > 0){
				logger.info("-------------------------------记录文件有效期:{}",tb.getRecordValidDay());
				logger.info("-------------------------------记录文件区间:{}",this.getDateFewDays(Integer.parseInt(tb.getRecordValidDay())));
				return MyFileUtil.readFilesByDelimiterToArrayList(MyFileUtil.getFilesFilter(recordDir,new RecordFileFilter(this.getDateFewDays(Integer.parseInt(tb.getRecordValidDay())))), GlobalBean.Record_Delimiter);
			}else{
				return MyFileUtil.readFilesByDelimiterToArrayList(MyFileUtil.getFiles(recordDir), GlobalBean.Record_Delimiter);
			}			
		}else{
			File todayRecordFile = new File(recordDir + 
					File.separator + TimeProcessor.getCurrentTimeString() + GlobalBean.Source_Record_Postfix);
			File yesterdayRecordFile = new File(recordDir + 
					File.separator + TimeProcessor.getYesterdayDate() + GlobalBean.Source_Record_Postfix);			
			File[] recordFiles = {todayRecordFile, yesterdayRecordFile};		
			return MyFileUtil.readFilesByDelimiterToArrayList(recordFiles, GlobalBean.Record_Delimiter);
		}		
	}
	
	/**
	 * 得到今天的记录文件
	 * @param recorddir
	 * @return
	 * @throws IOException 
	 */
	public File getTodayRecord() throws IOException{
		File todayRecordFile = new File(recordDir + File.separator + 
				TimeProcessor.getCurrentTimeString() + GlobalBean.Source_Record_Postfix);
		if(!todayRecordFile.isFile()){
			try {
				todayRecordFile.createNewFile();
			} catch (IOException e) {
				throw e;
			}
		}
		return todayRecordFile;
	}
	
	/**
	 * 得到今天回执文件的记录文件
	 * @param recorddir
	 * @return
	 * @throws IOException 
	 */
	public File getReturnRecord() throws IOException{
		File returnRecordFile = new File(recordDir + 
				File.separator + TimeProcessor.getCurrentTimeString() + GlobalBean.Return_Record_Postfix);
		if(!returnRecordFile.isFile()){
			try {
				returnRecordFile.createNewFile();
			} catch (IOException e) {
				throw e;
			}
		}
		return returnRecordFile;
	}
	
	/**
	 * 记录文件
	 * @param files
	 * @throws IOException 
	 */
	public void recordFile(File[] files) throws IOException{
		if(files == null || files.length == 0){
			return;
		}
		StringBuilder sb = new StringBuilder();
		for(File f : files){
			sb.append(f.getName());
			sb.append(GlobalBean.Record_Delimiter);
			logger.debug("开始记录操作成功的文件名:{}",f.getName());
		}
		MyFileUtil.StringToFile(this.getTodayRecord(), sb.toString(), true);
	}
	
	/**
	 * 记录回执文件
	 * @param files
	 * @throws IOException 
	 */
	public void recordReturnFile(File[] files) throws IOException{
		if(files == null || files.length == 0){
			return;
		}
		StringBuilder sb = new StringBuilder();
		for(File f : files){
			sb.append(f.getName());
			sb.append(GlobalBean.Record_Delimiter);
			logger.debug("开始记录操作成功的文件名:{}",f.getName());
		}
		MyFileUtil.StringToFile(this.getReturnRecord(), sb.toString(), true);
	}
	
	/**
	 * 验证插件信息时候完整
	 * @param tb
	 * @return
	 * @throws Exception 
	 */
	public boolean checkPlugin(){
		if(CommonTool.checkNull(tb.getPluginName()) ||
				CommonTool.checkNull(tb.getPluginPath()) ||
				CommonTool.checkNull(tb.getPluginClassPath()) ||
				CommonTool.checkNull(tb.getPluginId())){
			return false;
		}
		return true;
	}
	
	/**
	 * 拼装文件组文件名,监控用
	 * @param files
	 * @return
	 */
	public String appendFileName(File[] files){
		StringBuilder sb = new StringBuilder();
		if(files != null){
			for(File f : files){
				sb.append(f.getName());
				sb.append(GlobalBean.Record_Delimiter);
			}
		}
		if(sb.length() > 30000){
			logger.error("需入库文件名字符串大于30000,程序将截断>30000的数据\n{}",sb.toString());
			sb.delete(30000, sb.length());
		}
		return sb.toString();
	}
	
	/**
	 * 动态正则表达式
	 * @param destDir
	 * @return
	 * @throws IOException
	 * @throws FdException 
	 */
	public String dynamicRegExp(String sourceDir) throws IOException, FdException{
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
	
	private List<String> getDateFewDays(int i){
		List<String> l = new ArrayList<String>();
		Date d = new Date();
		l.add(TimeUtils.date2String(d,"yyyyMMdd"));
		int j =0;
		while(j<(i-1)){
			j++;
			l.add(TimeUtils.date2String(this.getDateBefore(d,j),"yyyyMMdd"));
		}
		return l;
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
	 * 得到几天前日期
	 * @return
	 */
	private Date getDateBefore(Date d, int day){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, -day);
		return c.getTime();
	}
	
	public static boolean isExistInRecord(String fileName,ArrayList<String> records){
		StringBuilder sb = new StringBuilder("*");
		sb.append(fileName).append("*");		
		return StringUtils.wildcardMatchList(sb.toString(), records);
	}
	
//	/**
//	 * 插件异常打印
//	 */
//	public void printPlugin(Exception e){
//		StringBuilder sb = new StringBuilder();
//		sb.append("任务[" + tb.getTaskName() + "]");
//		sb.append("插件[" + tb.getPluginName() + "]生成文件数量为空或者插件异常!");
//		if(e != null){
//			logger.error(sb.toString(), e);
//		}else{
//			logger.error(sb.toString());
//		}		
//	}
	
}
