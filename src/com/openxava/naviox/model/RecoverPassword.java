package com.openxava.naviox.model;

import javax.persistence.*;

import org.openxava.annotations.*;

/**
 * @since 5.7
 * @author Javier Paniza
 */
public class RecoverPassword { 
	
	@Column(length=60) @Stereotype("EMAIL")
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
		
}
