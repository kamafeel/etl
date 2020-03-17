package com.linkage.ftpdrudgery.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * 文件过滤器(此类在本项目中没有用到)
 * @author run
 *
 */
public class RecordFileFilter implements FilenameFilter {
	
	private List<String> fileNames;
	
	public RecordFileFilter(List<String> fileNames){
		this.fileNames = fileNames;
	}
	
	
	public boolean isRecordValid(String file) {
		if (file.toLowerCase().endsWith(".sr")) {
			if(fileNames != null && fileNames.size() > 0){
				if(fileNames.contains(file.substring(0,8))){
					return true;
				}
			}else{
				return true;
			}
		}
		return false;		
	}

	public boolean accept(File dir, String fname) {
		return isRecordValid(fname);
	}

}
