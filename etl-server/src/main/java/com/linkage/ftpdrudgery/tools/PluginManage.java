package com.linkage.ftpdrudgery.tools;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.ftpdrudgery.bean.TaskBean;


/**
 * 插件管理者
 * @author run[zhangqi@lianchuang.com]
 * 3:43:05 PM May 25, 2009
 */
public class PluginManage {
	
	private static Logger logger = LoggerFactory.getLogger(PluginManage.class);
	
	/* 插件名称 */
	private String pluginName;
	
	/* 插件路径 */
	private String pluginPath;
	
	/* 反射Class所有路径 */
	private String pluginClassPath;
	
	/* 插件配置ID */
	private String pluginId;
	
	/**
	 * 构造函数
	 * @param pluginName
	 * @param pluginPath
	 * @param classPath
	 * @param returnPath
	 */
	public PluginManage(TaskBean tb){
		
		this.pluginName = tb.getPluginName();
		this.pluginPath = tb.getPluginPath();
		this.pluginClassPath = tb.getPluginClassPath();
		this.pluginId = tb.getPluginId();
		Object[] paramArray = {pluginName, pluginPath, pluginClassPath};
		logger.info("准备加载插件{}\n插件程序路径:{}\n反射Class路径:{}",paramArray);
	}
	
	/**
	 * 获取插件目录下的文件
	 * @return
	 * @throws Exception 
	 * @throws Exception 
	 */
	private File[] getPluginFile() throws Exception {
		File[] pluginFile = MyFileUtil.getFiles(pluginPath);
		if(pluginFile == null || pluginFile.length == 0){
			throw new Exception("插件[" + pluginName + "]的程序路径：" + pluginPath + "下面没有文件");
		}
		return pluginFile;
	}
	
	/**
	 * 获取插件URL
	 * @param jars
	 * @return
	 * @throws MalformedURLException 
	 */
	public URL[] getPluginUrls(File[] jars) throws MalformedURLException {
		ArrayList<URL> urls = new ArrayList<URL>();
		for (File f : jars) {
			try {
				urls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				throw e;
			}
		}
		URL[] u = new URL[urls.size()];
		for (int i = 0; i < urls.size(); i++) {
			u[i] = urls.get(i);
		}
		return u;
	}
	
	/**
	 * 反射插件方法,方法名字约定为disposeFile()
	 * @return
	 */
	public File[] reflectDisposeFileMethod(File[] origFile) throws Exception {
		// 初始化反射加载器,并加载
		//new ReflectSysClassLoader(this.getPluginFile());
		//插件自身类加载器,避免自身干扰
		PluginClassLoader pcl = new PluginClassLoader(this.getPluginUrls(this.getPluginFile()),this.getClass().getClassLoader());	
		// 获取Class资源
//		Class<?> cls = ReflectSysClassLoader.sysloader
//				.loadClass(pluginClassPath);
		
		// 获取Class资源
		Class<?> cls = pcl.loadClass(pluginClassPath);
		
		// 获得Class的方法,约定方法名为:disposeFile
		if(pluginId.equalsIgnoreCase("0")){
			Method method = cls.getDeclaredMethod("disposeFile",
					new Class[] { File[].class });
			if (method == null) {
				throw new Exception("disposeFile方法未能反射得到");
			} else {
				return (File[]) method.invoke(cls.newInstance(),
						new Object[]{origFile});
			}
		}else{
			Method method = cls.getDeclaredMethod("disposeFile",
					new Class[] { String.class, File[].class });
			if (method == null) {
				throw new Exception("disposeFile方法未能反射得到");
			} else {
				return (File[]) method.invoke(cls.newInstance(),
						new Object[]{pluginId,origFile});
			}
		}		
	}
	
	/**
	 * 反射插件方法,方法名字约定为produceFile()
	 * @return
	 * @throws Exception 
	 */
	public File[] reflectProduceFileMethod() throws Exception {
		// 初始化反射加载器,并加载
		//new ReflectSysClassLoader(this.getPluginFile());
		
		//插件自身类加载器,避免自身干扰
		PluginClassLoader pcl = new PluginClassLoader(this.getPluginUrls(this.getPluginFile()),this.getClass().getClassLoader());	
		// 获取Class资源
//		Class<?> cls = ReflectSysClassLoader.sysloader
//				.loadClass(pluginClassPath);
		
		// 获取Class资源
		Class<?> cls = pcl.loadClass(pluginClassPath);
		
		// 获得Class的方法,预订名字为:produceFile
		Method method = cls.getDeclaredMethod("produceFile",new Class[]{String.class});
		if (method == null) {
			throw new Exception("produceFile方法未能反射得到");
		} else {
			return (File[]) method.invoke(cls.newInstance(), new Object[]{pluginId});
		}

	}
}
