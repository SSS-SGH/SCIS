package ch.speleo.scis.model.karst;

import ch.speleo.scis.model.common.Codeable;

/**
 * Categories for Speleo (underground) objects
 * @author florian
 * @version 1.1
 */
public enum LocationAccuracyEnum
implements Codeable {

	NO_COORDINATES_UNCERTAIN_EXISTENCE("0"), 
	NO_COORDINATES_PROBABLE_EXISTENCE("1"), 
	KNOWN_LOCATION_WITHOUT_COORDINATES("2"), 
	COORDINATES_WITHOUT_ACCES_DESCRIPTION("3"),
	COORDINATES_AND_ACCES_DESCRIPTION("4"),
	CONTROLLED_COORDINATES_AND_ACCES_DESCRIPTION("5");
	
	public static final String CLASSNAME = "ch.speleo.scis.model.karst.LocationAccuracyEnum";

	/** 
	 * List of 1-character codes, in the same order as the enumeration. 
	 * Can be used with {@code indexOf} and {@code charAt} to map to the database. 
	 * */
	public static final String ORDERD_1CHAR_CODES = "012345";
	
	private final String code;
	
	private LocationAccuracyEnum(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static LocationAccuracyEnum fromCode(String code) {
		return Codeable.Utils.fromCode(code, values());
	}
}
