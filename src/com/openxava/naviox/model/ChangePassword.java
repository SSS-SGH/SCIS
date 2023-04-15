package com.openxava.naviox.model;

import javax.persistence.*;
import org.openxava.annotations.*;

/**
 * To generate the view to change the password of the current user.
 * 
 * @author Javier Paniza
 */

public class ChangePassword {
	
	@Column(length=41) @DisplaySize(30) 
	@Stereotype("PASSWORD")		
	private String currentPassword;

	@Column(length=41) @DisplaySize(30) 
	@Stereotype("PASSWORD")			
	private String newPassword;
	
	@Column(length=41) @DisplaySize(30) 
	@Stereotype("PASSWORD")			
	private String repeatNewPassword;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getRepeatNewPassword() {
		return repeatNewPassword;
	}

	public void setRepeatNewPassword(String repeatNewPassword) {
		this.repeatNewPassword = repeatNewPassword;
	}

}
