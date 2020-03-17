package com.linkage.ftpdrudgery.wicket;

import java.io.UnsupportedEncodingException;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.intf.tools.CodeUtils;


public class AddTaskWizardResult extends WebPage{
	
	public AddTaskWizardResult(PageParameters pageParameters){
		super();
		String returnCode;
		String returnInfo;
		if (pageParameters == null) {
			returnCode = "0001";
			returnInfo = "添加任务失败";
		}else{
			returnCode = pageParameters.getString("returnCode");
			returnInfo = pageParameters.getString("returnInfo");
			try {
				returnInfo = new String(returnInfo.getBytes(CodeUtils.getEncoding(returnInfo)), GlobalBean.Encode);
			} catch (UnsupportedEncodingException e) {}
		}
		this.add(new Label("AddTaskWizardResult.returnCode", returnCode));
		this.add(new Label("AddTaskWizardResult.returnInfo", returnInfo));
	}
}
