package com.linkage.ftpdrudgery.wicket;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;

import com.linkage.ftpdrudgery.bean.FtpInfoBean;
import com.linkage.ftpdrudgery.bean.TaskBean;
import com.linkage.ftpdrudgery.console.ConsoleTools;
import com.linkage.ftpdrudgery.tools.CommonalityTool;
import com.linkage.ftpdrudgery.wicket.Validator.IpValidator;

/**
 * 任务更新
 * @author run[zhangqi@lianchuang.com]
 * 12:47:10 AM May 20, 2009
 */

@AuthorizeInstantiation("ADMIN")
public class TaskEditPage extends WebPage {
		
	private String result;
	
	private TaskBean tb;
	
	/**
	 * Form
	 * @author run[zhangqi@lianchuang.com]
	 * 2:03:55 PM May 20, 2009
	 */
	private class TaskEditForm extends Form<TaskBean>{
		
		private static final long serialVersionUID = -1809592322497672714L;

		private final List<String> transfersType = Arrays.asList(new String[] { "BINARY", "ASCII" });
		
		private final List<String> workType = Arrays.asList(new String[] { "PORT", "PASV" });
		
		private final List<Boolean> isDeleteList = Arrays.asList(new Boolean[] { true, false });
		
		private final List<Boolean> IsBackUpList = Arrays.asList(new Boolean[] { true, false });
		
		private List<Boolean> isNoOp_s = Arrays.asList(new Boolean[] { true, false });
		
		private List<Boolean> isNoOp_d = Arrays.asList(new Boolean[] { true, false });
		
		private List<Boolean> isRBD_s = Arrays.asList(new Boolean[] { true, false });
		
		private List<Boolean> isRBD_d = Arrays.asList(new Boolean[] { true, false });
				
