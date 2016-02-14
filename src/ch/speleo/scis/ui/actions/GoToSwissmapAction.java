package ch.speleo.scis.ui.actions;

import org.openxava.actions.IForwardAction;
import org.openxava.actions.IPropertyAction;
import org.openxava.controller.Environment;
import org.openxava.util.Messages;
import org.openxava.view.View;

import ch.speleo.scis.business.utils.Axis;
import ch.speleo.scis.business.utils.SwissCoordsUtils;

public class GoToSwissmapAction implements IForwardAction, IPropertyAction {
	
	private View view;
	 
	public String getForwardURI() {
		Integer coordEast = (Integer) view.getValue("coordEast");
		Integer coordNorth = (Integer) view.getValue("coordNorth");
		if (coordEast != null && coordNorth != null) {
			coordEast = SwissCoordsUtils.toLV03(coordEast, Axis.EAST);
			coordNorth = SwissCoordsUtils.toLV03(coordNorth, Axis.NORTH);
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
