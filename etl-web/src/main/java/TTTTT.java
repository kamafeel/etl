import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.linkage.ftpdrudgery.thread.PartdisBean;
import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.tools.TimeUtils;


public class TTTTT {
	
	private int getTimeDiffByMin(Date date1, Date date2){
		if(date1 == null || date2 == null){
			throw new RuntimeException("计算时间差,参数不能为NULL");
		}
		return (int)((date1.getTime() - date2.getTime()) / ( 60 * 1000));
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		String[] aa = {"222.txt"};
//		Collection<File> cc = FileUtils.listFiles(new File("D:\\test111"), null, false);
//		if(cc.size() == 0){
//			
//		}
		
//		String[] sss = {"2","3","4","5","6","7",};
//		//System.out.println(sss.length);
//		System.out.println(sss[sss.length-1]);
//		//aFiles[aFiles.length-1].matches("(?:uplc_)\\d{14}(?:.csv)")
//		
//		String a = "uplc_20150325173200.csv";
//		System.out.println(a.matches("(?:uplc_)\\d{14}(?:.csv)"));
//		System.out.println(a.substring(5, 19));
		
		//System.out.println(new TTTTT().getTimeDiffByMin(new Date(), TimeUtils.string2Date("20150325170000", "yyyyMMddHHmmss") ));
//		PartdisBean.getInstance().setPartPath("D:\\test111");
//		FileUtils.listFiles(new File(PartdisBean.getInstance().getPartPath()), null, false);
		
//		String[] cmds = {"sh", "startup_cron.sh"};
//		String result = RunCommand.runProcess(cmds, "/home/ocdc/app/bin/fd_new/bin/");
//		System.out.println(result);
		int startMins = 0;
		boolean over = true;
		while (over) {
			while(true){
				startMins++;
				if(startMins < 30){
					Thread.sleep(1000l);
					System.out.println("休眠1秒钟");
				}else{
					over = false;
					break;
				}
			}
		}
		
		System.out.println(startMins);
		
	}

}
