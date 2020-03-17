package com.linkage.ftpdrudgery.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;

import com.linkage.ftpdrudgery.bean.GlobalBean;


/**
 * 全局Session控制
 * @author Run
 *
 */

public class WicketWebSession extends AuthenticatedWebSession {
	
	private static final long serialVersionUID = 1L;	
	private String userName;	
	
	public WicketWebSession(Request request) {
		super(request);
	}

	@Override
	public boolean authenticate(String username, String password) {
		if(GlobalBean.getInstance().getUserDB().containsKey(username) && GlobalBean.getInstance().getUserDB().get(username).equalsIgnoreCase(password)){
			this.setUserName(username);
			return true;
		}
		return false;
	}

	@Override
	public Roles getRoles() {
		if (isSignedIn())
		{
			// If the user is signed in, they have these roles
			return new Roles(GlobalBean.getInstance().getUserRoles().get(this.getUserName()));
		}
		return null;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}
}
