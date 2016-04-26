package ch.speleo.scis.business.imports;

import javax.persistence.Column;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.model.common.Karstologist;

/**
 */
public class KarstologistConverter
extends EntityConverter<Karstologist> {

	/**
     * {@inheritDoc}
     */
    @Override
    public Karstologist getEntity(String[] row) {
        Karstologist karstologist = new Karstologist();
        karstologist.setInitials(row[0]);
        karstologist.setLastname(row[1]);
        karstologist.setFirstname(row[2]);
        karstologist.setClub(row[3]);
        if (row.length >= 5) {
            karstologist.setComment(row[4]);
        }
        karstologist.setDeleted(false);
        return karstologist;
    }

    public String getIdentifier(Karstologist karstologist) {
    	return "initials="+karstologist.getInitials();
    }
    
    @Override
    public boolean check(Karstologist karstologist) {
    	boolean valid = super.check(karstologist);
    	try {
    		int initialsLength = Karstologist.class.getDeclaredField("initials").getAnnotation(Column.class).length();
    		if (karstologist.getInitials() != null && karstologist.getInitials().length() > initialsLength) {
        		helper.error(Karstologist.class, getIdentifier(karstologist), 
            			"initials", "too long value '"+karstologist.getInitials()+"', cannot import");
        		valid = false;
    		}
    	} catch (NoSuchFieldException exception) {
    		throw new RuntimeException("unable to perform validation of "+karstologist.toString(), exception);
    	} catch (NullPointerException exception) {
    		throw new RuntimeException("unable to perform validation of "+karstologist.toString(), exception);
    	}
    	return valid;
    }

}
