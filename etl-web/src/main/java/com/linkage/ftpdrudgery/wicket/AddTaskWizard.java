package com.linkage.ftpdrudgery.wicket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.wizard.StaticContentStep;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.extensions.wizard.WizardModel.ICondition;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;


import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.ReturnBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.tools.CommonalityTool;
import com.linkage.ftpdrudgery.wicket.Validator.IpValidator;

/**
 * 任务新增向导
 * @author run[zhangqi@lianchuang.com]
 * 3:59:22 PM May 13, 2009
 */

public class AddTaskWizard extends Wizard{
	
	private static final long serialVersionUID = -7748712293094024454L;
	
	/**
	 * 是否要添加提供方FTP配置Step
	 * 9:47:46 PM May 14, 2009
	 */
	private final class IsAddSourceFtpStep extends WizardStep {
		
		private static final long serialVersionUID = -1941311311944212386L;

		public IsAddSourceFtpStep(){
			//步骤基本信息
			super(new ResourceModel("IsAddSourceFtpStep.Title"), new ResourceModel("IsAddSourceFtpStep.Summary"));		
	    	this.add(new Label("IsAddSourceFtpStep.Label", new ResourceModel("IsAddSourceFtpStep.Explain")));
			this.add(new CheckBox("isAddSourceFtp"));		
		}
	}
	
	
	/**
	 * 选择现有提供方FTP配置Step
	 * 9:47:46 PM May 14, 2009
	 */
	private final class ChoiceSourceFtpStep extends WizardStep implements ICondition {
		
		private static final long serialVersionUID = 5731136630568188871L;

		private List<FtpInfoBean> ftpInfoList = new ArrayList<FtpInfoBean>();
		
		private Map<String, String> displayMap = new HashMap<String, String>();
		
		private String queryResult;
				
		/**
		 * 远程初始化FtpInfo
		 */
		public void initFtpInfoList(){
			try {
				ftpInfoList.clear();
				ftpInfoList = ConsoleTools.returnFtpInfoList();
				this.setQueryResult("查询成功");
			} catch (Exception e) {
				this.setQueryResult("查询失败");
			}
		}
			
		/**
		 * 初始化展现Map(id, display)
		 */
		public void initDisplayMap(){
			for(FtpInfoBean fb :  ftpInfoList){
				displayMap.put(fb.getId(), this.getFtpInfoDisplay(fb));
			}
		}
			
		/**
		 * 拼装最终展现
		 * @param fb
		 * @return
		 */
		public String getFtpInfoDisplay(FtpInfoBean fb){
			StringBuilder sb = new StringBuilder();
			sb.append("ID:");
			sb.append(fb.getId());
			sb.append("|");
			sb.append("IP:");
			sb.append(fb.getRemoteIP());
			sb.append("|");
			sb.append("端口:");
			sb.append(fb.getPort());
			sb.append("|");
			sb.append("登录账号:");
			sb.append(fb.getUserName());
			sb.append("|");
			sb.append("传输模式:");
			sb.append(fb.getTransfersType());
			sb.append("|");
			sb.append("工作模式:");
			sb.append(fb.getWorkType());			
			return sb.toString();
		}
				
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ChoiceSourceFtpStep(){
			super(new ResourceModel("ChoiceSourceFtpStep.Title"), new ResourceModel("ChoiceSourceFtpStep.Summary"));
			
			//RMI远程查询FtpInfoList
			this.initFtpInfoList();
			
			//初始化展示
			this.initDisplayMap();
			
			//查询Ftp配置结果显示
			final Label orderResult = new Label("ChoiceSourceFtpStep.QueryResult", new PropertyModel<Object>(this, "queryResult"));	
			
			final TaskBorder border = new TaskBorder("ChoiceSourceFtpStep.ResultBorder");
			border.add(orderResult);
			this.add(border);
			
			IChoiceRenderer<Object> renderer = new ChoiceRenderer<Object>(){
				private static final long serialVersionUID = 8308753083747970994L;				
				public Object getDisplayValue(Object object) {
					FtpInfoBean fb = (FtpInfoBean)object;
					return displayMap.get(fb.getId());
				}	
			};
			this.add(new ListChoice("choiceSourceFtpInfoBean", ftpInfoList, renderer));
			
			this.add(new CheckBox("addNewSourceFtp"));
		}

		public boolean evaluate() {
			return isAddSourceFtp;
		}

		@SuppressWarnings("unused")
		public String getQueryResult() {
			return queryResult;
		}

