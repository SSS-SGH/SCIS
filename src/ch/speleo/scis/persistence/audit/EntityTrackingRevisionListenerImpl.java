package ch.speleo.scis.persistence.audit;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.openxava.util.Users;

import ch.speleo.scis.model.common.Revision;

public class EntityTrackingRevisionListenerImpl 
implements EntityTrackingRevisionListener {
	
    public void newRevision(Object revisionEntity) {
    	
    	Revision revision = (Revision) revisionEntity;
		String username = Users.getCurrent();
		if (username != null) {
			revision.setUsername(username);
		} else {
			Log loger = LogFactory.getLog(EntityTrackingRevisionListenerImpl.class);
			loger.warn("cannot set a user id as worker of revision because nobody is logged in");
		}
    }

    @SuppressWarnings("rawtypes")
    public void entityChanged(Class entityClass, String entityName,
                              Serializable entityId, RevisionType revisionType,
                              Object revisionEntity) {
    	((Revision) revisionEntity).addModifiedEntity(
        		entityName, 
        		entityClass.getCanonicalName(), 
        		entityId, 
        		revisionType);
    }

}