package ch.speleo.scis.business.imports;

import ch.speleo.scis.business.imports.EntitiesReader.EntityConverter;
import ch.speleo.scis.model.common.Commune;

/**
 */
public class CommuneConverter
extends EntityConverter<Commune> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Commune getEntity(String[] row) {
        Commune commune = new Commune();
        //commune.setFsoNr(Integer.valueOf(row[0].getContents()));
        //commune.setName(row[1].getContents());
        //commune.setDistrict(row[2].getContents());
        //commune.setCanton(row[3].getContents());
        //commune.setBaronNr(row[4].getContents());
        
        commune.setFsoNr(Integer.valueOf(row[2]));
        commune.setName(row[1]);
        commune.setDistrict(row[3]);
        commune.setCanton(row[4]);
        commune.setBaronNr(row[0]);
        return commune;
    }
    
}
