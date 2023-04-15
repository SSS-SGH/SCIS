package ch.speleo.scis.model.karst;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.text.StrBuilder;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.openxava.annotations.Action;
import org.openxava.annotations.AsEmbedded;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DisplaySize;
import org.openxava.annotations.LabelStyle;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoFrame;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.NoSearch;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.filters.FilterException;
import org.openxava.filters.IFilter;
import org.openxava.util.Labels;

import ch.speleo.scis.business.utils.Axis;
import ch.speleo.scis.business.utils.InventoryNumberUtils;
import ch.speleo.scis.business.utils.SwissCoordsUtils;
import ch.speleo.scis.business.utils.SwissCoordsUtils.CoordsSystem;
import ch.speleo.scis.business.utils.SwissCoordsUtils.SwissCoords;
import ch.speleo.scis.model.common.Commune;
import ch.speleo.scis.model.common.GenericIdentityWithRevision;
import ch.speleo.scis.model.karst.GroundObject.GroundObjectFilter;
import ch.speleo.scis.persistence.audit.ScisUserUtils;
import ch.speleo.scis.persistence.audit.ScisUserUtils.ScisRole;
import ch.speleo.scis.persistence.typemapping.CodedEnumType;
import ch.speleo.scis.persistence.utils.SimpleQueries;
import ch.speleo.scis.ui.editors.TabViewableOnMap;
import lombok.Getter;
import lombok.Setter;

/**
 * An object on the ground (entrance, doline, spring, ...).
 */
