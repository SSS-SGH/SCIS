package ch.speleo.scis.business.imports;

import java.text.ParseException;

import javax.persistence.NoResultException;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.business.imports.ReaderHelper.ConversionResult;
import ch.speleo.scis.model.common.Karstologist;
import ch.speleo.scis.model.karst.GroundObject;
import ch.speleo.scis.model.karst.KarstObjectDocument;

/**
 * Converter for a document of a karst object as in the old FileMaker database. 
 */
public class KarstObjectDocumentConverter
extends EntityConverter<KarstObjectDocument> {

    /**
     * {@inheritDoc}
     */
    @Override
    public KarstObjectDocument getEntity(String[] row) throws Exception {
    	KarstObjectDocument document = new KarstObjectDocument();
        try {
        	document.setObject(helper.toGroundObject(row[0]));
        } catch (NoResultException e) {
        	helper.error(KarstObjectDocument.class, row[0], 
        			"object", "unknown ground object with inventoryNr '"+row[0]+"', cannot import");
        	return null;
        }
        try {
        	document.setTransmissionDate(helper.toDate(row[1]));
        } catch (ParseException e) {
        	helper.error(KarstObjectDocument.class, row[0], 
        			"transmissionDate", "unparsable date '"+row[1]+"', will be let empty");
        }
        ConversionResult<Karstologist> karstologistResult = helper.toKarstologist(row[2]);
        if (karstologistResult.getResult() != null) {
        	document.setContact(karstologistResult.getResult());
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, row[0], 
    					"contact", karstologistResult.getMessage());
        	}
        } else {
        	if (karstologistResult.getMessage() != null) {
            	helper.error(GroundObject.class, row[0], 
    					"contact", karstologistResult.getMessage());
        	}
        }
    	document.setRolledMap(helper.toBoolean(row[3]));
    	document.setSuspensionFolder(helper.toBoolean(row[4]));
    	document.setRemarks(row[5]);
    	
        return document;
    }

}
