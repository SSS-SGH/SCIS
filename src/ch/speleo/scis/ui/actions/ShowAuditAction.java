package ch.speleo.scis.ui.actions;

import java.util.*;

import org.apache.commons.logging.*;
import org.openxava.actions.*;
import org.openxava.util.*;

import ch.speleo.scis.model.common.*;

public class ShowAuditAction extends ViewBaseAction {

	private static final Log logger = LogFactory.getLog(ShowAuditAction.class);
	
	private static String DEFAULT_VIEW_NAME = "";


    public void execute() throws Exception {
    	
        String newViewName = GenericIdentityWithRevision.AUDIT_VIEW_NAME.equals(getView().getViewName()) 
        		? DEFAULT_VIEW_NAME : GenericIdentityWithRevision.AUDIT_VIEW_NAME;
		String modelName = getView().getModelName();
        Map<?, ?> key = getView().getKeyValues();
        
        try {
            getView().getMetaModel().getMetaView(newViewName);
        } catch (ElementNotFoundException e) {
        	logger.info("No "+newViewName+" for "+modelName+", "+ShowAuditAction.class.getSimpleName()+" aborted");
        	addInfo("no_audit_for_model", modelName);
        	return;
        }

		try {
			showNewView();  
            getView().setModelName(modelName);                 
            getView().setViewName(newViewName); 
            getView().setValues(key);                             
            getView().findObject();                                
            getView().setKeyEditable(false);
            setControllers("Return");                            
        } catch (Exception ex) {
        	logger.error("Error when showing "+newViewName+" for "+modelName, ex);
            addError("system_error");
        }
    }
 
}