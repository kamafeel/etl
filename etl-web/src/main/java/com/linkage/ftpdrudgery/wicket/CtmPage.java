package com.linkage.ftpdrudgery.wicket;

import java.io.UnsupportedEncodingException;
import java.rmi.Naming;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.linkage.ftpdrudgery.console.IFtpdrudgeryConsole;

public class CtmPage extends WebPage {

	private static final long serialVersionUID = 4615166713907846222L;
	private static final String W = "W";
	private static final String T = "T";
	private static final String D = "D";
	private Object objLock = new Object();

	public CtmPage(PageParameters parameters) {
		super();
		final FeedbackPanel debugFeedback = new FeedbackPanel("DebugFeedback");
		add(debugFeedback);
		try {
			info(this.callR(parameters.getString(CtmPage.W),
					parameters.getString(CtmPage.T),
					parameters.getString(CtmPage.D)));
		} catch (Exception e) {
			e.printStackTrace();
			info(e.getMessage());
		}
	}

	private String callR(String w, String t, String d) {

		synchronized (objLock) {

			try {
				this.uknowKgMKeastMiKit(5);
				IFtpdrudgeryConsole ic = (IFtpdrudgeryConsole) Naming.lookup(w);
				ic.setIDKey(t, d);
				return ic.getHump().toString();
			} catch (UnsupportedEncodingException e) {
			} catch (Exception e) {
			}
		}
		return "ERROR";
	}

	private void uknowKgMKeastMiKit(int i) throws UnsupportedEncodingException {
		if (i == 0) {
			throw new UnsupportedEncodingException();
		}
	}
}