@Entity
@Table(name = "GROUND_OBJECT", 
uniqueConstraints = {
	@UniqueConstraint(columnNames = "INVENTORY_NR", name="UNIQUE_GROUND_OBJECT_INVENTORY_NR"), 
	@UniqueConstraint(columnNames = {"CANTON_BARON", "COMMUNE_BARON_NR", "CAVE_BARON_NR"}, name="UNIQUE_GROUND_OBJECT_BARON_NR"), 
	@UniqueConstraint(columnNames = "PRIVACY_ID") 
})
@Audited
@Tab(editor = "List", editors = "List, Charts, Cards, MapScis",
    properties = "inventoryNr, baronNr, name, type, speleoObject.systemNr, deleted", 
	rowStyles = {@RowStyle(style="deletedData", property="deleted", value="true")},
    filter = GroundObjectFilter.class, baseCondition = GroundObjectFilter.CONDITION
)
@TabViewableOnMap(northColumnName = "coordNorthLv95", eastColumnName = "coordEastLv95", coordsSystemEpsgNr = 2056)
@Views({ 
	@View(name = "Short", members = "name, type, commune.name"), 
	@View(name = "ShortWithId", members = "inventoryNr, baronNr, name, type, deleted"), 
	@View(members = "definition [name; inventoryNr, nextInventoryNrs; cantonBaron, communeBaronNr, caveBaronNr; type; comment; deleted], " +
			"location [locationAccuracy; commune; coordEast, coordEastLv95; coordNorth, coordNorthLv95; coordAltitude; mapNr]; " +
			"verified; manager; creationDate, lastModifDate; literature; dataHistory; privacy; document; speleoObject; "),
	@View(name=GenericIdentityWithRevision.AUDIT_VIEW_NAME, members = " auditedValues")
})
@Getter @Setter
public class GroundObject 
extends KarstObject implements Serializable {

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
	@DisplaySize(value=30, forViews="Short, ShortWithId") 
	private GroundObjectTypeEnum type;

    /**
     * Accuracy of the coordinates and the access description.
     */
    @Column(name = "LOCATION_ACCURACY", nullable = true, length=2)
    @Type(type=CodedEnumType.CLASSNAME,
		parameters={ @Parameter(name=CodedEnumType.TYPE, value=LocationAccuracyEnum.CLASSNAME)})
    private LocationAccuracyEnum locationAccuracy;

    /**
     * Information related to the privacy of a ground object.
     */
    @OneToOne(optional = true, cascade = {CascadeType.ALL}, orphanRemoval=true)
    @JoinColumn(name = "PRIVACY_ID", nullable = true, unique = true)
    @AsEmbedded
    @NoSearch @NoCreate @NoModify
    private Privacy privacy;

    /**
     * The commune (administrative district of a town) where the entrance is located.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "COMMUNE_ID", nullable = true)
    @NoCreate @NoModify
    @ReferenceView(value = "Short")
    @NoFrame
    private Commune commune;

    /**
	 * The canton of 1974 (reference of Baron's list) where the entrance is located.
	 */
	@Column(name = "CANTON_BARON", nullable = true, length=2)
	@LabelStyle(value="CantonBaronLabelInDetail", forViews="DEFAULT")
	private String cantonBaron;
	/**
     * The commune of 1974 (reference of Baron's list) where the entrance is located.
     */
    @Column(name = "COMMUNE_BARON_NR", nullable = true, precision=5)
	@LabelStyle(value="CommuneBaronLabelInDetail", forViews="DEFAULT")
    private Integer communeBaronNr;
    /**
     * Cave number of the entrance on a commune (according to Baron's numbering system).
     */
    @Column(name = "CAVE_BARON_NR", nullable = true, precision=8)
	@LabelStyle(value="CaveBaronLabelInDetail", forViews="DEFAULT")
    private Integer caveBaronNr;
    /**
     * Full identifier of the entrance (according to Baron's numbering system).
     */
    @Formula("concat(CANTON_BARON, case when (CANTON_BARON is null or CANTON_BARON = '') then '' else ' ' end, COMMUNE_BARON_NR, '/', CAVE_BARON_NR)")
	@NotAudited
    private String baronNr;
    /**
     * Connected speleo object.
     */

    /**
     * East coordinate (Y for geometers, X for matematicians) of the ground object.
     */
    @Column(name = "COORD_EAST", nullable = true, precision=7)
    @SwissCoords(axis = Axis.EAST)
    @ReadOnly
    private Integer coordEast;
    /**
     * North coordinate (X for geometers, Y for matematicians) of the ground object.
     */
    @Column(name = "COORD_NORTH", nullable = true, precision=7)
    @SwissCoords(axis = Axis.NORTH)
    @ReadOnly
    private Integer coordNorth;
    /**
     * Altitude of the ground object.
     */
    @Column(name = "COORD_ALTITUDE", nullable = true, precision=5)
    @SwissCoords(axis = Axis.ALTITUDE)
    @Action(value="Geo.goToSwissMap", alwaysEnabled=true)
    private Integer coordAltitude;

    /**
     * East coordinate (Y for geometers, X for matematicians) of the ground object.
     */
    @Column(name = "COORD_EAST_LV95", nullable = true, precision=9, scale = 2)
    @SwissCoords(axis = Axis.EAST, coordsSystem = CoordsSystem.LV95)
    private BigDecimal coordEastLv95;
    /**
     * North coordinate (X for geometers, Y for matematicians) of the ground object.
     */
    @Column(name = "COORD_NORTH_LV95", nullable = true, precision=9, scale = 2)
    @SwissCoords(axis = Axis.NORTH, coordsSystem = CoordsSystem.LV95)
    private BigDecimal coordNorthLv95;

    @ManyToOne
    @JoinColumn(name = "SPELEO_OBJECT_ID", nullable = true)
    @ReferenceView(value = "ShortWithId")
    private SpeleoObject speleoObject;
    

    @Stereotype("LABEL")
    @DefaultValueCalculator(InventoryNumberUtils.NextNumbersCalculator.class)
    /**
     * @return The next unused / available inventory numbers
     */
    public String getNextInventoryNrs() {
    	return InventoryNumberUtils.getNextUnusedNumbersAsString();
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
     * @return Number of the 1:25000 map from Swisstopo in which the ground
     * object is located, {@code null} if unknown. 
     * @see SwissCoordsUtils#computeMapNr(int, int) the limits of the validity
     */
	@Depends("coordEastLv95, coordNorthLv95")
    public Integer getMapNr() {
		if (coordEastLv95 == null || coordNorthLv95 == null)
			return null;
		else
			return SwissCoordsUtils.computeMapNr(coordEastLv95.intValue(), coordNorthLv95.intValue());
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
	
    @ListProperties("revision.modificationDate, revision.username, deleted, inventoryNr, baronNr, name, type, comment, " +
    		"locationAccuracy, commune.name, coordEast, coordNorth, coordEastLv95, coordNorthLv95, coordAltitude, verified, manager.initialsAndName, " +
    		"literature, dataHistory, privacy.startDate, privacy.endDate, speleoObject.systemNr")
    @ReadOnly
    public Collection<GroundObject> getAuditedValues() {
    	Collection<GroundObject> auditedValues = loadAuditedValues();
    	if (!ScisUserUtils.hasRoleInCurrentUser(ScisRole.SGH_ARCHIVAR)) {
    		for (GroundObject auditedValue: auditedValues) {
    			auditedValue.setCoordEast(null);
    			auditedValue.setCoordNorth(null);
    			auditedValue.setCoordAltitude(null);
    			auditedValue.setCoordEastLv95(null);
    			auditedValue.setCoordNorthLv95(null);
    		}
    	}
    	return auditedValues;
    }

	/*@PrePersist @PreUpdate
    public void handlePermissionsOnWrite() {
		coord = new GeometryFactory().createPoint(new Coordinate(
				SwissCoordsUtils.toLV95(coordEast, Axis.EAST), 
				SwissCoordsUtils.toLV95(coordNorth, Axis.NORTH),
				coordAltitude
			));
    }*/
    
	@Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", noInventory=");
		builder.append(inventoryNr);
		builder.append(", type=");
		builder.append(type);
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
    
    public static Collection<GroundObject> getPermittedBySpeleoObject(SpeleoObject speleoObject) {
       	StrBuilder msg = new StrBuilder();
    	msg.append(" while searching ").append(GroundObject.class.getSimpleName());
    	msg.append(" with speleoObject.id = " ).append(speleoObject.getId()).append(" and role-based-permission");
     	return SimpleQueries.getMultipleResults(
     			msg.toString(), 
    			GroundObject.class, 
    			"speleoObject = ?3 and not exists (select p from Privacy p where e.privacy = p and p.startDate <= ?1 and p.endDate > ?2)", 
    			new GroundObjectFilter().filter(new Object[]{speleoObject}));
    }
    
    public static class GroundObjectFilter implements IFilter {
   	
		private static final long serialVersionUID = 3656925091277610151L;

		public static final String CONDITION = "not exists (select p from Privacy p where e.privacy = p and p.startDate <= ? and p.endDate > ?)";
        private static final int ONE_DAY = 24*3600*1000;

		@Override
		public Object[] filter(Object o) throws FilterException {
			if (ScisUserUtils.hasRoleInCurrentUser(ScisRole.SGH_ARCHIVAR)) {
				return ScisUserUtils.combineFilters(o, new Date(0), new Date(2 * ONE_DAY));
			} else {
				long now = System.currentTimeMillis();
				return ScisUserUtils.combineFilters(o, new Date(now), new Date(now - ONE_DAY));
			}
		}
   	
   }
   
}
