package ch.speleo.scis.model.common;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;

import ch.speleo.scis.persistence.utils.SimpleQueries;

/**
 * Class representing a generic technical Identity for the database together with a "deleted" flag.
 * 
 * @author florian
 */
@MappedSuperclass
@Audited
public abstract class GenericIdentityWithRevision 
extends GenericIdentity {

	private static final long serialVersionUID = 85073371290935232L;

	public final static String AUDIT_VIEW_NAME = "Audit";
	
    /**
     * Revision information from the audit, filled if loaded with the audit.
     * @see #loadAuditedValues(Class)
     */
    @Transient @OneToOne 
    @ReferenceView(value = "Short")
    @ReadOnly
    private RevisionInfo revision;

    public RevisionInfo getRevision() {
		return revision;
	}

    public void setRevision(RevisionInfo revision) {
		this.revision = revision;
	}

    protected <T extends GenericIdentityWithRevision> List<T> loadAuditedValues() {
    	Class<? extends GenericIdentityWithRevision> entityClass = this.getClass();
    	List<Object[]> auditedValues = SimpleQueries.getAuditedInfosOfEntity(entityClass, getId());
    	List<T> result = new ArrayList<T>(auditedValues.size());
    	for (Object[] auditElement: auditedValues) {
    		T element = (T) auditElement[0];
    		Revision revision = (Revision) auditElement[1];
    		element.revision = new RevisionInfo(revision);
    		result.add(element);
    	}
    	return result;
    }

}
