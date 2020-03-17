package com.ai.ods.data;


import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.tools.StringUtils;

public class RedoDB2 {
	
	//异常信息
	private String exceptionInfo;
	private int redoNum = 100;
	private static String LOCATION = RedoDB2.class.getProtectionDomain().getCodeSource().getLocation().getFile();
	
	public void redo(String sqlFile){
		int i=0;
		boolean exeSql = false;
		String result = null;
		while(i<redoNum && !exeSql){
			i++;
			String[] cmdc = {"sh", "dosql.sh",sqlFile};
			result = RunCommand.runProcess(cmdc, LOCATION);
			this.analyseResult(result);
			if(analyseResult){
				
			}
		}
	}
	/**
	 * 分析Load文件
	 * @param result
	 * @return
	 */
	private short analyseResult(String result){
		if(StringUtils.isEmpty(result)){
			System.out.println("Shell执行结果:NULL,程序程序认为Load操作执行失败");
			exceptionInfo = "Shell执行结果:NULL,程序程序认为Load操作执行失败";
			return 0;
		}
		if(result.contains("SQLSTATE")){
			exceptionInfo = result;
			return 0;
		}
	
		return 1;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

}
