package ch.speleo.scis.model.common;

import java.util.*;

import javax.persistence.*;

import org.hibernate.envers.*;
import org.openxava.annotations.*;

import ch.speleo.scis.persistence.utils.*;
import lombok.*;

/**
 * A generic technical Identity for the database together with revision information (alias audit or log).
 * 
 * @author florian
 */
@MappedSuperclass
@Audited
@Getter @Setter
public abstract class GenericIdentityWithRevision 
extends GenericIdentity {

	private static final long serialVersionUID = 85073371290935232L;

	public final static String AUDIT_VIEW_NAME = "Audit";
	
    /**
     * Revision information from the audit, filled if loaded with the audit.
     * @see #loadAuditedValues(Class)
     */
    @Transient @OneToOne 
    @ReadOnly
    private RevisionInfo revision;

    @SuppressWarnings("unchecked")
	protected <T extends GenericIdentityWithRevision> List<T> loadAuditedValues() {
    	Class<? extends GenericIdentityWithRevision> entityClass = this.getClass();
    	List<Object[]> auditedValues = SimpleQueries.getAuditedInfosOfEntity(entityClass, getId());
    	List<T> result = new ArrayList<T>(auditedValues.size());
    	for (Object[] auditElement: auditedValues) {
    		T element = (T) auditElement[0];
    		Revision revision = (Revision) auditElement[1];
    		element.setRevision(new RevisionInfo(revision));
    		result.add(element);
    	}
    	return result;
    }

}
