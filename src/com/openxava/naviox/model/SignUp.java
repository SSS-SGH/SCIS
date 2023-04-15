package com.openxava.naviox.model;

import javax.persistence.*;

import org.openxava.annotations.*;

/**
 * 
 * @author Javier Paniza
 */
public class SignUp {
	 
	@Column(length=60) 
	@DisplaySize(30) 
	@Required
	private String userName;
	
	@Column(length=30) @Required
	@Stereotype("PASSWORD")	
	private String password;
	
	@Column(length=30) @Required
	@Stereotype("PASSWORD")	
	private String repeatPassword;
	
	private boolean privacyPolicy; 

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}

	public boolean isPrivacyPolicy() {
		return privacyPolicy;
	}

	public void setPrivacyPolicy(boolean privacyPolicy) {
		this.privacyPolicy = privacyPolicy;
	}

	
}