		public void setQueryResult(String queryResult) {
			this.queryResult = queryResult;
		}
	}
	
	
	/**
	 * 增加一个新提供方FtpInfoStep
	 * 6:17:07 PM May 15, 2009
	 */
	private final class AddNewSourceFtpInfoStep extends WizardStep implements ICondition
	{
		
		private static final long serialVersionUID = 4843581278542387226L;

		private List<String> transfersType = Arrays.asList(new String[] { "BINARY", "ASCII" });
		
		private List<String> workType = Arrays.asList(new String[] { "PORT", "PASV" });
		
		private List<Boolean> isNoOp = Arrays.asList(new Boolean[] { true, false });
		private List<Boolean> isRBD = Arrays.asList(new Boolean[] { true, false });
		
		public AddNewSourceFtpInfoStep(){
			super(new ResourceModel("AddNewSourceFtpInfoStep.Title"), new ResourceModel("AddNewSourceFtpInfoStep.Summary"));
			
			add(new RequiredTextField<String>("addNewSourceFtpInfoBean.remoteIP").add(IpValidator.getInstance()));
			add(new RequiredTextField<Integer>("addNewSourceFtpInfoBean.port"));
			add(new RequiredTextField<String>("addNewSourceFtpInfoBean.userName"));
			add(new RequiredTextField<String>("addNewSourceFtpInfoBean.passWord"));
			add(new DropDownChoice<String>("addNewSourceFtpInfoBean.transfersType", transfersType));
			add(new DropDownChoice<String>("addNewSourceFtpInfoBean.workType", workType));
			add(new RequiredTextField<Integer>("addNewSourceFtpInfoBean.timeout"));
			add(new RequiredTextField<String>("addNewSourceFtpInfoBean.encoding"));
			add(new RequiredTextField<Integer>("addNewSourceFtpInfoBean.retryCount"));
			add(new RequiredTextField<Long>("addNewSourceFtpInfoBean.retryInterval"));
			add(new RequiredTextField<Long>("addNewSourceFtpInfoBean.maxConnect"));
			add(new DropDownChoice<Boolean>("addNewSourceFtpInfoBean.isNoOp", isNoOp));
			add(new DropDownChoice<Boolean>("addNewSourceFtpInfoBean.isRBD", isRBD));
		}
		
		public boolean evaluate() {
			return addNewSourceFtp;
		}
	}
	
	
	/**
	 * 是否要添加目的方FTP配置
	 * 9:47:46 PM May 14, 2009
	 */
	private final class IsAddDestFtpStep extends WizardStep {

		private static final long serialVersionUID = 3742426010227566393L;

		public IsAddDestFtpStep(){
			//步骤基本信息
			super(new ResourceModel("IsAddDestFtpStep.Title"), new ResourceModel("IsAddDestFtpStep.Summary"));		
	    	this.add(new Label("IsAddDestFtpStep.Label", new ResourceModel("IsAddDestFtpStep.Explain")));
			this.add(new CheckBox("isAddDestFtp"));		
		}
	}
	
	
	/**
	 * 选择现有提供方FTP配置Step
	 * 9:47:46 PM May 14, 2009
	 */
	private final class ChoiceDestFtpStep extends WizardStep implements ICondition {
		
		private static final long serialVersionUID = -5552569500816099253L;

		private List<FtpInfoBean> ftpInfoList = new ArrayList<FtpInfoBean>();
		
		private Map<String, String> displayMap = new HashMap<String, String>();
		
		private String queryResult;
				
		/**
		 * 远程初始化FtpInfo
		 */
		public void initFtpInfoList(){
			try {
				ftpInfoList.clear();
				ftpInfoList = ConsoleTools.returnFtpInfoList();
				this.setQueryResult("查询成功");
			} catch (Exception e) {
				this.setQueryResult("查询失败");
			}
		}
			
		/**
		 * 初始化展现Map(id, display)
		 */
		public void initDisplayMap(){
			for(FtpInfoBean fb :  ftpInfoList){
				displayMap.put(fb.getId(), this.getFtpInfoDisplay(fb));
			}
		}
			
