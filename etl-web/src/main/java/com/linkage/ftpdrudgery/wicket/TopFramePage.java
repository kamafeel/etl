package com.linkage.ftpdrudgery.wicket;

import java.util.TimeZone;

//import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
//import org.apache.wicket.util.time.Duration;

/**
 * 框架顶层
 * @author run[zhangqi@lianchuang.com]
 * 4:05:22 PM May 20, 2009
 */
public class TopFramePage extends WebPage{
	
	public TopFramePage(){
		super();
		Clock clock = new Clock("serverClock", TimeZone.getTimeZone("Etc/GMT-8"));
		add(clock);
		add(new Label("currentUser",new PropertyModel<String>(this, "session.userName")));
		/*
		 * 有bug,造成页面会话丢失
		 */
		//5秒刷新一次
		//clock.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(60)));
	}
}
