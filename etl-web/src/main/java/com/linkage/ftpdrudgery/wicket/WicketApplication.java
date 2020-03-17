package com.linkage.ftpdrudgery.wicket;

import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;

import com.linkage.ftpdrudgery.bean.GlobalBean;

/**
 * 全局应用控制
 * @author run[zhangqi@lianchuang.com]
 * 4:02:01 PM May 20, 2009
 */
public class WicketApplication extends AuthenticatedWebApplication {

	public void init() {
		super.init();		
		getDebugSettings().setAjaxDebugModeEnabled(false);
		GlobalBean.getInstance().setUserDB();
		GlobalBean.ConsoleUrlList = getInitParameter("ConsoleUrlList");		
		GlobalBean.ConsoleUrl = GlobalBean.ConsoleUrlList.split(";")[0];
		GlobalBean.Encode = getInitParameter("Encode");
	}

	public WicketApplication() {
	}

	public Class<FdFramePage> getHomePage() {
		return FdFramePage.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		// TODO Auto-generated method stub
		return FdSignInPage.class;
	}

	@Override
	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
		// TODO Auto-generated method stub
		return WicketWebSession.class;
	}
}
