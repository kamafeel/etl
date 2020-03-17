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
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

/**
 * װ������
 * @author ����
 *
 */
public class LoadDataDisposer {
	
	private static Logger logger = LoggerFactory.getLogger(LoadDataDisposer.class);
	/* �ļ����� */
	private HashMap<String,String> fphm;
	/* ���ݸ��� */
	private HashMap<String,String> rphm;
	/* ҵ����Ϣ */
	private HashMap<String,String> bphm;
	/* Ԥ����������Ϣ */
	private HashMap<String,String> psphm;
	/* �ļ��������� */
	private HashMap<String,String> fdphm;
	
	private Properties sysp;	
	private OdsPubLog opl;
	
	private String tableName;
	private String schemaName;
	private boolean isGoOn;	
	private Date fileDate;
	private short odsFlag;
	//�±�,�ձ��־
	private int tableType;
	//�쳣��Ϣ
	private String exceptionInfo;
	private boolean isToDB;
	/**
	 * ��ܷ������
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public File[] disposeFile(File[] files) throws Exception{
		//װ��Properties
		this.loadProperties();
		for(File f : files){
			this.disposeFile(f);
		}
		return null;
	}
	
	/**
	 * �Ƿ�Ҫ��ձ�
	 * @param fileName
	 * @return
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 * @throws IOException 
	 */
	@SuppressWarnings("rawtypes")
	private void replace(String fileName) throws NumberFormatException, ParseException, IOException {
		for (Iterator it = rphm.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			//ƥ���ļ���������ʽ
			if (fileName.matches(e.getKey().toString())) {
				String[] subs = e.getValue().toString().split("\\,");
				tableName = subs[2];
				//�ж��Ƿ����±�
				if(!subs[1].contains("dd")){
					tableType = 1;
				}
				//�������÷ָ��ļ���,��ȡ�ļ�ʱ��
				fileDate = TimeUtils.string2Date(fileName.substring(Integer.parseInt(subs[0]),Integer.parseInt(subs[0]) + subs[1].length()), subs[1]);				
				//�Ƿ�Ҫ����¼
				isToDB = !(subs.length > 3);
				//�ļ������������ļ������ڹ�������
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
				break;				
			}
		}
	}
	
	private void updateLogToDB2(){
		if(!isToDB){
			return;
		}
		opl.setEndTime(new Date());
		opl.setOdsFlag(odsFlag);
		DBOperation_Ods.getInstance().updateOdsPubLog(opl);
	}
	
	private void addLogToDB2(){
		if(!isToDB){
			return;
		}
		opl = new OdsPubLog();
		opl.setEtlDataCycle(tableType == 0 ? TimeUtils.date2String(fileDate, "yyyyMMdd") : TimeUtils.date2String(fileDate, "yyyyMM"));
		opl.setSchemaName(schemaName);
		opl.setTableName(tableName);
		opl.setStartTime(new Date());		
		DBOperation_Ods.getInstance().addOdsPubLog(opl);
	}
	
