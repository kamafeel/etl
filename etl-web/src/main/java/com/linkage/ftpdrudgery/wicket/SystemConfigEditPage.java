package com.linkage.ftpdrudgery.wicket;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.SystemBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;

/**
 * 系统配置更新
 * @author run[zhangqi@lianchuang.com]
 * 12:47:10 AM May 20, 2009
 */

@AuthorizeInstantiation("ADMIN")
public class SystemConfigEditPage extends WebPage {
		
	private String result;
	
	private SystemBean sb;	
	
	public void initSystemBean(){
		try {
			sb = ConsoleTools.getSystemBean();
			this.setResult("获取系统信息成功,更新以下参数需要重启服务");
		} catch (Exception e) {
			e.printStackTrace();
			this.setResult("获取系统信息失败,更新以下参数需要重启服务");
		}
	}
	
	/**
	 * Form
	 * @author run[zhangqi@lianchuang.com]
	 * 2:03:55 PM May 20, 2009
	 */
	private class SystemConfigEditForm extends Form<SystemBean>{
		private static final long serialVersionUID = -8570974094414395582L;
		private final List<Boolean> consoleStart = Arrays.asList(new Boolean[] { true, false });
				
		public SystemConfigEditForm(String id) {
			super(id, new CompoundPropertyModel<SystemBean>(sb));
			add(new RequiredTextField<String>("url"));
			add(new DropDownChoice<Boolean>("consoleStart", consoleStart));
			add(new DropDownChoice<Boolean>("isRecordMonitor", consoleStart));
			add(new RequiredTextField<String>("downLoadTempDir"));
			add(new RequiredTextField<String>("backUpDir"));
			add(new RequiredTextField<String>("uploadPostfix"));				
			
			add(new Button("updateButton"){

				private static final long serialVersionUID = 4507174305771289607L;

				@Override
				public void onSubmit(){
					//如果控制台地址被更新,通知控制前台页面程序变更
					GlobalBean.ConsoleUrl = sb.getUrl();
					result = ConsoleTools.updateSystem(sb).getReturnInfo();
				}				
			});
		}		
	}
	
	/**
	 * 构造方法
	 * @param tb
	 */
	public SystemConfigEditPage(){
		super();
		this.initSystemBean();
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
		add(new SystemConfigEditForm("form"));
		//验证信息
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public SystemBean getSb() {
		return sb;
	}

	public void setSb(SystemBean sb) {
		this.sb = sb;
	}
}
