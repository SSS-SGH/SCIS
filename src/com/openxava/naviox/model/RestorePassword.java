package com.openxava.naviox.model;

import javax.persistence.*;
import org.openxava.annotations.*;

/**
 * 
 * @author Javier Paniza
 */

public class RestorePassword {
	
	@Column(length=41) @DisplaySize(30) 
	@Stereotype("PASSWORD")			
	private String newPassword;
	
	@Column(length=41) @DisplaySize(30) 
	@Stereotype("PASSWORD")			
	private String repeatNewPassword;

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
