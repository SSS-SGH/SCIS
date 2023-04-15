package ch.speleo.scis.persistence.audit;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.meta.*;

import ch.speleo.scis.persistence.audit.ScisUserUtils.*;

public class HideUnauthorizedFields extends TabBaseAction {
	// currently not used because does not work in all cases yet

	public void execute() throws Exception {
		boolean isArchivar = ScisUserUtils.hasRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
		setHidden("entrances", !isArchivar);
		setHidden("entrancesPermitted", isArchivar);
//		boolean isHidden = !ScisUserUtils.hasRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
//		setHidden("coordEast", isHidden);
//		setHidden("coordNorth", isHidden);
//		setHidden("coordAltitude", isHidden);
	}
	
	// inspired by http://www.lostinsoftware.com/2015/08/customizing-navigation-and-security-in-openxava/
	
	private void setHidden(String fieldName, boolean isHidden) {
		if (isHidden) {
			Collection<?> props = getView().getMetaView().getViewPropertiesNames();
			getTab().getMetaTab().removeProperty(fieldName); // hide in list mode
			MetaProperty property = getView().getMetaProperty(fieldName);
			//property.setHidden(true); // hide in detail mode
			property.setReadOnly(true);
			//getView().getMetaProperty(fieldName).setHidden(true);
			//getView().setValue(fieldName, null);
			MetaProperty metaProp = getView().getMetaModel().getMetaProperty(fieldName);
		} else {
			Collection<?> props1 = getView().getMetaView().getViewPropertiesNames();
			//Collection<?> props2 = getView().getCollectionValues();
		    //getView().setHidden(fieldName, false);  // show in detail mode
		    //MetaProperty metaprop = getView().getMetaProperty(fieldName);
			MetaProperty property = getView().getMetaView().getMetaProperty(fieldName);
			property.setHidden(false); // show in list mode
			property.setReadOnly(true);
		}
	}
	
}
