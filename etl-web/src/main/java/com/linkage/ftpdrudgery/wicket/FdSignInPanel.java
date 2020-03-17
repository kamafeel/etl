package com.linkage.ftpdrudgery.wicket;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authentication.panel.SignInPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

import com.ai.vgop.db.VGOPDBOperation;
import com.linkage.ftpdrudgery.bean.GlobalBean;
import com.linkage.intf.tools.RandomUtils;
import com.linkage.intf.tools.StringUtils;

public class FdSignInPanel extends Panel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3828291837657124215L;

	/** True if the panel should display a remember-me checkbox */
	private boolean includeRememberMe = true;

	private TextField<String> password;

	/** True if the user should be remembered via form persistence (cookies) */
	private boolean rememberMe = true;

	/** Field for user name. */
	private TextField<String> username;
	
	/**
	 * Sign in form.
	 */
	public final class SignInForm extends Form<Void>
	{
		private static final long serialVersionUID = 1L;

		/** El-cheapo model for form. */
		private final ValueMap properties = new ValueMap();

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            id of the form component
		 */
		public SignInForm(final String id)
		{
			super(id);

			// Attach textfield components that edit properties map
			// in lieu of a formal beans model
			add(username = new TextField<String>("username", new PropertyModel<String>(properties,
				"username")));
			add(password = new TextField<String>("password", new PropertyModel<String>(properties,
				"password")));
			
			add(new Button("getRpassword"){
				private static final long serialVersionUID = 4096467138168678342L;
				
				@Override
				public void onSubmit(){
					if(!StringUtils.isEmpty(getUsername()) && GlobalBean.getInstance().getUserDB().containsKey(getUsername())){
						String rStr = RandomUtils.randomNumeric(5);
						GlobalBean.getInstance().setUserDB(getUsername(), rStr);
						InetAddress addr;
						boolean isSend = false;
						int count = 0;
						while(count < 3 && !isSend){
				        	count++;
							try {
								addr = InetAddress.getLocalHost();
								String ip = addr.getHostAddress();
								String address = addr.getHostName();
								Map<String,String> keysMap = new HashMap<String,String>();
								keysMap.put(VGOPDBOperation.SMS_PHONE_NO, GlobalBean.getInstance().getUserPH().get(getUsername()));
								keysMap.put(VGOPDBOperation.SMS_CONTENT, "FD_WEB帐户[" + getUsername() + "]密码:\r\n" + rStr + "\r\n信息来源:\r\nIP:" + ip + "\r\nName:" + address);
								VGOPDBOperation.getInstance().sendSms(keysMap);
								isSend = true;
								info("[" + getUsername() + "]帐号的登录密码短信发送成功,请注意查收");
							} catch (Exception e) {								
								e.printStackTrace();
							}
						}
						if(!isSend){
							error("登录密码短信发送3次均失败,FD_WEB帐户[" + getUsername() + "]密码:"+rStr);
						}						
					}else{
						error("[" + getUsername() + "]帐号不存在!");
					}					
				}
			});
			
			add(new Button("signSubMit"){
				private static final long serialVersionUID = 4096467138168678342L;
				
				@Override
				public void onSubmit(){
					if (signIn(getUsername(), getPassword()))
					{
						onSignInSucceeded();
					}
					else
					{
						onSignInFailed();
					}
				}
			});
			
			// MarkupContainer row for remember me checkbox
			final WebMarkupContainer rememberMeRow = new WebMarkupContainer("rememberMeRow");
			add(rememberMeRow);

			// Add rememberMe checkbox
			rememberMeRow.add(new CheckBox("rememberMe", new PropertyModel<Boolean>(
					FdSignInPanel.this, "rememberMe")));

			// Make form values persistent
			setPersistent(rememberMe);

			// Show remember me checkbox?
			rememberMeRow.setVisible(includeRememberMe);
		}
	}

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public FdSignInPanel(final String id)
	{
		this(id, true);
	}

	/**
	 * @param id
	 *            See Component constructor
	 * @param includeRememberMe
	 *            True if form should include a remember-me checkbox
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public FdSignInPanel(final String id, final boolean includeRememberMe)
	{
		super(id);

		this.includeRememberMe = includeRememberMe;

		// Create feedback panel and add to page
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		// Add sign-in form to page, passing feedback panel as
		// validation error handler
		add(new SignInForm("signInForm"));
	}

	/**
	 * Removes persisted form data for the signin panel (forget me)
	 */
	public final void forgetMe()
	{
		// Remove persisted user data. Search for child component
		// of type SignInForm and remove its related persistence values.
		getPage().removePersistedFormData(SignInPanel.SignInForm.class, true);
	}

	/**
	 * Convenience method to access the password.
	 * 
	 * @return The password
	 */
	public String getPassword()
	{
		return password.getDefaultModelObjectAsString();
	}

	/**
	 * Get model object of the rememberMe checkbox
	 * 
	 * @return True if user should be remembered in the future
	 */
	public boolean getRememberMe()
	{
		return rememberMe;
	}

	/**
	 * Convenience method to access the username.
	 * 
	 * @return The user name
	 */
	public String getUsername()
	{
		return username.getDefaultModelObjectAsString();
	}

	/**
	 * Convenience method set persistence for username and password.
	 * 
	 * @param enable
	 *            Whether the fields should be persistent
	 */
	public void setPersistent(final boolean enable)
	{
		username.setPersistent(enable);
	}

	/**
	 * Set model object for rememberMe checkbox
	 * 
	 * @param rememberMe
	 */
	public void setRememberMe(final boolean rememberMe)
	{
		this.rememberMe = rememberMe;
		setPersistent(rememberMe);
	}

	/**
	 * Sign in user if possible.
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if signin was successful
	 */
	public boolean signIn(String username, String password)
	{	
		if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		return AuthenticatedWebSession.get().signIn(username, password);
	}

	protected void onSignInFailed()
	{
		// Try the component based localizer first. If not found try the
		// application localizer. Else use the default
		error(getLocalizer().getString("登录失败", this, "登录失败"));
	}

	protected void onSignInSucceeded()
	{
		// If login has been called because the user was not yet
		// logged in, than continue to the original destination,
		// otherwise to the Home page
		if (!continueToOriginalDestination())
		{
			setResponsePage(getApplication().getSessionSettings().getPageFactory().newPage(
				getApplication().getHomePage(), (PageParameters)null));
		}
	}


	

}
