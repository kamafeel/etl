package com.linkage.ftpdrudgery.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;

/**
 * 任务列表
 * @author run[zhangqi@lianchuang.com]
 * 12:31:45 PM May 12, 2009
 */

public class JobListPage extends WebPage{
	
	private static List<TaskBean> taskList = new ArrayList<TaskBean>();
			
	private String result;
		
	/**
	 * 初始化Jobs队列
	 */
	public void initJobs(){
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
	
	/**
	 * 控制界面
	 */
	public JobListPage(){
		super();
		
		//刷新Jobs
		this.initJobs();
		
		final Label orderResult = new Label("JobListPage.orderResult", new PropertyModel<Object>(this, "result"));	
		//这里设置true值是为了在输出Html时，带上id的属性值
		//这样Wicket才能通过该属性能调用Ajax
		orderResult.setOutputMarkupId(true);
		this.add(orderResult);
		
		//增加一个跳出面板
		final ModalWindow paneFtpInfo = new ModalWindow("JobListPage.paneFtpInfo");
		add(paneFtpInfo);
		paneFtpInfo.setTitle("Ftp信息");
		paneFtpInfo.setCookieName("JobListPage.paneFtpInfo");
		paneFtpInfo.setInitialHeight(365);
		paneFtpInfo.setInitialWidth(330);
		
		// 当模态窗口被关闭时的处理
		paneFtpInfo.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
			private static final long serialVersionUID = 0L;
			public boolean onCloseButtonClicked(AjaxRequestTarget target) {
				return true;
			}
		});
		
		//窗口被关闭时的处理
		paneFtpInfo.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			private static final long serialVersionUID = 0L;
			public void onClose(AjaxRequestTarget target) {

			}
		});
		
		//增加一个跳出面板
		final ModalWindow panePluginInfo = new ModalWindow("JobListPage.panePluginInfo");
		add(panePluginInfo);
		panePluginInfo.setTitle("插件信息");
		panePluginInfo.setCookieName("JobListPage.panePluginInfo");
		panePluginInfo.setInitialHeight(200);
		panePluginInfo.setInitialWidth(500);
		
		// 当模态窗口被关闭时的处理
		panePluginInfo.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
			private static final long serialVersionUID = 0L;
			public boolean onCloseButtonClicked(AjaxRequestTarget target) {
				return true;
			}
		});
		
		//窗口被关闭时的处理
		panePluginInfo.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			private static final long serialVersionUID = 0L;
			public void onClose(AjaxRequestTarget target) {

			}
		});
		
		//分页任务队列
		final PageableListView<TaskBean> listView = new PageableListView<TaskBean>("JobListPage.taskList", taskList, 10){

			private static final long serialVersionUID = 0L;
			
			@Override
			protected void populateItem(ListItem<TaskBean> item) {		
				final TaskBean tb = (TaskBean)item.getModelObject();
				item.add(new Label("JobListPage.taskId", tb.getId()));
				item.add(new Label("JobListPage.taskName", tb.getTaskName()));
				item.add(new Label("JobListPage.sourceDir", tb.getSourceDir()));
				item.add(new Label("JobListPage.destDir", tb.getDestDir()));
				item.add(new AjaxLink<Object>("JobListPage.sourceFtp") {
					private static final long serialVersionUID = -2463975429615134983L;
					public void onClick(AjaxRequestTarget target) {
						paneFtpInfo.setContent(new FtpInfoPane(paneFtpInfo.getContentId(), tb.getSourceFtp()));
						paneFtpInfo.show(target);
					}
				});
				item.add(new AjaxLink<Object>("JobListPage.destFtp") {
					private static final long serialVersionUID = -2463975429615134983L;
					public void onClick(AjaxRequestTarget target) {
						paneFtpInfo.setContent(new FtpInfoPane(paneFtpInfo.getContentId(), tb.getDestFtp()));
						paneFtpInfo.show(target);
					}
				});
				item.add(new Label("JobListPage.regExp", tb.getRegExp()));
				item.add(new Label("JobListPage.isDelete", tb.isDelete() ? "是" : "否"));
				item.add(new Label("JobListPage.checkSleepTime", tb.getCheckSleepTime()));
				item.add(new Label("JobListPage.cronTrigger", tb.getCronTrigger()));
				item.add(new Label("JobListPage.isbackUp", tb.isBackUp() ? "是" : "否"));
				item.add(new Label("JobListPage.backUpDir", tb.getBackUpDir()));
				
				//插件信息
				item.add(new AjaxLink<Object>("JobListPage.pluginInfo") {
					private static final long serialVersionUID = 7018968389687175716L;

					public void onClick(AjaxRequestTarget target) {
						panePluginInfo.setContent(new PluginInfoPane(panePluginInfo.getContentId(), tb));
						panePluginInfo.show(target);
					}
				});
				
				//更新Task
				item.add(new PageLink<Object>("JobListPage.updateJob", new IPageLink()
				{
					private static final long serialVersionUID = 0L;

					public Page getPage()
					{
						return new TaskEditPage(tb);
					}

					public Class<? extends Page> getPageIdentity()
					{
						return TaskEditPage.class;
					}
				}));

			}		
		};
		
		this.add(listView);
		//分页
		this.add(new PagingNavigator("JobListPage.navigator", listView));
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}	
}