		/**
		 * 拼装最终展现
		 * @param fb
		 * @return
		 */
		public String getFtpInfoDisplay(FtpInfoBean fb){
			StringBuilder sb = new StringBuilder();
			sb.append("ID:");
			sb.append(fb.getId());
			sb.append("|");
			sb.append("IP:");
			sb.append(fb.getRemoteIP());
			sb.append("|");
			sb.append("端口:");
			sb.append(fb.getPort());
			sb.append("|");
			sb.append("登录账号:");
			sb.append(fb.getUserName());
			sb.append("|");
			sb.append("传输模式:");
			sb.append(fb.getTransfersType());
			sb.append("|");
			sb.append("工作模式:");
			sb.append(fb.getWorkType());			
			return sb.toString();
		}
				
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ChoiceDestFtpStep(){
			super(new ResourceModel("ChoiceDestFtpStep.Title"), new ResourceModel("ChoiceDestFtpStep.Summary"));
			
			//RMI远程查询FtpInfoList
			this.initFtpInfoList();
			
			//初始化展示
			this.initDisplayMap();
			
			//查询Ftp配置结果显示
			final Label orderResult = new Label("ChoiceDestFtpStep.QueryResult", new PropertyModel<Object>(this, "queryResult"));	
			
			final TaskBorder border = new TaskBorder("ChoiceDestFtpStep.ResultBorder");
			border.add(orderResult);
			this.add(border);
			
			IChoiceRenderer<Object> renderer = new ChoiceRenderer<Object>(){
				private static final long serialVersionUID = 8308753083747970994L;				
				public Object getDisplayValue(Object object) {
					FtpInfoBean fb = (FtpInfoBean)object;
					return displayMap.get(fb.getId());
				}	
			};	
			this.add(new ListChoice("choiceDestFtpInfoBean", ftpInfoList, renderer));
			
			this.add(new CheckBox("addNewDestFtp"));
		}

		public boolean evaluate() {
			return isAddDestFtp;
		}

		@SuppressWarnings("unused")
		public String getQueryResult() {
			return queryResult;
		}

		public void setQueryResult(String queryResult) {
			this.queryResult = queryResult;
		}
	}
	
	
	
	/**
	 * 增加一个新提供方FtpInfoStep
	 * 6:17:07 PM May 15, 2009
	 */
	private final class AddNewDestFtpInfoStep extends WizardStep implements ICondition
	{
		
		private static final long serialVersionUID = 1881256748491074281L;

		private List<String> transfersType = Arrays.asList(new String[] { "BINARY", "ASCII" });
		
		private List<String> workType = Arrays.asList(new String[] { "PORT", "PASV" });
		
		private List<Boolean> isNoOp = Arrays.asList(new Boolean[] { true, false });
		private List<Boolean> isRBD = Arrays.asList(new Boolean[] { true, false });

		public AddNewDestFtpInfoStep(){
			super(new ResourceModel("AddNewDestFtpInfoStep.Title"), new ResourceModel("AddNewDestFtpInfoStep.Summary"));
			
			add(new RequiredTextField<String>("addNewDestFtpInfoBean.remoteIP").add(IpValidator.getInstance()));
			add(new RequiredTextField<Integer>("addNewDestFtpInfoBean.port"));
			add(new RequiredTextField<String>("addNewDestFtpInfoBean.userName"));
			add(new RequiredTextField<String>("addNewDestFtpInfoBean.passWord"));
			add(new DropDownChoice<String>("addNewDestFtpInfoBean.transfersType", transfersType));
			add(new DropDownChoice<String>("addNewDestFtpInfoBean.workType", workType));
			add(new RequiredTextField<Integer>("addNewDestFtpInfoBean.timeout"));
			add(new RequiredTextField<String>("addNewDestFtpInfoBean.encoding"));
			add(new RequiredTextField<Integer>("addNewDestFtpInfoBean.retryCount"));
			add(new RequiredTextField<Long>("addNewDestFtpInfoBean.retryInterval"));
			add(new RequiredTextField<Long>("addNewDestFtpInfoBean.maxConnect"));
			add(new DropDownChoice<Boolean>("addNewDestFtpInfoBean.isNoOp", isNoOp));
			add(new DropDownChoice<Boolean>("addNewDestFtpInfoBean.isRBD", isRBD));
		}
		
		public boolean evaluate() {
			return addNewDestFtp;
		}
	}
	
	
	/**
	 * 任务信息Step
	 * 4:33:33 PM May 13, 2009
	 */
	private final class TaskInfoStep extends WizardStep
	{
		private static final long serialVersionUID = 5588935697666588370L;
		
		private List<Boolean> isDelete = Arrays.asList(new Boolean[] { true, false });
		
		private List<Boolean> IsBackUp = Arrays.asList(new Boolean[] { true, false });
		
