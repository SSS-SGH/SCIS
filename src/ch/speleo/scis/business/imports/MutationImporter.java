package ch.speleo.scis.business.imports;

import java.util.Locale;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Users;
import org.openxava.util.XSystem;

import ch.speleo.scis.business.imports.ReaderHelper.Report;

/**
 * Runnable class used for mutating data into the database from files.
 */
public class MutationImporter {
	
	private static final String importFolder = "../../../massmutations/";

	private static final Log logger = LogFactory.getLog(MutationImporter.class);
	static {		
		XSystem._setLogLevelFromJavaLoggingLevelOfXavaPreferences();
		XPersistence.setPersistenceUnit("remote"); // "junit" for dev, "remote" for test and prod
		// for test and prod, a SSH tunnel is required: ssh -L 55432:localhost:5432 sgharchiv@sgharchiv.ogh.ch
		Locale.setDefault(Locale.ENGLISH);
	}
	
    /**
     * @param args input arguments.
     */
	public static void main(String[] args) {
        try {

        	Users.setCurrent("import"); // telling who we are, used for the audit / historisation
                        
            EntitiesReader importer = new EntitiesReader();
            importer.helper.setBadFile(importFolder+"badimported.txt");
            
            importer.read(new ExcelReader(importFolder+"ModifOberflächenobjekte.xls", 1), 
            		new EntranceCoordsConverter(), true);
            importer.read(new ExcelReader(importFolder+"ModifUntergrundobjekte.xls", 1), 
            		new SpeleoObjectLengthDepthConverter(), true);
            
             // reporting problems
            if (! importer.helper.missingInitials.isEmpty()) {
	            logger.warn(new StrBuilder("missing ")
	            		.append(importer.helper.missingInitials.size()).append(" Karstologists: \n" )
						.appendWithSeparators(importer.helper.missingInitials, "\n").toString());
            }
            logger.info(importer.helper.getErrors().size() + " errors and " + importer.helper.getWarnings().size() + " warnings");
            for (Report warning: importer.helper.getWarnings()) {
            	logger.warn(warning.toString());
            }
            for (Report error: importer.helper.getErrors()) {
            	logger.warn(error.toString());
            }
            for (Report summary: importer.helper.getSummaries()) {
            	logger.info(summary.toString());
            }
            logger.info("total: "+importer.helper.getErrors().size() + " errors and " + importer.helper.getWarnings().size() + " warnings");

            importer.helper.closeBadFile();

        } catch (Exception e) {
        	logger.error("error when importing", e);
        }
        
    }
        
}
