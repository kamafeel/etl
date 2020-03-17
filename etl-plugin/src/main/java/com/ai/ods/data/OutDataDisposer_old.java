package com.ai.ods.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.tools.FdException;
import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.file.FileUtil;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;


/**
 * 导出数据
 * @author Run
 *
 */
public class OutDataDisposer_old {
	
	private static Logger logger = LoggerFactory.getLogger(OutDataDisposer_old.class);
	/* 数据库配置 */
	private HashMap<String,String> dbphm;
	/* 导出表导出配置 */
	private HashMap<String,String> expphm;
	/* 导出表业务信息 */
	private HashMap<String,String> ofphm;
	/* 导出表后续处理命令信息 */
	private HashMap<String,String> fsphm;
	/* 导出表后续处理命令信息 */
	private HashMap<String,String> umphm;
	/* 系统配置 */
	private Properties sysp;
	/* 导出文件临时后缀 */
	private final static String outFileFix = ".exp";
	private final static String customPathRegex = "%";
	/**
	 * 框架反射调用
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public File[] produceFile(String taskTag) throws Exception{
		//装载Properties
		this.loadProperties();
		//插件生成文件列表
		List<File> outFiles = new ArrayList<File>();
		String mapValue_ = null;
		for(Iterator it = umphm.entrySet().iterator(); it.hasNext();){
			Map.Entry e = (Map.Entry) it.next();
			if (taskTag.equalsIgnoreCase(e.getKey().toString())) {
				mapValue_ = e.getValue().toString();
			}
		}
		
		for (Iterator it_expphm = expphm.entrySet().iterator(); it_expphm.hasNext();) {
			Map.Entry e_expphm = (Map.Entry) it_expphm.next();
			if(mapValue_.equalsIgnoreCase(e_expphm.getKey().toString())){
				String expKey = e_expphm.getKey().toString();
				String expValue = e_expphm.getValue().toString();
				
				//是否要导出
				boolean exp = true;
				//拼装表名
				String expTableName = null;
				//拼装导出文件名
				String outFileName = null;
				
				//导出表业务信息,导出数据日期判断
				for(Iterator it_ofphm = ofphm.entrySet().iterator();it_ofphm.hasNext();){
					Map.Entry e_ofphm = (Map.Entry)it_ofphm.next();
					if(expKey.equals(e_ofphm.getKey().toString())){
						String[] subs = e_ofphm.getValue().toString().split("\\,");
						//数据帐期
						Date dataDate = null;
						//判断是否是月表
						boolean isMon = false;
						if(!subs[0].contains("dd")){
							dataDate = this.getDateBeforeMONTH(new Date(), Integer.parseInt(subs[3]));
							isMon = true;
						}else{
							dataDate = this.getDateBefore(new Date(), Integer.parseInt(subs[3]));
						}
						//判断入库日期记录文件是否存在
						File expDateRecordFile = new File(sysp.getProperty("recordDir") + expKey + ".r");
						if(!expDateRecordFile.isFile()){
							logger.warn("{}入库日期记录文件不存在,程序创建:{}",expKey,expDateRecordFile.createNewFile());
						}
						String recordDate = FileUtil.readFile(expDateRecordFile.getAbsolutePath());
						if(StringUtils.isEmpty(recordDate)){
							logger.warn("导出记录日期为空,程序写入当前文件时间");
							FileUtil.writeStringToFile(expDateRecordFile,TimeUtils.date2String(dataDate, isMon ? "yyyy-MM" : "yyyy-MM-dd"), false);
						}else{
							//导出日期和记录日期一致或在其之前,不导出
							if(TimeUtils.date2String(dataDate, isMon ? "yyyy-MM" : "yyyy-MM-dd").equals(recordDate) 
									|| dataDate.before(TimeUtils.string2Date(recordDate, isMon ? "yyyy-MM" : "yyyy-MM-dd"))){
								exp = false;
								break;
							}else{
								//更新记录文件
								FileUtil.writeStringToFile(expDateRecordFile,TimeUtils.date2String(dataDate, isMon ? "yyyy-MM" : "yyyy-MM-dd"), false);
							}
						}					
						//表名
						expTableName = e_ofphm.getKey().toString().replace(subs[0], TimeUtils.date2String(dataDate, subs[0]));
						//导出文件名
						outFileName = subs[1].replace("日期", TimeUtils.date2String(dataDate, subs[2]));
						break;
					}
				}
				if(exp){
					//执行导出命令
					File loadLogDir = new File(sysp.getProperty("exportLogDir") + expTableName + File.separator + TimeUtils.getCurrentTime("yyyyMMdd") + File.separator);
					if(!loadLogDir.isDirectory()){
						loadLogDir.mkdirs();
					}
					//导出文件
					File outFile = new File(sysp.getProperty("workDir") + "temp" + File.separator + outFileName + outFileFix);
//					if(!outFile.getParentFile().isDirectory()){
//						outFile.getParentFile().mkdirs();
//					}
					expValue = expValue.replaceFirst("日志文件路径", loadLogDir.getAbsolutePath() + File.separator + outFileName + ".log");
					expValue = expValue.replaceFirst("数据文件路径", outFile.getAbsolutePath());
					expValue = expValue.replaceFirst("表名", expTableName);
					
					//日期灵活处理
					expValue = this.dynamicReplace(expValue);
					
					//数据库配置信息
					for(Iterator it_dbphm = dbphm.entrySet().iterator();it_dbphm.hasNext();){
						Map.Entry e_dbphm = (Map.Entry)it_dbphm.next();
						if(expKey.split("\\.")[0].equals(e_dbphm.getKey().toString())){
							String[] subs = e_dbphm.getValue().toString().split("\\,");
							//数据名称
							String db2Name = subs[0];
							//用户名
							String user = subs[1];
							//密码
							String password = subs[2];
							//拼装命令
							String[] cmds = {"sh", sysp.getProperty("db2Shell"), db2Name, user, password,expValue};
							String result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
							logger.info("DB2开始导出数据:{},处理结果:{}",outFile.getAbsolutePath(),result);
							break;
						}
					}
					
					//后续处理
					for(Iterator it_fsphm = fsphm.entrySet().iterator();it_fsphm.hasNext();){
						Map.Entry e_fsphm = (Map.Entry)it_fsphm.next();
						if(expKey.equals(e_fsphm.getKey().toString())){
							//替换动态值
							String cl = e_fsphm.getValue().toString().replaceFirst("源文件", outFile.getAbsolutePath());
							cl = cl.replaceFirst("目地文件", outFile.getAbsolutePath().substring(0,outFile.getAbsolutePath().length() - outFileFix.length()));
							//拼装命令
							String[] cmds = {"sh", sysp.getProperty("followShell"), cl};	
							String result = RunCommand.runProcess(cmds,sysp.getProperty("workDir"));
							logger.info("后续处理结果:{}",result);						
							break;
						}
					}				
					outFiles.add(new File(outFile.getAbsolutePath().substring(0,outFile.getAbsolutePath().length() - outFileFix.length())));
					outFile.delete();
				}
				
			}
			
		}
		return this.arrayList2Array(outFiles);		
	}
	
	
	private String dynamicReplace(String source) throws IOException, FdException{
		if(source.contains(customPathRegex)){
			List<String> cp = Arrays.asList(source.split(customPathRegex));
			//System.out.println(cp);
			List<String> dd = new ArrayList<String>(cp.size());
			StringBuilder sb = new StringBuilder();
			
			for(String s : cp){
				dd.add(this.customDate(s));
			}
			
			for(String s : dd){
				sb.append(s);
			}
			return sb.toString();
		}
		return source;
	}
	
	
	/**
	 * 动态日期处理
	 * @param s
	 * @return
	 */
	private String customDate(String s){
		String dateFormat = null;
		if(s.startsWith("yyyyMMdd")){
			dateFormat=	"yyyyMMdd";
		}else if(s.startsWith("yyyy-MM-dd")){
			dateFormat=	"yyyy-MM-dd";
		}else if(s.startsWith("yyyyMM")){
			dateFormat=	"yyyyMM";
		}else if(s.startsWith("yyyy-MM")){
			dateFormat=	"yyyy-MM";
		}
		
		if(StringUtils.isEmpty(dateFormat)){
			return s;
		}
		if(s.contains("减")){
			return TimeUtils.date2String(this.getDateBefore(new Date(), Integer.parseInt(s.split("减")[1])), dateFormat);
		}else if(s.contains("加")){
			return TimeUtils.date2String(this.getDateBefore(new Date(), -Integer.parseInt(s.split("加")[1])), dateFormat);
		}else{
			return TimeUtils.getCurrentTime(dateFormat);
		}	
	}
	
	
	/**
	 * 拼装ArrayList为数组
	 * @return
	 */
	public File[] arrayList2Array(List<File> filesList){
		
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
	 * 得到几月前日期
	 * @return
	 */
	private Date getDateBeforeMONTH(Date d, int mon){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, -mon);
		return c.getTime();
	}
	