		public TaskInfoStep()
		{
			//步骤基本信息
			super(new ResourceModel("TaskInfoStep.Title"), new ResourceModel("TaskInfoStep.Summary"));
			add(new RequiredTextField<String>("taskBean.taskName"));
			final TextField<String> sourceDir = new TextField<String>("taskBean.sourceDir");
			add(sourceDir);
			final TextField<String> destDir = new TextField<String>("taskBean.destDir");
			add(destDir);
			add(new RequiredTextField<String>("taskBean.regExp"));
			add(new DropDownChoice<Boolean>("taskBean.isDelete", isDelete));
			add(new RequiredTextField<String>("taskBean.cronTrigger"));
			add(new DropDownChoice<Boolean>("taskBean.isBackUp", IsBackUp));
			add(new RequiredTextField<String>("taskBean.backUpDir"));
			add(new RequiredTextField<String>("taskBean.checkSleepTime"));
			add(new RequiredTextField<String>("taskBean.beforeDay"));
			add(new RequiredTextField<String>("taskBean.recordValidDay"));
			
			final TextField<String> pluginName = new TextField<String>("taskBean.pluginName");
			final TextField<String> pluginPath = new TextField<String>("taskBean.pluginPath");
			final TextField<String> pluginClassPath = new TextField<String>("taskBean.pluginClassPath");
			final TextField<String> pluginClassId = new TextField<String>("taskBean.pluginId");
			add(pluginName);
			add(pluginPath);
			add(pluginClassPath);
			add(pluginClassId);
			
			add(new AbstractFormValidator() {

				private static final long serialVersionUID = -7695763468173223298L;

				public FormComponent<?>[] getDependentFormComponents() {

					return null;
				}

				public void validate(Form<?> form) {
					
					//如果原始文件获取路径为空,插件不能为空
					if(CommonalityTool.checkNull(sourceDir.getInput())){
						if(CommonalityTool.checkNull(pluginName.getInput()) ||
								CommonalityTool.checkNull(pluginPath.getInput()) ||
								CommonalityTool.checkNull(pluginClassPath.getInput())){
							sourceDir.error((IValidationError)new ValidationError().addMessageKey("error.noSetSourceDir"));
						}
					}
					
					//原始文件目的路径和原始文件获取路径不能同时为空
					if(CommonalityTool.checkNull(destDir.getInput())){
						if(CommonalityTool.checkNull(sourceDir.getInput())){
							destDir.error((IValidationError)new ValidationError().addMessageKey("error.noSetDestDir"));
						}
					}
					
					//[数据目的路径]和插件信息不能同时为空
					if(CommonalityTool.checkNull(destDir.getInput())){
						if(CommonalityTool.checkNull(pluginName.getInput()) ||
								CommonalityTool.checkNull(pluginPath.getInput()) ||
								CommonalityTool.checkNull(pluginClassPath.getInput())){
							destDir.error((IValidationError)new ValidationError().addMessageKey("error.noSetDestDirAndPlugin"));
						}
					}
					
					//插件信息完整性
					if(!CommonalityTool.checkNull(pluginName.getInput())){
						if(CommonalityTool.checkNull(pluginPath.getInput()) ||
								CommonalityTool.checkNull(pluginClassPath.getInput())){
							pluginName.error((IValidationError)new ValidationError().addMessageKey("error.noSetPlugin"));
						}
					}
					
					//插件信息完整性
					if(!CommonalityTool.checkNull(pluginPath.getInput())){
						if(CommonalityTool.checkNull(pluginName.getInput()) ||
								CommonalityTool.checkNull(pluginClassPath.getInput())){
							pluginName.error((IValidationError)new ValidationError().addMessageKey("error.noSetPlugin"));
						}
					}
					
					//插件信息完整性
					if(!CommonalityTool.checkNull(pluginClassPath.getInput())){
						if(CommonalityTool.checkNull(pluginPath.getInput()) ||
								CommonalityTool.checkNull(pluginName.getInput())){
							pluginName.error((IValidationError)new ValidationError().addMessageKey("error.noSetPlugin"));
						}
					}
				}
			});
		}
	}

	
	/**
	 * 任务是否永久保存Step
	 * 4:33:33 PM May 13, 2009
	 */
	private final class TaskSaveStep extends WizardStep
	{	
		private static final long serialVersionUID = -6692637852429059431L;

