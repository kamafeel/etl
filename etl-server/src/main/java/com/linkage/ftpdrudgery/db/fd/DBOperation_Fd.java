package com.linkage.ftpdrudgery.db.fd;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.db.fd.bean.FdHisMonitor;
import com.linkage.ftpdrudgery.db.fd.bean.FdHisMonitorExp;
import com.linkage.ftpdrudgery.db.fd.bean.FdHisMonitorKey;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;
import com.linkage.intf.tools.RandomUtils;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

/**
 * 数据库操作类
 * @author run[zhangqi@lianchuang.com]
 * 8:14:30 PM Jan 14, 2010
 */

public class DBOperation_Fd {
	
	private static Logger logger = LoggerFactory.getLogger(DBOperation_Fd.class);
	
	private static final int max_BetchNum = 3000;
	
	private static DBOperation_Fd SINGLE = new DBOperation_Fd();
	
	public static synchronized DBOperation_Fd getInstance() {

		if (SINGLE == null) {
			SINGLE = new DBOperation_Fd();			
		}
		return SINGLE;
	}
	
	private DBOperation_Fd(){		
		DBSessionFactory_Fd.getInstance();
	}
	
	@SuppressWarnings("rawtypes")
	public int addTWMonitorBean(TWMonitorBean twBean, int betchNum) {
		betchNum = betchNum > max_BetchNum ? max_BetchNum : betchNum;
		logger.debug("批量提交betchNum参数值为{}",betchNum);
		
		int result = 0;
		SqlSession session = DBSessionFactory_Fd.getSqlSessionFactoryInstance().openSession(ExecutorType.BATCH,false);
		//SqlSession session = DBSessionFactory_Fd.getSqlSessionFactoryInstance().openSession();
		try {
			String snId = TimeUtils.date2String(new Date(), "yyyyMMddHHmmss")+RandomUtils.randomNumeric(4);
			FdHisMonitor fm = new FdHisMonitor();
			fm.setSnId(snId);
			fm.setTaskId(twBean.getId());
			fm.setTaskName(twBean.getTaskName());
			fm.setTaskStatus(StringUtils.isEmpty(twBean.getTaskStatus())? "UnKown" : twBean.getTaskStatus());
			fm.setStartTime(twBean.getStartTime());
			fm.setEndTime(twBean.getEndTime() == null ? new Date() : twBean.getEndTime());
			fm.setCastTime(StringUtils.isEmpty(twBean.getCastTime())? TimeUtils.formatLong(new Date().getTime()-twBean.getStartTime().getTime()) : twBean.getCastTime());
			fm.setInsertTime(new Date());			
			result = session.insert("FD_HIS_MONITOR.addFdHisMonitor", fm);
			
			if(twBean.getTaskExp() != null && twBean.getTaskExp().size() >0){				
				List<FdHisMonitorExp> list = new ArrayList<FdHisMonitorExp>();				
				for (Iterator it = twBean.getTaskExp().entrySet().iterator(); it.hasNext();) {
					Map.Entry e = (Map.Entry) it.next();
					FdHisMonitorExp fme = new FdHisMonitorExp();
					fme.setSnId(snId);
					fme.setMapKey(e.getKey().toString());
					fme.setMapValue(e.getValue().toString());
					list.add(fme);
				}
				
				int i =0;
				for(FdHisMonitorExp fme : list){
					result = session.insert("FD_HIS_MONITOR_EXP.addFdHisMonitorExp", fme);
					i++;
					if(i == betchNum){
						session.commit();
						i=0;
					}
				}
				session.commit();
				
			}else{
				session.commit();
			}	
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in addTWMonitorBean,will be rollback", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<TWMonitorBean> getTWMonitorBean(FdHisMonitorKey fmk) {
		List<TWMonitorBean> twBeanList = new ArrayList<TWMonitorBean>();		
		SqlSession session = DBSessionFactory_Fd.getSqlSessionFactoryInstance().openSession();
		try {
			List<FdHisMonitor> fmList = session.selectList("FD_HIS_MONITOR.getFdHisMonitor", fmk);
			for(FdHisMonitor fm : fmList){
				List<FdHisMonitorExp> fmeList = session.selectList("FD_HIS_MONITOR_EXP.getFdHisMonitorExp", fm.getSnId());
				TWMonitorBean twBean = new TWMonitorBean();
				twBean.setId(fm.getTaskId());
				twBean.setTaskName(fm.getTaskName());
				twBean.setTaskStatus(fm.getTaskStatus());
				twBean.setStartTime(fm.getStartTime());
				twBean.setEndTime(fm.getEndTime());
				twBean.setCastTime(fm.getCastTime());
				for(FdHisMonitorExp fme : fmeList){
					twBean.setTaskExp(fme.getMapKey(), fme.getMapValue());
				}
				twBeanList.add(twBean);
			}
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in getTWMonitorBean", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return twBeanList;
	}
	
	
	public int addFdPluginInfo(FdPluginInfo dfp) {
		int result = 0;
		SqlSession session = DBSessionFactory_Fd.getSqlSessionFactoryInstance().openSession();
		try {
			result = session.insert("FD_PLUGIN_INFO.addFdPluginInfo", dfp);
			session.commit();	
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in addFdPluginInfo,will be rollback", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<FdPluginInfo> getFdPluginInfo(FdPluginInfo fdp) {		
		SqlSession session = DBSessionFactory_Fd.getSqlSessionFactoryInstance().openSession();
		try {
			return session.selectList("FD_PLUGIN_INFO.getFdPluginInfo", fdp);
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in getFdPluginInfo", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return null;
	}
	
	public int updateFdPluginInfo(FdPluginInfo fdp) {
		int result = 0;
		SqlSession session = DBSessionFactory_Fd.getSqlSessionFactoryInstance().openSession();
		try {			
			result = session.update("FD_PLUGIN_INFO.updateFdPluginInfo", fdp);
			session.commit();
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in updateFdPluginInfo,will be rollback", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return result;
	}
	
	public int delFdPluginInfo(FdPluginInfo fdp) {
		int result = 0;
		SqlSession session = DBSessionFactory_Fd.getSqlSessionFactoryInstance().openSession();
		try {			
			result = session.delete("FD_PLUGIN_INFO.delFdPluginInfo", fdp);
			session.commit();
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in delFdPluginInfo,will be rollback", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return result;
	}
	
}
