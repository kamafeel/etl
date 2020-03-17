package com.linkage.ftpdrudgery.tools;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 插件ClassLoader,避免相互干扰
 * @author run[zhangqi@lianchuang.com]
 * 11:13:33 PM Jun 4, 2009
 */
public class PluginClassLoader extends URLClassLoader {
	
	public PluginClassLoader(URL[] urls) {
		super(urls);
	}
	
	public PluginClassLoader(URL[] url, ClassLoader parent){
		super(url, parent);		
	}
}