		public TaskEditForm(String id) {
			super(id, new CompoundPropertyModel<TaskBean>(tb));
			
			/* 任务信息 */
			add(new RequiredTextField<String>("id"));
			add(new RequiredTextField<String>("taskName"));
			final TextField<String> sourceDir = new TextField<String>("sourceDir");
			add(sourceDir);
			final TextField<String> destDir = new TextField<String>("destDir");
			add(destDir);
			add(new RequiredTextField<String>("regExp"));
			add(new DropDownChoice<Boolean>("isDelete", isDeleteList));
			add(new DropDownChoice<Boolean>("isBackUp", IsBackUpList));
			add(new RequiredTextField<String>("cronTrigger"));
			add(new RequiredTextField<String>("backUpDir"));
			add(new RequiredTextField<String>("checkSleepTime"));
			add(new RequiredTextField<String>("beforeDay"));
			add(new RequiredTextField<String>("recordValidDay"));
			/* 插件 */
			final TextField<String> pluginName = new TextField<String>("pluginName");
			final TextField<String> pluginPath = new TextField<String>("pluginPath");
			final TextField<String> pluginClassPath = new TextField<String>("pluginClassPath");	
			final TextField<String> pluginId = new TextField<String>("pluginId");	
			add(pluginName);
			add(pluginPath);
			add(pluginClassPath);
			add(pluginId);
			
			/* 源FTP信息 */
			final TextField<String> sourceFtp_id = new TextField<String>("sourceFtp.id");
			final TextField<String> sourceFtp_remoteIP = new TextField<String>("sourceFtp.remoteIP");
			//IP的校验
			sourceFtp_remoteIP.add(IpValidator.getInstance());
			final TextField<String> sourceFtp_port = new TextField<String>("sourceFtp.port");
			final TextField<String> sourceFtp_userName = new TextField<String>("sourceFtp.userName");
			final TextField<String> sourceFtp_passWord = new TextField<String>("sourceFtp.passWord");
			final TextField<String> sourceFtp_timeout = new TextField<String>("sourceFtp.timeout");
			final TextField<String> sourceFtp_encoding = new TextField<String>("sourceFtp.encoding");
			final TextField<String> sourceFtp_retryCount = new TextField<String>("sourceFtp.retryCount");			
			final TextField<String> sourceFtp_retryInterval = new TextField<String>("sourceFtp.retryInterval");
			final TextField<String> sourceFtp_maxConnect = new TextField<String>("sourceFtp.maxConnect");
			add(new DropDownChoice<Boolean>("sourceFtp.isNoOp", isNoOp_s));
			add(new DropDownChoice<Boolean>("sourceFtp.isRBD", isRBD_s));
			add(sourceFtp_id);
			add(sourceFtp_remoteIP);
			add(sourceFtp_port);
			add(sourceFtp_userName);
			add(sourceFtp_passWord);
			add(sourceFtp_timeout);
			add(sourceFtp_encoding);
			add(sourceFtp_retryCount);
			add(sourceFtp_retryInterval);
			add(sourceFtp_maxConnect);
			
			/* 目标FTP信息 */
			final TextField<String> destFtp_id = new TextField<String>("destFtp.id");
			final TextField<String> destFtp_remoteIP = new TextField<String>("destFtp.remoteIP");
			//IP的校验
			destFtp_remoteIP.add(IpValidator.getInstance());
			final TextField<String> destFtp_port = new TextField<String>("destFtp.port");
			final TextField<String> destFtp_userName = new TextField<String>("destFtp.userName");
			final TextField<String> destFtp_passWord = new TextField<String>("destFtp.passWord");
			final TextField<String> destFtp_timeout = new TextField<String>("destFtp.timeout");
			final TextField<String> destFtp_encoding = new TextField<String>("destFtp.encoding");
			final TextField<String> destFtp_retryCount = new TextField<String>("destFtp.retryCount");			
			final TextField<String> destFtp_retryInterval = new TextField<String>("destFtp.retryInterval");
			final TextField<String> destFtp_maxConnect = new TextField<String>("destFtp.maxConnect");
			add(new DropDownChoice<Boolean>("destFtp.isNoOp", isNoOp_d));
			add(new DropDownChoice<Boolean>("destFtp.isRBD", isRBD_d));
			add(destFtp_id);
			add(destFtp_remoteIP);
			add(destFtp_port);
			add(destFtp_userName);
			add(destFtp_passWord);
			add(destFtp_timeout);
			add(destFtp_encoding);
			add(destFtp_retryCount);
			add(destFtp_retryInterval);
			add(destFtp_maxConnect);
			
			add(new DropDownChoice<String>("sourceFtp.transfersType", transfersType));
			add(new DropDownChoice<String>("sourceFtp.workType", workType));
			add(new DropDownChoice<String>("destFtp.transfersType", transfersType));
			add(new DropDownChoice<String>("destFtp.workType", workType));
			
			/* FROM验证 */
			add(new AbstractFormValidator() {
				private static final long serialVersionUID = 0L;
				
				public void validate(Form<?> form) {
					
					/* 来源方FTP校验 */
					if(!CommonalityTool.checkNull(sourceFtp_remoteIP.getInput())){
						if(CommonalityTool.checkNull(sourceFtp_port.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_userName.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_passWord.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_timeout.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_encoding.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_retryCount.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_retryInterval.getInput())){
							sourceFtp_id.error((IValidationError) new ValidationError()
											.addMessageKey("error.sourceFtp"));
						}
					}
					
					if(!CommonalityTool.checkNull(sourceFtp_userName.getInput())){
						if(CommonalityTool.checkNull(sourceFtp_port.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_remoteIP.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_passWord.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_timeout.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_encoding.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_retryCount.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_retryInterval.getInput())){
							sourceFtp_id.error((IValidationError) new ValidationError()
											.addMessageKey("error.sourceFtp"));
						}
					}
					
					if(!CommonalityTool.checkNull(sourceFtp_passWord.getInput())){
						if(CommonalityTool.checkNull(sourceFtp_port.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_userName.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_remoteIP.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_timeout.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_encoding.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_retryCount.getInput()) ||
								CommonalityTool.checkNull(sourceFtp_retryInterval.getInput())){
							sourceFtp_id.error((IValidationError) new ValidationError()
											.addMessageKey("error.sourceFtp"));
						}
					}
					
					/* 目的方FTP校验 */
					if(!CommonalityTool.checkNull(destFtp_remoteIP.getInput())){
						if(CommonalityTool.checkNull(destFtp_port.getInput()) ||
								CommonalityTool.checkNull(destFtp_userName.getInput()) ||
								CommonalityTool.checkNull(destFtp_passWord.getInput()) ||
								CommonalityTool.checkNull(destFtp_timeout.getInput()) ||
								CommonalityTool.checkNull(destFtp_encoding.getInput()) ||
								CommonalityTool.checkNull(destFtp_retryCount.getInput()) ||
								CommonalityTool.checkNull(destFtp_retryInterval.getInput())){
							sourceFtp_id.error((IValidationError) new ValidationError()
											.addMessageKey("error.destFtp"));
						}
					}
					
					if(!CommonalityTool.checkNull(destFtp_userName.getInput())){
						if(CommonalityTool.checkNull(destFtp_port.getInput()) ||
								CommonalityTool.checkNull(destFtp_remoteIP.getInput()) ||
								CommonalityTool.checkNull(destFtp_passWord.getInput()) ||
								CommonalityTool.checkNull(destFtp_timeout.getInput()) ||
								CommonalityTool.checkNull(destFtp_encoding.getInput()) ||
								CommonalityTool.checkNull(destFtp_retryCount.getInput()) ||
								CommonalityTool.checkNull(destFtp_retryInterval.getInput())){
							sourceFtp_id.error((IValidationError) new ValidationError()
											.addMessageKey("error.destFtp"));
						}
					}
					
					if(!CommonalityTool.checkNull(destFtp_passWord.getInput())){
						if(CommonalityTool.checkNull(destFtp_port.getInput()) ||
								CommonalityTool.checkNull(destFtp_userName.getInput()) ||
								CommonalityTool.checkNull(destFtp_remoteIP.getInput()) ||
								CommonalityTool.checkNull(destFtp_timeout.getInput()) ||
								CommonalityTool.checkNull(destFtp_encoding.getInput()) ||
								CommonalityTool.checkNull(destFtp_retryCount.getInput()) ||
								CommonalityTool.checkNull(destFtp_retryInterval.getInput())){
							sourceFtp_id.error((IValidationError) new ValidationError()
											.addMessageKey("error.destFtp"));
						}
					}
					
					/* 插件验证 */					
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
					
					
					if(!CommonalityTool.checkNull(pluginName.getInput())){
						if(CommonalityTool.checkNull(pluginPath.getInput()) ||
								CommonalityTool.checkNull(pluginClassPath.getInput())){
							pluginName.error((IValidationError)new ValidationError().addMessageKey("error.noSetPlugin"));
						}
					}
					
					if(!CommonalityTool.checkNull(pluginPath.getInput())){
						if(CommonalityTool.checkNull(pluginName.getInput()) ||
								CommonalityTool.checkNull(pluginClassPath.getInput())){
							pluginName.error((IValidationError)new ValidationError().addMessageKey("error.noSetPlugin"));
						}
					}
					
					if(!CommonalityTool.checkNull(pluginClassPath.getInput())){
						if(CommonalityTool.checkNull(pluginPath.getInput()) ||
								CommonalityTool.checkNull(pluginName.getInput())){
							pluginName.error((IValidationError)new ValidationError().addMessageKey("error.noSetPlugin"));
						}
					}
									
				}

				public FormComponent<?>[] getDependentFormComponents() {
					// TODO Auto-generated method stub
					return null;
				}

			});
			
			
			add(new Button("updateButton"){
				private static final long serialVersionUID = 4554235560153551181L;
				
				@Override
				public void onSubmit(){
					//不需要改变源FTP和目的FTP
					if(CommonalityTool.checkNull(sourceFtp_remoteIP.getInput()) ||
							CommonalityTool.checkNull(sourceFtp_userName.getInput()) ||
							CommonalityTool.checkNull(sourceFtp_passWord.getInput())){
						tb.setSourceFtp(null);
					}else{
						tb.getSourceFtp().setRemoteIP(sourceFtp_remoteIP.getInput());
						tb.getSourceFtp().setUserName(sourceFtp_userName.getInput());
						tb.getSourceFtp().setPassWord(sourceFtp_passWord.getInput());
					}
					if(CommonalityTool.checkNull(destFtp_remoteIP.getInput()) ||
							CommonalityTool.checkNull(destFtp_userName.getInput()) ||
							CommonalityTool.checkNull(destFtp_passWord.getInput())){
						tb.setDestFtp(null);
					}else{
						tb.getDestFtp().setRemoteIP(destFtp_remoteIP.getInput());
						tb.getDestFtp().setUserName(destFtp_userName.getInput());
						tb.getDestFtp().setPassWord(destFtp_passWord.getInput());
					}
					result = ConsoleTools.updateTask(tb, tb.isSave()).getReturnInfo();
					TaskEditPage tep = new TaskEditPage(tb);
					tep.setResult(result);
					setResponsePage(tep);
				}
				
			});

			add(new Button("returnButton"){
				
				private static final long serialVersionUID = 4207717569035694136L;

				@Override
				public void onSubmit(){
					//重新得到一个TaskEditPage实例
					setResponsePage(JobListPage.class);
				}
			}.setDefaultFormProcessing(false));
			
			add(new CheckBox("isSave"));
		}		
	}
	
