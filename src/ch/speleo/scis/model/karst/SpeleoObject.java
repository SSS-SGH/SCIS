package ch.speleo.scis.model.karst;

import static ch.speleo.scis.persistence.typemapping.CodedEnumType.TYPE;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
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

import ch.speleo.scis.model.common.GenericIdentityWithRevision;
import ch.speleo.scis.persistence.typemapping.CodedEnumType;
import ch.speleo.scis.persistence.utils.SimpleQueries;
import lombok.Getter;
import lombok.Setter;

/**
 * A subsurface object (a cave, a mine, etc.).
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
	@View(name = "Short", members = "name, type, length, depthAndElevation"), 
	@View(name = "ShortWithId", members = "systemNr, name, type, deleted"), 
	@View(members = "definition [name; systemNr; type; documentationState; comment; deleted] " +
			"dimensions [length; depth; elevation; depthAndElevation, depthAndElevationComputed]; " +
			"verified; manager; creationDate, lastModifDate; literature; dataHistory; document; entrances; entrancesPermitted; "),
	@View(name=GenericIdentityWithRevision.AUDIT_VIEW_NAME, members = " auditedValues")
})
@Getter @Setter
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
	@DisplaySize(value=30, forViews="Short, ShortWithId") 
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
    @RowStyle(style="deletedData", property="deleted", value="true")
    @ListAction("CollectionScis.add")
    private Collection<GroundObject> entrances = new LinkedList<GroundObject>();

    @ListProperties(value = "inventoryNr, baronNr, name, type, deleted")
    @RowStyle(style="deletedData", property="deleted", value="true")
    @ReadOnly
    public Collection<GroundObject> getEntrancesPermitted() {
    	if (getId() == null) {
    		return List.of();
    	}
		return GroundObject.getPermittedBySpeleoObject(this);
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
	}

    @ListProperties("revision.modificationDate, revision.username, deleted, systemNr, name, type, documentationState, " +
    		"length, depth, elevation, depthAndElevation, verified; manager.initialsAndName, literature, dataHistory")
    @ReadOnly
    public Collection<SpeleoObject> getAuditedValues() {
    	return loadAuditedValues();
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
