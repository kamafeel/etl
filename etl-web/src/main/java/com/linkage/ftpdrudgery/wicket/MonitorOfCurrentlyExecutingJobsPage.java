package com.linkage.ftpdrudgery.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.fdenum.TaskStatusEnum;
import com.linkage.ftpdrudgery.tools.TimeProcessor;
import com.linkage.intf.tools.TimeUtils;

/**
 * 当前执行任务监测
 * @author run[zhangqi8@asiainfo-linkage.com]
 * 3:16:31 PM Aug 24, 2011
 */

public class MonitorOfCurrentlyExecutingJobsPage extends WebPage {
	
	private static List<TWMonitorBean> twMonitorBeanList = new ArrayList<TWMonitorBean>();
	
	private String result;
	
	private String refurbishTime;

	public void initTaskList(){
		//调用远程任务列表
		try {
			twMonitorBeanList.clear();
			twMonitorBeanList = ConsoleTools.getMonitorOfCurrentlyExecutingJobs();
			this.setResult("当前运行的任务队列监控信息查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			this.setResult("当前运行的任务队列监控信息查询失败");
		}
	}

	public MonitorOfCurrentlyExecutingJobsPage(){
		super();		
		this.initTaskList();
		
		//增加一个跳出面板
		final ModalWindow paneMonitorInfo = new ModalWindow("MonitorOfCurrentlyExecutingJobsPage.paneMonitorInfo");
		add(paneMonitorInfo);
		paneMonitorInfo.setTitle("监控详情");
		paneMonitorInfo.setCookieName("MonitorOfCurrentlyExecutingJobsPage.paneMonitorInfo");
		paneMonitorInfo.setInitialHeight(300);
		paneMonitorInfo.setInitialWidth(335);
		
		// 当模态窗口被关闭时的处理
		paneMonitorInfo.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
			private static final long serialVersionUID = 0L;
			public boolean onCloseButtonClicked(AjaxRequestTarget target) {
				return true;
			}
		});
		
		//窗口被关闭时的处理
		paneMonitorInfo.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			private static final long serialVersionUID = 0L;
			public void onClose(AjaxRequestTarget target) {

			}
		});
		
		
		this.setRefurbishTime("任务状态快照: " + TimeProcessor.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
		//分页任务队列
		final PageableListView<TWMonitorBean> listView = new PageableListView<TWMonitorBean>("twMonitorBeanList", twMonitorBeanList, 10){

			private static final long serialVersionUID = 4883726819495327534L;

			@Override
			protected void populateItem(ListItem<TWMonitorBean> item) {		
				final TWMonitorBean twb = (TWMonitorBean)item.getModelObject();
				item.add(new Label("id", twb.getId()));
				item.add(new Label("taskName", twb.getTaskName()));
				item.add(new Label("taskStatus", TaskStatusEnum.valueOf(twb.getTaskStatus()).getStatus()));
				item.add(new Label("startTime", TimeUtils.date2String(twb.getStartTime(), "yyyy-MM-dd HH:mm:ss")));
				item.add(new Label("endTime", TimeUtils.date2String(twb.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
				item.add(new Label("castTime", twb.getCastTime()));
				item.add(new AjaxLink<Object>("mapExp") {
					private static final long serialVersionUID = -2463975429615134983L;
					public void onClick(AjaxRequestTarget target) {
						paneMonitorInfo.setContent(new MonitorInfoPane(paneMonitorInfo.getContentId(), twb.getTaskExp()));
						paneMonitorInfo.show(target);
					}
				});
			}		
		};
		
		Form<Object> form = new Form<Object>("form");	
		form.add(new Button("refurbishButton"){
			private static final long serialVersionUID = 8859924585374190838L;

			@Override
			public void onSubmit(){
				setResponsePage(MonitorOfCurrentlyExecutingJobsPage.class);
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
