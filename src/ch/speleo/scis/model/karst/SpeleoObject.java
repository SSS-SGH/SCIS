package ch.speleo.scis.model.karst;

import static ch.speleo.scis.persistence.typemapping.CodedEnumType.TYPE;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.openxava.annotations.AsEmbedded;
import org.openxava.annotations.Collapsed;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DisplaySize;
import org.openxava.annotations.LabelFormat;
import org.openxava.annotations.LabelFormatType;
import org.openxava.annotations.ListAction;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.util.Labels;

import ch.speleo.scis.persistence.typemapping.CodedEnumType;
import ch.speleo.scis.persistence.utils.SimpleQueries;

/**
 * Class representing a subsurface object (a cave, a mine, etc.).
 * 
 * @author miguel
 * @version 1.0
 */
@Entity
@Table(name = "SPELEO_OBJECT",
    uniqueConstraints = {
		@UniqueConstraint(columnNames = "SYSTEM_NR", name="UNIQUE_SPELEO_OBJECT_SYSTEM_NR")
	})
@Audited
@Tab(properties = "systemNr, name, type, deleted", 
	rowStyles = {@RowStyle(style="deletedData", property="deleted", value="true")})
@Views({ 
	@View(name = "Short", members = "systemNr, name, type, deleted"), 
	@View(members = "definition [name; systemNr; type; documentationState; comment; deleted] " +
			"dimensions [length; depth; elevation; depthAndElevation, depthAndElevationComputed]; " +
			"verified; manager; creationDate, lastModifDate; literature; dataHistory; document; entrances; " + 
			"auditedValues") 
})
public class SpeleoObject 
extends KarstObject implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 18189381772661526L;
    
    /**
     * Number of the system (speleo object / cave network).
     */
    @Column(name = "SYSTEM_NR", nullable = true, precision=8)
    private Integer systemNr;
    /**
     * Type of the speleo object: cave, mine, ...
     */
    @Column(name = "TYPE", nullable = true, length=1)
    @Type(type=CodedEnumType.CLASSNAME,
    	parameters={ @Parameter(name=TYPE, value=SpeleoObjectTypeEnum.CLASSNAME)})
	@DisplaySize(value=10, forViews="Short") 
    private SpeleoObjectTypeEnum type;
    /**
     * Length of the speleo object.
     */
    @Column(name = "LENGTH", nullable = true, precision=7)
    @Min(0)
    private Integer length;
    /**
     * Elevation of the speleo object.
     */
    @Column(name = "ELEVATION", nullable = true, precision=5)
    @Min(0)
    private Integer elevation;
    /**
     * Depth of the speleo object.
     */
    @Column(name = "DEPTH", nullable = true, precision=5)
    @Min(0)
    private Integer depth;
    /**
     * Depth and elevation (total difference in altitude) of the speleo object.
     */
    @Column(name = "DEPTH_AND_ELEVATION", nullable = true, precision=5)
    @Min(0)
    private Integer depthAndElevation;
    /**
     * State of the documentation (description, topography) except location.
     */
    @Column(name = "DOCUMENTATION_STATE", nullable = true, length=2)
    //@Type(type=DocumentationStateEnum.CodedType.CLASSNAME)
    @Type(type=CodedEnumType.CLASSNAME,
		parameters={ @Parameter(name=TYPE, value=DocumentationStateEnum.CLASSNAME)})
    private DocumentationStateEnum documentationState;
    /**
     * List of connected ground objects (entries, ...).
     */
    @OneToMany(mappedBy = "speleoObject", 
    		cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @ListProperties(value = "inventoryNr, baronNr, name, type, deleted")
    @AsEmbedded
    @ListAction("CollectionScis.add")
    private Collection<GroundObject> entrances = new LinkedList<GroundObject>();

    /**
     * Empty constructor.
     */
    public SpeleoObject() { }
    
    /**
     * @return Number of the system (speleo object / cave network).
     */
    public Integer getSystemNr() {
        return systemNr;
    }
    /**
     * @param systemNr Number of the system (speleo object / cave network).
     */
    public void setSystemNr(Integer systemNr) {
        this.systemNr = systemNr;
    }
    /**
	 * @return Type of the speleo object: cave, mine, ...
	 */
	public SpeleoObjectTypeEnum getType() {
		return type;
	}
	/**
	 * @param type Type of the speleo object: cave, mine, ...
	 */
	public void setType(SpeleoObjectTypeEnum type) {
		this.type = type;
	}
	@Depends("type")
	public String getTranslatedType() {
		if (type != null) {
			String text = type.name();
			return Labels.get(text);
		} else {
			return super.getTranslatedType();
		}
	}
    /**
     * @return length of the speleo object.
     */
    public Integer getLength() {
        return length;
    }
    /**
     * @param length length of the speleo object.
     */
    public void setLength(Integer length) {
        this.length = length;
    }
    /**
     * @return elevation of the speleo object.
     */
    public Integer getElevation() {
        return elevation;
    }
    /**
     * @param elevation elevation of the speleo object.
     */
    public void setElevation(Integer elevation) {
        this.elevation = elevation;
    }
    /**
     * @return depth of the speleo object.
     */
    public Integer getDepth() {
        return depth;
    }
    /**
     * @param depth depth of the speleo object.
     */
    public void setDepth(Integer depth) {
        this.depth = depth;
    }
	/**
	 * @return Depth and elevation (total difference in altitude) of the speleo object.
	 */
	public Integer getDepthAndElevation() {
		return depthAndElevation;
	}
	/**
	 * @param depthAndElevation Depth and elevation (total difference in altitude) of the speleo object.
	 */
	public void setDepthAndElevation(Integer depthAndElevation) {
		this.depthAndElevation = depthAndElevation;
	}
	@Depends("depth, elevation, depthAndElevation")
	@Max(99999)
    @Stereotype("LABEL")
	@LabelFormat(value=LabelFormatType.NO_LABEL, forViews="DEFAULT")
	public Integer getDepthAndElevationComputed() {
		if (depth != null && elevation != null) {
			return depth + elevation;
		} else if (depthAndElevation != null) {
			return depthAndElevation;
		} else if (depth != null){
			return depth;
		} else {
			return elevation;
		}
	}	/**
	 * @return State of the documentation (description, topography) except location.
	 */
	public DocumentationStateEnum getDocumentationState() {
		return documentationState;
	}
	/**
	 * @param documentationState State of the documentation (description, topography) except location.
	 */
	public void setDocumentationState(DocumentationStateEnum documentationState) {
		this.documentationState = documentationState;
	}
    /**
     * @return list of connected entrances.
     */
    public Collection<GroundObject> getEntrances() {
        return entrances;
    }
    /**
     * @param entrances list of connected entrances.
     */
    public void setEntrances(Collection<GroundObject> entrances) {
        this.entrances = entrances;
    }

    @ListProperties("revision.modificationDate, revision.username, deleted, systemNr, name, type, documentationState, " +
    		"length, depth, elevation, depthAndElevation, verified; manager.initialsAndName, literature, dataHistory")
    @ReadOnly
    @Collapsed 
    public Collection<SpeleoObject> getAuditedValues() {
    	return loadAuditedValues(SpeleoObject.class);
    }

    @Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", systemNr=");
		builder.append(systemNr);
		builder.append(", type=");
		builder.append(type);
		builder.append(", length=");
		builder.append(length);
		builder.append(", elevation=");
		builder.append(elevation);
		builder.append(", depth=");
		builder.append(depth);
		builder.append(", depthAndElevation=");
		builder.append(depthAndElevation);
		builder.append(", documentationState=");
		builder.append(documentationState);
	}
        
    public static SpeleoObject getBySystemNr(Integer systemNr) {
    	return SimpleQueries.getByUniqueField(SpeleoObject.class, "systemNr", systemNr);
    }

}
