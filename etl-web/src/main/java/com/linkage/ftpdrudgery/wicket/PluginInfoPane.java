package com.linkage.ftpdrudgery.wicket;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.linkage.ftpdrudgery.bean.TaskBean;


/**
 * PluginInfoPane
 * @author run[zhangqi@lianchuang.com]
 * 8:14:57 PM May 12, 2009
 */
public class PluginInfoPane extends Panel{
	
	private static final long serialVersionUID = -4216399460792066810L;

	public PluginInfoPane(String id, TaskBean tb){
		super(id);
		if(tb == null){
			tb = new TaskBean();
		}
		this.add(new Label("PluginInfoPane.pluginName", tb.getPluginName()));
		this.add(new Label("PluginInfoPane.pluginPath", tb.getPluginPath()));
		this.add(new Label("PluginInfoPane.pluginClassPath", tb.getPluginClassPath()));
		this.add(new Label("PluginInfoPane.pluginId", tb.getPluginId()));
	}
}
