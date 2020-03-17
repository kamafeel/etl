package com.linkage.ftpdrudgery.tools;

import java.util.Comparator;

import com.linkage.ftpdrudgery.bean.TaskBean;

/**
 * 对ArrayList<TaskBean> 根据Id大小排序
 * @author run[zhangqi@lianchuang.com]
 * 3:43:23 PM Aug 11, 2009
 */

public class MySort implements Comparator<TaskBean> {

	public int compare(TaskBean o1, TaskBean o2) {
		// TODO Auto-generated method stub
		String id = o1.getId();
		String id_next = o2.getId();
		
		if(Integer.valueOf(id) > Integer.valueOf(id_next)){
			return -1;
		}else if(Integer.valueOf(id) < Integer.valueOf(id_next)){
			return 1;
		}
		return 0;

	}
}
