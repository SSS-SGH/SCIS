package ch.speleo.scis.business.imports;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.jpa.XPersistence;

/**
 * Generic Reader of a list of Entities. The Reader deals with 
 * a {@link DataReader}, which read fields in a file, and 
 * an {@link EntityConverter}, which convert fields into an Entity. 
 */
public class EntitiesReader {

	private static final Log logger = LogFactory.getLog(EntitiesReader.class);
	
	public final ReaderHelper helper = new ReaderHelper();
	
	public <T> Collection<T> read(DataReader reader, EntityConverter<T> converter, boolean doSaveInDB) 
			throws Exception {
		
		reader.setHelper(helper);
		converter.setHelper(helper);
		Collection<T> entities = new LinkedList<T>();
		int nbInputRows = 0;
		int nbCompleted = 0;
		int nbIgnored = 0;
		Iterator<String[]> iterator = reader.open();
		while (iterator.hasNext()) {
			String[] row = iterator.next();
			nbInputRows++;
			
			// read
			T entity = null;
            try {
            	if (row == null || row.length == 0) {
        			nbInputRows--; // do not count as a data row
            		continue;
            	}
                entity = converter.getEntity(row);
                if (entity != null)
                	entities.add(entity);
                else {
                	nbIgnored++;
                }
            } catch (Exception e) {
            	StrBuilder message = new StrBuilder();
            	message.append("error while converting entity [");
            	message.appendWithSeparators(row, ", ");
            	message.append("]");
            	message.append(" cause ");
            	//message.append(e);
            	//message.append(" at ");
            	//message.append(e.getStackTrace()[0].toString());
            	logger.error(message.toString(), e);
            	entity = null;
            }
            
            // check
            if (entity == null)
            	continue; // ignored and already reported
            if (!converter.check(entity)) {
            	// check failed
            	// error should already have been reported
            	continue;
            }
			List<String> validationMessages = helper.getValidationViolations(entity);
			for (String validationMessage: validationMessages) {
				logger.error("unvalid "+entity+" cause "+validationMessage);
			}
			if (!validationMessages.isEmpty()) {
				continue;
			}
            
            // write
        	if (doSaveInDB) {
        		try {
        			XPersistence.getManager().persist(entity);
        			XPersistence.commit();
        			nbCompleted++;
    	        } catch (Exception e) { 
                	XPersistence.rollback();
                	Throwable duplicateException = null;
                	for (Throwable cause = e ; cause != null ; cause = cause.getCause()) {
                		if (cause.getMessage().toLowerCase().contains("duplicate")) {
                			duplicateException = cause;
                		}
                	}
                	if (duplicateException != null) {
                		// a similar entity already exists in the database
                		helper.error(entity.getClass(), entity.toString(), null, 
                				duplicateException.getMessage());
                	} else {
                		// other database problem
                    	StrBuilder message = new StrBuilder();
                    	message.append("error while saving ");
                		message.append(entity);
                		message.append(" cause ");
                		//message.append(e);
                		//message.append(" at ");
                		//message.append(e.getStackTrace()[0].toString());
                    	logger.error(message.toString(), e);
                		helper.error(entity.getClass(), entity.toString(), null, e.getMessage());
                	}
        		}
        	} else /* don't save in DB, only read the input */ {
        		nbCompleted++;
        	}
		}
		reader.close();

        StrBuilder message = new StrBuilder();
        message.append("--> READ ");
        message.append(nbInputRows);
        message.append(" rows from ");
        message.append(reader.getDatasourceName());
        message.append(" through ");
        message.append(converter.getClass().getSimpleName());
        message.append(": ");
        message.append(nbCompleted);
        message.append(" (");
        message.append(getPercent(nbCompleted, nbInputRows));
        message.append("%) correct, ");
        message.append(nbIgnored);
        message.append(" (");
        message.append(getPercent(nbIgnored, nbInputRows));
        message.append("%) ignored, ");
        int nbError = nbInputRows - nbCompleted - nbIgnored;
        message.append(nbError);
        message.append(" (");
        message.append(getPercent(nbError, nbInputRows));
        message.append("%) failed ");
        helper.summarise(message.toString());
        //logger.info(message.toString());

        return entities;
	}
	
	private String getPercent(int nb, int total) {
		return String.format("%.0f", ((double) nb / (double) total) * 100.0);
	}
	
	
    /**
     * Converter from a data row.
     */
	protected static abstract class EntityConverter<T> {
	    /**
	     * Converts an entity from a data row.
	     * 
	     * @param row one input row.
	     * @return entity built from the given input row. {@code null} means that the row should be ignored. 
	     * @throws Exception a problem occurs while building the entity 
	     */
	    public abstract T getEntity(String[] row) throws Exception;
	    
	    /**
	     * Checks that an entity is valid. 
	     * Override to implement. Implementation should report errors or warnings in the helper. 
	     * 
	     * @param entity the entity to check
	     * @return if the entity is valid (true=valid)
	     */
	    public boolean check(T entity) {
	    	return true;
	    }
		
		protected ReaderHelper helper;
		protected void setHelper(ReaderHelper helper) {
			this.helper = helper;
		}
	}
	
	/**
	 * Reader for data in form of a table. 
	 */
	protected static abstract class DataReader {
		public abstract Iterator<String[]> open() throws Exception;
		public abstract void close() throws Exception;
		/**
		 * @return a human-readable name of the datasource, such as a file name or a URL. 
		 */
		public abstract String getDatasourceName();

		protected ReaderHelper helper;
		protected void setHelper(ReaderHelper helper) {
			this.helper = helper;
		}
	}

}
