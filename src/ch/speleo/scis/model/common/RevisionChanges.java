package ch.speleo.scis.model.common;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.envers.RevisionType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A revision change is a database modification of one object. 
 * One or several revision changes are grouped in a transaction, making a {@link Revision}.
 * The auditing / historization is automatic thanks to Envers, that comes with Hibernate.  
 * @author florian
 */
@Entity
@Table(name = "REVISION_CHANGE")
public class RevisionChanges implements Serializable {
	
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
     * @return database ID.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id database ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

	/**
	 * @return the revision
	 */
	public Revision getRevision() {
		return revision;
	}
	/**
	 * @param revision the revision
	 */
	public void setRevision(Revision revision) {
		this.revision = revision;
	}

	/**
	 * @return the name of the entity (database table)
	 */
	public String getEntityName() {
		return entityName;
	}
	/**
	 * @param entityName the name of the entity (database table)
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * @return the classname of the entity
	 */
	public String getEntityClassName() {
		return entityClassName;
	}
	/**
	 * @param entityClassName the classname of the entity
	 */
	public void setEntityClassName(String entityClassName) {
		this.entityClassName = entityClassName;
	}
	
	/**
	 * @return the id of the changed entity
	 */
	public Long getEntityId() {
		return entityId;
	}
	/**
	 * @param entityId the id of the changed entity
	 */
	public void setEntityId(Serializable entityId) {
		if (entityId instanceof Long) {
			this.entityId = (Long) entityId;
		} else if (entityId instanceof Integer) {
			this.entityId = Long.valueOf((Integer) entityId);
		} else {
			Log logger = LogFactory.getLog(RevisionChanges.class);
			logger.warn("entity id '"+entityId+"' couldn't be recognize as a number (Long or Integer)");
		}
	}

	/**
	 * @return the action type (add, modify or delete)
	 */
	public RevisionType getAction() {
		return action;
	}
	/**
	 * @param action action type (add, modify or delete)
	 */
	public void setAction(RevisionType action) {
		this.action = action;
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
			return super.equals(obj);
		if (getClass() != obj.getClass())
			return false;
		RevisionChanges other = (RevisionChanges) obj;
		return id.equals(other.id);
	}
    
}