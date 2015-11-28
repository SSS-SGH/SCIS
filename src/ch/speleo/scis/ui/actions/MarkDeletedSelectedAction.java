package ch.speleo.scis.ui.actions;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.actions.IModelAction;
import org.openxava.actions.TabBaseAction;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.validators.ValidationException;
import org.openxava.view.View;

import ch.speleo.scis.model.common.GenericIdentityWithDeleted;

/** OpenXava Action that marks as deleted the selected elements */
public class MarkDeletedSelectedAction extends TabBaseAction implements IModelAction {

	private static final Log logger = LogFactory.getLog(MarkDeletedSelectedAction.class);
	
	private String model;
	
	@SuppressWarnings("rawtypes")
	public void execute() throws Exception {
		View view = getView();
		Class<?> entityClass = view.getMetaModel().getPOJOClass();
		Map [] selectedOnes = getSelectedKeys(); 
		if (selectedOnes == null) {
			return;
		} else if (GenericIdentityWithDeleted.class.isAssignableFrom(entityClass)) {
			for (int i = 0; i < selectedOnes.length; i++) {				
				Map key = selectedOnes[i];
				try {									
					Object entityId = key.get("id");
					GenericIdentityWithDeleted entity = 
							(GenericIdentityWithDeleted) XPersistence.getManager().find(
									entityClass, entityId);
					entity.setDeleted(true);
					XPersistence.commit();
					logger.info(new StringBuilder(entityClass.getSimpleName())
					                      .append("<").append(entityId)
					                      .append("> marked as deleted").toString());
				}
				catch (ValidationException ex) {
					addError("no_delete_row", new Integer(i), key);
					addErrors(ex.getErrors());
				}								
				catch (Exception ex) { 
					addError("no_delete_row", new Integer(i), key);
				}						
			}
		} else {						
			for (int i = 0; i < selectedOnes.length; i++) {				
				Map key = selectedOnes[i];
				try {									
					MapFacade.remove(model, key);				
				}
				catch (ValidationException ex) {
					addError("no_delete_row", new Integer(i), key);
					addErrors(ex.getErrors());
				}								
				catch (Exception ex) { 
					addError("no_delete_row", new Integer(i), key);
				}						
			}
		}
		getTab().deselectAll();
		resetDescriptionsCache();
	}
	
	public void setModel(String modelName) {
		this.model = modelName;		
	}
	
}
