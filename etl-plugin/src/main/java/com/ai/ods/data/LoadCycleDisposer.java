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

public class LoadCycleDisposer{
	
	private static Logger logger = LoggerFactory.getLogger(LoadCycleDisposer.class);
	
	private Properties sysp;
	private FdPluginInfo fdp;	
	private OdsPubLog opl;	
	private String tableName;
	private Date fileDate;
	private short odsFlag;
	//�±�,�ձ��־
	private int tableType;
	//�쳣��Ϣ
	private String exceptionInfo;
	private boolean isOdsPubLog;
	private boolean isGoOn;
	
	/**
	 * ��ܷ������
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
	
	/**
	 * �õ����������
	 * @return
	 */
	private Date getDateAfter(Date d, int day){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, day);
		return c.getTime();
	}
	
	private void analysePluginInfo(String fileName) throws Exception{
		if(StringUtils.isEmpty(fdp.getPluginInfo())){
			throw new Exception("���ID[" + fdp.getPluginId() + "]�����������Ϣ");
		}
		String[] subs = fdp.getPluginInfo().split("\\,");
		tableName = subs[2];
		//�ж��Ƿ����±�
		if(!subs[1].contains("dd")){
			tableType = 1;
		}
		//�������÷ָ��ļ���,��ȡ�ļ�ʱ��
		fileDate = TimeUtils.string2Date(fileName.substring(Integer.parseInt(subs[0]),Integer.parseInt(subs[0]) + subs[1].length()), subs[1]);
		//�Ƿ�Ҫ��¼��OdsPubLog
		//isOdsPubLog = !(subs.length > 4);
		isOdsPubLog = subs[subs.length-1].equalsIgnoreCase("Y_OPL");
		if(fdp.getDateOffset() != 0){
			Calendar c = Calendar.getInstance();
			c.setTime(fileDate);
			c.add(Calendar.DAY_OF_MONTH, - fdp.getDateOffset());
			fileDate = c.getTime();
		}
		
		//ODS��洢����
		String saveCycle = subs.length > 3 ? subs[3] : "1";
		//������־
		boolean clear = false;
		//�ж�������ڼ�¼�ļ��Ƿ����
		File loadDateRecordFile = new File(sysp.getProperty("recordDir") + subs[2] + ".r");
		if(!loadDateRecordFile.isFile()){
			logger.warn("{}������ڼ�¼�ļ�������,���򴴽�:{}",subs[2],loadDateRecordFile.createNewFile());
		}
		String recordDate = FileUtil.readFile(loadDateRecordFile.getAbsolutePath());
		if(StringUtils.isEmpty(recordDate)){
			logger.warn("����¼����Ϊ��,����д�뵱ǰ�ļ�ʱ��");
			logger.warn("����¼����Ϊ��,�����޷��жϵ�ǰODS��:{}������ʱ��,���Խ���մ˱�",tableName);
			clear = true;
		}else{
			//�ļ������ڼ�¼����֮ǰ
			if(fileDate.before(TimeUtils.string2Date(recordDate, "yyyy-MM-dd"))){
				logger.error("��ǰ�ļ����ڱ�����¼������,������Ժ�������");
				isGoOn =false;
				return;
			}else{
				Date saveMaxCycle = this.getDateAfter(TimeUtils.string2Date(recordDate, "yyyy-MM-dd"), Integer.parseInt(saveCycle));
				//���洢ʱ����ڵ�ǰ�ļ�����
				if(saveMaxCycle.after(fileDate)){
					logger.info("��ǰ�ļ��������ڵ�ǰ�洢������,�������ִ������");
					return;
				}else{
					logger.info("�ļ����ڴ���ODS��{},���洢����,���Խ���մ˱�",tableName);
					clear = true;							
				}
			}															
		}
		if(clear){
			//����������ڼ�¼�ļ�
			logger.info("����������ڼ�¼�ļ�");
			FileUtil.writeStringToFile(loadDateRecordFile,TimeUtils.date2String(fileDate, "yyyy-MM-dd"), false);
			//���ODS��
			String cl = sysp.getProperty("replace").replaceFirst("����", tableName);
			String[] cmds = {"sh", sysp.getProperty("db2Shell"), fdp.getDb2Environment().split("\\,")[0], 
					fdp.getDb2Environment().split("\\,")[1], fdp.getDb2Environment().split("\\,")[2],cl};
			String result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
			logger.info("DB2��ձ�:{},������:{}",tableName,result);
		}
	}
	
	/**
	 * �����ļ�
	 * @param f
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	private void disposeFlowFile(File f) throws Exception{
		
		//���ñ���
		tableType = 0;
		odsFlag = 0;		
		exceptionInfo = null;
		isOdsPubLog = false;
		isGoOn = true;
		
		if(f == null || !f.isFile()){
			logger.error("�ļ��Ƿ�:{}",f.getAbsolutePath());
			return;
		}		
		String fileName = f.getName();
		logger.info("��ʼ�����ļ�:{}",fileName);		
		this.analysePluginInfo(fileName);
		if(!isGoOn){
			logger.error("����������ֹ:{}",f.getAbsolutePath());
			return;
		}
		//��¼���ݿ�
		this.addLogToOdsPubLog();
		
		String result = null;
		String postfix = "";
		//��ѹ
		for(Iterator it = GlobalBean.getInstance().getCompressConmandMap().entrySet().iterator();it.hasNext();){
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
			this.updateLogToOdsPubLog();
			return;
		}
		
		//Ԥ����
		if(!StringUtils.isEmpty(fdp.getPerShell())){
			//�滻��ֵ̬
			String cl = fdp.getPerShell().toString().replaceFirst("Դ�ļ�", filePath);
			cl = cl.replaceFirst("�ļ�����", tableType == 0 ? TimeUtils.date2String(fileDate, "yyyyMMdd") : TimeUtils.date2String(fileDate, "yyyyMM"));
			cl = cl.replaceFirst("Ŀ���ļ�", filePath + ".pres");
			//ƴװ����
			String[] cmds = {"sh", sysp.getProperty("preShell"), cl};	
			result = RunCommand.runProcess(cmds,sysp.getProperty("workDir"));
			logger.info("Ԥ������:{}",result);
			//Ԥ�����־
			preShellFix = ".pres";
		}
		
		//װ������
		if(!StringUtils.isEmpty(fdp.getdB2Statement())){
			if(StringUtils.isEmpty(fdp.getDb2Environment())){
				throw new Exception("���ID[" + fdp.getPluginId() + "]������ִ�л�����Ϣ");
			}
			if(fdp.getDb2Environment().split("\\,").length != 3){
				throw new Exception("���ID[" + fdp.getPluginId() + "]ִ�л�����Ϣ����ȷ");
			}
			//��ʼװ������
			result = null;
			//�滻��ֵ̬
			String cl = fdp.getdB2Statement().replaceFirst("�����ļ�·��", filePath + preShellFix);
			File loadLogDir = new File(sysp.getProperty("loadLogDir") + tableName + File.separator + TimeUtils.getCurrentTime("yyyyMMdd") + File.separator);
			if(!loadLogDir.isDirectory()){
				loadLogDir.mkdirs();
			}
			cl = cl.replaceFirst("��־�ļ�·��", loadLogDir.getAbsolutePath() + File.separator + fileName + ".log");
			//ƴװ����
			String[] cmds = {"sh", sysp.getProperty("db2Shell"), fdp.getDb2Environment().split("\\,")[0], 
					fdp.getDb2Environment().split("\\,")[1], fdp.getDb2Environment().split("\\,")[2],cl};
			result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
			logger.info("DB2��ʼװ������:{},������:{}",f.getAbsolutePath(),result);
			
		}
		
		//����result
		odsFlag = this.analyseResult(result);
		logger.info("{},�����:{}",fileName,odsFlag);
		//����ODS_PUB_LOG
		this.updateLogToOdsPubLog();
		
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
	
	private void loadProperties() throws IOException{		
		//װ��ϵͳ����
		InputStream in = this.getClass().getResourceAsStream("system.properties");
		sysp = new Properties();
		sysp.load(in);
	}
	
}
