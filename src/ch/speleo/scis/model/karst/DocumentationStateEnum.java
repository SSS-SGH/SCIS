package ch.speleo.scis.model.karst;

import org.hibernate.usertype.UserType;

import ch.speleo.scis.model.common.Codeable;
import ch.speleo.scis.persistence.typemapping.CodedEnumType;

/**
 * Categories for Speleo (underground) objects
 * @author florian
 * @version 1.1
 */
public enum DocumentationStateEnum 
implements Codeable {

	NO_AVAILABLE_DOCUMENT("0"), 
	OLD_MAP_OF_UNKNOWN_QUALITY("1"),
	ROUGH_REPORT_OR_SKETCH("2"),
	MAP_WITHOUT_DESCRIPTION("3"), 
	DESCRIPTION_WITHOUT_MAP("4"), 
	MAP_AND_DESCRIPTION("5"),
	EVOLVING_CONTINUOUSLY("6");
	
	public static final String CLASSNAME = "ch.speleo.scis.model.karst.DocumentationStateEnum";

	private final String code;
	
	private DocumentationStateEnum(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static DocumentationStateEnum fromCode(String code) {
		return Codeable.Utils.fromCode(code, values());
	}
	
	
	public static class CodedType extends CodedEnumType implements UserType {
		public static final String CLASSNAME = "ch.speleo.scis.model.karst.DocumentationStateEnum.CodedType";
		public Class<? extends Codeable> returnedClass() {
			return DocumentationStateEnum.class;
		}
	}
	
}
