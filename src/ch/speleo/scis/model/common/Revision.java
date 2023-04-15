package ch.speleo.scis.model.common;

import java.io.*;
import java.util.*;

import javax.persistence.*;

import org.hibernate.envers.*;
import org.openxava.annotations.*;

import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;
import lombok.*;

/**
 * A revision is a database modification for one or several objects in a single transaction. 
 * The auditing / historization is automatic thanks to Envers, that comes with Hibernate.  
 * @author florian
 */
@Entity
@Table(name = "REVISION")
@RevisionEntity(EntityTrackingRevisionListenerImpl.class)
@Views({
		@View(members = "modificationDate, username; modifiedEntities"),
		@View(name = "Short", members = "modificationDate, username")
})
@Getter @Setter
public class Revision {

    /**
     * Database ID.
     */
    @Id
    @GeneratedValue
    @Column(name = "REV_ID", insertable = false, updatable = false)
    @RevisionNumber
    private int id;

    /**
     * The time of the modification.
     */
    @Column(name = "MODIF_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Stereotype("DATETIME")
    @RevisionTimestamp
    private Date modificationDate;

    /**
     * Username of the user that does this change.
     */
    @Column(name = "USERNAME", nullable = true, length=30)
    private String username;

    /**
     * The entities modified for this revision. 
     */
    @OneToMany(mappedBy="revision", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    @ListProperties("action, entityNameTranslated, businessId")
    @ReadOnly
    @Setter(AccessLevel.PRIVATE)
    private Collection<RevisionChanges> modifiedEntities =
                                              new LinkedList<RevisionChanges>();

	/**
	 * Add a modified entity for this revision. 
	 * @param entityName
	 * @param entityClassName
	 * @param entityId
	 * @param action action type (add, modify or delete)
	 */
    public void addModifiedEntity(String entityName, String entityClassName, Serializable entityId, RevisionType action) {
		RevisionChanges revEntity = new RevisionChanges();
		revEntity.setRevision(this);
		revEntity.setEntityName(entityName);
		revEntity.setEntityClassName(entityClassName);
		revEntity.setEntityId(entityId);
		revEntity.setAction(action);
		modifiedEntities.add(revEntity);
    }
    
	@PrePersist @PreUpdate
    public void handlePermissionsOnWrite() {
        ScisUserUtils.checkRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
    }
    
	@PreDelete
    public void handlePermissionsOnDelete() {
		throw new NoPermissionException(Revision.class.getSimpleName() + " shall never be deleted");
    }
    
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(Revision.class.getSimpleName());
		builder.append(" [id=");
		builder.append(id);
		builder.append(", modificationDate=");
		builder.append(modificationDate);
		builder.append(", username=");
		builder.append(username);
		builder.append(", modifiedEntities.size=");
		builder.append(modifiedEntities.size());
		builder.append("]");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		if (id == 0)
			return super.hashCode();
		else
			return id;
	}
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (id == 0)
			return super.equals(obj);
		if (getClass() != obj.getClass())
			return false;
		Revision other = (Revision) obj;
		return id == other.id;
	}
    
}
