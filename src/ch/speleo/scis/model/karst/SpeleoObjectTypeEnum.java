package ch.speleo.scis.model.karst;

import ch.speleo.scis.model.common.Codeable;

/**
 * Type of a speleo (underground) object.
 * @author florian
 * @version 1.1
 */
public enum SpeleoObjectTypeEnum
implements Codeable {

	NATURAL_CAVE_SINGLE("N"), 
	NATURAL_CAVE_SYSTEM("R"), 
	HUMAN_MADE_CAVE("A"), 
	MINES_OR_QUARRY("X");
	// Spring, doline, etc. are only for ground objects. Let empty the type of the related speleo object (if any). 
	
	public static final String CLASSNAME = "ch.speleo.scis.model.karst.SpeleoObjectTypeEnum";

	private final String code;
	
	private SpeleoObjectTypeEnum(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static SpeleoObjectTypeEnum fromCode(String code) {
		return Codeable.Utils.fromCode(code, values());
	}
	
}
