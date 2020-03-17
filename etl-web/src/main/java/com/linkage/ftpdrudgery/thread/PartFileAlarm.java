package com.linkage.ftpdrudgery.thread;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.ai.vgop.db.VGOPDBOperation;
import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.tools.TimeUtils;

public class PartFileAlarm implements Runnable {

	private static Logger logger = Logger.getLogger(PartFileAlarm.class.getName());
	
	private int getTimeDiffByMin(Date date1, Date date2) throws Exception{
		if(date1 == null || date2 == null){
			throw new Exception("计算时间差,参数不能为NULL");
		}
		return (int)((date1.getTime() - date2.getTime()) / ( 60 * 1000));
	}
	
	public void run() {
		try {
			Collection<File> files = FileUtils.listFiles(new File(PartdisBean.getInstance().getPartPath()), null, false);
			if ((files == null) || (files.size() == 0)) {
				logger.info("本次迭代未发现PART文件,总共连续未发现" + PartdisBean.getInstance().getTimeNum().size() + "次");
				PartdisBean.getInstance().getTimeNum().add(TimeUtils.getCurrentTime("yyyyMMddHHmm"));
				if (PartdisBean.getInstance().getTimeNum().size() >= 30) {
					int timeCha = 0;
					File fback = new File(PartdisBean.getInstance().getBackPath()+ TimeUtils.getCurrentTime("yyyyMMdd") + ".sr");
					if (fback.isFile()) {
						String backContent = FileUtils.readFileToString(fback);
						String[] aFiles = backContent.split("\\,");
						if (aFiles != null) {
							if (aFiles[aFiles.length-1].matches("(?:uplc_)\\d{14}(?:.csv)")) {
								String aDate = aFiles[aFiles.length-1].substring(5, 19);
								timeCha = this.getTimeDiffByMin(new Date(), TimeUtils.string2Date(aDate, "yyyyMMddHHmmss"));
							}
						}
					}else{
						logger.info("BACK文件未发现" + fback.getCanonicalPath());
					}
					
					String result = PartdisBean.getInstance().getRestartNum()+ "超过最大重启次数,本次忽略重启FD";
					if(PartdisBean.getInstance().getRestartNum() < 2){
						int restartNum = PartdisBean.getInstance().getRestartNum()+1;
						PartdisBean.getInstance().setRestartNum(restartNum);
						String[] cmds = {"sh", "startup_cron.sh"};
						result = RunCommand.runProcess(cmds, "/home/ocdc/app/bin/fd_new/bin/");
						logger.info("重启FD结果:" + result);	
					}else{
						logger.info(result);	
					}

					if(PartdisBean.getInstance().getSendNum() < 10){
						int sendNum = PartdisBean.getInstance().getSendNum()+1;
						PartdisBean.getInstance().setSendNum(sendNum);
						
						try {
							boolean isSend = false;
							int count = 0;
							while (count < 3 && !isSend) {
								count++;
								Map<String, String> keysMap = new HashMap<String, String>();
								keysMap.put(VGOPDBOperation.SMS_PHONE_NO,"13908062905");
								keysMap.put(VGOPDBOperation.SMS_CONTENT, "Part文件最近" + PartdisBean.getInstance().getTimeNum().size() + "次迭代均未发现新文件,A口Ftp记录文件中最后一次文件时间距离现在" + timeCha + "分钟,程序强制重启ETL-FD2,重启情况:" + result);
								VGOPDBOperation.getInstance().sendSms(keysMap);
								isSend = true;
							}
						} catch (Exception e) {
							logger.error("发送PART文件预警短信异常",e);
						}
						
					}else{
						logger.info("连续发送PART文件预警短信次数" + PartdisBean.getInstance().getSendNum() + "超过10次,忽略本次发送");
					}					
				}
			}
		} catch (Exception e) {
			logger.error("监控PART文件预警异常_未找到PART",e);
		}
		
		try {			
			Collection<File> files = FileUtils.listFiles(new File(PartdisBean.getInstance().getPartPath()), null, false);
			File fback = new File("/home/ocdc/app/bin/fd_new/part_back/part_" + TimeUtils.getCurrentTime("yyyyMMdd") + ".back");
			if(!fback.isFile()){
				fback.createNewFile();
			}
			List<String> ll = new ArrayList<String>();
			if ((files != null) && (files.size() > 0)) {
				PartdisBean.getInstance().setRestartNum(0);
				PartdisBean.getInstance().setSendNum(0);
				PartdisBean.getInstance().getTimeNum().clear();
				logger.info("本次迭代发现PART文件,清空TimeNum");
				for(File f : files){
					if(f.isFile()){
						ll.add(f.getName());
						f.delete();
					}
				}
				FileUtils.writeLines(fback, ll, true);
			}
		} catch (Exception e) {
			logger.error("监控PART文件预警异常_找到PART",e);
		}
		
	}

}
