package com.linkage.ftpdrudgery.tools;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * 动态加载classpath
 * @author run[zhangqi@lianchuang.com]
 * 3:12:27 PM May 25, 2009
 */
public class ReflectSysClassLoader {
		
	public static URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
	private static Class<URLClassLoader> sysclass = URLClassLoader.class;
	private static Method method;
	
	/**
	 * jars以文件数组的形式传递
	 * @param pluginJar
	 * @throws Exception 
	 */
	public ReflectSysClassLoader(File[] pluginJar) throws Exception{
		for(File f : pluginJar){
			try {
				this.addURL(f.toURI().toURL());
			} catch (MalformedURLException e) {
				throw e;
			}
		}
	}
		
	public ReflectSysClassLoader(String[] pluginJar) throws Exception{
		for(String f : pluginJar){
			try {
				this.addURL(new File(f).toURI().toURL());
			} catch (MalformedURLException e) {
				throw e;
			}
		}
	} 
	
	/**
	 * jar列表以分割符
	 * @param jarString
	 * @throws Exception 
	 */
	public ReflectSysClassLoader(String jarString) throws Exception{
		String[] url = jarString.split(File.pathSeparator);
		for(String u : url){
			try {
				this.addURL(new File(u).toURI().toURL());
			} catch (MalformedURLException e) {
				throw e;
			}
		}
	}
	
	/**
	 * 反射调用系统ClassLoad加载classpath
	 * @param url
	 * @throws Exception 
	 */	
	private void addURL(URL url) throws Exception {
        try {
	        if(method == null) {
	        	//封装系统URLClassLoader.addURL()方法的参数
	        	Class<?>[] cls = new Class[] {URL.class};
	        	//获得此方法的实例
	        	method = sysclass.getDeclaredMethod("addURL", cls);
	        	method.setAccessible(true);
	        }
	        //把自己的jar动态加载到系统的URLClassLoader里面
            method.invoke(sysloader, new Object[]{ url });
        } catch (Exception e) {
        	throw e;
        }
    }
 
}
