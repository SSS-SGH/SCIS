package ch.speleo.scis.model.common;

import javax.persistence.*;

import org.hibernate.envers.*;
import org.openxava.annotations.*;
import org.openxava.calculators.*;

import lombok.*;

/**
 * A generic technical Identity for the database together with a "deleted" flag.
 * 
 * @author florian
 */
@MappedSuperclass
@Audited
@Getter @Setter
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

    @Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", deleted=");
		builder.append(deleted);
	}
	
}
