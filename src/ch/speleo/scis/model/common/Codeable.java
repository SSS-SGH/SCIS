package ch.speleo.scis.model.common;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface for an enumeration having a code. Implement it in enumerations. 
 * The idea is to give a small, name-independent, unique code to each value of an enumeration. 
 * It can be used to save an enumeration into a database. 
 * @author florian
 * @version 1.1
 */
public interface Codeable extends Serializable {
	
	/**
	 * The unique code for each enum value. 
	 */
	public String getCode();
	
	
	/** 
	 * Utilities about "codeable" enumerations
	 * 
	 * @author florian
	 */
	public static class Utils {
		
		/**
		 * Gives an enumaration from its code. 
		 * @param code the code to convert
		 * @param values all possible enumeration values, as an array
		 * @return an enumeration value
		 * @throws IllegalArgumentException if no such code exists
		 */
		public static <E extends Codeable> E fromCode(String code, E[] values) throws IllegalArgumentException {
			if (code == null || values == null) return null;
			for (E value : values) {
				if (value.getCode().equals(code)) {
					return value;
				}
			}
			throw new IllegalArgumentException("code " + code + " is unknown for " 
					+ values.getClass().getComponentType().getSimpleName());
		}

		/**
		 * Gives an enumaration from its code. 
		 * @param code the code to convert
		 * @param values all possible enumeration values, as a map from code to enumeration value
		 * @return an enumeration value
		 * @throws IllegalArgumentException if no such code exists
		 */
		public static <E extends Codeable> E fromCode(String code, Map<String, E> values) throws IllegalArgumentException {
			if (code == null || values == null) return null;
			if (values.containsKey(code)) {
				return values.get(code);
			}
			StringBuilder message = new StringBuilder();
			message.append("code ");
			message.append(code);
			message.append(" is unknown");
			if (values.keySet().size() > 0) {
				message.append(" for ");
				message.append(values.keySet().iterator().next().getClass().getSimpleName());
			}
			throw new IllegalArgumentException(message.toString());
		}
		
	}
	
}
