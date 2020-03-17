package com.linkage.ftpdrudgery.db.ods;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 得到ibatis会话工厂
 * @author run[zhangqi@lianchuang.com]
 * 8:11:15 PM Jan 14, 2010
 */
public class DBSessionFactory_Ods {
	
	private static Logger logger = LoggerFactory.getLogger(DBSessionFactory_Ods.class);
	
	private static DBSessionFactory_Ods SINGLE = new DBSessionFactory_Ods();
	
	private static SqlSessionFactory sessionFactory;
	
	public static synchronized DBSessionFactory_Ods getInstance(){
		if(SINGLE == null){
			SINGLE = new DBSessionFactory_Ods();
		}
		return SINGLE;
	}
	
	private DBSessionFactory_Ods(){
		sessionFactory = this.getSqlSessionFactory(); 
	}
	
	private SqlSessionFactory getSqlSessionFactory(){
		SqlSessionFactory sessionFactory = null;
		try {
			Reader reader = Resources.getResourceAsReader("com/linkage/ftpdrudgery/db/ods/config/Configuration_ODS.xml");
			sessionFactory = new SqlSessionFactoryBuilder().build(reader);
			reader.close();
		} catch (IOException e) {
			logger.error("创建ibatis会话工厂(Configuration_FD.xml)异常",e);
		}		
		return sessionFactory;
	}
	
	 public static SqlSessionFactory getSqlSessionFactoryInstance() {
		 return sessionFactory;
	 }
	
}
