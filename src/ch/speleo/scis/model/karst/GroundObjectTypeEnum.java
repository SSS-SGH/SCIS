package ch.speleo.scis.model.karst;

import ch.speleo.scis.model.common.Codeable;

/**
 * Type of a ground object. See {@link SpeleoObjectTypeEnum} for mines or quarry. 
 * @author florian
 * @version 1.1
 */
public enum GroundObjectTypeEnum
implements Codeable {

	NO_DATA("0"), 
	/** Entrance of singly-entrance speleo object or the main entrance of a system */
	MAIN_ENTRANCE("1"), 
	/** Other entrances of a system */
	SECONDARY_ENTRANCE("2"), 
	/** clogged or collapsed entrance */
	CLOGGED_ENTRANCE("3"),
	DOLINE_OR_SURFACE_SHAPE("D"), 
	SPRING("S");
	
	public static final String CLASSNAME = "ch.speleo.scis.model.karst.GroundObjectTypeEnum";

	private final String code;
	
	private GroundObjectTypeEnum(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static GroundObjectTypeEnum fromCode(String code) {
		return Codeable.Utils.fromCode(code, values());
	}
}
