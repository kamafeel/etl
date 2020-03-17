package com.linkage.ftpdrudgery.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.tools.CommonalityTool;

/**
 * FTP配置管理
 * @author run[zhangqi@lianchuang.com]
 * 5:23:31 PM May 20, 2009
 */

@AuthorizeInstantiation("ADMIN")
public class FtpManagePage extends WebPage {
	
	private static List<FtpInfoBean> ftpList = new ArrayList<FtpInfoBean>();
	
	private static List<FtpInfoBean> newFtpList = new ArrayList<FtpInfoBean>();
	
	private static List<TaskBean> taskList = new ArrayList<TaskBean>();
	
	private static Logger logger = Logger.getLogger(FtpManagePage.class.getName());
	
	private String result;
			
	public void initFtpList() {
		//调用远程Ftp配置队列列表
		try {
			ftpList.clear();
			ftpList = ConsoleTools.returnFtpInfoList();
		} catch (Exception e) {
			this.setResult("获取FTP配置队列失败");
			logger.error("远程Ftp配置队列异常",e);
		}
	}
	
	public void initTaskList(){
		//调用远程任务列表
		try {
			taskList.clear();
			taskList = ConsoleTools.returnTaskList();
		} catch (Exception e) {
			this.setResult("获取FTP配置队列失败");
			logger.error("远程任务列表异常",e);
		}
	}
	
	/**
	 * 添加映射关系
	 */
	public void Relationship(){
		
		newFtpList.clear();	
		for(FtpInfoBean fi : ftpList){
			String ftpId = fi.getId();
			StringBuilder relationship  = new StringBuilder();		
			for(TaskBean tb : taskList){
				if(tb.getSourceFtp() != null){
					String sFtpId = tb.getSourceFtp().getId();
					if(sFtpId.equalsIgnoreCase(ftpId)){
						relationship.append("[");
						relationship.append(CommonalityTool.getJobName(tb));
						relationship.append("]");
					}
				}
				if(tb.getDestFtp() != null){
					String dFtpId = tb.getDestFtp().getId();
					if(dFtpId.equalsIgnoreCase(ftpId)){
						relationship.append("[");
						relationship.append(CommonalityTool.getJobName(tb));
						relationship.append("]");
					}
				}
			}
			fi.setRelationship(relationship.toString());
			newFtpList.add(fi);
		}		
	}	
	
	public FtpManagePage(){
		super();
		
		this.initFtpList();
		this.initTaskList();
		this.Relationship();
				
		//分页任务队列
		final PageableListView<FtpInfoBean> listView = new PageableListView<FtpInfoBean>("newFtpList", newFtpList, 10) {
			private static final long serialVersionUID = -489056816693807637L;

			@Override
			protected void populateItem(ListItem<FtpInfoBean> item) {		
				final FtpInfoBean fi = (FtpInfoBean)item.getModelObject();
				item.add(new CheckBox("isSelect", new PropertyModel<Boolean>(fi, "isSelect")));	
				item.add(new Label("id", fi.getId()));
				item.add(new Label("remoteIP", fi.getRemoteIP()));
				item.add(new Label("port", String.valueOf(fi.getPort())));
				item.add(new Label("userName", fi.getUserName()));
				item.add(new Label("passWord", fi.getPassWord()));
				item.add(new Label("transfersType", fi.getTransfersType()));
				item.add(new Label("workType", fi.getWorkType()));
				item.add(new Label("relationship", fi.getRelationship()));
			}		
		};
		
		Form<Object> form = new Form<Object>("form");
		
		form.add(new Button("deleteButton"){

			private static final long serialVersionUID = 1872016357883915229L;

			@Override
			public void onSubmit(){
				StringBuilder sb = new StringBuilder();
				
				for(FtpInfoBean fi : newFtpList){
					if(fi.isSelect()){
						sb.append("[");
						if(CommonalityTool.checkNull(fi.getRelationship())){						
							sb.append(ConsoleTools.delFtpInfo(fi.getId()).getReturnInfo());							
						}else{
							sb.append("Ftp(" + fi.getId() + ")配置和当前任务有关联,不允许删除");
						}
						sb.append("]");
					}
				}
				result = sb.toString();
			}
		});
				
		form.add(listView);
		//分页
		form.add(new PagingNavigator("navigator", listView));
		
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
		
		this.add(form);			
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}

	public static List<FtpInfoBean> getNewFtpList() {
		return newFtpList;
	}

	public static void setNewFtpList(List<FtpInfoBean> newFtpList) {
		FtpManagePage.newFtpList = newFtpList;
	}
}
