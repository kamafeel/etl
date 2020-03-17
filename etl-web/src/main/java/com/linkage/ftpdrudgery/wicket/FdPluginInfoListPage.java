package com.linkage.ftpdrudgery.wicket;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.ftpdrudgery.bean.QryFdPluginInfo;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.db.fd.bean.FdPluginInfo;
import com.linkage.intf.tools.CodeUtils;
import com.linkage.intf.tools.StringUtils;

/**
 * 插件信息列表
 * @author run[zhangqi@lianchuang.com]
 * 12:31:45 PM Jan 6, 2013
 */

public class FdPluginInfoListPage extends WebPage{
	
	private static List<FdPluginInfo> fdPluginInfoList = new ArrayList<FdPluginInfo>();
	private QryFdPluginInfo qfd;
	private String result;
		
	public void initFdPluginInfoList(){
		//调用远程任务列表
		try {
			fdPluginInfoList.clear();
			if(StringUtils.isEmpty(qfd.getQryPdId())){
				fdPluginInfoList = ConsoleTools.getFdPluginInfoList(null);
			}else{
				FdPluginInfo fdp = new FdPluginInfo();
				fdp.setPluginId(qfd.getQryPdId());
				fdPluginInfoList = ConsoleTools.getFdPluginInfoList(fdp);
			}
			this.setResult("插件信息查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			this.setResult("插件信息查询成功");
		}
	}
	
	private class QryFdPluginInfoForm extends Form<QryFdPluginInfo>{
		
		private static final long serialVersionUID = -9008780634455016247L;

		public QryFdPluginInfoForm(String id) {
			super(id, new CompoundPropertyModel<QryFdPluginInfo>(qfd));
			add(new TextField<String>("qryPdId"));
			
			add(new Button("queryButton"){
				private static final long serialVersionUID = 4295436029770508369L;

				public void onSubmit(){
					if(!StringUtils.isEmpty(qfd.getQryPdId())){
						PageParameters pp = new PageParameters();
						pp.put("qryPdId", qfd.getQryPdId());
						setResponsePage(FdPluginInfoListPage.class, pp);
					}else{
						setResponsePage(FdPluginInfoListPage.class);
					}
					
				}
			});
		}		
	}
	
	/**
	 * 控制界面
	 */
	public FdPluginInfoListPage(PageParameters pp){
		super();
		
		if(pp != null && pp.size()==1){
			qfd = new QryFdPluginInfo();
			String qry = pp.getString("qryPdId");
			try {
				qry = new String(qry.getBytes(CodeUtils.getEncoding(qry)), GlobalBean.Encode);
			} catch (UnsupportedEncodingException e) {}
			qfd.setQryPdId(qry);
		}else{
			qfd = new QryFdPluginInfo();
		}
		
		
		//刷新插件信息
		this.initFdPluginInfoList();
		
		final Label orderResult = new Label("FdPluginInfoListPage.orderResult", new PropertyModel<Object>(this, "result"));	
		//这里设置true值是为了在输出Html时，带上id的属性值
		//这样Wicket才能通过该属性能调用Ajax
		orderResult.setOutputMarkupId(true);
		this.add(orderResult);
		
		//分页任务队列
		final PageableListView<FdPluginInfo> listView = new PageableListView<FdPluginInfo>("FdPluginInfoListPage.fdPluginInfoList", fdPluginInfoList, 10){

			private static final long serialVersionUID = 0L;
			
			@Override
			protected void populateItem(ListItem<FdPluginInfo> item) {		
				final FdPluginInfo fdp = (FdPluginInfo)item.getModelObject();
				item.add(new Label("FdPluginInfoListPage.pluginId", fdp.getPluginId()));
				item.add(new Label("FdPluginInfoListPage.taskId", fdp.getTaskId()));
				item.add(new Label("FdPluginInfoListPage.dateOffset", String.valueOf(fdp.getDateOffset())));
				item.add(new Label("FdPluginInfoListPage.dB2Statement", fdp.getdB2Statement()));
				item.add(new Label("FdPluginInfoListPage.perShell", fdp.getPerShell()));
				item.add(new Label("FdPluginInfoListPage.db2Environment", fdp.getDb2Environment()));
				item.add(new Label("FdPluginInfoListPage.pluginInfo", fdp.getPluginInfo()));
				
				//更新/删除插件信息
				item.add(new PageLink<Object>("FdPluginInfoListPage.manFdPluginInfo", new IPageLink()
				{
					private static final long serialVersionUID = 0L;

					public Page getPage()
					{
						return new FdPluginInfoEditPage(fdp);
					}

					public Class<? extends Page> getPageIdentity()
					{
						return TaskEditPage.class;
					}
				}));

			}		
		};
		this.add(new QryFdPluginInfoForm("form"));
		
		this.add(listView);
		//分页
		this.add(new PagingNavigator("FdPluginInfoListPage.navigator", listView));
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}	
}
