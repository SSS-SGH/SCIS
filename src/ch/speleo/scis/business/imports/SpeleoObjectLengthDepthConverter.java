package ch.speleo.scis.business.imports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.business.imports.ReaderHelper.ConversionResult;
import ch.speleo.scis.model.karst.SpeleoObject;

/**
 * Converter for a speleo object as in the old FileMaker database. 
 */
public class SpeleoObjectLengthDepthConverter
extends EntityConverter<SpeleoObject> {

	private static final Log logger = LogFactory.getLog(SpeleoObjectLengthDepthConverter.class);

    /**
     * Add or update the length and depth of a speleo object.
     */
    @Override
    public SpeleoObject getEntity(String[] row) throws Exception {
    	Integer systemNr = helper.toInteger(row[0]);
    	SpeleoObject speleoObject = SpeleoObject.getBySystemNr(systemNr);
        // depth and elevation
    	if (row.length > 2) {
	    	ConversionResult<Integer> depthResult = helper.toIntegerFlexible(row[2]);
	    	if (depthResult.getResult() != null) {
	    		if (depthResult.getMessage() != null)
	    			helper.warn(SpeleoObject.class, getIdentifier(speleoObject),
	    					"depthAndElevation", depthResult.getMessage() + " for '"+row[2]+"'");
	            if (depthResult.getResult() < 0) {
	            	logger.debug("depthAndElevation < 0 for SpeleoObject "+speleoObject.getSystemNr()+", moving to depth");
	            	speleoObject.setDepth(-speleoObject.getDepthAndElevation());
	        		speleoObject.setDepthAndElevation(null);
	            } else {
	        		speleoObject.setDepthAndElevation(depthResult.getResult());
	            	speleoObject.setDepth(null);
	            	speleoObject.setElevation(null);
	            }
	    	} else {
	    		if (depthResult.getMessage() != null)
	            	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
	            			"depthAndElevation", depthResult.getMessage() + " for '"+row[2]+"'");
	    	}
    	}
        // length
    	if (row.length > 2) {
	    	ConversionResult<Integer> lengthResult = helper.toIntegerFlexible(row[1]);
	    	if (lengthResult.getResult() != null) {
	    		if (lengthResult.getMessage() != null)
	    			helper.warn(SpeleoObject.class, getIdentifier(speleoObject),
	    					"length", lengthResult.getMessage() + " for '"+row[1]+"'");
	    		if (lengthResult.getResult() < 0) {
	            	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
	    					"length", "number cannot be negative but is '"+row[1]+"', will be let unmodified");
	    		} else {
	        		speleoObject.setLength(lengthResult.getResult());
	    		}
	    	} else {
	    		if (lengthResult.getMessage() != null)
	            	helper.error(SpeleoObject.class, getIdentifier(speleoObject), 
	    					"length", lengthResult.getMessage() + " for '"+row[1]+"'");
	    	}
    	}
        return speleoObject;
    }

    public String getIdentifier(SpeleoObject speleoObject) {
    	return "systemNr="+speleoObject.getSystemNr()+", name="+speleoObject.getName();
    }
    
}
