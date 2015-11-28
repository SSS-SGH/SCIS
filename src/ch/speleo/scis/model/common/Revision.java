package ch.speleo.scis.model.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.RevisionType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ch.speleo.scis.persistence.audit.EntityTrackingRevisionListenerImpl;

/**
 * A revision is a database modification for one or several objects in a single transaction. 
 * The auditing / historization is automatic thanks to Envers, that comes with Hibernate.  
 * @author florian
 */
@Entity
@Table(name = "REVISION")
@RevisionEntity(EntityTrackingRevisionListenerImpl.class)
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
    private Collection<RevisionChanges> modifiedEntities =
                                              new LinkedList<RevisionChanges>();

	/**
	 * @return Database ID.
	 */
	public int getId() {
		return id;
	}
    /**
     * @param id database ID.
     */
    public void setId(int id) {
        this.id = id;
    }
	/**
	 * @return The time of the modification.
	 */
	public Date getModificationDate() {
		return modificationDate;
	}
	/**
	 * @param modificationDate The time of the modification.
	 */
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}
	/**
	 * @return Username of the user that does this change.
	 */
	public String getUsername() { 
		return username; 
	}
	/**
	 * @param username Username of the user that does this change.
	 */
	public void setUsername(String username) { 
		this.username = username; 
	}

	public Collection<RevisionChanges> getModifiedEntities() {
		return modifiedEntities;
	}
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
