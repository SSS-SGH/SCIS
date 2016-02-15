package ch.speleo.scis.business.imports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.business.imports.ReaderHelper.ConversionResult;
import ch.speleo.scis.model.common.Karstologist;
import ch.speleo.scis.model.karst.GroundObject;
import ch.speleo.scis.model.karst.GroundObjectTypeEnum;

/**
 * Converter for a ground object other than an entrance as in the old FileMaker database. 
 */
public class GroundObjectConverter
extends EntityConverter<GroundObject> {

	private static final Log logger = LogFactory.getLog(GroundObjectConverter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public GroundObject getEntity(String[] row) throws Exception {
        GroundObject groundObject = new GroundObject();
        groundObject.setVerified(false);
        if (row[0] != null && row[0].length() > 8) {
         	helper.error(GroundObject.class, "inventoryNr="+row[0]+", name="+row[1], 
        			"inventoryNr", "Inventory Nr "+row[0]+" is too big, will be let empty");
        } else {
        	groundObject.setInventoryNr(helper.toInteger(row[0]));
        }
        groundObject.setName(row[1]);
        groundObject.setType(GroundObjectTypeEnum.fromCode(row[2]));
        ConversionResult<Karstologist> karstologistResult = helper.toKarstologist(row[3]);
        if (karstologistResult.getResult() != null) {
        	groundObject.setManager(karstologistResult.getResult());
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, getIdentifier(groundObject), 
    					"manager", karstologistResult.getMessage());
        	}
        } else {
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, getIdentifier(groundObject), 
    					"manager", karstologistResult.getMessage());
        	}
        }
        groundObject.setComment(row[4]);
        groundObject.setCoordEast(helper.toInteger(row[5]));
        groundObject.setCoordNorth(helper.toInteger(row[6]));
        if (groundObject.getCoordEast() != null && groundObject.getCoordNorth() != null &&
        		groundObject.getCoordEast() < groundObject.getCoordNorth()) {
        	logger.debug("East coord < North coord for GroundObject "+groundObject.getInventoryNr()+", will be swapped");
        	//helper.warn(GroundObject.class, "inventoryNr="+groundObject.getInventoryNr()+", name="+groundObject.getName(), 
        	//		"East coord < North coord, will be swapped");
        	Integer coordNorthCorrected = groundObject.getCoordEast();
        	groundObject.setCoordEast(groundObject.getCoordNorth());
        	groundObject.setCoordNorth(coordNorthCorrected);
        }
        groundObject.setCoordAltitude(helper.toInteger(row[7]));
        groundObject.setLocationAccuracy(helper.toLocationAccuracyEnum(row[8]));
        //groundObject.setDocumentationState(toDocumentationState(row[].getContents()));
        /*try {
            PrivacyDB protection = new PrivacyDB();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                protection.setStart(format.parse(row[10].getContents()));
                protection.setEnd(format.parse(row[11].getContents()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            protection.setReason(row[12].getContents());
            try {
                protection.setProtector(
                        super.getKarstologistDAO().getById(row[13].getContents()));
            } catch (ArrayIndexOutOfBoundsException e) { }
            groundObject.setPrivacy(protection);
        } catch (ArrayIndexOutOfBoundsException e) { }*/
        return groundObject;
    }

    public String getIdentifier(GroundObject groundObject) {
    	return "inventoryNr="+groundObject.getInventoryNr()+", name="+groundObject.getName();
    }

}
