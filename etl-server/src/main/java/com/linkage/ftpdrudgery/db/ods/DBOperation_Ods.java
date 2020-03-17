package com.linkage.ftpdrudgery.db.ods;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.db.ods.bean.OdsPubLog;

/**
 * 数据库操作类
 * @author run[zhangqi@lianchuang.com]
 * 8:14:30 PM Jan 14, 2010
 */

public class DBOperation_Ods {
	
	private static Logger logger = LoggerFactory.getLogger(DBOperation_Ods.class);
	
	private static DBOperation_Ods SINGLE = new DBOperation_Ods();
	
	public static synchronized DBOperation_Ods getInstance() {

		if (SINGLE == null) {
			SINGLE = new DBOperation_Ods();			
		}
		return SINGLE;
	}
	
	private DBOperation_Ods(){		
		DBSessionFactory_Ods.getInstance();
	}
	
	public int addOdsPubLog(OdsPubLog opl) {
		this.delOdsPubLog(opl);
		int result = 0;
		SqlSession session = DBSessionFactory_Ods.getSqlSessionFactoryInstance().openSession();
		try {			
			result = session.insert("ODS_PUB_LOG.addOdsPubLog", opl);
			session.commit();
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in addOdsPubLog,will be rollback", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return result;
	}
	
	public int updateOdsPubLog(OdsPubLog opl) {
		int result = 0;
		SqlSession session = DBSessionFactory_Ods.getSqlSessionFactoryInstance().openSession();
		try {			
			result = session.update("ODS_PUB_LOG.updateOdsPubLog", opl);
			session.commit();
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in updateOdsPubLog,will be rollback", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return result;
	}
	
	private int delOdsPubLog(OdsPubLog opl) {
		int result = 0;
		SqlSession session = DBSessionFactory_Ods.getSqlSessionFactoryInstance().openSession();
		try {			
			result = session.delete("ODS_PUB_LOG.delOdsPubLog", opl);
			session.commit();
		} catch (Exception e) {
			session.rollback();
			logger.error("Some Exception occur in delOdsPubLog,will be rollback", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}		
		return result;
	}
}