	/**
	 * �����ļ�
	 * @param f
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	private void disposeFile(File f) throws Exception{
		
		//���ñ���
		tableType = 0;
		isGoOn = true;
		odsFlag = 0;		
		schemaName = sysp.getProperty("schema");
		exceptionInfo = null;
		isToDB = true;
		
		if(f == null || !f.isFile()){
			logger.error("�ļ��Ƿ�:{}",f.getAbsolutePath());
			return;
		}
		
		String fileName = f.getName();
		logger.info("��ʼ�����ļ�:{}",fileName);
		
		this.replace(fileName);
		
		if(!isGoOn){
			logger.error("����������ֹ:{}",f.getAbsolutePath());
			return;
		}
				
		//��¼���ݿ�
		this.addLogToDB2();
		
		String result = null;
		String postfix = "";
		//��ѹ
		for(Iterator it = fphm.entrySet().iterator();it.hasNext();){
			Map.Entry e = (Map.Entry)it.next();
			if(fileName.endsWith(e.getKey().toString()) || fileName.endsWith(e.getKey().toString().toUpperCase())){
				String[] cmds = {e.getValue().toString(),fileName};				
				result = RunCommand.runProcess(cmds, f.getParent());
				logger.info("��ʼ�����ļ�:{},������:{}",f.getAbsolutePath(),result);
				postfix = e.getKey().toString();
				break;
			}	
		}
		
		if(StringUtils.isEmpty(postfix)){
			logger.info("�ļ�:{},����Ҫ��ѹ",f.getAbsolutePath());
		}
		
		//��ѹ���ļ�ȫ·��
		String filePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-postfix.length());
		String preShellFix = "";
		
		if(new File(filePath).length() == 0l){
			logger.error("�ļ��Ƿ�:{},�ļ���С:{}",filePath,f.length());
			//����ODS_PUB_LOG
			odsFlag = 1;
			this.updateLogToDB2();
			return;
		}
				
		//Ԥ����
		for(Iterator it = psphm.entrySet().iterator();it.hasNext();){
			Map.Entry e = (Map.Entry)it.next();
			if(fileName.matches(e.getKey().toString())){
				//�滻��ֵ̬
				String cl = e.getValue().toString().replaceFirst("Դ�ļ�", filePath);
				cl = cl.replaceFirst("�ļ�����", tableType == 0 ? TimeUtils.date2String(fileDate, "yyyyMMdd") : TimeUtils.date2String(fileDate, "yyyyMM"));
				cl = cl.replaceFirst("Ŀ���ļ�", filePath + ".pres");
				//ƴװ����
				String[] cmds = {"sh", sysp.getProperty("preShell"), cl};	
				result = RunCommand.runProcess(cmds,sysp.getProperty("workDir"));
				logger.info("Ԥ������:{}",result);
				//Ԥ�����־
				preShellFix = ".pres";
				break;
			}
		}
		
		//װ������
		for(Iterator it = bphm.entrySet().iterator();it.hasNext();){
			Map.Entry e = (Map.Entry)it.next();
			if(fileName.matches(e.getKey().toString())){
				result = null;
				//�滻��ֵ̬
				String cl = e.getValue().toString().replaceFirst("�����ļ�·��", filePath + preShellFix);
				File loadLogDir = new File(sysp.getProperty("loadLogDir") + tableName + File.separator + TimeUtils.getCurrentTime("yyyyMMdd") + File.separator);
				if(!loadLogDir.isDirectory()){
					loadLogDir.mkdirs();
				}
				cl = cl.replaceFirst("��־�ļ�·��", loadLogDir.getAbsolutePath() + File.separator + fileName + ".log");
				//ƴװ����
				String[] cmds = {"sh", sysp.getProperty("db2Shell"), sysp.getProperty("db2Name"), 
						sysp.getProperty("user"), sysp.getProperty("password"),cl};
				result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
				logger.info("DB2��ʼװ������:{},������:{}",f.getAbsolutePath(),result);
				break;
			}	
		}
		
		//����result
		odsFlag = this.analyseResult(result);
		logger.info("{},�����:{}",fileName,odsFlag);
		//����ODS_PUB_LOG
		this.updateLogToDB2();
		
		if(!StringUtils.isEmpty(exceptionInfo)){
			throw new Exception("�����ļ�[" + fileName + "],Loadʧ��:"+exceptionInfo);
		}
	}
	
	/**
	 * ����Load�ļ�
	 * @param result
	 * @return
	 */
	private short analyseResult(String result){
		if(StringUtils.isEmpty(result)){
			logger.error("Shellִ�н��:NULL,���������ΪLoad����ִ��ʧ��");
			exceptionInfo = "Shellִ�н��:NULL,���������ΪLoad����ִ��ʧ��";
			return 0;
		}
		if(result.contains("SQLSTATE")){
			exceptionInfo = result;
			return 0;
		}
		
		if(StringUtils.countMatches(result, "+00000000") != 10){
			logger.warn("Shellִ�н��:SQL Code��ȫΪ'+00000000',���������ΪLoad����ִ�гɹ�,�������Load��־");
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
				logger.error("Shellִ�н�� ��־�����쳣");
				exceptionInfo = "Shellִ�н�� ��־�����쳣:" + result;
				return 0;
			}
			if(((LoadLogNum.get(0).longValue() == 0) || (LoadLogNum.get(2).longValue() == 0)) || (LoadLogNum.get(0).longValue() != LoadLogNum.get(2).longValue())){
				logger.debug("Rows Read:{},Rows Partitioned:{},����ͬ���߾�����0",LoadLogNum.get(0),LoadLogNum.get(2));
				return 2;
			}
			if(((LoadLogNum.get(3).longValue() ==0) || (LoadLogNum.get(5).longValue() ==0)) || (LoadLogNum.get(3).longValue() != LoadLogNum.get(5).longValue())){
				logger.debug("Number of rows read:{},Number of rows loaded:{},����ͬ���߾�����0",LoadLogNum.get(3),LoadLogNum.get(5));
				return 2;
			}
			if(((LoadLogNum.get(5).longValue() ==0) || (LoadLogNum.get(8).longValue() ==0)) || (LoadLogNum.get(5).longValue() != LoadLogNum.get(8).longValue())){
				logger.debug("Number of rows loaded:{},Number of rows committed:{},����ͬ���߾�����0",LoadLogNum.get(5),LoadLogNum.get(8));
				return 2;
			}
		}else{
			logger.error("Shellִ�н�� ��־�����쳣");
			exceptionInfo = "Shellִ�н�� ��־�����쳣:" + result;
			return 0;
		}		
		return 1;
	}
	
	
	@SuppressWarnings("rawtypes")
	private void loadProperties() throws IOException{
		
		//װ��ϵͳ����
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
		new LoadDataDisposer().loadProperties();
	}
}
