package ch.speleo.scis.model.karst;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.text.StrBuilder;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DisplaySize;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoFrame;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.NoSearch;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.util.Labels;

import ch.speleo.scis.business.utils.Axis;
import ch.speleo.scis.business.utils.InventoryNumberUtils;
import ch.speleo.scis.business.utils.SwissCoordsUtils;
import ch.speleo.scis.business.utils.SwissCoordsUtils.SwissCoords;
import ch.speleo.scis.model.common.Commune;
import ch.speleo.scis.persistence.typemapping.CodedEnumType;
import ch.speleo.scis.persistence.utils.SimpleQueries;

/**
 * Class representing a ground object (entrance, doline, spring, ...) 
 * using Hibernate Annotation.
 * 
 * @author miguel
 * @version 1.0
 */
@Entity
@Table(name = "GROUND_OBJECT", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "INVENTORY_NR", name="UNIQUE_GROUND_OBJECT_INVENTORY_NR"), 
		@UniqueConstraint(columnNames = {"CANTON_BARON", "COMMUNE_BARON_NR", "CAVE_BARON_NR"}, name="UNIQUE_GROUND_OBJECT_BARON_NR"), 
		@UniqueConstraint(columnNames = "PRIVACY_ID") 
	})
@DiscriminatorValue("E")
@Audited
@Tab(properties = "inventoryNr, baronNr, name, type, speleoObject.systemNr, deleted", 
	rowStyles = {@RowStyle(style="deletedData", property="deleted", value="true")})
