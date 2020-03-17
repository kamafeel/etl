package com.linkage.ftpdrudgery.wicket;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.QryTaskBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.intf.tools.CodeUtils;
import com.linkage.intf.tools.StringUtils;

/**
 * 任务查询界面
 * @author Run
 *
 */
public class QryTaskPage extends WebPage{
	
	private static List<TaskBean> taskList = new ArrayList<TaskBean>();
	private String result;
	private QryTaskBean qtb;
		
	/**
	 * 初始化Jobs队列
	 */
	public void initQryTask(){
		//查询任务
		try {
			taskList.clear();
			if(StringUtils.isEmpty(qtb.getQry())){
				this.setResult("请输入查询内容");
			}else{
				String qryKeyType = "";
				if("BackDir".equalsIgnoreCase(qtb.getQryKey())){
					qryKeyType = "2";
				}else if("TaskName".equalsIgnoreCase(qtb.getQryKey())){
					qryKeyType = "0";
				}else if("RegExp".equalsIgnoreCase(qtb.getQryKey())){
					qryKeyType = "1";
				}else{
					qryKeyType = "9";
				}
				taskList = ConsoleTools.qryTaskList(qtb.getQry(),qryKeyType);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			this.setResult("查询任务失败");
		}
	}
	
	/**
	 * Form
	 * @author run[zhangqi8@asiainfo-linkage.com]
	 * 2:03:55 PM May 20, 2009
	 */
	private class QryTaskForm extends Form<QryTaskBean>{
		
		private static final long serialVersionUID = -5704359652122171296L;
		private final List<String> qryKeyList = Arrays.asList(new String[] { "BackDir", "TaskName", "RegExp"});
				
		public QryTaskForm(String id) {
			super(id, new CompoundPropertyModel<QryTaskBean>(qtb));
			add(new RadioChoice<String>("qryKey", qryKeyList));
			add(new TextField<String>("qry"));
			
			add(new Button("queryButton"){
				private static final long serialVersionUID = 8859924585374190838L;
				@Override
				public void onSubmit(){
					if(!StringUtils.isEmpty(qtb.getQry())){
						PageParameters pp = new PageParameters();
						pp.put("qry", qtb.getQry());
						pp.put("qryKey", qtb.getQryKey());
						setResponsePage(QryTaskPage.class, pp);
					}else{
						setResponsePage(QryTaskPage.class);
					}
					
				}
			});
		}		
	}
	
	/**
	 * 控制界面
	 */
	public QryTaskPage(PageParameters pp){
		super();
		
		if(pp.size()<1){
			qtb = new QryTaskBean();
			qtb.setQryKey("BackDir");
		}else{
			qtb = new QryTaskBean();
			qtb.setQryKey(pp.getString("qryKey"));
			String qry = pp.getString("qry");
			try {
				qry = new String(qry.getBytes(CodeUtils.getEncoding(qry)), GlobalBean.Encode);
			} catch (UnsupportedEncodingException e) {}
			qtb.setQry(qry);
		}
		
		//刷新Jobs
		this.initQryTask();
		
		final Label orderResult = new Label("QryTaskPage.orderResult", new PropertyModel<Object>(this, "result"));	
		//这里设置true值是为了在输出Html时，带上id的属性值
		//这样Wicket才能通过该属性能调用Ajax
		orderResult.setOutputMarkupId(true);
		this.add(orderResult);
		
		//增加一个跳出面板
		final ModalWindow paneFtpInfo = new ModalWindow("QryTaskPage.paneFtpInfo");
		add(paneFtpInfo);
		paneFtpInfo.setTitle("Ftp信息");
		paneFtpInfo.setCookieName("QryTaskPage.paneFtpInfo");
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
		final ModalWindow panePluginInfo = new ModalWindow("QryTaskPage.panePluginInfo");
		add(panePluginInfo);
		panePluginInfo.setTitle("插件信息");
		panePluginInfo.setCookieName("QryTaskPage.panePluginInfo");
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
		final PageableListView<TaskBean> listView = new PageableListView<TaskBean>("QryTaskPage.taskList", taskList, 10){

			private static final long serialVersionUID = 0L;
			
			@Override
			protected void populateItem(ListItem<TaskBean> item) {
				final TaskBean tb = (TaskBean)item.getModelObject();
				item.add(new Label("QryTaskPage.taskId", tb.getId()));
				item.add(new Label("QryTaskPage.taskName", tb.getTaskName()));
				item.add(new Label("QryTaskPage.sourceDir", tb.getSourceDir()));
				item.add(new Label("QryTaskPage.destDir", tb.getDestDir()));
				item.add(new AjaxLink<Object>("QryTaskPage.sourceFtp") {
					private static final long serialVersionUID = -2463975429615134983L;
					public void onClick(AjaxRequestTarget target) {
						paneFtpInfo.setContent(new FtpInfoPane(paneFtpInfo.getContentId(), tb.getSourceFtp()));
						paneFtpInfo.show(target);
					}
				});
				item.add(new AjaxLink<Object>("QryTaskPage.destFtp") {
					private static final long serialVersionUID = -2463975429615134983L;
					public void onClick(AjaxRequestTarget target) {
						paneFtpInfo.setContent(new FtpInfoPane(paneFtpInfo.getContentId(), tb.getDestFtp()));
						paneFtpInfo.show(target);
					}
				});
				item.add(new Label("QryTaskPage.regExp", tb.getRegExp()));
				item.add(new Label("QryTaskPage.isDelete", tb.isDelete() ? "是" : "否"));
				item.add(new Label("QryTaskPage.checkSleepTime", tb.getCheckSleepTime()));
				item.add(new Label("QryTaskPage.cronTrigger", tb.getCronTrigger()));
				item.add(new Label("QryTaskPage.isbackUp", tb.isBackUp() ? "是" : "否"));
				item.add(new Label("QryTaskPage.backUpDir", tb.getBackUpDir()));
				
				//插件信息
				item.add(new AjaxLink<Object>("QryTaskPage.pluginInfo") {
					private static final long serialVersionUID = 7018968389687175716L;

					public void onClick(AjaxRequestTarget target) {
						panePluginInfo.setContent(new PluginInfoPane(panePluginInfo.getContentId(), tb));
						panePluginInfo.show(target);
					}
				});
			}		
		};
		
		add(new QryTaskForm("form"));
		
		this.add(listView);
		//分页
		this.add(new PagingNavigator("QryTaskPage.navigator", listView));
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}	
}
