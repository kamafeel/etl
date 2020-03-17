package com.linkage.ftpdrudgery.start;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.console.StartFtpdrudgeryConsole;
import com.linkage.ftpdrudgery.db.fd.DBOperation_Fd;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;
import com.linkage.ftpdrudgery.quartz.QuartzManager;
import com.linkage.ftpdrudgery.tools.SystemInit;
import com.linkage.intf.tools.DESPlus;
import com.linkage.intf.tools.StringUtils;
/**
 * 
 * @author run[zhangqi@lianchuang.com] 4:20:19 PM May 10, 2009
 */
public class StartServer {

	private static Logger logger = LoggerFactory.getLogger(StartServer.class);
	private Object objLock = new Object();
	
	public static void initFDPMap(){
		List<FdPluginInfo> ll = DBOperation_Fd.getInstance().getFdPluginInfo(null);
		GlobalBean.getInstance().getFdpMap().clear();
		for(FdPluginInfo fdp : ll){
			try {
				fdp.setDateOffset(fdp.getDateOffset());
				fdp.setTaskId(fdp.getTaskId());
				if(!StringUtils.isEmpty(fdp.getdB2Statement())){
					fdp.setdB2Statement(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdp.getdB2Statement()));
				}
				if(!StringUtils.isEmpty(fdp.getPerShell())){
					fdp.setPerShell(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdp.getPerShell()));
				}
				fdp.setDb2Environment(fdp.getDb2Environment());
				fdp.setPluginInfo(new DESPlus(GlobalBean.getInstance().getDESPlusKey()).decrypt(fdp.getPluginInfo()));
				GlobalBean.getInstance().setFdpMap(fdp.getPluginId(), fdp);
			} catch (Exception e) {
				logger.error("任务{},加载插件信息异常",fdp.getTaskId(),e);
			}			
		}		
	}
	
	private void uknowKgMKeastMiKit(int i)
			throws UnsupportedEncodingException {
		if (i == 0) {
			throw new UnsupportedEncodingException();
		}
	}

	private void startMain() throws Exception{
		synchronized (objLock) {
			try {
				this.uknowKgMKeastMiKit(5);
				logger.info("读取配置文件到内存...");
				long Start = System.currentTimeMillis();
				SystemInit.getInstance();
				long End = System.currentTimeMillis();
				logger.info("配置文件读取完毕,耗时={}毫秒", (End - Start));
				GlobalBean gb = GlobalBean.getInstance();
				if (gb.getSystemBean().isConsoleStart()) {
					logger.info("控制台初始化开始...");
					Start = System.currentTimeMillis();
					StartFtpdrudgeryConsole.getInstance();
					End = System.currentTimeMillis();
					logger.info("控制台启动完毕,耗时={}毫秒", (End - Start));
				}
				Start = System.currentTimeMillis();
				logger.info("调度控制开始装载任务信息...");
				QuartzManager.getInstance();
				End = System.currentTimeMillis();
				logger.info("调度控制装载任务信息完毕,耗时={}毫秒", (End - Start));
				
				logger.info("开始装载任务插件信息...");
				Start = System.currentTimeMillis();
				StartServer.initFDPMap();
				End = System.currentTimeMillis();
				logger.info("装载任务插件信息完毕,耗时={}毫秒", (End - Start));
			} catch (UnsupportedEncodingException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			}
		}
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new StartServer().startMain();
	}
}
