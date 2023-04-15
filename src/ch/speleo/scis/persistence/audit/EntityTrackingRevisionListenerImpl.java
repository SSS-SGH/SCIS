package ch.speleo.scis.persistence.audit;

import java.io.*;

import org.apache.commons.logging.*;
import org.hibernate.envers.*;

import ch.speleo.scis.model.common.*;

public class EntityTrackingRevisionListenerImpl 
implements EntityTrackingRevisionListener {
	
	private static final Log loger = LogFactory.getLog(EntityTrackingRevisionListenerImpl.class);
	
    public void newRevision(Object revisionEntity) {
    	
    	Revision revision = (Revision) revisionEntity;
		String username = ScisUserUtils.getCurrentUserName();
		if (username != null) {
			revision.setUsername(username);
		} else {
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