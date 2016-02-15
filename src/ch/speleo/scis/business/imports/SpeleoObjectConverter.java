package ch.speleo.scis.business.imports;

import java.text.ParseException;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.business.imports.ReaderHelper.ConversionResult;
import ch.speleo.scis.model.common.Karstologist;
import ch.speleo.scis.model.karst.GroundObject;
import ch.speleo.scis.model.karst.SpeleoObject;

/**
 * Converter for a speleo object as in the old FileMaker database. 
 */
public class SpeleoObjectConverter
extends EntityConverter<SpeleoObject> {

	private static final Log logger = LogFactory.getLog(SpeleoObjectConverter.class);

    /**
     * Convert a speleo object from a row with all the fields from FileMaker. 
     * Rows for secondary entrances are ignored and {@code null} is returned.
     */
    @Override
    public SpeleoObject getEntity(String[] row) throws Exception {
        Integer systemNr = helper.toInteger(row[21]);
        Integer inventoryNr = helper.toInteger(row[20]);
        if (systemNr != null && !systemNr.equals(inventoryNr))
    		return null; // ignored because secondary entrance
        SpeleoObject speleoObject = new SpeleoObject();
        speleoObject.setVerified(false);
        speleoObject.setSystemNr(systemNr);
        if (!helper.isBlank(row[7])) {
        	speleoObject.setName(row[7]);
        } else {
        	speleoObject.setName("?");
        	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
        			"name", "empty name, will be set to '?', please fill in");
        }
        ConversionResult<Karstologist> karstologistResult = helper.toKarstologist(row[4]);
        if (karstologistResult.getResult() != null) {
        	speleoObject.setManager(karstologistResult.getResult());
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, getIdentifier(speleoObject), 
    					"manager", karstologistResult.getMessage());
        	}
        } else {
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, getIdentifier(speleoObject), 
    					"manager", karstologistResult.getMessage());
        	}
        }
        try {
        	speleoObject.setType(helper.toSpeleoObjectTypeEnum(row[5]));
        } catch (IllegalArgumentException e) {
        	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
        			"type", "unrecognize cave type '"+row[5]+"', will be let empty");
        }
        // depth and elevation
        //speleoObjectDB.setElevation(new Integer(row[11].getContents())); // info not available
        //speleoObjectDB.setDepth(new Integer(row[11].getContents())); // info not available
    	ConversionResult<Integer> depthResult = helper.toIntegerFlexible(row[11]);
    	if (depthResult.getResult() != null) {
    		speleoObject.setDepthAndElevation(depthResult.getResult());
    		if (depthResult.getMessage() != null)
    			helper.warn(SpeleoObject.class, getIdentifier(speleoObject),
    					"depthAndElevation", depthResult.getMessage() + " for '"+row[11]+"'");
    	} else {
    		if (depthResult.getMessage() != null)
            	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
            			"depthAndElevation", depthResult.getMessage() + " for '"+row[11]+"'");
    	}
        if (speleoObject.getDepthAndElevation() != null && speleoObject.getDepthAndElevation() < 0) {
        	logger.debug("depthAndElevation < 0 for SpeleoObject "+speleoObject.getSystemNr()+", moving to depth");
        	//helper.warn(SpeleoObject.class, "systemNr="+speleoObject.getSystemNr(), 
        	//		"depthAndElevation < 0, moving to depth");
        	speleoObject.setDepth(-speleoObject.getDepthAndElevation());
        	speleoObject.setDepthAndElevation(null);
        }
        // length
    	ConversionResult<Integer> lengthResult = helper.toIntegerFlexible(row[12]);
    	if (lengthResult.getResult() != null) {
    		speleoObject.setLength(lengthResult.getResult());
    		if (lengthResult.getMessage() != null)
    			helper.warn(SpeleoObject.class, getIdentifier(speleoObject),
    					"length", lengthResult.getMessage() + " for '"+row[12]+"'");
    		if (speleoObject.getLength() < 0) {
    			speleoObject.setLength(null);
            	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
    					"length", "number cannot be negative but is '"+row[12]+"', will be let empty");
    		}
    	} else {
    		if (lengthResult.getMessage() != null)
            	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
    					"length", lengthResult.getMessage() + " for '"+row[12]+"'");
    	}
        // documentation state
        try {
        	speleoObject.setDocumentationState(helper.toDocumentationState(row[14]));
        } catch (IllegalArgumentException e) {
        	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
        			"documentationState", "unrecognize documentation state '"+row[14]+"', will be let empty");
        }
        //speleoObject.setLiterature(row[15]);
        try {
        	speleoObject.setCreationDate(helper.toDate(row[17]));
        } catch (ParseException e) {
        	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
        			"creationDate", "unparsable date '"+row[17]+"', will be let empty");
        }
        speleoObject.setDeleted(helper.toBoolean(row[18]));
        //speleoObject.setDataHistory(row[23]);
        //speleoObject.setComment(row[25]);
        /*speleoObject.setPrivacy(helper.toPrivacy(row[27], 
        		                                 row[28],
        		                                 row[29],
        		                                 row[30] ));
        if (speleoObject.getPrivacy() != null && speleoObject.getPrivacy().getStartDate() == null) {
        	speleoObject.getPrivacy().setStartDate(speleoObject.getCreationDate());
        }*/
        return speleoObject;
    }

    public String getIdentifier(SpeleoObject speleoObject) {
    	return "systemNr="+speleoObject.getSystemNr()+", name="+speleoObject.getName();
    }
    
    @Override
    public boolean check(SpeleoObject speleoObject) {
    	boolean valid = super.check(speleoObject);
    	if (speleoObject.getSystemNr() == null) {
    		helper.warn(SpeleoObject.class, getIdentifier(speleoObject), 
        			"systemNr", "missing nr, will be let empty");
    	} else {
	    	try {
	    		SpeleoObject existing = SpeleoObject.getBySystemNr(speleoObject.getSystemNr());
	    		helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
	        			"systemNr", "already existing nr for object nammed '"+existing.getName()+"', cannot import");
	    		valid = false;
	    	} catch (NoResultException exception) {
	    		; // expected
	    	}
    	}
    	return valid;
    }

}
