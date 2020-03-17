package com.linkage.ftpdrudgery.wicket;

import java.util.HashMap;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;


/**
 * MonitorInfoPane
 * @author run[zhangqi@lianchuang.com]
 * 8:14:57 PM May 12, 2009
 */
public class MonitorInfoPane extends Panel{
	
	private static final long serialVersionUID = -925057818560470074L;

	public MonitorInfoPane(String id, HashMap<String,String> taskExp){
		super(id);
		if(taskExp == null){
			taskExp = new HashMap<String,String>();
		}
		this.add(new Label("MonitorInfoPane.EXP_SourceFile_Num", taskExp.get(TWMonitorBean.EXP_SourceFile_Num)));
		this.add(new Label("MonitorInfoPane.EXP_SourceFile_Size", taskExp.get(TWMonitorBean.EXP_SourceFile_Size)));
		this.add(new Label("MonitorInfoPane.EXP_SourceFile_Names", taskExp.get(TWMonitorBean.EXP_SourceFile_Names)));
		this.add(new Label("MonitorInfoPane.EXP_ReturnFiles_Num", taskExp.get(TWMonitorBean.EXP_ReturnFiles_Num)));
		this.add(new Label("MonitorInfoPane.EXP_ReturnFiles_Size", taskExp.get(TWMonitorBean.EXP_ReturnFiles_Size)));
		this.add(new Label("MonitorInfoPane.EXP_ReturnFiles_Names", taskExp.get(TWMonitorBean.EXP_ReturnFiles_Names)));
		this.add(new Label("MonitorInfoPane.EXP_ResultInfo", taskExp.get(TWMonitorBean.EXP_ResultInfo)));
		this.add(new MultiLineLabel("MonitorInfoPane.EXP_Exception", taskExp.get(TWMonitorBean.EXP_Exception)));
	}
}
