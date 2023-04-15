package ch.speleo.scis.model.common;

import java.io.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.logging.*;
import org.hibernate.envers.*;
import org.openxava.annotations.*;
import org.openxava.util.*;

import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;
import ch.speleo.scis.persistence.utils.*;
import lombok.*;

/**
 * A revision change is a database modification of one object. 
 * One or several revision changes are grouped in a transaction, making a {@link Revision}.
 * The auditing / historization is automatic thanks to Envers, that comes with Hibernate.  
 * @author florian
 */
@Entity
@Table(name = "REVISION_CHANGE")
@Tab(properties = "revision.modificationDate, revision.username, action, entityNameTranslated, businessId", 
		defaultOrder="${revision.modificationDate} desc")
@View(members = "revision; entityNameTranslated, businessId; changesOfEntity")
@Getter @Setter
public class RevisionChanges implements Serializable {
	
	private static final Log loger = LogFactory.getLog(RevisionChanges.class);
	
    /**
     * Serial version UID.
	 */
	private static final long serialVersionUID = -1130923507850036872L;
	
	/**
     * Database ID.
     */
	@Id
	@GeneratedValue
    @Column(name = "ID", insertable = false, updatable = false)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "REVISION_ID", nullable = false)
    @ReadOnly
	private Revision revision;
	
    @Column(name = "ENTITY_NAME", nullable = false)
	private String entityName;

    @Column(name = "ENTITY_CLASS_NAME", nullable = false)
	private String entityClassName;

    @Column(name = "ENTITY_ID", nullable = true)
	private Long entityId;

	/**
	 * action type (add, modify or delete)
	 */
    @Column(name = "ACTION", nullable = false)
	private RevisionType action;
	
	/**
	 * @return The translation of the object's type, if it exists, otherwise of the class name
	 */
	public String getEntityNameTranslated() {
		String classSimpleName = entityClassName.substring(entityClassName.lastIndexOf("."));
		return Labels.get(classSimpleName);
	}
	
	/**
	 * @param entityId the id of the changed entity
	 */
	public void setEntityId(Serializable entityId) {
		if (entityId instanceof Long) {
			this.entityId = (Long) entityId;
		} else if (entityId instanceof Number) {
			this.entityId = ((Number) entityId).longValue();
		} else {
			Log logger = LogFactory.getLog(RevisionChanges.class);
			logger.warn("entity id '"+entityId+"' couldn't be recognize as a number (Long or Integer)");
		}
	}
	
	public String getBusinessId() {
		try {
			Class<?> entityClass = Class.forName(entityClassName);
			if (Identifiable.class.isAssignableFrom(entityClass) && GenericIdentity.class.isAssignableFrom(entityClass)) {
				Object object = SimpleQueries.getByUniqueField(entityClass, "id", entityId);
				Identifiable identifiable = (Identifiable) object;
				return identifiable.getBusinessId();
			}
		} catch (ClassNotFoundException e) {
			loger.warn(String.format("Cannot get businessId on %s with id %s cause %s", entityClassName, entityId, e.toString()));
		} catch (NoResultException e) {
			loger.warn(String.format("Cannot get businessId on %s with id %s cause %s", entityClassName, entityId, e.toString()));
		}
		return null;
	}

    @ListProperties("revision.modificationDate, revision.username, action")
    @ReadOnly
	public Collection<RevisionChanges> getChangesOfEntity() {
		return getChangesOfEntity(entityClassName, entityId);
	}
	private static Collection<RevisionChanges> getChangesOfEntity(String entityClassName, Object entityId) {
    	List<RevisionChanges> relatedChanges = SimpleQueries.getMultipleResults(
				" while searching related Changes ", 
				RevisionChanges.class, 
				"entityClassName=?1 and entityId=?2", 
				entityClassName, entityId);
		Collections.sort(relatedChanges, new ModifDateComparator());
		return relatedChanges;
	}
	public static Collection<RevisionChanges> getByEntity(Class<?> entityClass, Object entityId) {
		return getChangesOfEntity(entityClass.getCanonicalName(), entityId);
	}
	
	@PrePersist @PreUpdate
    public void handlePermissionsOnWrite() {
        ScisUserUtils.checkRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
    }
    
	@PreDelete
    public void handlePermissionsOnDelete() {
		throw new NoPermissionException(RevisionChanges.class.getSimpleName() + " shall never be deleted");
    }
    
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(RevisionChanges.class.getSimpleName());
		builder.append(" [id=");
		builder.append(id);
		builder.append(", revision.id=");
		builder.append(revision.getId());
		builder.append(", entityName=");
		builder.append(entityName);
		builder.append(", entityClassName=");
		builder.append(entityClassName);
		builder.append(", entityId=");
		builder.append(entityId);
		builder.append(", action=");
		builder.append(action);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		if (id == null)
			return super.hashCode();
		else
			return id.hashCode();
	}
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (id == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RevisionChanges other = (RevisionChanges) obj;
		return (other.id != null) && id.equals(other.id);
	}
    
    static class ModifDateComparator implements Comparator<RevisionChanges> {

		public int compare(RevisionChanges revChanges1, RevisionChanges revChanges2) {
			return revChanges1.getRevision().getModificationDate().compareTo(revChanges2.getRevision().getModificationDate());
		}
    	
    }
}