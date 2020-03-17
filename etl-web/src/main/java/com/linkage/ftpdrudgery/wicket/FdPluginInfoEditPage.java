package com.linkage.ftpdrudgery.wicket;


import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;

/**
 * 插件信息更新
 * @author run[zhangqi@lianchuang.com]
 * 12:47:10 AM Jan 6, 2013
 */

@AuthorizeInstantiation("ADMIN")
public class FdPluginInfoEditPage extends WebPage {
		
	private String result;
	private FdPluginInfo fdp;
		
	/**
	 * Form
	 * @author run[zhangqi@lianchuang.com]
	 * 2:03:55 AM Jan 6, 2013
	 */
	private class FdPluginInfoEditForm extends Form<FdPluginInfo>{
		
		private static final long serialVersionUID = -1809592322497672714L;	
		
		public FdPluginInfoEditForm(String id) {
			super(id, new CompoundPropertyModel<FdPluginInfo>(fdp));
			
			/* 插件信息 */
			add(new TextField<String>("pluginId"));
			add(new TextField<String>("taskId"));
			add(new RequiredTextField<Integer>("dateOffset"));
			add(new TextField<String>("dB2Statement"));
			add(new TextField<String>("perShell"));
			add(new RequiredTextField<String>("db2Environment"));
			add(new TextField<String>("pluginInfo"));
			
			add(new Button("delButton"){
				
				private static final long serialVersionUID = 2923107450358178958L;

				public void onSubmit(){
					result = ConsoleTools.delFdPluginInfo(fdp).getReturnInfo();
					FdPluginInfoEditPage tep = new FdPluginInfoEditPage(fdp);
					tep.setResult(result);
					setResponsePage(tep);
				}
				
			});
			
			add(new Button("updateButton"){
				private static final long serialVersionUID = 4554235560153551181L;
				
				@Override
				public void onSubmit(){
					result = ConsoleTools.updateFdPluginInfo(fdp).getReturnInfo();
					FdPluginInfoEditPage tep = new FdPluginInfoEditPage(fdp);
					tep.setResult(result);
					setResponsePage(tep);
				}
				
			});

			add(new Button("returnButton"){
				
				private static final long serialVersionUID = 4207717569035694136L;

				@Override
				public void onSubmit(){
					//重新得到一个FdPluginInfoListPage实例
					setResponsePage(FdPluginInfoListPage.class);
				}
			}.setDefaultFormProcessing(false));
		}		
	}
	
	/**
	 * 构造方法
	 * @param tb
	 */
	public FdPluginInfoEditPage(FdPluginInfo fdp){
		super();
		/* 保存对象到Form中 */
		this.setFdp(fdp);
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
		add(new FdPluginInfoEditForm("form"));
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
	
	public FdPluginInfo getFdp() {
		return fdp;
	}

	public void setFdp(FdPluginInfo fdp) {
		this.fdp = fdp;
	}

}
