package ch.speleo.scis.ui.actions;

import org.openxava.actions.*;
import org.openxava.controller.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class GoToSwissmapAction implements IForwardAction, IPropertyAction {
	
	private View view;
	 
	public String getForwardURI() {
		Number coordEast = (Number) view.getValue("coordEastLv95");
		Number coordNorth = (Number) view.getValue("coordNorthLv95");
		if (coordEast != null && coordNorth != null) {
		    return "http://map.geo.admin.ch/?crosshair=marker&Y="+coordEast+"&X="+coordNorth+"&zoom=6";
		} else {
			return null;
		}
	}
	 
	public boolean inNewWindow() {
	    return true;
	}

	public void execute() throws Exception {
	}

	public void setErrors(Messages errors) {
	}

	public Messages getErrors() {
		return null;
	}

	public void setMessages(Messages messages) {
	}

	public Messages getMessages() {
		return null;
	}

	public void setEnvironment(Environment environment) {
	}

	public void setProperty(String propertyName) {
	}

	public void setView(View view) {
		this.view = view;
	}

}
