package com.linkage.ftpdrudgery.wicket;


import java.util.Arrays;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.ServerBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;

/**
 * 选择服务器
 * @author run[zhangqi8@asiainfo-linkage.com]
 * 3:16:31 PM Aug 24, 2011
 */
@AuthorizeInstantiation({"ADMIN","USER"})
public class ChioceServerPage extends WebPage {
	
	private String result;
	private ServerBean sb;
	/**
	 * Form
	 * @author run[zhangqi8@asiainfo-linkage.com]
	 * 2:03:55 PM May 20, 2009
	 */
	private class ChioceServerForm extends Form<ServerBean>{
		
		private static final long serialVersionUID = -5704359652122171296L;
		private final List<String> serverList = Arrays.asList(GlobalBean.ConsoleUrlList.split(";"));
				
		public ChioceServerForm(String id) {
			super(id, new CompoundPropertyModel<ServerBean>(sb));
			final RadioChoice<String> rc = new RadioChoice<String>("serverURL", serverList);			
			add(rc);			
			add(new Button("ChioceButton"){
				private static final long serialVersionUID = 6079798386064040854L;
				@Override
				public void onSubmit(){
					GlobalBean.ConsoleUrl=sb.getServerURL();
					ConsoleTools.connectRMI();
					ChioceServerPage csp = new ChioceServerPage();
					if(ConsoleTools.ic == null){
						csp.setResult("选择["+sb.getServerURL()+"]为目标服务器,目标监控服务状态异常");
					}else{
						csp.setResult("选择["+sb.getServerURL()+"]为目标服务器,目标监控服务状态正常");
					}					
					setResponsePage(csp);					
				}
			});
		}		
	}
	
	
	public ChioceServerPage(){
		super();
		sb = new ServerBean();
		sb.setServerURL(GlobalBean.ConsoleUrl);
		add(new ChioceServerForm("form"));
		
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public ServerBean getSb() {
		return sb;
	}

	public void setSb(ServerBean sb) {
		this.sb = sb;
	}
}
