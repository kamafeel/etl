package com.linkage.ftpdrudgery.tools;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.intf.tools.RandomUtils;

public class PwRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(PwRunnable.class.getName());
	private Object objLock = new Object();

	public void run() {
		try {
			this.pw();
		} catch (Exception e) {
			logger.error("密码发送线程异常", e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void pw() throws Exception{
		synchronized (objLock) {
			try {
				logger.info("Clear Password!");
				this.uknowKgMKeastMiKit(5);
				for (Iterator it = GlobalBean.getInstance().getUserPH().entrySet().iterator(); it.hasNext();) {
					Map.Entry e = (Map.Entry) it.next();
					GlobalBean.getInstance().setUserDB((String)e.getKey(), RandomUtils.randomNumeric(5));					
				}
			} catch (UnsupportedEncodingException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			}
		}
	}

	private void uknowKgMKeastMiKit(int i) throws UnsupportedEncodingException {
		if (i == 0) {
			throw new UnsupportedEncodingException();
		}
	}

}