	private void loadProperties() throws IOException{
		
		//装载系统配置
		InputStream in = this.getClass().getResourceAsStream("system.properties");
		sysp = new Properties();
		sysp.load(in);
		
		Properties p = new Properties();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("exportPara_Path")));		
		p.load(in);
		expphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			expphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("followShell_Path")));		
		p.load(in);
		fsphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			fsphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("outFile_Path")));		
		p.load(in);
		ofphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			ofphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("database_Path")));		
		p.load(in);
		dbphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			dbphm.put(key, p.getProperty(key));
		}
		p.clear();
		
		in = new BufferedInputStream(new FileInputStream(sysp.getProperty("outMap_Path")));		
		p.load(in);
		umphm = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			umphm.put(key, p.getProperty(key));
		}
		p.clear();
		
	}
	
	
	/**
	 * @param args
	 * @throws FdException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, FdException {
		// TODO Auto-generated method stub
		//String a ="export to 数据文件路径 of del modified by nochardel coldel, striplzeros messages 日志文件路径 SELECT PHONE_NO,C_SMS_NUM,C_MMS_NUM,POINTS,DENSERANK() OVER(ORDER by POINTS desc) AS RK,NVL(ETL_DATA_CYCLE,'%yyyy-MM-dd减2%') AS ETL_DATA_CYCLE FROM VGOP_DWD.DWD_SMS_MMS_PARTY_POINT_%yyyyMM% WHERE ETL_DATA_CYCLE='%yyyyMMdd减2%';";
		
		
		//System.out.println(new OutDataDisposer().dynamicReplace(a));
	}

}
