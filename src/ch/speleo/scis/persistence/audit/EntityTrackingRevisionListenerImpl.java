package ch.speleo.scis.persistence.audit;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.openxava.util.UserInfo;
import org.openxava.util.Users;

import ch.speleo.scis.model.common.Revision;

public class EntityTrackingRevisionListenerImpl 
implements EntityTrackingRevisionListener {
	
	private static final Log loger = LogFactory.getLog(EntityTrackingRevisionListenerImpl.class);
	
    public void newRevision(Object revisionEntity) {
    	
    	Revision revision = (Revision) revisionEntity;
		UserInfo userinfo = Users.getCurrentUserInfo();
		String username = StringUtils.isNotBlank(userinfo.getNickName()) ? userinfo.getNickName() : userinfo.getId();
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