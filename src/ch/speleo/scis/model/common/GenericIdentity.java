package ch.speleo.scis.model.common;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;
import org.openxava.annotations.Hidden;

/**
 * Class representing a generic technical Identity for the database.
 * 
 * @author florian
 */
@MappedSuperclass
@Audited
public abstract class GenericIdentity implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 8404333762940955332L;
    
    /**
     * Database ID.
     */
    @Id
    @GeneratedValue
    @Column(name = "ID", insertable = false, updatable = false)
    @Hidden
    private Long id;
        
    /**
     * @return Database ID.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id Database ID.
     */
    public void setId(Long id) {
        this.id = id;
    }
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append(" [");
		writeFields(builder);
		builder.append("]");
		return builder.toString();
	}
    /**
     * Write the content of the fields in the given builder
     * @param builder the StringBuilder where to write
     */
	protected void writeFields(StringBuilder builder) {
		builder.append("id=");
		builder.append(id);
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
		GenericIdentity other = (GenericIdentity) obj;
		return id.equals(other.id);
	}
    
}
