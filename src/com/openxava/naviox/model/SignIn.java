package com.openxava.naviox.model;

import javax.persistence.*;
import org.openxava.annotations.*;

/**
 * 
 * @author Javier Paniza 
 */

@View(name="Unlock", members="password")
public class SignIn {
	
	
	@ManyToOne 
	@DescriptionsList(condition="${active} = true") 
	@LabelFormat(LabelFormatType.SMALL)
	@NoModify @NoCreate
	@Required 
	private Organization organization;
	
	@LabelFormat(LabelFormatType.SMALL)
	@Column(length=50)
	private String organizationName;  
	
	@Column(length=60) 
	@LabelFormat(LabelFormatType.SMALL)
	@Required 
	private String user; 

	@Column(length=30) @Stereotype("PASSWORD")
	@LabelFormat(LabelFormatType.SMALL)
	@Required 
	private String password;
	

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}
	
	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

}
