package com.linkage.ftpdrudgery.tools;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 文件过滤器(此类在本项目中没有用到)
 * @author run
 *
 */
public class MyFileFilter implements FilenameFilter {
	
	public boolean isXml(String file) {
		if (file.toLowerCase().endsWith(".xml")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isMd5(String file) {
		if (file.toLowerCase().endsWith(".md5")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isCnfm(String file) {
		if (file.toLowerCase().endsWith(".cnfm")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean accept(File dir, String fname) {
		return (isXml(fname) || isMd5(fname) || isCnfm(fname));
	}

}
