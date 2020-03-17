package com.linkage.ftpdrudgery.wicket;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.QryHisMonitorBean;
import com.linkage.ftpdrudgery.bean.TWMonitorBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.fdenum.TaskStatusEnum;
import com.linkage.ftpdrudgery.tools.TimeProcessor;
import com.linkage.intf.tools.CodeUtils;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

/**
 * 历史任务监测
 * @author run[zhangqi8@asiainfo-linkage.com]
 * 3:16:31 PM Aug 24, 2011
 */

public class MonitorOfHisJobsPage extends WebPage {
	
	private static List<TWMonitorBean> twMonitorBeanList = new ArrayList<TWMonitorBean>();
		
	private String result;	
	private String refurbishTime;	
	private QryHisMonitorBean qmb;
	
	private static final List<String> TASK_STATUS_CHOICES = Arrays.asList("T8", "T9", "T10");
	
	public void initTaskList(){
		//调用远程任务列表
		try {
			twMonitorBeanList.clear();
			System.out.println((StringUtils.isEmpty(qmb.getTaskDate()) ? TimeUtils.getCurrentTime("yyyy-MM-dd") : qmb.getTaskDate()));
			twMonitorBeanList = ConsoleTools.getMonitorOfHistorJobs(qmb.getQryKey(), qmb.getQry(),qmb.getStatus(),
					(StringUtils.isEmpty(qmb.getTaskDate()) ? TimeUtils.getCurrentTime("yyyy-MM-dd") : qmb.getTaskDate()));
			this.setResult(StringUtils.isEmpty(qmb.getQry())? "历史任务监测查询成功[未输入查询条件,程序只显示今日记录]" : "历史任务监测查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			this.setResult("历史任务监测查询失败");
		}
	}
	
	/**
	 * Form
	 * @author run[zhangqi8@asiainfo-linkage.com]
	 * 2:03:55 PM May 20, 2009
	 */
	private class QryHisMonitorForm extends Form<QryHisMonitorBean>{
		
		private static final long serialVersionUID = -5704359652122171296L;
		private final List<String> qryKeyList = Arrays.asList(new String[] { "Id", "Name"});
		private Date date;
		
		public QryHisMonitorForm(String id) {
			super(id, new CompoundPropertyModel<QryHisMonitorBean>(qmb));
			try {
				date = StringUtils.isEmpty(qmb.getTaskDate()) ? new Date() : TimeUtils.string2Date(qmb.getTaskDate(), "yyyy-MM-dd");
			} catch (ParseException e) {}
			add(new RadioChoice<String>("qryKey", qryKeyList));
			add(new TextField<String>("qry"));
			
			add(new DropDownChoice<String>("status", TASK_STATUS_CHOICES,
					new IChoiceRenderer<String>() {
						private static final long serialVersionUID = 1L;

						public Object getDisplayValue(String value) {
							if(value.equalsIgnoreCase("T8")){
								return "任务完成";
							}else if(value.equalsIgnoreCase("T9")){
								return "无文件";
							}else if(value.equalsIgnoreCase("T10")){
								return "任务异常";
							}
							return "任务异常";
						}

						public String getIdValue(String object, int index) {
							return String.valueOf(TASK_STATUS_CHOICES.get(index));
						}
					}));
			
			DateTextField dateTextField = new DateTextField("dateTextField",
					new PropertyModel<Date>(this, "date"),
					new StyleDateConverter("S-", true)) {
				private static final long serialVersionUID = 1L;

				@Override
				public Locale getLocale() {
					return Locale.CHINESE;
				}
			};
			dateTextField.add(new DatePicker());
			add(dateTextField);			 
			
			add(new Button("queryButton"){
				private static final long serialVersionUID = 8859924585374190838L;
				@Override
				public void onSubmit(){
					PageParameters pp = new PageParameters();
					if(!StringUtils.isEmpty(qmb.getQry())){						
						pp.put("qry", qmb.getQry());
						pp.put("qryKey", qmb.getQryKey());
						
					}
					if(!StringUtils.isEmpty(qmb.getStatus())){
						pp.put("status", qmb.getStatus());
					}
					pp.put("taskDate", TimeUtils.date2String(date, "yyyy-MM-dd"));
					setResponsePage(MonitorOfHisJobsPage.class, pp);					
				}
			});
		}		
	}
	
	
	public MonitorOfHisJobsPage(PageParameters pp){
		super();
		
		qmb = new QryHisMonitorBean();
		if(pp.containsKey("qryKey")){
			qmb.setQryKey(pp.getString("qryKey"));
		}
		if(pp.containsKey("qry")){
			String qry = pp.getString("qry");
			try {
				qry = new String(qry.getBytes(CodeUtils.getEncoding(qry)), GlobalBean.Encode);
			} catch (UnsupportedEncodingException e) {}
			qmb.setQry(qry);
		}
		if(pp.containsKey("status")){
			qmb.setStatus(pp.getString("status"));
		}else{
			qmb.setStatus("T10");
		}
		if(pp.containsKey("taskDate")){
			qmb.setTaskDate(pp.getString("taskDate"));
		}

		this.initTaskList();
		this.setRefurbishTime("任务状态快照: " + TimeProcessor.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
		
		//增加一个跳出面板
		final ModalWindow paneMonitorInfo = new ModalWindow("MonitorOfHisJobsPage.paneMonitorInfo");
		add(paneMonitorInfo);
		paneMonitorInfo.setTitle("监控详情");
		paneMonitorInfo.setCookieName("MonitorOfHisJobsPage.paneMonitorInfo");
		paneMonitorInfo.setInitialHeight(350);
		paneMonitorInfo.setInitialWidth(525);
		
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

		//分页任务队列
		final PageableListView<TWMonitorBean> listView = new PageableListView<TWMonitorBean>("twMonitorBeanList", twMonitorBeanList, 25){

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
		
		add(new QryHisMonitorForm("form"));
				
		add(listView);
		//分页
		add(new PagingNavigator("navigator", listView));
		
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
		
		//更新结果
		final Label refurbishTimeL = new Label("refurbishTimeL", new PropertyModel<Object>(this, "refurbishTime"));
		add(refurbishTimeL);		
			
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
