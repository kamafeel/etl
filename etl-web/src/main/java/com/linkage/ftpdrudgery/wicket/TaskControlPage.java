package com.linkage.ftpdrudgery.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.IClusterable;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.tools.CommonalityTool;
import com.linkage.ftpdrudgery.tools.TimeProcessor;

/**
 * 任务控制
 * @author run[zhangqi@lianchuang.com]
 * 5:23:31 PM May 20, 2009
 */

@AuthorizeInstantiation("ADMIN")
public class TaskControlPage extends WebPage {
	
	private static List<TaskBean> taskList = new ArrayList<TaskBean>();
	
	private String result;
	
	private String refurbishTime;
		
	public void initTaskList(){
		//调用远程任务列表
		try {
			taskList.clear();
			taskList = ConsoleTools.returnTaskList();
			this.setResult("任务查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			this.setResult("任务查询失败");
		}
	}
	
	
	public static String getTaskLastAction(String id){
		String lastAction = GlobalBean.TaskLastAction.get(id);
		return CommonalityTool.checkNull(lastAction) ? "未曾操作" : lastAction;
	}
	
	private static class DelSaveI implements IClusterable {
		private static final long serialVersionUID = 2839241484642264466L;
		public Boolean delSave = Boolean.FALSE;

		@Override
		public String toString() {
			return "delSave = '" + delSave + "'";
		}
	}
	
	
	public TaskControlPage(){
		super();
		
		final DelSaveI delSaveI = new DelSaveI();
		setDefaultModel(new CompoundPropertyModel<DelSaveI>(delSaveI));
		
		this.initTaskList();
		
		this.setRefurbishTime("任务状态快照: " + TimeProcessor.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
		
		
		//分页任务队列
		final PageableListView<TaskBean> listView = new PageableListView<TaskBean>("taskList", taskList, 10){

			private static final long serialVersionUID = 4883726819495327534L;

			@Override
			protected void populateItem(ListItem<TaskBean> item) {		
				final TaskBean tb = (TaskBean)item.getModelObject();
				item.add(new CheckBox("isSelect", new PropertyModel<Boolean>(tb, "isSelect")));
				item.add(new Label("id", tb.getId()));
				item.add(new Label("taskName", tb.getTaskName()));
				item.add(new Label("state", tb.getState()));
				item.add(new Label("lastAction", TaskControlPage.getTaskLastAction(tb.getId())));
			}		
		};
		
		Form<Object> form = new Form<Object>("form");
		
		form.add(new CheckBox("delSave"));
		
		form.add(new Button("pauseButton"){

			private static final long serialVersionUID = 2456979737317594229L;

			@Override
			public void onSubmit(){
				StringBuilder sb = new StringBuilder();
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						sb.append("[");
						sb.append(ConsoleTools.pauseJob(tb.getId()).getReturnInfo());
						sb.append("]");			
					}
				}
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						GlobalBean.setTaskLastAction(tb.getId(), "暂停");
					}
				}				
				result = sb.toString();
				//setResponsePage(TaskControlPage.class);
			}
		});
		
		form.add(new Button("pauseAllButton"){
			
			private static final long serialVersionUID = 1824029794666111175L;

			@Override
			public void onSubmit(){
				for(TaskBean tb : taskList){
					GlobalBean.setTaskLastAction(tb.getId(), "暂停所有");
				}
				result = ConsoleTools.pauseAll().getReturnInfo();
				//setResponsePage(TaskControlPage.class);
			}
		});
		
		form.add(new Button("resumeAllButton"){

			private static final long serialVersionUID = -7053736132713038884L;

			@Override
			public void onSubmit(){
				for(TaskBean tb : taskList){
					GlobalBean.setTaskLastAction(tb.getId(), "恢复所有");
				}
				result = ConsoleTools.resumeAll().getReturnInfo();
				//setResponsePage(TaskControlPage.class);
			}
		});
		
		form.add(new Button("resumeButton"){

			private static final long serialVersionUID = -3775162782405670633L;

			@Override
			public void onSubmit(){
				StringBuilder sb = new StringBuilder();
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						sb.append("[");
						sb.append(ConsoleTools.resumeJob(tb.getId()).getReturnInfo());
						sb.append("]");
					}
				}
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						GlobalBean.setTaskLastAction(tb.getId(), "恢复");
					}
				}
				result = sb.toString();
				//setResponsePage(TaskControlPage.class);
			}
		});
		
		form.add(new Button("interruptButton"){

			private static final long serialVersionUID = -3775162782405670633L;

			@Override
			public void onSubmit(){
				StringBuilder sb = new StringBuilder();
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						sb.append("[");
						sb.append(ConsoleTools.interruptJob(tb.getId()).getReturnInfo());
						sb.append("]");
					}
				}
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						GlobalBean.setTaskLastAction(tb.getId(), "强行终止");
					}
				}
				result = sb.toString();
				//setResponsePage(TaskControlPage.class);
			}
		});
		
		form.add(new Button("triggerButton"){

			private static final long serialVersionUID = -8352309940248794808L;

			@Override
			public void onSubmit(){
				StringBuilder sb = new StringBuilder();
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						sb.append("[");
						sb.append(ConsoleTools.triggerJob(tb.getId()).getReturnInfo());
						sb.append("]");
					}
				}
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						GlobalBean.setTaskLastAction(tb.getId(), "立即执行");
					}
				}
				result = sb.toString();
				//setResponsePage(TaskControlPage.class);
			}
		});
		
		form.add(new Button("deleteButton"){

			private static final long serialVersionUID = 1872016357883915229L;

			@Override
			public void onSubmit(){
				StringBuilder sb = new StringBuilder();
				for(TaskBean tb : taskList){
					if(tb.isSelect()){
						sb.append("[");
						sb.append(ConsoleTools.deleteJob(tb.getId(), delSaveI.delSave.booleanValue()).getReturnInfo());
						sb.append("]");
					}
				}
				result = sb.toString();
				//setResponsePage(TaskControlPage.class);
			}
		});
		
		form.add(new Button("refurbishButton"){
			private static final long serialVersionUID = 8859924585374190838L;

			@Override
			public void onSubmit(){
				setResponsePage(TaskControlPage.class);
			}
		});
		
		
		form.add(listView);
		//分页
		form.add(new PagingNavigator("navigator", listView));
		
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
		
		//更新结果
		final Label refurbishTimeL = new Label("refurbishTimeL", new PropertyModel<Object>(this, "refurbishTime"));
		form.add(refurbishTimeL);
		
		this.add(form);			
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}


	public String getRefurbishTime() {
		return refurbishTime;
	}


	public void setRefurbishTime(String refurbishTime) {
		this.refurbishTime = refurbishTime;
	}
}
