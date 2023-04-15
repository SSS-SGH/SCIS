package ch.speleo.scis.model.common;

import java.io.*;

import javax.persistence.*;

import org.hibernate.envers.*;
import org.openxava.annotations.*;

import lombok.*;

/**
 * A generic technical Identity for the database.
 * 
 * @author florian
 */
@MappedSuperclass
@Audited
@Getter @Setter
public abstract class GenericIdentity implements Serializable {

	private static final long serialVersionUID = 8404333762940955332L;
    
    /**
     * Database ID.
     */
    @Id
    @GeneratedValue
    @Column(name = "ID", insertable = false, updatable = false)
    @Hidden
    private Long id;
        
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
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericIdentity other = (GenericIdentity) obj;
		return (other.id != null) && id.equals(other.id);
	}
    
}
