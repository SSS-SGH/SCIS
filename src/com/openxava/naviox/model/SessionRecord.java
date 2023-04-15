package com.openxava.naviox.model;

import java.sql.*;

import javax.persistence.*;

import org.openxava.model.*;

/**
 * 
 * @author Javier Paniza
 */
@Entity
@Table(name="OXSESSIONSRECORD")
public class SessionRecord extends Identifiable {
	
	@ManyToOne
	private User user;
	
	private Timestamp singInTime;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Timestamp getSingInTime() {
		return singInTime;
	}

	public void setSingInTime(Timestamp singInTime) {
		this.singInTime = singInTime;
	}

}