@Views({ 
	@View(name = "short", members = "inventoryNr, baronNr, name, type, deleted"), 
	@View(members = "definition [name; inventoryNr, nextInventoryNrs; cantonBaron, communeBaronNr, caveBaronNr; type; comment; deleted], " +
			"location [locationAccuracy; commune; coordEast; coordNorth, mapNr; coordAltitude]; " +
			"verified; manager; creationDate, lastModifDate; literature; dataHistory; privacy; document; speleoObject; ") 
})
public class GroundObject 
extends KarstObject implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5941071612297331566L;
    
    /**
     * Inventory number of the ground object.
     */
    @Column(name = "INVENTORY_NR", nullable = true, unique = true, precision=8)
    private Integer inventoryNr;
    /**
	 * @return Type of the ground object
	 */
    @Column(name = "TYPE", nullable = true, length=1)
    @Type(type=CodedEnumType.CLASSNAME,
		parameters={ @Parameter(name=CodedEnumType.TYPE, value=GroundObjectTypeEnum.CLASSNAME)})
	@DisplaySize(value=10, forViews="short") 
	private GroundObjectTypeEnum type;
    /**
     * East coordinate (Y for geometers, X for matematicians) of the ground object.
     */
    @Column(name = "COORD_EAST", nullable = true, precision=7)
    @SwissCoords(axis = Axis.EAST)
    private Integer coordEast;
    /**
     * North coordinate (X for geometers, Y for matematicians) of the ground object.
     */
    @Column(name = "COORD_NORTH", nullable = true, precision=7)
    @SwissCoords(axis = Axis.NORTH)
    private Integer coordNorth;
    /**
     * Altitude of the ground object.
     */
    @Column(name = "COORD_ALTITUDE", nullable = true, precision=5)
    @SwissCoords(axis = Axis.ALTITUDE)
    private Integer coordAltitude;
    /**
     * Accuracy of the coordinates and the access description.
     */
    @Column(name = "LOCATION_ACCURACY", nullable = true, length=2)
    //@Type(type = "org.openxava.types.EnumLetterType", parameters = {
    //    @Parameter(name="letters", value=LocationAccuracyEnum.ORDERD_1CHAR_CODES),
    //    @Parameter(name="enumType", value=LocationAccuracyEnum.CLASSNAME) })
    @Type(type=CodedEnumType.CLASSNAME,
		parameters={ @Parameter(name=CodedEnumType.TYPE, value=LocationAccuracyEnum.CLASSNAME)})
    private LocationAccuracyEnum locationAccuracy;
    /**
     * Information related to the privacy of a ground object.
     */
    @OneToOne(optional = true, cascade = {CascadeType.ALL}, orphanRemoval=true)
    @JoinColumn(name = "PRIVACY_ID", nullable = true, unique = true)
    @ReferenceView(value = "short")
    @NoSearch
    private Privacy privacy;
    /**
     * The commune (administrative district of a town) where the entrance is located.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "COMMUNE_ID", nullable = true)
    @NoCreate @NoModify
    @ReferenceView(value = "short")
    @NoFrame
    private Commune commune;
    /**
	 * The canton of 1974 (reference of Baron's list) where the entrance is located.
	 */
	@Column(name = "CANTON_BARON", nullable = true, length=2)
	private String cantonBaron;
	/**
     * The commune of 1974 (reference of Baron's list) where the entrance is located.
     */
    @Column(name = "COMMUNE_BARON_NR", nullable = true, precision=5)
    private Integer communeBaronNr;
    /**
     * Cave number of the entrance on a commune (according to Baron's numbering system).
     */
    @Column(name = "CAVE_BARON_NR", nullable = true, precision=8)
    private Integer caveBaronNr;
    /**
     * Connected speleo object.
     */
    @ManyToOne
    @JoinColumn(name = "SPELEO_OBJECT_ID", nullable = true)
    @ReferenceView(value = "short")
    private SpeleoObject speleoObject;
    

    /**
     * @return inventory number of the ground object.
     */
    public Integer getInventoryNr() {
        return inventoryNr;
    }
    /**
     * @param noInventory inventory number of the ground object.
     */
    public void setInventoryNr(Integer noInventory) {
        this.inventoryNr = noInventory;
    }
    @Stereotype("LABEL")
    @DefaultValueCalculator(InventoryNumberUtils.NextNumbersCalculator.class)
    /**
     * @return The next unused / available inventory numbers
     */
    public String getNextInventoryNrs() {
    	return InventoryNumberUtils.getNextUnusedNumbersAsString();
    }
    /**
	 * @return Type of the ground object
	 */
	public GroundObjectTypeEnum getType() {
		return type;
	}
	/**
	 * @param type Type of the ground object
	 */
	public void setType(GroundObjectTypeEnum type) {
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
     * @return East coordinate (Y for geometers, X for matematicians) of the ground object.
     */
    public Integer getCoordEast() {
        return coordEast;
    }
    /**
     * @param coordEast East coordinate (Y for geometers, X for matematicians) of the ground object.
     */
    public void setCoordEast(Integer coordEast) {
    	this.coordEast = coordEast;
    }
    /**
     * @return North coordinate (X for geometers, Y for matematicians) of the ground object.
     */
    public Integer getCoordNorth() {
        return coordNorth;
    }
    /**
     * @param coordNorth North coordinate (X for geometers, Y for matematicians) of the ground object.
     */
    public void setCoordNorth(Integer coordNorth) {
    	this.coordNorth = coordNorth;
    }
    /**
     * @return Number of the 1:25000 map from Swisstopo in which the ground
     * object is located, {@code null} if unknown. 
     * @see SwissCoordsUtils#computeMapNr(int, int) the limits of computeMapNr
     */
	@Depends("coordEast, coordNorth")
    public Integer getMapNr() {
		if (coordEast == null || coordNorth == null)
			return null;
		else
			return SwissCoordsUtils.computeMapNr(coordEast, coordNorth);
    }
    /**
     * @return The coordinates reference system (CRS) for the coordinates of this object, 
     * {@code null} if unknown.
     * @see SwissCoordsUtils#getCoordReferenceSystem(int, int)
     */
	@Depends("coordEast, coordNorth")
    public String getCoordReferenceSystem() {
		if (coordEast == null || coordNorth == null)
			return null;
		else
			return SwissCoordsUtils.getCoordReferenceSystem(coordEast, coordNorth);
    }
	
    /**
     * @return Altitude of the ground object.
     */
    public Integer getCoordAltitude() {
        return coordAltitude;
    }
    /**
     * @param coordAltitude Altitude of the ground object.
     */
    public void setCoordAltitude(Integer coordAltitude) {
        this.coordAltitude = coordAltitude;
    }
    
    /**
	 * @return Accuracy of the coordinates and the access description.
	 */
	public LocationAccuracyEnum getLocationAccuracy() {
		return locationAccuracy;
	}
	/**
	 * @param locationAccuracy Accuracy of the coordinates and the access description.
	 */
	public void setLocationAccuracy(LocationAccuracyEnum locationAccuracy) {
		this.locationAccuracy = locationAccuracy;
	}
	
	/**
     * @return information related to the privacy of a ground object.
     */
    public Privacy getPrivacy() {
        return privacy;
    }
    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

	/**
     * @return The commune (administrative district of a town) where the entrance is located.
     */
    public Commune getCommune() {
        return commune;
    }
    /**
     * @param commune The commune (administrative district of a town) where the entrance is located.
     */
    public void setCommune(Commune commune) {
        this.commune = commune;
    }
    
    /**
	 * @return The canton of 1974 (reference of Baron's list) where the entrance is located.
	 */
	public String getCantonBaron() {
	    return cantonBaron;
	}
	/**
	 * @param cantonBaron The canton of 1974 (reference of Baron's list) 
	 *                    where the entrance is located.
	 */
	public void setCantonBaron(String cantonBaron) {
	    this.cantonBaron = cantonBaron;
	}
	/**
     * @return The commune of 1974 (reference of Baron's list) where the entrance is locate
     */
    public Integer getCommuneBaronNr() {
        return communeBaronNr;
    }
    /**
     * @param communeBaronNr ID The commune of 1974 (reference of Baron's list) 
     *                       where the entrance is locate
     */
    public void setCommuneBaronNr(Integer communeBaronNr) {
        this.communeBaronNr = communeBaronNr;
    }
    /**
	 * @return Cave number of the entrance on a commune (according to Baron's numbering system).
	 */
	public Integer getCaveBaronNr() {
		return caveBaronNr;
	}
	/**
	 * @param caveBaronNr Cave number of the entrance on a commune (according to Baron's numbering system).
	 */
	public void setCaveBaronNr(Integer caveBaronNr) {
		this.caveBaronNr = caveBaronNr;
	}
	@Length(max=17)
	@Depends("cantonBaron, communeBaronNr, caveBaronNr")
	public String getBaronNr() {
		StringBuilder sb = new StringBuilder();
		if(cantonBaron!=null) sb.append(cantonBaron);
		sb.append(" ");
		if(communeBaronNr!=null) sb.append(communeBaronNr);
		sb.append("/");
		if(caveBaronNr!=null) sb.append(caveBaronNr);
		return sb.toString();
	}
	
	/**
     * @return connected speleo object.
     */
    public SpeleoObject getSpeleoObject() {
        return speleoObject;
    }
    /**
     * @param speleoObject connected speleo object.
     */
    public void setSpeleoObject(SpeleoObject speleoObject) {
        this.speleoObject = speleoObject;
    }

	@Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", noInventory=");
		builder.append(inventoryNr);
		builder.append(", type=");
		builder.append(type);
		builder.append(", coordEast=");
		builder.append(coordEast);
		builder.append(", coordNorth=");
		builder.append(coordNorth);
		builder.append(", coordAltitude=");
		builder.append(coordAltitude);
		builder.append(", locationAccuracy=");
		builder.append(locationAccuracy);
		builder.append(", privacy=");
		builder.append((privacy==null)?"null":privacy.toString());
		builder.append(", commune.name=");
		builder.append((commune==null)?"null":commune.getName());
		builder.append(", communeBaronNr=");
		builder.append(communeBaronNr);
		builder.append(", cantonBaron=");
		builder.append(cantonBaron);
		builder.append(", caveBaronNr=");
		builder.append(caveBaronNr);
		builder.append(", speleoObject.id=");
		builder.append((speleoObject==null)?"null":speleoObject.getId());
	}

    public static GroundObject getByInventoryNr(Integer inventoryNr) {
    	return SimpleQueries.getByUniqueField(GroundObject.class, "inventoryNr", inventoryNr);
    }
    
    public static GroundObject getByBaronNr(String cantonBaron, Integer communeBaronNr, Integer caveBaronNr) {
    	StrBuilder msg = new StrBuilder();
    	StrBuilder query = new StrBuilder();
    	ArrayList<Object> params = new ArrayList<Object>(3);
    	msg.append(" while searching ").append(GroundObject.class.getSimpleName());
    	msg.append(" with baronNr = ");
		if(cantonBaron!=null) {
			msg.append(cantonBaron);
			query.append("cantonBaron = ?");
			params.add(cantonBaron);
		}
		msg.append(" ");
		if(communeBaronNr!=null) {
			msg.append(communeBaronNr);
			query.appendSeparator(" and ").append("communeBaronNr = ?");
			params.add(communeBaronNr);
		}
		msg.append("/");
		if(caveBaronNr!=null) {
			msg.append(caveBaronNr);
			query.appendSeparator(" and ").append("caveBaronNr = ?");
			params.add(caveBaronNr);
		}
    	if (params.isEmpty()) {
    		throw new IllegalArgumentException("at least one part of the baronNr to search should not be null");
    	}
    	return SimpleQueries.getSingleResult(msg.toString(), GroundObject.class, 
    			query.toString(), params.toArray());
    }
    
}
