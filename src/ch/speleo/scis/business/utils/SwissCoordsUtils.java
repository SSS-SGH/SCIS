package ch.speleo.scis.business.utils;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;


/** Utilities for Swiss coordinates */
public class SwissCoordsUtils {

	// boudaries of the CH1903 ("conventional") coordinates system
	/** minimal East coordinate in the CH1903 reference system */
    public static final int EAST_MIN_LV03 = 480000;
	/** maximal East coordinate in the CH1903 reference system */
    public static final int EAST_MAX_LV03 = 850000;
	/** minimal North coordinate in the CH1903 reference system */
    public static final int NORTH_MIN_LV03 = 70000;
	/** maximal North coordinate in the CH1903 reference system */
    public static final int NORTH_MAX_LV03 = 310000;
	
	// boudaries of the CH1903+ ("new") coordinates system
	/** minimal East coordinate in the CH1903+ reference system */
    public static final int EAST_MIN_LV95 = 2480000;
	/** maximal East coordinate in the CH1903+ reference system */
    public static final int EAST_MAX_LV95 = 2850000;
	/** minimal North coordinate in the CH1903+ reference system */
    public static final int NORTH_MIN_LV95 = 1070000;
	/** maximal North coordinate in the CH1903+ reference system */
    public static final int NORTH_MAX_LV95 = 1310000;
    
    // boundaries of the altitude
	/** minimal Altitude in Switzerland */
    public static final int ALTITUDE_MIN = 100;
	/** maximal Altitude in Switzerland */
    public static final int ALTITUDE_MAX = 4999;
    
    
	public static final Range EAST_LV03 = new Range(EAST_MIN_LV03, EAST_MAX_LV03);
	public static final Range EAST_LV95 = new Range(EAST_MIN_LV95, EAST_MAX_LV95);
 	public static final Range NORTH_LV03 = new Range(NORTH_MIN_LV03, NORTH_MAX_LV03);
	public static final Range NORTH_LV95 = new Range(NORTH_MIN_LV95, NORTH_MAX_LV95);
	public static final Range ALTITUDE_ = new Range(ALTITUDE_MIN, ALTITUDE_MAX);
    
	public static final Ranges EAST = new Ranges(EAST_LV03, EAST_LV95);
	public static final Ranges NORTH = new Ranges(NORTH_LV03, NORTH_LV95);
	public static final Ranges ALTITUDE = new Ranges(ALTITUDE_);
	public static final Ranges EMPTY = new Ranges();
    
	/**
	 * Computes the coordinates reference system (CRS) for the given coordinates. 
	 * @param east   East coordinate in the CH1903 or CH1903+ reference system
	 * @param north  North coordinate in the CH1903 or CH1903+ reference system
	 * @return CH1903 or CH1903+ or {@code null} if unknown / unclear.
	 */
	public static String getCoordReferenceSystem(int east, int north) {
		if (EAST_LV03.contains(east) && NORTH_LV03.contains(north))
			return "CH1903";
		else if (EAST_LV95.contains(east) && NORTH_LV95.contains(north))
			return "CH1903+";
		else
			return null;
	}    
    
    /** Gives the possible ranges (CH1903 and CH1903+) for a swiss coordinate. 
     */
    public static Ranges getRanges(Axis axis) {
    	switch(axis) {
    	case EAST:
    		return EAST;
    	case NORTH:
    		return NORTH;
    	case ALTITUDE:
    		return ALTITUDE;
    	default: 
    		return EMPTY;
    	}
    }

    /** Constraint for a Swiss coordinate. */
    @Constraint(validatedBy = SwissCoordsValidator.class)
    @Target({FIELD, METHOD})
    @Retention(RUNTIME)
    @Documented
    public static @interface SwissCoords {
    	/** the axis to constraint */
    	Axis axis();
    	String message() default "validator.SwissCoords";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
   }
    
