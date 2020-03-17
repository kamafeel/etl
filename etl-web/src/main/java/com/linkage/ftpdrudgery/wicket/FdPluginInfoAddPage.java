package com.linkage.ftpdrudgery.wicket;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;

/**
 * 插件信息增加
 * @author run[zhangqi@lianchuang.com]
 * 12:47:10 AM Jan 6, 2013
 */

@AuthorizeInstantiation("ADMIN")
public class FdPluginInfoAddPage extends WebPage {
		
	private String result;
	private List<TaskBean> taskBeanList = new ArrayList<TaskBean>();
	private Map<String, String> displayMap = new HashMap<String, String>();
	private FdPluginInfo fdp;
	
	/**
	 * 远程初始化TaskBean
	 */
	public void initTaskBeanList(){
		try {
			taskBeanList.clear();
			taskBeanList = ConsoleTools.qryTaskList(null,"3");
			this.setResult("查询成功");
		} catch (Exception e) {
			this.setResult("查询失败");
		}
	}
	
	/**
	 * 初始化展现Map(id, display)
	 */
	public void initDisplayMap(){
		for(TaskBean tb :  taskBeanList){
			displayMap.put(tb.getId(), this.getTaskBeanDisplay(tb));
		}
	}
		
	/**
	 * 拼装最终展现
	 * @param fb
	 * @return
	 */
	public String getTaskBeanDisplay(TaskBean tb){
		StringBuilder sb = new StringBuilder();
		sb.append("ID:");
		sb.append(tb.getId());
		sb.append("|");
		sb.append("Name:");
		sb.append(tb.getTaskName());			
		return sb.toString();
	}
	
	
	/**
	 * Form
	 * @author run[zhangqi@lianchuang.com]
	 * 2:03:55 AM Jan 6, 2013
	 */
	private class FdPluginInfoAddForm extends Form<FdPluginInfo>{
		
		private static final long serialVersionUID = -1809592322497672714L;	
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public FdPluginInfoAddForm(String id) {
			super(id, new CompoundPropertyModel<FdPluginInfo>(fdp));
			
			/* 插件信息 */
			add(new RequiredTextField<Integer>("dateOffset"));
			add(new TextField<String>("dB2Statement"));
			add(new TextField<String>("perShell"));
			add(new RequiredTextField<String>("db2Environment"));
			add(new TextField<String>("pluginInfo"));
			
			IChoiceRenderer<Object> renderer = new ChoiceRenderer<Object>(){
				private static final long serialVersionUID = 8308753083747970994L;				
				public Object getDisplayValue(Object object) {
					TaskBean tb = (TaskBean)object;
					return displayMap.get(tb.getId());
				}	
			};
			
			add(new ListChoice("choiceTaskBean", taskBeanList, renderer));
			
			add(new Button("updateButton"){
				private static final long serialVersionUID = 4554235560153551181L;
				
				@Override
				public void onSubmit(){
					if(fdp.getChoiceTaskBean() == null){
						FdPluginInfoAddPage tep = new FdPluginInfoAddPage();
						tep.setResult("[异常]:请选择关联任务!!");
						setResponsePage(tep);
					}else{
						result = ConsoleTools.addFdPluginInfo(fdp).getReturnInfo();
						FdPluginInfoAddPage tep = new FdPluginInfoAddPage();
						tep.setResult(result);
						setResponsePage(tep);
					}					
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
	public FdPluginInfoAddPage(){
		super();
		//RMI远程查询TaskBeanList
		this.initTaskBeanList();
		this.initDisplayMap();
		/* 保存对象到Form中 */
		this.setFdp(new FdPluginInfo());
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
		add(new FdPluginInfoAddForm("form"));
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
