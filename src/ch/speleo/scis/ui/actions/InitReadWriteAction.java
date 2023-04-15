package ch.speleo.scis.ui.actions;

import java.util.*;

import org.openxava.actions.*;

import com.openxava.naviox.model.Module;

import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;

public class InitReadWriteAction extends BaseAction {
	
	private static final Set<String> MODELS_READONLY = 
			Set.of("Revision", "RevisionChanges");
	private static final Set<String> FOLDERS_WRITABLE_BY_ARCHIVAR = 
			Set.of("Common", "Karst");
	
    public void execute() throws Exception {
    	String modelName = getManager().getModelName();
    	Module module = Module.findByMetaModule(getManager().getMetaModule());
    	String folderName = module.getFolder().getName();
    	/*String defaultActionName = getManager().getDefaultActionQualifiedName();
    	if (defaultActionName != null) {
    		return; // otherwise trouble OpenXava internal stuff like Return.
    	}*/
    	if (MODELS_READONLY.contains(modelName)) {
    		setControllers("ReadOnlyScis"); 
    	} else if (FOLDERS_WRITABLE_BY_ARCHIVAR.contains(folderName) ) {
    		if (ScisUserUtils.hasRoleInCurrentUser(ScisRole.SGH_ARCHIVAR, ScisRole.ADMIN)) {
    			setControllers("TypicalScis"); 
    		} else {
    			setControllers("ReadOnlyWithHistoryScis");
    		}
    	}
    }

}