		public TaskSaveStep()
		{
			//步骤基本信息
			this.setTitleModel(new ResourceModel("TaskSaveStep.Title"));
			this.setSummaryModel(new StringResourceModel("TaskSaveStep.Summary", this, new Model<TaskBean>(taskBean)));
			add(new CheckBox("isSave"));
		}
	}
	
	/**
	 * 任务确认Step
	 * @author run[zhangqi@lianchuang.com]
	 * 10:39:33 PM May 13, 2009
	 */
	private final class ConfirmationStep extends StaticContentStep
	{
		private static final long serialVersionUID = -7437590490326578985L;

		public ConfirmationStep()
		{
			super(true);
			IModel<TaskBean> tbwModel = new Model<TaskBean>(taskBean);
			setTitleModel(new ResourceModel("ConfirmationStep.Title"));
			setSummaryModel(new StringResourceModel("ConfirmationStep.Summary", this, tbwModel));
			setContentModel(new StringResourceModel("ConfirmationStep.Content", this, tbwModel));
		}
	}
	
	/* 全局参数传递 */
	private TaskBean taskBean;
	
	/* 全局参数传递 */
	private FtpInfoBean choiceSourceFtpInfoBean;
	
	/* 全局参数传递 */
	private FtpInfoBean choiceDestFtpInfoBean;
	
	/* 全局参数传递 */
	private FtpInfoBean addNewSourceFtpInfoBean;
	
	/* 全局参数传递 */
	private FtpInfoBean addNewDestFtpInfoBean;
	
	/*
	 * 是否需要下载文件
	 */
	private boolean isAddSourceFtp = false;
	
	/*
	 * 现在配置中无符合条件的,需要新增 
	 */
	private boolean addNewSourceFtp = false;
	
	/*
	 * 是否需要上传
	 */
	private boolean isAddDestFtp = false;
	
	/*
	 * 现在配置中无符合条件的,需要新增
	 */
	private boolean addNewDestFtp = false;
	
	private boolean isSave = false;
	
	/**
	 * 任务链路配置
	 * @param id
	 */
	public AddTaskWizard(String id){
		super(id);
		
		//创建一个干净的WizardBean,反射机制,需要对象唯一性
		taskBean = new TaskBean();
		taskBean.setCheckSleepTime("0");
		taskBean.setBeforeDay("0");
		taskBean.setRecordValidDay("0");
		/* 选择源FTP */
		choiceSourceFtpInfoBean = new FtpInfoBean();
		/* 选择目标FTP */
		choiceDestFtpInfoBean = new FtpInfoBean();
		/* 源FTP */
		addNewSourceFtpInfoBean = new FtpInfoBean();
		addNewSourceFtpInfoBean.setPort(21);
		addNewSourceFtpInfoBean.setTransfersType("BINARY");
		addNewSourceFtpInfoBean.setWorkType("PORT");
		addNewSourceFtpInfoBean.setTimeout(12000);
		addNewSourceFtpInfoBean.setEncoding("GBK");
		addNewSourceFtpInfoBean.setRetryCount(3);
		addNewSourceFtpInfoBean.setRetryInterval(20000);
		addNewSourceFtpInfoBean.setMaxConnect(0);
		addNewSourceFtpInfoBean.setNoOp(false);
		addNewSourceFtpInfoBean.setRBD(false);
		/* 目标FTP */
		addNewDestFtpInfoBean = new FtpInfoBean();
		addNewDestFtpInfoBean.setPort(21);
		addNewDestFtpInfoBean.setTransfersType("BINARY");
		addNewDestFtpInfoBean.setWorkType("PORT");
		addNewDestFtpInfoBean.setTimeout(12000);
		addNewDestFtpInfoBean.setEncoding("GBK");
		addNewDestFtpInfoBean.setRetryCount(3);
		addNewDestFtpInfoBean.setRetryInterval(20000);
		addNewDestFtpInfoBean.setMaxConnect(0);
		addNewDestFtpInfoBean.setNoOp(false);
		addNewDestFtpInfoBean.setRBD(false);
		
		//向导链路并初始化
		this.setDefaultModel(new CompoundPropertyModel<AddTaskWizard>(this));
		WizardModel model = new WizardModel();
		model.add(new IsAddSourceFtpStep());	
		model.add(new ChoiceSourceFtpStep());
		model.add(new AddNewSourceFtpInfoStep());
		model.add(new IsAddDestFtpStep());
		model.add(new ChoiceDestFtpStep());
		model.add(new AddNewDestFtpInfoStep());
		model.add(new TaskInfoStep());
		model.add(new TaskSaveStep());
		model.add(new ConfirmationStep());
		
		init(model);		
	}
	
