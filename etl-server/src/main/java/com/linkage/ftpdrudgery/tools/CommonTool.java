package com.linkage.ftpdrudgery.tools;

import java.security.MessageDigest;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.fdenum.TaskStatusEnum;


/**
 * 公共方法
 * @author run
 *
 */
public class CommonTool {
	
	private static Logger logger = LoggerFactory.getLogger(CommonTool.class);
	
	private final static String[] hexDigits = { 
	      "0", "1", "2", "3", "4", "5", "6", "7", 
	      "8", "9", "a", "b", "c", "d", "e", "f"}; 
	
	
	/**
	 * 得到任务名字
	 * @param tb
	 * @return
	 */
	public static String getJobName(TaskBean tb){
		StringBuilder sb = new StringBuilder("ID:");
		sb.append(tb.getId());
		sb.append(",Name:");
		sb.append(tb.getTaskName());
		return sb.toString();
	}
	
	public static void setTaskStatusExc(TWMonitorBean twb,String con){
		twb.setTaskStatus(TaskStatusEnum.TaskStatus_Exception.getStatus());
		twb.setTaskExp(TWMonitorBean.EXP_Exception, con);
		GlobalBean.getInstance().setTaskOfWork(twb);
	}
	
	/**
	 * 检查ArrayList是否有元素
	 * @param al
	 * @return
	 */
	public static boolean checkArrayList(ArrayList<String> al){
		if(al == null || al.size() == 0){
			return false;
		}else{
			return true;
		}
	}
	
	
	/**
	 * 检查字符串是否为null
	 * 
	 * @param string
	 * @return
	 */
	public static boolean checkNull(String input) {
		if (input == null || "".equals(input)) {
			return true;
		}
		return false;
	}
	
	/**
	 * String MD5 String
	 * @param origin
	 * @return
	 */
	public static String MD5Encode(String origin) {
		String resultString = null;
		try {
			resultString = new String(origin);
			resultString = MD5Encode(resultString.getBytes());
		} catch (Exception e) {
			logger.error("MD5编码错误", e);
		}
		return resultString;
	}
	
	/**
	 * byte MD5 String
	 * @param origin
	 * @return
	 */
	public static String MD5Encode(byte[] origin) {
		String resultString = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(origin));
		} catch (Exception e) {
			logger.error("MD5编码错误", e);
		}
		return resultString;
	}

	/** 
	 * 转换字节数组为16进制字串 
	 * @param b 字节数组 
	 * @return 16进制字串 
	 */

	public static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}
	
	/**
	 * byte to String
	 * @param b
	 * @return
	 */
	private static String byteToHexString(byte b) { 
	    int n = b; 
	    if (n < 0) 
	      n = 256 + n; 
	    int d1 = n / 16; 
	    int d2 = n % 16; 
	    return hexDigits[d1] + hexDigits[d2]; 
	}
	
	/**
	 * byte[] to String
	 * @param data
	 * @return
	 */
	public static String bytesToString(byte[] data) {
		
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',   
                'e', 'f'};
        char[] temp = new char[data.length * 2];   
        for (int i = 0; i < data.length; i++) {   
            byte b = data[i];   
            temp[i * 2] = hexDigits[b >>> 4 & 0x0f];   
            temp[i * 2 + 1] = hexDigits[b & 0x0f];   
        }   
        return new String(temp);   
  
    }
	
}
