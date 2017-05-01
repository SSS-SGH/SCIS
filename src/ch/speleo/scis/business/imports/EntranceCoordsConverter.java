package ch.speleo.scis.business.imports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.business.imports.ReaderHelper.ConversionResult;
import ch.speleo.scis.business.utils.SwissCoordsUtils;
import ch.speleo.scis.model.karst.GroundObject;

/**
 * Converter for an entrance as in the old FileMaker database. 
 */
public class EntranceCoordsConverter
extends EntityConverter<GroundObject> {
    
	private static final Log logger = LogFactory.getLog(EntranceCoordsConverter.class);

    /**
     * Add or update the coordinates to an entrance
     */
    @Override
    public GroundObject getEntity(String[] row) throws Exception {
    	Integer inventoryNr = helper.toInteger(row[0]);
    	GroundObject entrance = GroundObject.getByInventoryNr(inventoryNr);
        // coordinates
    	ConversionResult<Integer> coordEastResult = helper.toCoordinate(row[2]);
    	if (coordEastResult.getResult() != null) {
    		if (coordEastResult.getMessage() != null)
    			helper.warn(GroundObject.class, getIdentifier(entrance),
    					"coordinate", coordEastResult.getMessage());
    	} else {
    		if (coordEastResult.getMessage() != null)
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"coordinate", coordEastResult.getMessage());
    	}
    	ConversionResult<Integer> coordNorthResult = helper.toCoordinate(row[3]);
    	if (coordNorthResult.getResult() != null) {
    		if (coordNorthResult.getMessage() != null)
    			helper.warn(GroundObject.class, getIdentifier(entrance),
    					"coordinate", coordNorthResult.getMessage());
    	} else {
    		if (coordNorthResult.getMessage() != null)
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"coordinate", coordNorthResult.getMessage());
    	}
    	if ( (  // east sounds like north, north doesn't sound like north
    			coordEastResult.getResult() != null && SwissCoordsUtils.NORTH.contains(coordEastResult.getResult()) &&
    			(coordNorthResult.getResult() == null || !SwissCoordsUtils.NORTH.contains(coordNorthResult.getResult()))
    	   ) || ( // north sounds like east, east doesn't sound like east
    			   coordNorthResult.getResult() != null && SwissCoordsUtils.EAST.contains(coordNorthResult.getResult()) &&
    			(coordEastResult.getResult() == null || !SwissCoordsUtils.EAST.contains(coordEastResult.getResult()))
    	   ) ) {
        	logger.debug("inverted East and North coords for Entrance "+entrance.getInventoryNr()+", will be swapped");
        	ConversionResult<Integer> coordNorthCorrected = coordEastResult;
        	coordEastResult = coordNorthResult;
        	coordNorthResult = coordNorthCorrected;
    	}
        if (coordEastResult.getResult() != null) {
        	if (!SwissCoordsUtils.EAST.contains(coordEastResult.getResult())) 
	        	helper.error(GroundObject.class, getIdentifier(entrance), 
						"coordinate", "invalid east coordinate "+coordEastResult.getResult()+", will be let unmodified");
        	else 
        		entrance.setCoordEast(coordEastResult.getResult());
        }
        if (coordNorthResult.getResult() != null) {
        	if (!SwissCoordsUtils.NORTH.contains(coordNorthResult.getResult())) 
        		helper.error(GroundObject.class, getIdentifier(entrance), 
        				"coordinate", "invalid north coordinate "+coordNorthResult.getResult()+", will be let unmodified");
        	else 
        		entrance.setCoordNorth(coordNorthResult.getResult());
        }
    	ConversionResult<Integer> coordAltitudeResult = helper.toCoordinate(row[4]);
    	if (coordAltitudeResult.getResult() != null) {
    		if (coordAltitudeResult.getMessage() != null)
    			helper.warn(GroundObject.class, getIdentifier(entrance),
    					"altitude", coordAltitudeResult.getMessage());
            if (!SwissCoordsUtils.ALTITUDE.contains(coordAltitudeResult.getResult())) {
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"altitude", "invalid altitude "+coordAltitudeResult.getResult()+", will be let unmodified");
            } else {
        		entrance.setCoordAltitude(coordAltitudeResult.getResult());
            }
    	} else {
    		if (coordAltitudeResult.getMessage() != null)
            	helper.error(GroundObject.class, getIdentifier(entrance), 
    					"altitude", coordAltitudeResult.getMessage());
    	}
    	return entrance;
    }
    
    public String getIdentifier(GroundObject entrance) {
    	return "inventoryNr="+entrance.getInventoryNr()+", name="+entrance.getName();
    }

}
