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
 * ��������
 * @author Run
 *
 */
public class OutDataDisposer_old {
	
	private static Logger logger = LoggerFactory.getLogger(OutDataDisposer_old.class);
	/* ���ݿ����� */
	private HashMap<String,String> dbphm;
	/* ������������ */
	private HashMap<String,String> expphm;
	/* ������ҵ����Ϣ */
	private HashMap<String,String> ofphm;
	/* �������������������Ϣ */
	private HashMap<String,String> fsphm;
	/* �������������������Ϣ */
	private HashMap<String,String> umphm;
	/* ϵͳ���� */
	private Properties sysp;
	/* �����ļ���ʱ��׺ */
	private final static String outFileFix = ".exp";
	private final static String customPathRegex = "%";
	/**
	 * ��ܷ������
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public File[] produceFile(String taskTag) throws Exception{
		//װ��Properties
		this.loadProperties();
		//��������ļ��б�
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
				
				//�Ƿ�Ҫ����
				boolean exp = true;
				//ƴװ����
				String expTableName = null;
				//ƴװ�����ļ���
				String outFileName = null;
				
				//������ҵ����Ϣ,�������������ж�
				for(Iterator it_ofphm = ofphm.entrySet().iterator();it_ofphm.hasNext();){
					Map.Entry e_ofphm = (Map.Entry)it_ofphm.next();
					if(expKey.equals(e_ofphm.getKey().toString())){
						String[] subs = e_ofphm.getValue().toString().split("\\,");
						//��������
						Date dataDate = null;
						//�ж��Ƿ����±�
						boolean isMon = false;
						if(!subs[0].contains("dd")){
							dataDate = this.getDateBeforeMONTH(new Date(), Integer.parseInt(subs[3]));
							isMon = true;
						}else{
							dataDate = this.getDateBefore(new Date(), Integer.parseInt(subs[3]));
						}
						//�ж�������ڼ�¼�ļ��Ƿ����
						File expDateRecordFile = new File(sysp.getProperty("recordDir") + expKey + ".r");
						if(!expDateRecordFile.isFile()){
							logger.warn("{}������ڼ�¼�ļ�������,���򴴽�:{}",expKey,expDateRecordFile.createNewFile());
						}
						String recordDate = FileUtil.readFile(expDateRecordFile.getAbsolutePath());
						if(StringUtils.isEmpty(recordDate)){
							logger.warn("������¼����Ϊ��,����д�뵱ǰ�ļ�ʱ��");
							FileUtil.writeStringToFile(expDateRecordFile,TimeUtils.date2String(dataDate, isMon ? "yyyy-MM" : "yyyy-MM-dd"), false);
						}else{
							//�������ںͼ�¼����һ�»�����֮ǰ,������
							if(TimeUtils.date2String(dataDate, isMon ? "yyyy-MM" : "yyyy-MM-dd").equals(recordDate) 
									|| dataDate.before(TimeUtils.string2Date(recordDate, isMon ? "yyyy-MM" : "yyyy-MM-dd"))){
								exp = false;
								break;
							}else{
								//���¼�¼�ļ�
								FileUtil.writeStringToFile(expDateRecordFile,TimeUtils.date2String(dataDate, isMon ? "yyyy-MM" : "yyyy-MM-dd"), false);
							}
						}					
						//����
						expTableName = e_ofphm.getKey().toString().replace(subs[0], TimeUtils.date2String(dataDate, subs[0]));
						//�����ļ���
						outFileName = subs[1].replace("����", TimeUtils.date2String(dataDate, subs[2]));
						break;
					}
				}
				if(exp){
					//ִ�е�������
					File loadLogDir = new File(sysp.getProperty("exportLogDir") + expTableName + File.separator + TimeUtils.getCurrentTime("yyyyMMdd") + File.separator);
					if(!loadLogDir.isDirectory()){
						loadLogDir.mkdirs();
					}
					//�����ļ�
					File outFile = new File(sysp.getProperty("workDir") + "temp" + File.separator + outFileName + outFileFix);
//					if(!outFile.getParentFile().isDirectory()){
//						outFile.getParentFile().mkdirs();
//					}
					expValue = expValue.replaceFirst("��־�ļ�·��", loadLogDir.getAbsolutePath() + File.separator + outFileName + ".log");
					expValue = expValue.replaceFirst("�����ļ�·��", outFile.getAbsolutePath());
					expValue = expValue.replaceFirst("����", expTableName);
					
					//��������
					expValue = this.dynamicReplace(expValue);
					
					//���ݿ�������Ϣ
					for(Iterator it_dbphm = dbphm.entrySet().iterator();it_dbphm.hasNext();){
						Map.Entry e_dbphm = (Map.Entry)it_dbphm.next();
						if(expKey.split("\\.")[0].equals(e_dbphm.getKey().toString())){
							String[] subs = e_dbphm.getValue().toString().split("\\,");
							//��������
							String db2Name = subs[0];
							//�û���
							String user = subs[1];
							//����
							String password = subs[2];
							//ƴװ����
							String[] cmds = {"sh", sysp.getProperty("db2Shell"), db2Name, user, password,expValue};
							String result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
							logger.info("DB2��ʼ��������:{},������:{}",outFile.getAbsolutePath(),result);
							break;
						}
					}
					
					//��������
					for(Iterator it_fsphm = fsphm.entrySet().iterator();it_fsphm.hasNext();){
						Map.Entry e_fsphm = (Map.Entry)it_fsphm.next();
						if(expKey.equals(e_fsphm.getKey().toString())){
							//�滻��ֵ̬
							String cl = e_fsphm.getValue().toString().replaceFirst("Դ�ļ�", outFile.getAbsolutePath());
							cl = cl.replaceFirst("Ŀ���ļ�", outFile.getAbsolutePath().substring(0,outFile.getAbsolutePath().length() - outFileFix.length()));
							//ƴװ����
							String[] cmds = {"sh", sysp.getProperty("followShell"), cl};	
							String result = RunCommand.runProcess(cmds,sysp.getProperty("workDir"));
							logger.info("����������:{}",result);						
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
	 * ��̬���ڴ���
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
		if(s.contains("��")){
			return TimeUtils.date2String(this.getDateBefore(new Date(), Integer.parseInt(s.split("��")[1])), dateFormat);
		}else if(s.contains("��")){
			return TimeUtils.date2String(this.getDateBefore(new Date(), -Integer.parseInt(s.split("��")[1])), dateFormat);
		}else{
			return TimeUtils.getCurrentTime(dateFormat);
		}	
	}
	
	
	/**
	 * ƴװArrayListΪ����
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
	 * �õ�����ǰ����
	 * @return
	 */
	private Date getDateBefore(Date d, int day){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, -day);
		return c.getTime();
	}
	
	/**
	 * �õ�����ǰ����
	 * @return
	 */
	private Date getDateBeforeMONTH(Date d, int mon){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, -mon);
		return c.getTime();
	}
	
	private void loadProperties() throws IOException{
		
		//װ��ϵͳ����
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
		//String a ="export to �����ļ�·�� of del modified by nochardel coldel, striplzeros messages ��־�ļ�·�� SELECT PHONE_NO,C_SMS_NUM,C_MMS_NUM,POINTS,DENSERANK() OVER(ORDER by POINTS desc) AS RK,NVL(ETL_DATA_CYCLE,'%yyyy-MM-dd��2%') AS ETL_DATA_CYCLE FROM VGOP_DWD.DWD_SMS_MMS_PARTY_POINT_%yyyyMM% WHERE ETL_DATA_CYCLE='%yyyyMMdd��2%';";
		
		
		//System.out.println(new OutDataDisposer().dynamicReplace(a));
	}

}
