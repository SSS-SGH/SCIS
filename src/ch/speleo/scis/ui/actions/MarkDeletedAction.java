package ch.speleo.scis.ui.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.actions.IChainAction;
import org.openxava.actions.ViewBaseAction;
import org.openxava.jpa.XPersistence;
import org.openxava.view.View;

import ch.speleo.scis.model.common.GenericIdentityWithDeleted;

/** OpenXava Action that marks as deleted an element */
public class MarkDeletedAction extends ViewBaseAction implements IChainAction {

	private static final Log logger = LogFactory.getLog(MarkDeletedAction.class);
	
	private String nextAction;
	
	public void execute() throws Exception {
		nextAction = null;
		View view = getView();
		Class<?> entityClass = view.getMetaModel().getPOJOClass();
		if (!GenericIdentityWithDeleted.class.isAssignableFrom(entityClass)) {
			nextAction = "CRUD.delete"; // standard delete
			return;
		}
		Object entityId = view.getValue("id");
		if (entityId == null) {
			addError("no_delete_not_exists");
		} else {
			GenericIdentityWithDeleted entity = 
					(GenericIdentityWithDeleted) XPersistence.getManager().find(
							entityClass, entityId);
			entity.setDeleted(true);
			XPersistence.commit();
			logger.info(new StringBuilder(entityClass.getSimpleName())
			                      .append("<").append(entityId)
			                      .append("> marked as deleted").toString());
			resetDescriptionsCache();
			addMessage("object_marked_deleted", view.getModelName());
			view.clear();
			view.setEditable(false); // user has to click [new] or existing entity
		}
	}
	
	public String getNextAction() {
		return nextAction;
	}

}
