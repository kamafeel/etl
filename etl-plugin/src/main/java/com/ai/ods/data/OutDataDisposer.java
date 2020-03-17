package com.ai.ods.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;
import com.linkage.ftpdrudgery.tools.FdException;
import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.tools.RandomUtils;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

public class OutDataDisposer {
	
	private static Logger logger = LoggerFactory.getLogger(OutDataDisposer.class);
	
	private Properties sysp;
	private FdPluginInfo fdp;	
	private String outFileName;
	private File expFile;
	/* �����ļ���ʱ��׺ */
	private final static String outFileFix = ".exp";
	/* ��̬·���ָ�� */
	private final static String customPathRegex = "%";
	
	
	/**
	 * ��ܷ������
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public File[] produceFile(String pluginId) throws Exception{
		fdp = GlobalBean.getInstance().getFdpMap().get(pluginId);
		this.loadProperties();
		return exportFile();
	}
	
	
	private void analysePluginInfo() throws Exception{
		if(StringUtils.isEmpty(fdp.getPluginInfo())){
			throw new Exception("���ID[" + fdp.getPluginId() + "]�����������Ϣ");
		}
		String[] subs = fdp.getPluginInfo().split("\\,");
		outFileName = subs[0];	
	}
	
	private String dynamicReplace(String source) throws IOException, FdException{
		if(source.contains(customPathRegex)){
			List<String> cp = Arrays.asList(source.split(customPathRegex));
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
		boolean isMon = false;
		if(s.startsWith("yyyyMMdd")){
			dateFormat=	"yyyyMMdd";
		}else if(s.startsWith("yyyy-MM-dd")){
			dateFormat=	"yyyy-MM-dd";
		}else if(s.startsWith("yyyyMM")){
			dateFormat=	"yyyyMM";
			isMon = true;
		}else if(s.startsWith("yyyy-MM")){
			dateFormat=	"yyyy-MM";
			isMon = true;
		}
		
		if(StringUtils.isEmpty(dateFormat)){
			return s;
		}
		if(s.contains("��")){
			if(isMon){
				return TimeUtils.date2String(this.getDateMonBefore(new Date(), Integer.parseInt(s.split("��")[1])), dateFormat);
			}else{
				return TimeUtils.date2String(this.getDateBefore(new Date(), Integer.parseInt(s.split("��")[1])), dateFormat);
			}
		}else if(s.contains("��")){
			if(isMon){
				return TimeUtils.date2String(this.getDateMonBefore(new Date(), -Integer.parseInt(s.split("��")[1])), dateFormat);
			}else{
				return TimeUtils.date2String(this.getDateBefore(new Date(), -Integer.parseInt(s.split("��")[1])), dateFormat);
			}			
		}else{
			return TimeUtils.getCurrentTime(dateFormat);
		}	
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
	private Date getDateMonBefore(Date d, int mon){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, -mon);
		return c.getTime();
	}
	
	/**
	 * �����ļ�
	 * @param f
	 * @return
	 * @throws Exception 
	 */
	private File[] exportFile() throws Exception{
		
		this.analysePluginInfo();
		String result = null;
		String preShellFix = "";
		
		expFile = new File(sysp.getProperty("workDir") + "temp" + File.separator + dynamicReplace(outFileName) + outFileFix);
		if(expFile.isFile()){
			expFile.delete();
		}
		
		//��ʼ��������
		if(!StringUtils.isEmpty(fdp.getdB2Statement())){
			if(StringUtils.isEmpty(fdp.getDb2Environment())){
				throw new Exception("���ID[" + fdp.getPluginId() + "]������ִ�л�����Ϣ");
			}
			if(fdp.getDb2Environment().split("\\,").length != 3){
				throw new Exception("���ID[" + fdp.getPluginId() + "]ִ�л�����Ϣ����ȷ");
			}			
			
			//�滻��ֵ̬
			String cl = fdp.getdB2Statement().replaceFirst("�����ļ�·��", expFile.getAbsolutePath());
			File loadLogDir = new File(sysp.getProperty("loadLogDir") + "exp_" + TimeUtils.getCurrentTime("yyyyMMdd"));
			if(!loadLogDir.isDirectory()){
				loadLogDir.mkdirs();
			}
			cl = cl.replaceFirst("��־�ļ�·��", loadLogDir.getAbsolutePath() + File.separator + RandomUtils.randomNumeric(5) + ".log");
			cl = dynamicReplace(cl);
			//ƴװ����
			String[] cmds = {"sh", sysp.getProperty("db2Shell"), fdp.getDb2Environment().split("\\,")[0], 
					fdp.getDb2Environment().split("\\,")[1], fdp.getDb2Environment().split("\\,")[2],cl};
			result = RunCommand.runProcess(cmds, sysp.getProperty("workDir"));
			logger.info("DB2��ʼ��������:{},������:{}",expFile.getAbsolutePath(),result);		
		}
		
		//Ԥ����
		if(!StringUtils.isEmpty(fdp.getPerShell())){
			//�滻��ֵ̬
			String cl = fdp.getPerShell().toString().replaceFirst("Դ�ļ�", expFile.getAbsolutePath());
			cl = cl.replaceFirst("Ŀ���ļ�", expFile.getAbsolutePath() + ".pres");
			//ƴװ����
			String[] cmds = {"sh", sysp.getProperty("preShell"), cl};	
			result = RunCommand.runProcess(cmds,sysp.getProperty("workDir"));
			logger.info("Ԥ������:{}",result);
			//Ԥ�����־
			preShellFix = ".pres";
			if(expFile.isFile()){
				expFile.delete();
			}
		}
		File outFile = new File(expFile.getAbsolutePath() + preShellFix);
		File outFile_ftp = new File(outFile.getAbsolutePath().substring(0,outFile.getAbsolutePath().length() - outFileFix.length()-preShellFix.length()));
		outFile.renameTo(outFile_ftp);
		
		logger.info("�����ļ�:{}",outFile_ftp.getAbsolutePath());
		
		File[] outFileArray = {outFile_ftp};
		return outFileArray;
	}
	
	private void loadProperties() throws IOException{		
		//װ��ϵͳ����
		InputStream in = this.getClass().getResourceAsStream("system.properties");
		sysp = new Properties();
		sysp.load(in);
	}
}
