package ch.speleo.scis.business.imports;

import java.text.ParseException;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.business.imports.ReaderHelper.ConversionResult;
import ch.speleo.scis.business.utils.SwissCoordsUtils;
import ch.speleo.scis.model.common.Karstologist;
import ch.speleo.scis.model.karst.GroundObject;

/**
 * Converter for an entrance as in the old FileMaker database. 
 */
public class EntranceConverter
extends EntityConverter<GroundObject> {
    
	private static final Log logger = LogFactory.getLog(EntranceConverter.class);

    /**
     * Convert an entrance from a row with all the fields from FileMaker
     */
    @Override
    public GroundObject getEntity(String[] row) throws Exception {
    	GroundObject entrance = new GroundObject();
        entrance.setVerified(false);
        entrance.setInventoryNr(helper.toInteger(row[20]));
        if (!helper.isBlank(row[7])) {
            entrance.setName(row[7]);
        } else {
            entrance.setName("?");
        	helper.error(GroundObject.class, getIdentifier(entrance), 
        			"name", "empty name, will be set to '?', please fill in");
        }
        entrance.setCantonBaron(row[1]);
        try {
        	entrance.setCommuneBaronNr(helper.toInteger(row[2]));
        } catch (NumberFormatException e) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
        			"communeBaronNr", "unrecognize number '"+row[2]+"' for commune number, will be let empty");
        }
        try {
        	entrance.setCaveBaronNr(helper.toInteger(row[3]));
        } catch (NumberFormatException e) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
        			"caveBaronNr", "unrecognize number '"+row[3]+"' for baron number, will be let empty");
        }
        if (entrance.getCantonBaron() != null && entrance.getCommuneBaronNr() != null && entrance.getCaveBaronNr() != null) {
	        try {
	        	GroundObject.getByBaronNr(entrance.getCantonBaron(), entrance.getCommuneBaronNr(), entrance.getCaveBaronNr());
	        	// expected is a NoResultException, otherwise there is a duplicate
	        	helper.error(GroundObject.class, getIdentifier(entrance), 
	        			"caveBaronNr", "already existing baron number '"+entrance.getCantonBaron()+" "+entrance.getCommuneBaronNr()+"/"+entrance.getCaveBaronNr()+"', will be let empty");
	        	entrance.setCaveBaronNr(null);
	        } catch (NoResultException e) { /* that's right */ }
        }
        ConversionResult<Karstologist> karstologistResult = helper.toKarstologist(row[4]);
        if (karstologistResult.getResult() != null) {
            entrance.setManager(karstologistResult.getResult());
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"manager", karstologistResult.getMessage());
        	}
        } else {
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"manager", karstologistResult.getMessage());
        	}
        }
        try {
        	entrance.setType(helper.toGroundObjectTypeEnum(row[6]));
        } catch (IllegalArgumentException e) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
					"type", e.getMessage());
        }
        // coordinates
    	ConversionResult<Integer> coordEastResult = helper.toCoordinate(row[8]);
    	if (coordEastResult.getResult() != null) {
    		entrance.setCoordEast(coordEastResult.getResult());
    		if (coordEastResult.getMessage() != null)
    			helper.warn(GroundObject.class, getIdentifier(entrance),
    					"coordinate", coordEastResult.getMessage());
    	} else {
    		if (coordEastResult.getMessage() != null)
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"coordinate", coordEastResult.getMessage());
    	}
    	ConversionResult<Integer> coordNorthResult = helper.toCoordinate(row[9]);
    	if (coordNorthResult.getResult() != null) {
    		entrance.setCoordNorth(coordNorthResult.getResult());
    		if (coordNorthResult.getMessage() != null)
    			helper.warn(GroundObject.class, getIdentifier(entrance),
    					"coordinate", coordNorthResult.getMessage());
    	} else {
    		if (coordNorthResult.getMessage() != null)
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"coordinate", coordNorthResult.getMessage());
    	}
    	if ( (  // east sounds like north, north doesn't sound like north
    			entrance.getCoordEast() != null && SwissCoordsUtils.NORTH.contains(entrance.getCoordEast()) &&
    			(entrance.getCoordNorth() == null || !SwissCoordsUtils.NORTH.contains(entrance.getCoordNorth()))
    	   ) || ( // north sounds like east, east doesn't sound like eath
    			entrance.getCoordNorth() != null && SwissCoordsUtils.EAST.contains(entrance.getCoordNorth()) &&
    			(entrance.getCoordEast() == null || !SwissCoordsUtils.EAST.contains(entrance.getCoordEast()))
    	   ) ) {
        	logger.debug("inverted East and North coords for Entrance "+entrance.getInventoryNr()+", will be swapped");
        	//helper.warn(Entrance.class, getIdentifier(entrance), 
        	//		"coordinate", "East coord < North coord, will be swapped");
        	Integer coordNorthCorrected = entrance.getCoordEast();
        	entrance.setCoordEast(entrance.getCoordNorth());
        	entrance.setCoordNorth(coordNorthCorrected);
    	}
        if (entrance.getCoordEast() != null && !SwissCoordsUtils.EAST.contains(entrance.getCoordEast())) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
					"coordinate", "invalid east coordinate "+entrance.getCoordEast()+", will be let empty");
        	entrance.setCoordEast(null);
        }
        if (entrance.getCoordNorth() != null && !SwissCoordsUtils.NORTH.contains(entrance.getCoordNorth())) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
					"coordinate", "invalid north coordinate "+entrance.getCoordNorth()+", will be let empty");
        	entrance.setCoordNorth(null);
        }
    	ConversionResult<Integer> coordAltitudeResult = helper.toCoordinate(row[10]);
    	if (coordAltitudeResult.getResult() != null) {
    		entrance.setCoordAltitude(coordAltitudeResult.getResult());
    		if (coordAltitudeResult.getMessage() != null)
    			helper.warn(GroundObject.class, getIdentifier(entrance),
    					"altitude", coordAltitudeResult.getMessage());
    	} else {
    		if (coordAltitudeResult.getMessage() != null)
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"altitude", coordAltitudeResult.getMessage());
    	}
        if (entrance.getCoordAltitude() != null && !SwissCoordsUtils.ALTITUDE.contains(entrance.getCoordAltitude())) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
					"altitude", "invalid altitude "+entrance.getCoordAltitude()+", will be let empty");
        	entrance.setCoordAltitude(null);
        }
        // location accuracy
        try {
        	entrance.setLocationAccuracy(helper.toLocationAccuracyEnum(row[13]));
        } catch (IllegalArgumentException e) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
        			"locationAccuracy", "unrecognize location accuracy '"+row[13]+"', will be let empty");
        }
        entrance.setLiterature(row[15]);
        try {
        	entrance.setCreationDate(helper.toDate(row[17]));
        } catch (ParseException e) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
        			"creationDate", "unparsable date '"+row[17]+"', will be let empty");
        }
        entrance.setDeleted(helper.toBoolean(row[18]));
        try {
        	entrance.setSpeleoObject(helper.toSpeleoObject(row[21]));
        } catch (NoResultException e) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
        			"speleoObject", "unknown cave with systemNr '"+row[21]+"', will be let empty");
        }
        entrance.setDataHistory(row[23]);
        entrance.setComment(row[25]);
        try {
	        entrance.setPrivacy(helper.toPrivacy(row[27], 
	                                             row[28],
	                                             row[29],
	                                             row[30] ));
        } catch (ParseException e) {
        	helper.error(GroundObject.class, getIdentifier(entrance), 
        			"speleoObject", "unparsable privacy ("+e+"), will be let empty");
        }
		if (entrance.getPrivacy() != null && entrance.getPrivacy().getStartDate() == null) {
			entrance.getPrivacy().setStartDate(entrance.getCreationDate());
		}
		if (row.length >= 35) {
			try {
				entrance.setCommune(helper.toCommune(row[34]));
			} catch (NoResultException e) {
				helper.error(GroundObject.class, getIdentifier(entrance), 
	        			"commune", "unknown commune '"+row[34]+"', will be let empty");
			}
		}
    	return entrance;
    }
    
    public String getIdentifier(GroundObject entrance) {
    	return "inventoryNr="+entrance.getInventoryNr()+", name="+entrance.getName();
    }

    @Override
    public boolean check(GroundObject entrance) {
    	boolean valid = super.check(entrance);
    	if (entrance.getInventoryNr() == null) {
    		helper.warn(GroundObject.class, getIdentifier(entrance), 
        			"inventoryNr", "missing nr, will be let empty");
    	} else {
	    	try {
	    		GroundObject existing = GroundObject.getByInventoryNr(entrance.getInventoryNr());
	    		helper.error(GroundObject.class, getIdentifier(entrance), 
	        			"inventoryNr", "already existing nr for entrance named '"+existing.getName()+"', cannot import");
	    		valid = false;
	    	} catch (NoResultException exception) {
	    		; // expected
	    	}
    	}
    	return valid;
    }

}
