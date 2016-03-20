package ch.speleo.scis.model.common;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DisplaySize;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Required;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;

import ch.speleo.scis.persistence.utils.SimpleQueries;

/**
 * Class representing a commune (administrative district of a town) using Hibernate
 * Annotation.
 * 
 * @author miguel
 * @version 1.0
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
public class Commune 
extends GenericIdentityWithDeleted implements Serializable, Identifiable {
    /**
     * Serial version UID.
     */
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
    
    /**
     * Empty constructor.
     */
    public Commune() { }
    /**
     * @return FSO (Federal Statistic Office) number of the commune.
     */
    public Integer getFsoNr() {
        return fsoNr;
    }
    /**
     * @param fsoNr FSO (Federal Statistic Office) number of the commune.
     */
    public void setFsoNr(Integer fsoNr) {
        this.fsoNr = fsoNr;
    }
    /**
     * @return name of the commune.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name name of the commune.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return district in which the commune is.
     */
    public String getDistrict() {
        return district;
    }
    /**
     * @param district district in which the commune is.
     */
    public void setDistrict(String district) {
        this.district = district;
    }
    /**
     * @return Canton in which the commune is.
     */
    public String getCanton() {
        return canton;
    }
    /**
     * @param Canton canton in which the commune is.
     */
    public void setCanton(String canton) {
        this.canton = canton;
    }
    /**
	 * @return The number of the commune according to Baron, updated to the commune change.
	 */
	public String getBaronNr() {
		return baronNr;
	}
	/**
	 * @param baronNr The number of the commune according to Baron, updated to the commune change.
	 */
	public void setBaronNr(String baronNr) {
		this.baronNr = baronNr;
	}
	@Depends("name")
	@Hidden
	public String getBusinessId() {
		return getName();
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
    	/*if (fsoNr == null) throw new IllegalArgumentException("fsoNr to search should not be null");
    	TypedQuery<Commune> query = 
    			XPersistence.getManager().createQuery("from Commune where fsoNr = ?", 
    					Commune.class);
    	query.setParameter(1, fsoNr);
    	try {
    		return query.getSingleResult();
    	} catch (PersistenceException e) { // or a subtype
			throw new PersistenceException(e.getClass().getSimpleName() + 
					" while searching Commune with fsoNr " + fsoNr, e);
		}*/
    }

}