    /** Validator for a Swiss coordinate. */
    public static class SwissCoordsValidator
    implements ConstraintValidator<SwissCoords, Object> {
    	
    	private Axis axis;
    	
		public void initialize(SwissCoords parameters) {
			this.axis = parameters.axis();
		}

		public boolean isValid(Object value, ConstraintValidatorContext context) {
			if (value==null) return true;
			Ranges ranges = getRanges(axis);
			if ( value instanceof Double ) {
				return ranges.contains((Double) value);
			}
			else if ( value instanceof Float ) {
				return ranges.contains((Float) value);
			}
			else if ( value instanceof Number ) {
				return ranges.contains(((Number) value).longValue());
			}
			return false;
		}
    	
		public void apply(Property property) {
			Column col = (Column) property.getColumnIterator().next();
			StringBuilder check = new StringBuilder();
			Ranges ranges = getRanges(axis);
			for (Range range: ranges.getRanges()) {
				if(check.length() > 0) 
					check.append(" or ");
				check.append("(");
				check.append(col.getName());
				check.append(">=");
				check.append(range.getMin());
				check.append(" and ");
				check.append(col.getName());
				check.append("<=");
				check.append(range.getMax());
				check.append(")");
			}
			col.setCheckConstraint( check.toString() );
		}

    }
	
	/** Width [m] of a swiss 1:25'000 map */
	protected static final int MAP_WIDTH = 17500;
	/** Height [m] of a swiss 1:25'000 map */
	protected static final int MAP_HEIGHT = 12000;
	/** East coordinate [m]  of the first (left-most) swiss 1:25'000 map*/
	protected static final int MAP_EAST_OF_FIRST = 480000;
	/** North coordinate [m]  of the first (top-most) swiss 1:25'000 map*/
	protected static final int MAP_NORTH_OF_FIRST = 302000;
	/** Maximal number of maps horizontaly */
	protected static final int MAP_NR_HORIZONTAL = 20;
	/** Offset for the number of swiss 1:25'000 map */
	protected static final int MAP_OFSET_25K = 1000;

	/**
	 * Compute the map number on which a given point is. 
	 * @param east   East coordinate in the CH1903 or CH1903+ reference system
	 * @param north  North coordinate in the CH1903 or CH1903+ reference system
	 * @return Number of the 1:25'000 map from Swisstopo. 
	 *         Does not recognize the rare "bis" maps (like 1239bis) 
	 *         and is inaccurate for points outside the mapped part of Switzerland. 
	 */
	public static Integer computeMapNr(int east, int north) {
		// TODO Better use a GIS or a map overview, as inacurrate in all the special case. 
		if (EAST_LV95.contains(east)) east = east - 2000000;
		if (NORTH_LV95.contains(north)) north = north - 1000000;
		int horizontalNr = (east - MAP_EAST_OF_FIRST) / MAP_WIDTH;
		// horizonal map nr are between 0 and 19 (even sometimes 19bis in GR)
		horizontalNr = Math.min(Math.max(horizontalNr, 0), MAP_NR_HORIZONTAL - 1); 
		int verticalNr = (MAP_NORTH_OF_FIRST - north) / MAP_HEIGHT;
		return new Integer(MAP_OFSET_25K + (MAP_NR_HORIZONTAL * verticalNr) + horizontalNr);
	}
	
    /**
	 * Compute the right-angle area of a map
	 * @param mapNr Number of the 1:25'000 map from Swisstopo.
	 * @return The area of the given map
	 * @deprecated Inacurrate in all the special case. Better use a GIS or a map overview. 
	 */
	/*public static AreaRect2D computeArea(Integer mapNr) {
		int horizontalNr = mapNr.intValue() % (MAP_NR_HORIZONTAL);
		int verticalNr = (mapNr.intValue() - MAP_OFSET_25K) / MAP_NR_HORIZONTAL;
		return new AreaRect2D(MAP_EAST_OF_FIRST + (MAP_WIDTH * horizontalNr), 
		                      MAP_EAST_OF_FIRST + (MAP_WIDTH * (horizontalNr + 1)), 
		                      MAP_NORTH_OF_FIRST - (MAP_HEIGHT * verticalNr), 
		                      MAP_NORTH_OF_FIRST - (MAP_HEIGHT * (verticalNr + 1)));
	}*/
	

}
