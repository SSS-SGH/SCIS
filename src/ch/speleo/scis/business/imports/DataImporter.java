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
 * Runnable class used for importing data into the database from files.
 */
public class DataImporter {
	
	private static final String importFolder = "../../../filemaker_old/";

	private static final Log logger = LogFactory.getLog(DataImporter.class);
	static {		
		XSystem._setLogLevelFromJavaLoggingLevelOfXavaPreferences();
		XPersistence.setPersistenceUnit("junit"); // "junit" for dev, "remote" for test and prod
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
            
            // communes having a normal baron number (without x)
            //importer.read(new ExcelReader(importFolder+"communes_nx.xls"), new CommuneConverter(), true);
            // communes having a baron number with x (deleted or merged communes); should fail cause of duplicated FSO Nr
            ////importer.read(new ExcelReader(importFolder+"communes_x.xls"), new CommuneConverter(), true);

			// karstologists
            //importer.read(new ExcelReader(importFolder+"karstologists_praezis.xls"), new KarstologistConverter(), true);
            //importer.read(new ExcelReader(importFolder+"karstologists_jd_new.xls", 2), new KarstologistConverter(), true);

			// ground objects; problem with too big inventory nr, see with PH if relevant
            //importer.read(new ExcelReader(importFolder+"ground_objects.xls"), new GroundObjectConverter(), true);

			// caves, trial with old data
            //importer.read(new TabulatedTextReader(importFolder+"caves_all.txt", "MacRoman"), 
            //		new SpeleoObjectConverter(), true);
            // caves, Ostschweiz
            //importer.read(new TabulatedTextReader(importFolder+"inventar_ch_ost_20140226_.tab", "MacRoman"), 
            //		new SpeleoObjectConverter(), true);
            // caves, Zentralschweiz
            //importer.read(new ExcelReader(importFolder+"Cave_Zentral_2015-04-13.xls"), 
            //		new SpeleoObjectConverter(), true);
            //importer.read(new ExcelReader(importFolder+"Cavites_VD_VS_FR_BE_export.xls", 1), 
            //		new SpeleoObjectConverter(), false);
            importer.read(new ExcelReader(importFolder+"Inventaire-Ouest_2016-04-21.xls", 1), 
            		new SpeleoObjectConverter(), true);
            
            // entrances, trial with old data
            //importer.read(new TabulatedTextReader(importFolder+"caves_all.txt", "MacRoman"), 
            //		new EntranceConverter(), true);
            // entrances, Ostschweiz
            //importer.read(new TabulatedTextReader(importFolder+"inventar_ch_ost_20140226_.tab", "MacRoman"), 
            //		new EntranceConverter(), true);
            // entrances, Zentralschweiz
            //importer.read(new ExcelReader(importFolder+"Cave_Zentral_2015-04-13.xls"), 
            //		new EntranceConverter(), true);
            //importer.read(new ExcelReader(importFolder+"Cavites_VD_VS_FR_BE_export.xls", 1), 
            //		new EntranceConverter(), false);
            importer.read(new ExcelReader(importFolder+"Inventaire-Ouest_2016-04-21.xls", 1), 
            		new EntranceConverter(), true);

            // documents
            //importer.read(new TabulatedTextReader(importFolder+"documents.txt", "MacRoman"), 
            //		new KarstObjectDocumentConverter(), true);

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
