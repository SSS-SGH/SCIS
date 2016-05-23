package ch.speleo.scis.model.common;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.calculators.FalseCalculator;

/**
 * Class representing a generic technical Identity for the database together with a "deleted" flag.
 * 
 * @author florian
 */
@MappedSuperclass
@Audited
public abstract class GenericIdentityWithDeleted 
extends GenericIdentityWithRevision {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 8504733762990955232L;

    /**
     * Flag indicating if the karst object is deleted or not.
     */
    @Column(name = "DELETED", nullable = false)
    @DefaultValueCalculator(FalseCalculator.class)
    private Boolean deleted = Boolean.FALSE;

    /**
     * @return if the entity has been marked as deleted.
     */
    public boolean getDeleted() {
        return deleted;
    }
    /**
     * @param deleted if the entity has been marked as deleted.
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", deleted=");
		builder.append(deleted);
	}
	
}
