package ch.speleo.scis.model.common;

import java.io.*;

import javax.persistence.*;

import org.hibernate.envers.*;
import org.openxava.annotations.*;

import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;
import ch.speleo.scis.persistence.utils.*;
import lombok.*;

/**
 * Commune as an administrative district.
 */
@Entity
@Table(name = "COMMUNE",
    uniqueConstraints =
        @UniqueConstraint(columnNames = "FSO_NR"))
@Audited
@Tab(properties = "fsoNr, name, canton, deleted", 
	rowStyles = {@RowStyle(style="deletedData", property="deleted", value="true")})
@Views({
	@View(name = "Short", members = "fsoNr, name"), 
	@View(members = "fsoNr; name; district; canton; baronNr")
})
@Getter @Setter
public class Commune 
extends GenericIdentityWithDeleted implements Serializable, Identifiable {

	private static final long serialVersionUID = 5644131353056129922L;
    
    /**
     * FSO (Federal Statistic Office) number of the commune.
     */
    @Required
    @Column(name = "FSO_NR", updatable = false, nullable = false, precision=5)
    private Integer fsoNr;
    
    /**
     * Name of the commune.
     */
    @Required
    @Column(name = "NAME", nullable = false, length=50)
	@DisplaySize(value=30, forViews="Short") 
    private String name;
    
    /**
     * District in which the commune is.
     */
    @Column(name = "DISTRICT", nullable = false, length=50)
    private String district;
    
    /**
     * Canton in which the commune is.
     */
    @Column(name = "CANTON", nullable = false, length=2)
    private String canton;
    
    /**
     * The number of the commune according to Baron, updated to the commune change.
     */
    @Column(name = "BARON_NR", nullable = false, length=6)
    private String baronNr;
    
	@Depends("name")
	@Hidden
	public String getBusinessId() {
		return getName();
	}

	@PrePersist @PreUpdate @PreDelete
    public void handlePermissionsOnWrite() {
        ScisUserUtils.checkRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
    }
    
	@Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", fsoNr=");
		builder.append(fsoNr);
		builder.append(", name=");
		builder.append(name);
		builder.append(", district=");
		builder.append(district);
		builder.append(", canton=");
		builder.append(canton);
		builder.append(", baronNr=");
		builder.append(baronNr);
	}

    public static Commune getByFsoNr(Integer fsoNr) {
    	return SimpleQueries.getByUniqueField(Commune.class, "fsoNr", fsoNr);
    }

}
