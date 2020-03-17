package com.ai.ods.data;


import com.linkage.ftpdrudgery.tools.RunCommand;
import com.linkage.intf.tools.StringUtils;

public class RedoDB2 {
	
	//�쳣��Ϣ
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
	 * ����Load�ļ�
	 * @param result
	 * @return
	 */
	private short analyseResult(String result){
		if(StringUtils.isEmpty(result)){
			System.out.println("Shellִ�н��:NULL,���������ΪLoad����ִ��ʧ��");
			exceptionInfo = "Shellִ�н��:NULL,���������ΪLoad����ִ��ʧ��";
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