	/**
	 * 构造方法
	 * @param tb
	 */
	public TaskEditPage(TaskBean tb){
		super();
		
		/* 初始化源或者目的FTP为空的属性,方便填写 */
		FtpInfoBean sfi = new FtpInfoBean();
		sfi.setPort(21);
		sfi.setTransfersType("BINARY");
		sfi.setWorkType("PORT");
		sfi.setTimeout(12000);
		sfi.setEncoding("GBK");
		sfi.setRetryCount(3);
		sfi.setRetryInterval(20000);
		sfi.setNoOp(false);
		sfi.setRBD(false);
		sfi.setMaxConnect(0);
		
		//反射机制,需要对象唯一性
		FtpInfoBean dfi = new FtpInfoBean();
		dfi.setPort(21);
		dfi.setTransfersType("BINARY");
		dfi.setWorkType("PORT");
		dfi.setTimeout(12000);
		dfi.setEncoding("GBK");
		dfi.setRetryCount(3);
		dfi.setRetryInterval(20000);
		dfi.setNoOp(false);
		dfi.setRBD(false);
		dfi.setMaxConnect(0);
		
		//判断需要填充的对象
		if(tb.getSourceFtp() == null){
			tb.setSourceFtp(sfi);
		}
		if(tb.getDestFtp() == null){
			tb.setDestFtp(dfi);
		}
		/* 保存对象到Form中 */
		this.setTb(tb);
		//更新结果
		final Label orderResult = new Label("orderResult", new PropertyModel<Object>(this, "result"));
		add(orderResult);
		add(new TaskEditForm("form"));
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

	public TaskBean getTb() {
		return tb;
	}

	public void setTb(TaskBean tb) {
		this.tb = tb;
	}
	
}
