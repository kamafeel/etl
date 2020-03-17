package com.linkage.ftpdrudgery.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

//import com.linkage.ftpdrudgery.thread.WatchThread;
import com.linkage.intf.tools.StringUtils;


public class RunCommand {

	/**
	 * 执行shell命令 String[] cmd = { "sh", "-c", "lsmod |grep linuxVmux" }或者
	 * String[] cmd = { "sh", "-c", "./load_driver.sh" } int tp = 1 返回执行结果 非1
	 * 返回命令执行后的输出
	 */
	public static String runCommand(String[] cmd, int tp) {
		StringBuffer buf = new StringBuffer(1000);
		String rt = "-1";
		try {
			Process pos = Runtime.getRuntime().exec(cmd);
			pos.waitFor();
			if (tp == 1) {
				if (pos.exitValue() == 0) {
					rt = "1";
				}
			} else {
				InputStreamReader ir = new InputStreamReader(
						pos.getInputStream());
				LineNumberReader input = new LineNumberReader(ir);
				String ln = "";
				while ((ln = input.readLine()) != null) {
					buf.append(ln + "\n");
				}
				rt = buf.toString();
				input.close();
				ir.close();
			}
		} catch (java.io.IOException e) {
			rt = e.toString();
		} catch (Exception e) {
			rt = e.toString();
		}
		return rt;
	}
	
	
	/**
	 * 刷新输出流
	 * @param cmd
	 * @param tp
	 * @return
	 */
	public static String runCommandWithWatch(String cmd) {
		String rt = null;
		try {
			String str_0 = null;
			String str_1 = null;
			Process pos = Runtime.getRuntime().exec(cmd);
			WatchShell wt_0 = new WatchShell(pos,0);
			WatchShell wt_1 = new WatchShell(pos,1);
			FutureTask<String> ft_0 = new FutureTask<String>(wt_0);
			FutureTask<String> ft_1 = new FutureTask<String>(wt_1);
			new Thread(ft_0).start();
			new Thread(ft_1).start();
			pos.waitFor();
			if(!ft_0.isCancelled()){
				wt_0.setOver(true);
				str_0 = ft_0.get();
			}
			if(!ft_1.isCancelled()){
				wt_1.setOver(true);
				str_1 = ft_1.get();
			}
			if(StringUtils.isEmpty(str_0)){
				rt = str_1;
			}else{
				rt = str_0;
			}
		} catch (java.io.IOException e) {
			rt = e.toString();
		} catch (Exception e) {
			rt = e.toString();
		}
		return rt;
	}
	

	/**
	 * 执行简单命令 String cmd="ls" int tp = 1 返回执行结果 非1 返回命令执行后的输出
	 */
	public static String runCommand(String cmd, int tp) {
		StringBuffer buf = new StringBuffer(1000);
		String rt = "-1";
		try {
			Process pos = Runtime.getRuntime().exec(cmd);
			pos.waitFor();
			if (tp == 1) {
				if (pos.exitValue() == 0) {
					rt = "1";
				}
			} else {
				InputStreamReader ir = new InputStreamReader(
						pos.getInputStream());
				LineNumberReader input = new LineNumberReader(ir);
				String ln = "";
				while ((ln = input.readLine()) != null) {
					buf.append(ln + "\n");
				}
				rt = buf.toString();
				input.close();
				ir.close();
			}
		} catch (java.io.IOException e) {
			rt = e.toString();
		} catch (Exception e) {
			rt = e.toString();
		}
		return rt;
	}
	
	
	/**
	 * ProcessBuilder处理命令
	 * @param cmd
	 * @param directory
	 * @return
	 */
	public static String runProcess(String[] cmd, String directory) {
		StringBuilder buf = new StringBuilder(1000);
		String rt = "-1";
		
		ProcessBuilder pb = null;
		Process p = null;
		String line = null;
		BufferedReader stdout = null;
		
		try {
			pb = new ProcessBuilder(cmd);
			//System.out.println("---------1--------");
			if(!StringUtils.isEmpty(directory) && new File(directory).isDirectory()){
				pb.directory(new File(directory));
			}
			pb.redirectErrorStream(true);
			p = pb.start();
			//System.out.println("---------2--------");
			stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//int startMins = 0;
			//boolean over = true;
			while ((line = stdout.readLine()) != null) {
				System.out.println(line);
				buf.append(line + "\n");
				if(line.contains("Finish")){
					break;
				}
//				while(true){
//					startMins++;
//					if(startMins < 30){
//						Thread.sleep(1000l);
//						System.out.println("距离强制退出进程剩余" + (30-startMins) + "秒钟");
//					}else{
//						over = false;
//						break;
//					}
//				}
			}
			//System.out.println("---------3--------");
			//WatchThread wt = new WatchThread(p);
			//wt.start();
			int ret = p.waitFor();
			//wt.setOver(true);
			//System.out.println("---------4--------");
			buf.append(ret);			
			rt = buf.toString();
		} catch (java.io.IOException e) {
			rt = e.toString();
		} catch (Exception e) {
			rt = e.toString();
		} finally{
			if(stdout != null){
				try {
					stdout.close();
				} catch (IOException e) {
				}
			}
		}
		return rt;
	}
}

class WatchShell implements Callable<String> {
	Process p;
	boolean over;
	int type;

	public WatchShell(Process p, int type) {
		this.p = p;
		over = false;
		this.type = type;
	}
	
	public void setOver(boolean over) {
		this.over = over;
	}

	public String call() throws Exception {
		StringBuilder sb = new StringBuilder(1000);
		if (p == null){
			return null;
		}			
		BufferedReader br = null;
		InputStreamReader ir = null;
		if(type == 0){
			ir = new InputStreamReader(p.getErrorStream());			
		}else{
			ir = new InputStreamReader(p.getInputStream());
		}
		br = new BufferedReader(ir);
		String ln = "";
		while (true) {
			if (p == null || over) {
				break;
			}
			while ((ln = br.readLine()) != null){
				System.out.println(ln + "\n");
				sb.append(ln + "\n");
			}
		}
		if(br != null ){
			br.close();
		}
		if(ir != null ){
			ir.close();
		}
		return sb.toString();
	}
}
