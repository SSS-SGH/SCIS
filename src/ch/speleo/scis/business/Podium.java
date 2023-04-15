package ch.speleo.scis.business;

import java.util.List;
import java.util.Locale;

import javax.persistence.TypedQuery;

import org.apache.commons.lang.text.StrBuilder;
import org.openxava.jpa.XPersistence;
import org.openxava.util.XSystem;

import ch.speleo.scis.model.karst.SpeleoObject;

public class Podium {
	
	public static final int NB_RESULT_PER_DEFAULT = 10;
	
	// utility to start for debug
	
	public static void main(String[] args) {
		XSystem._setLogLevelFromJavaLoggingLevelOfXavaPreferences();
		XPersistence.setPersistenceUnit("junit"); 
		Locale.setDefault(Locale.ENGLISH);

		Podium podium = new Podium();
		System.out.println(formatedListOfCaves(podium.getLongestCaves(20), "length"));
		System.out.println(formatedListOfCaves(podium.getDeepestCaves(20), "depthAndElevationComputed"));
	}
	private static String formatedListOfCaves(List<SpeleoObject> caves, String criteria) {
		StrBuilder str = new StrBuilder();
		int i=0;
		for (SpeleoObject cave: caves) {
			i++;
			str.appendFixedWidthPadLeft(i, String.valueOf(caves.size()).length(), '0').append(") ");
			str.append(criteria).append(" ").append(criteria=="length"?cave.getLength():criteria=="depthAndElevationComputed"?cave.getDepthAndElevationComputed():"?");
			str.append(" ").append(cave.getName());
			str.append(" <").append(cave.getSystemNr()).append(">");
			str.append("\n");
		}
		return str.toString();
	}
	
	// Business functions
	
	public List<SpeleoObject> getLongestCaves(Integer nbCaves) {
		if (nbCaves == null) {
			nbCaves = NB_RESULT_PER_DEFAULT;
		}
		StrBuilder queryStr = new StrBuilder();
		queryStr.append("from ").append(SpeleoObject.class.getSimpleName());
		queryStr.append(" where ").append("length").append(" is not null ");
		queryStr.append(" order by ").append("length").append(" desc ");
		TypedQuery<SpeleoObject> query = XPersistence.getManager().createQuery(queryStr.toString(), SpeleoObject.class);
		query.setMaxResults(nbCaves);
		return query.getResultList();
	}

	public List<SpeleoObject> getDeepestCaves(Integer nbCaves) {
		if (nbCaves == null) {
			nbCaves = NB_RESULT_PER_DEFAULT;
		}
		String depthAndElevationComputedExpr = 
				"  case when depth is not null and elevation is not null then (depth + elevation) " +
				"       when depthAndElevation is not null then depthAndElevation "+
				"       when depth is not null then depth "+
				"       else elevation end "; // in SQL super-fast, otherwise too slow
		StrBuilder queryStr = new StrBuilder();
		queryStr.append("from ").append(SpeleoObject.class.getSimpleName());
		queryStr.append(" where ").append("depthAndElevation").append(" is not null ");
		queryStr.append("    or ").append("depth").append(" is not null ");
		queryStr.append("    or ").append("elevation").append(" is not null ");
		queryStr.append(" order by ").append(depthAndElevationComputedExpr).append(" desc ");
		TypedQuery<SpeleoObject> query = XPersistence.getManager().createQuery(queryStr.toString(), SpeleoObject.class);
		query.setMaxResults(nbCaves);
		return query.getResultList();
	}
	
}
