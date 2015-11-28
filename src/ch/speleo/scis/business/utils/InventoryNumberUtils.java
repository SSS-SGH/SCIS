package ch.speleo.scis.business.utils;

import javax.persistence.Query;

import org.apache.commons.lang.text.StrBuilder;
import org.openxava.calculators.ICalculator;
import org.openxava.jpa.XPersistence;

import ch.speleo.scis.model.karst.GroundObject;

/**
 * Utilities about the Inventory Number of an entrance (or other object on the surface). 
 * 
 * @author florian
 */
public class InventoryNumberUtils {

	// the swiss is divided in 3 parts, each having its own range of inventory number. 
	
	public final static Range PART_WEST = new Range(10000, 19999);
	public final static Range PART_CENTER = new Range(20000, 29999);
	public final static Range PART_EAST = new Range(30000, 39999);
	
	protected final static String CLASS_NAME= GroundObject.class.getSimpleName();
	protected final static String FIELD_NAME= "inventoryNr";
	protected static String queryStr;
	
	static {
		StrBuilder queryStrB = new StrBuilder();
		queryStrB.append(" select max(").append(FIELD_NAME).append(") + 1");
		queryStrB.append(" from ").append(CLASS_NAME);
		queryStrB.append(" where ").append(FIELD_NAME).append(" between ? and ?");
		queryStr = queryStrB.toString();
	}
	
	/**
	 * Gives the next unused number within a range. 
	 */
	public static int getNextUnusedNumber(Range range) {
		Query query = XPersistence.getManager().createQuery(queryStr);
		query.setParameter(1, range.getMin());
		query.setParameter(2, range.getMax());
		Object result = query.getSingleResult();
		if (result == null) 
			return range.getMin();
		else
			return (Integer) result;
	}
	
	/**
	 * Gives the next unused number within each part. 
	 */
	public static int[] getNextUnusedNumbers() {
		int nbWest = getNextUnusedNumber(PART_WEST);
		int nbCenter = getNextUnusedNumber(PART_CENTER);
		int nbEast = getNextUnusedNumber(PART_EAST);
		return new int[]{nbWest, nbCenter, nbEast};
	}
	
	/**
	 * Gives the next unused number within each parts as a human-readable String. 
	 */
	public static String getNextUnusedNumbersAsString() {
		StrBuilder sb = new StrBuilder();
		for (int nb: getNextUnusedNumbers()) {
			sb.appendSeparator(", ").append(nb);
		}
		return sb.toString();
	}
	
	/**
	 * Calculator for OpenXava that gives the next unused numbers. 
	 */
	public static class NextNumbersCalculator implements ICalculator {

		private static final long serialVersionUID = -8877169609154967963L;

		public String calculate() throws Exception {
			return getNextUnusedNumbersAsString();
		}

	}

}
