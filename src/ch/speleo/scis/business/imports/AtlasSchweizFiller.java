package ch.speleo.scis.business.imports;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.jpa.XPersistence;
import org.openxava.util.XSystem;

import ch.speleo.scis.model.karst.AtlasSchweizExport;
import ch.speleo.scis.model.karst.GroundObject;
import ch.speleo.scis.model.karst.SpeleoObject;

public class AtlasSchweizFiller {
	
	private static final int DISTANCE_DELTA_TOLERATED = 900;
	private static final int DISTANCE_OBFUSCASION_MIN = 100;
	private static final int DISTANCE_OBFUSCASION_MAX = 600;
	private static final int ALTITUDE_DELTA_TOLERATED = 90;
	private static final int ALTITUDE_OBFUSCASION_MIN = 10;
	private static final int ALTITUDE_OBFUSCASION_MAX = 50;
	
	private static final Log logger = LogFactory.getLog(DataImporter.class);
	private EntityManager persistenceManager = XPersistence.getManager();
	private Random random = new Random();
	
	public void fill(String exportId) {
		TypedQuery<GroundObject> query = persistenceManager.createQuery(
				"SELECT o FROM GroundObject o WHERE deleted = false AND inventory_nr is not null", GroundObject.class);
		int nbGroundObjects = 0;
		List<GroundObject> groundObjects = query.getResultList();
		for (GroundObject groundObject: groundObjects) {
			if (groundObject.getCoordEast() == null || groundObject.getCoordNorth() == null) 
				continue; // ignore
			if (groundObject.getPrivacy() != null && groundObject.getPrivacy().isPrivateNow()) 
				continue; // ignore
			AtlasSchweizExport atlas = findOrCreateAtlasEntity(groundObject.getInventoryNr());
			updateAtlasEntity(atlas, groundObject);
			atlas.setExportLastId(exportId);
			nbGroundObjects++;
		}
		persistenceManager.getTransaction().commit();
		logger.info("finished filling " + nbGroundObjects + " entries");
	}
	
	private AtlasSchweizExport findOrCreateAtlasEntity(Integer inventoryNr) {
		AtlasSchweizExport atlas = persistenceManager.find(AtlasSchweizExport.class, inventoryNr);
		if (atlas == null) {
			atlas = new AtlasSchweizExport();
			atlas.setInventoryNr(inventoryNr);
			persistenceManager.persist(atlas);
		}
		return atlas;
	}
	
	private void updateAtlasEntity(AtlasSchweizExport atlas, GroundObject groundObject) {
		atlas.setName(groundObject.getName());
		atlas.setType(getAtlasType(atlas, groundObject.getSpeleoObject()));
		atlas.setLengthInfo(getAtlasLength(groundObject.getSpeleoObject()));
		atlas.setDepthInfo(getAtlasDepthAndElevation(groundObject.getSpeleoObject()));
		setAtlasCoords(atlas, groundObject);
		
	}
	
	private String getAtlasType(AtlasSchweizExport atlas, SpeleoObject speleoObject) {
		if (atlas.getType() == "BESUCH") return "BESUCH";
		if (speleoObject == null) return null;
		if (speleoObject.getType() == null) return null;
		switch (speleoObject.getType()) {
			case NATURAL_CAVE_SINGLE: 
			case NATURAL_CAVE_SYSTEM: 
				return "NATUR";
		case HUMAN_MADE_CAVE: 
			case MINES_OR_QUARRY: 
				return "KUENST";
		default: 
				return null;
		}
	}

	private String getAtlasLength(SpeleoObject speleoObject) {
		if (speleoObject == null) return null;
		if (speleoObject.getLength() == null) return "";
		int length = speleoObject.getLength();
		if (length <= 20) return "<= 20 m";
		if (length <= 200) return "<= 200 m";
		if (length <= 2000) return "<= 2 km";
		if (length <= 20000) return "<= 20 km";
		if (length <= 200000) return "<= 200 km";
		return "> 200 km";		
	}
	
	private String getAtlasDepthAndElevation(SpeleoObject speleoObject) {
		if (speleoObject == null) return null;
		if (speleoObject.getDepthAndElevationComputed() == null) return "";
		int length = speleoObject.getDepthAndElevationComputed();
		if (length <= 20) return "<= 20 m";
		if (length <= 200) return "<= 200 m";
		return "> 200 m";		
	}
	
	private void setAtlasCoords(AtlasSchweizExport atlas, GroundObject groundObject) {
		if (isCoordToUpdate(atlas.getCoordEast(), groundObject.getCoordEast(), DISTANCE_DELTA_TOLERATED)) {
			atlas.setCoordEast(getObfuscatedCoord(groundObject.getCoordEast(), DISTANCE_OBFUSCASION_MIN, DISTANCE_OBFUSCASION_MAX));
		}
		if (isCoordToUpdate(atlas.getCoordNorth(), groundObject.getCoordNorth(), DISTANCE_DELTA_TOLERATED)) {
			atlas.setCoordNorth(getObfuscatedCoord(groundObject.getCoordNorth(), DISTANCE_OBFUSCASION_MIN, DISTANCE_OBFUSCASION_MAX));
		}
		if (isCoordToUpdate(atlas.getCoordAltitude(), groundObject.getCoordAltitude(), ALTITUDE_DELTA_TOLERATED)) {
			atlas.setCoordAltitude(getObfuscatedCoord(groundObject.getCoordAltitude(), ALTITUDE_OBFUSCASION_MIN,  ALTITUDE_OBFUSCASION_MAX));
		}
	}
	private boolean isCoordToUpdate(Integer altasCoord, Integer scisCoord, int toleratedDelta) {
		if (altasCoord == null && scisCoord == null) return false;
		if (altasCoord == null || scisCoord == null) return true;
		Integer scisCoordLv03 = scisCoord < 1000000 ? scisCoord : scisCoord % 1000000;
		return Math.abs(altasCoord - scisCoordLv03) > toleratedDelta;
	}
	private Integer getObfuscatedCoord(Integer scisCoord, int minObfuscation, int maxObsucation) {
		if (scisCoord == null) return null;
		Integer scisCoordLv03 = scisCoord < 1000000 ? scisCoord : scisCoord % 1000000;
		return scisCoordLv03 + getObfuscation(minObfuscation, maxObsucation);
	}
	private int getObfuscation(int min, int max) {
		int extend = max - min;
		int delta = random.nextInt(2 * extend) - extend;
		return (delta < 0) ? delta - min : delta + min;
	}

	public static void main(String[] args) {
		XSystem._setLogLevelFromJavaLoggingLevelOfXavaPreferences();
		XPersistence.setPersistenceUnit("junit"); 
		Locale.setDefault(Locale.ENGLISH);
		new AtlasSchweizFiller().fill("2021-04-05_export-B");
		System.exit(0);
	}
}