	@Override
	public void onCancel() {
		setResponsePage(WizardPage.class);
	}
	
	
	
	@Override
	public void onFinish() {
		ReturnBean rb = this.CallConsoleFinishAddTask();
		PageParameters pp = new PageParameters();
		if(rb == null){
			pp.put("returnCode", String.valueOf(0001));
			pp.put("returnInfo", "添加任务失败");
		}else{
			pp.put("returnCode", String.valueOf(rb.getReturnCode()));
			System.out.println(rb.getReturnInfo());
			pp.put("returnInfo", rb.getReturnInfo());
		}
		setResponsePage(AddTaskWizardResult.class, pp);
	}
	
	/**
	 * 业务实现
	 */
	public ReturnBean CallConsoleFinishAddTask(){
		
		TaskBean tb = this.getTaskBean();
		
		//数据来源FtpInfo
		if(this.isAddSourceFtp()){
			if(this.isAddNewSourceFtp() && !CommonalityTool.checkNull(this.getAddNewSourceFtpInfoBean().getRemoteIP())){
				tb.setSourceFtp(this.getAddNewSourceFtpInfoBean());
			}else if(!CommonalityTool.checkNull(this.getChoiceSourceFtpInfoBean().getRemoteIP())){
				tb.setSourceFtp(this.getChoiceSourceFtpInfoBean());
			}
		}
		//数据目的FtpInfo
		if(this.isAddDestFtp()){
			if(this.isAddNewDestFtp() && !CommonalityTool.checkNull(this.getAddNewDestFtpInfoBean().getRemoteIP())){
				tb.setDestFtp(this.getAddNewDestFtpInfoBean());
			}else if(!CommonalityTool.checkNull(this.getChoiceDestFtpInfoBean().getRemoteIP())){
				tb.setDestFtp(this.getChoiceDestFtpInfoBean());
			}
		}
		//新增任务
		return ConsoleTools.addTask(tb, this.isSave());
	}
	
	
	public TaskBean getTaskBean() {
		return taskBean;
	}

	public void setTaskBean(TaskBean taskBean) {
		this.taskBean = taskBean;
	}

	public boolean isAddNewSourceFtp() {
		return addNewSourceFtp;
	}

	public void setAddNewSourceFtp(boolean addNewSourceFtp) {
		this.addNewSourceFtp = addNewSourceFtp;
	}

	public FtpInfoBean getChoiceSourceFtpInfoBean() {
		return choiceSourceFtpInfoBean;
	}

	public void setChoiceSourceFtpInfoBean(FtpInfoBean choiceSourceFtpInfoBean) {
		this.choiceSourceFtpInfoBean = choiceSourceFtpInfoBean;
	}

	public FtpInfoBean getChoiceDestFtpInfoBean() {
		return choiceDestFtpInfoBean;
	}

	public void setChoiceDestFtpInfoBean(FtpInfoBean choiceDestFtpInfoBean) {
		this.choiceDestFtpInfoBean = choiceDestFtpInfoBean;
	}

	public FtpInfoBean getAddNewSourceFtpInfoBean() {
		return addNewSourceFtpInfoBean;
	}

	public void setAddNewSourceFtpInfoBean(FtpInfoBean addNewSourceFtpInfoBean) {
		this.addNewSourceFtpInfoBean = addNewSourceFtpInfoBean;
	}

	public FtpInfoBean getAddNewDestFtpInfoBean() {
		return addNewDestFtpInfoBean;
	}

	public void setAddNewDestFtpInfoBean(FtpInfoBean addNewDestFtpInfoBean) {
		this.addNewDestFtpInfoBean = addNewDestFtpInfoBean;
	}

	public boolean isAddDestFtp() {
		return isAddDestFtp;
	}

	public void setAddDestFtp(boolean isAddDestFtp) {
		this.isAddDestFtp = isAddDestFtp;
	}

	public boolean isAddNewDestFtp() {
		return addNewDestFtp;
	}

	public void setAddNewDestFtp(boolean addNewDestFtp) {
		this.addNewDestFtp = addNewDestFtp;
	}

	public boolean isAddSourceFtp() {
		return isAddSourceFtp;
	}

	public void setAddSourceFtp(boolean isAddSourceFtp) {
		this.isAddSourceFtp = isAddSourceFtp;
	}

	public boolean isSave() {
		return isSave;
	}

	public void setSave(boolean isSave) {
		this.isSave = isSave;
	}
}
