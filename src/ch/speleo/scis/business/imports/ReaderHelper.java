package ch.speleo.scis.business.imports;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.jpa.XPersistence;

import ch.speleo.scis.model.common.Commune;
import ch.speleo.scis.model.common.Karstologist;
import ch.speleo.scis.model.karst.DocumentationStateEnum;
import ch.speleo.scis.model.karst.GroundObject;
import ch.speleo.scis.model.karst.GroundObjectTypeEnum;
import ch.speleo.scis.model.karst.LocationAccuracyEnum;
import ch.speleo.scis.model.karst.Privacy;
import ch.speleo.scis.model.karst.SpeleoObject;
import ch.speleo.scis.model.karst.SpeleoObjectTypeEnum;

/**
 * Helper for reading and converting entites. It offers:
 * <br/>- converters to numbers
 * <br/>- test of input (blank, ...)
 * <br/>- reporting of errors and warnings.
 * 
 * @author florian
 */
public class ReaderHelper {
	
	// helpers for converting
	
	protected Pattern onlyQuestionMarks = Pattern.compile("\\?+");
	
	public boolean isBlank(String input) {
		return StringUtils.isBlank(input);
	}
	
    public Integer toInteger(String input) throws NumberFormatException {
        if (isBlank(input) || onlyQuestionMarks.matcher(input.trim()).matches()) {
        	return null;
        } else {
        	return Integer.valueOf(input.trim());
        }
    }
    public Integer toInteger(Double input) throws Exception {
        if (input == null) {
        	return null;
        } else {
        	return (int) Math.round(input);
        }
    }
    public ConversionResult<Integer> toIntegerFlexible(String input) {
        if (isBlank(input) || onlyQuestionMarks.matcher(input.trim()).matches()) {
        	return new ConversionResult<Integer>(null, null);
        } 
        StrBuilder message = new StrBuilder();
        if (input.contains("?")) {
        	input = input.replace("?", "");
        	message.appendSeparator(", ").append("question marks were suppressed");
        }
    	input = input.trim(); // removes spaces (if any)
        int posDot = input.indexOf(',');
        if (posDot >= 0 && (input.length() - posDot - 1) <= 2 /* max 2 decimals, otherwise could be a thousand separator */) {
        	input = input.substring(0, posDot) + '.' + input.substring(posDot + 1);
        	try {
        		int inputAsInt = (int) Math.round(Float.valueOf(input));
            	message.appendSeparator(", ").append("decimal with comma '").append(input).append("' was rounded to an integer");
        		return new ConversionResult<Integer>(inputAsInt, message.toString());
        	} catch (NumberFormatException e) {
            	message.appendSeparator(", ").append("unrecognize number '").append(input).append("', will be let empty");
        		return new ConversionResult<Integer>(null, message.toString());
        	}
        }
        posDot = input.indexOf('.');
        if (posDot >= 0) {
        	try {
        		int inputAsInt = (int) Math.round(Float.valueOf(input));
            	message.appendSeparator(", ").append("decimal with dot '").append(input).append("' was rounded to an integer");
        		return new ConversionResult<Integer>(inputAsInt, message.toString());
        	} catch (NumberFormatException e) {
            	message.appendSeparator(", ").append("unrecognize number '").append(input).append("', will be let empty");
        		return new ConversionResult<Integer>(null, message.toString());
        	}
        }
        return new ConversionResult<Integer>(
        		(isBlank(input)) ? null : Integer.valueOf(input), 
        		(message.isEmpty()) ? null : message.toString() );        
    }
    public ConversionResult<Integer> toCoordinate(String input) {
        if (isBlank(input) || onlyQuestionMarks.matcher(input.trim()).matches()) {
        	return new ConversionResult<Integer>(null, null);
        } 
        input = input.trim();
        StrBuilder message = new StrBuilder();
        if (input.contains("?") && input.length() > 1 /* more than just a question mark */) {
        	message.appendSeparator(", ").append("unsure coordinate '").append(input).append("', will be let empty");
        	return new ConversionResult<Integer>(null, message.toString());
        }
        ConversionResult<Integer> result = toIntegerFlexible(input);
        // 0 could be converted to null because it isn't a coordinate
        return result;
    }
        
    public boolean toBoolean(String yesno) throws IllegalArgumentException {
        if (isBlank(yesno)) {
        	return false;
        } else {
        	yesno = yesno.trim().toUpperCase();
        	switch(yesno.charAt(0)) {
        	case 'Y':
        	case 'O':
        	case 'J':
        	case 'T':
        	case 'V':
        	case 'W':
        		return true;
        	case 'N':
        	case 'F':
        		return false;
    		default:
    			throw new IllegalArgumentException(yesno+" not recognise as YES or NO");
        	}
        }
    }
    
    public Date toDate (String input) throws ParseException   {
    	if (isBlank(input)) 
    		return null;
    	else if (input.contains("."))
    		return new SimpleDateFormat("dd.MM.yyyy").parse(input.trim());
    	else if (input.contains("/"))
    		return new SimpleDateFormat("MM/dd/yyyy").parse(input.trim());
    	else 
    		return new SimpleDateFormat("dd MM yyyy").parse(input.trim());
    }
    
    public ConversionResult<Karstologist> toKarstologist(String initials) throws Exception {
        if (isBlank(initials) || "**".equals(initials)) {
        	return new ConversionResult<Karstologist>(null, null);
        } else {
        	try {
        		Karstologist karstologist = Karstologist.getByInitials(initials);
        		return new ConversionResult<Karstologist>(karstologist, null);
        	} catch (NoResultException e) {
    			missingInitials.add(initials);
        		Karstologist karstologist = new Karstologist();
        		karstologist.setInitials(initials);
        		karstologist.setDeleted(false);
    			List<String> validationMessages = getValidationViolations(karstologist);
    			for (String validationMessage: validationMessages) {
    				logger.error("unvalid "+karstologist+" cause "+validationMessage);
    			}
    			if (validationMessages.size() == 0) {
        			XPersistence.getManager().persist(karstologist);
        			XPersistence.commit();
            		return new ConversionResult<Karstologist>(karstologist, null);
    			} else {
    				return new ConversionResult<Karstologist>(null, 
    						"karstologist's initials '"+initials+"' not ok, "+validationMessages.get(0));
    			}
        	}
        }
    }
    public Set<String> missingInitials = new HashSet<String>();
    
    public Commune toCommune(String fsoNr) throws Exception {
    	//try {
    	Integer fsoInt = toInteger(fsoNr);
        if (fsoInt == null) {
        	return null;
        } else {
    		return Commune.getByFsoNr(fsoInt);
        }
    	//} catch (NumberFormatException e) {
    	//	return SimpleQueries.getByUniqueField(Commune.class, "name", fsoNr);
    	//}
    }
    
    public Privacy toPrivacy(String start, 
                             String end, 
                             String reason, 
                             String protector) throws Exception {
		return toPrivacy(toDate(start), toDate(end), reason, protector);
	}
	public Privacy toPrivacy(Date start, 
            				 Date end, 
            				 String reason, 
            				 String protector) throws Exception {
		if (start == null && end == null && isBlank(reason) && isBlank(protector)) {
			return null;
		}
		Privacy privacy = new Privacy();
		privacy.setStartDate(start);
		privacy.setEndDate(end);
		privacy.setReason(reason);
		privacy.setProtector(protector);
    	/*if ("Thomas Arbenz".equalsIgnoreCase(protector))
    		protector = "TA";
    	else if ("Sébastien rotzer".equalsIgnoreCase(protector))
    		protector = "SR";
    	if (protector.length() > 10) {
    		throw new IllegalArgumentException("initials '"+protector+"' too long for the protector");
    	}
		privacy.setProtector(toKarstologist(protector));*/
		return privacy;
	}

    public DocumentationStateEnum toDocumentationState(String input) {
    	if (isBlank(input) || "-".equals(input)) {
    		return null;
    	} else if ("1".equals(input)) {
    		return DocumentationStateEnum.NO_AVAILABLE_DOCUMENT;
    	} else if (input.length() == 2 && input.charAt(0) == input.charAt(1)) { // stammering
    		return DocumentationStateEnum.fromCode(input.substring(0, 1));
    	} else {
    		return DocumentationStateEnum.fromCode(input);
    	}
    }
    public DocumentationStateEnum toDocumentationState(Double input) {
    	if (input == null) 
    		return null;
    	String str = String.valueOf(Math.round(input));
    	return toDocumentationState(str);
    }
    
    public LocationAccuracyEnum toLocationAccuracyEnum(String input) {
    	if (isBlank(input) || "-".equals(input)) {
    		return null;
    	} else {
    		//if ('E' == input.charAt(0)) input = input.substring(1);
    		return LocationAccuracyEnum.fromCode(input);
    	}
    }
    
    public SpeleoObjectTypeEnum toSpeleoObjectTypeEnum(String input) {
    	if (isBlank(input) || "-".equals(input) || "?".equals(input)) {
    		return null;
    	} else {
    		return SpeleoObjectTypeEnum.fromCode(input.toUpperCase(Locale.GERMAN));
    	}
    }
    
    public GroundObjectTypeEnum toGroundObjectTypeEnum(String input) {
    	if (isBlank(input) || "-".equals(input) || "?".equals(input)) {
    		return null;
    	} else {
    		return GroundObjectTypeEnum.fromCode(input.toUpperCase(Locale.GERMAN));
    	}
    }
    
    public SpeleoObject toSpeleoObject(String systemNr) throws Exception {
    	Integer systemInt = toInteger(systemNr);
        if (systemInt == null) {
        	return null;
        } else {
    		return SpeleoObject.getBySystemNr(systemInt);
        }
    }
    
    public GroundObject toGroundObject(String inventoryNr) throws Exception {
    	Integer inventoryInt = toInteger(inventoryNr);
        if (inventoryInt == null) {
        	return null;
        } else {
    		return GroundObject.getByInventoryNr(inventoryInt);
        }
    }
    
    /**
     * Result of a flexible conversion, with possibly error or warning messages. 
     * If the message is {@code null}, there were neither error nor warning; 
     * the result, possibly {@code null}, can be used "as is". 
     * If the message is not {@code null} and the result not {@code null}, 
     * the message should be taken as a warning, but the result could be determined. 
     * If the message is not {@code null} and the result {@code null}, there were an error, which is described in the message.
     * 
     * @author florian
     *
     * @param <T> Type of the result
     */
    public static class ConversionResult<T> {
    	private T result;
    	private String message;
    	protected ConversionResult (T result, String message) {this.result = result; this.message = message;}
    	public T getResult() {return result;}
    	public String getMessage() {return message;}
    }

    public <T> List<String> getValidationViolations (T bean) {
	    List<String> validationViolations = new ArrayList<String>();
	    
		// JSR-303 validation
	    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
	    javax.validation.Validator validator = validatorFactory.getValidator();
	    Set<ConstraintViolation<T>> violations = validator.validate(bean);
	    for (ConstraintViolation<T> violation : violations) {
	        //String propertyPath = violation.getPropertyPath().toString();
	        //String message = violation.getMessage();
	        validationViolations.add(violation.getMessage());
	    }
	
	    return validationViolations;
	}

    // helpers for reporting
    
    private static final Log logger = LogFactory.getLog(ReaderHelper.class);
    
    private FileWriter errorWriter;
    public void setBadFile(String name) throws IOException {
    	errorWriter = new FileWriter(name);
    }
    public void closeBadFile() throws IOException {
    	errorWriter.flush();
    	errorWriter.close();
    }
    
    private Collection<Report> errors = new LinkedList<Report>();
    private Collection<Report> warnings = new LinkedList<Report>();
    private Collection<Report> summaries = new LinkedList<Report>();
    
    public void error(Class<?> entity, String identifier, String field, String message) {
    	error(entity, identifier, field, message, null);
    }
    public void error(Class<?> entity, String identifier, String field, String message, Exception exception) {
    	Report report = new Report(entity, identifier, field, message, exception);
    	//logger.error(report.toString());
    	errors.add(report);
    	try {
			errorWriter.append("ERROR\t");
			errorWriter.append(entity.getSimpleName()).append("\t");
			errorWriter.append((identifier==null)?"":identifier).append("\t");
			errorWriter.append((field==null)?"":field).append("\t");
			errorWriter.append(message).append("\t");
			errorWriter.append((exception==null)?"":exception.toString()).append("\t");
			errorWriter.append("\n");
		} catch (IOException e) {
		}
    }
    public void warn(Class<?> entity, String identifier, String field, String message) {
    	warn(entity, identifier, field, message, null);
    }
    public void warn(Class<?> entity, String identifier, String field, String message, Exception exception) {
    	Report report = new Report(entity, identifier, field, message, exception);
    	//logger.warn(report.toString());
    	warnings.add(report);
    	try {
			errorWriter.append("WARN\t");
			errorWriter.append(entity.getSimpleName()).append("\t");
			errorWriter.append((identifier==null)?"":identifier).append("\t");
			errorWriter.append((field==null)?"":field).append("\t");
			errorWriter.append(message).append("\t");
			errorWriter.append((exception==null)?"":exception.toString()).append("\t");
			errorWriter.append("\n");
		} catch (IOException e) {
		}
    }
    public void summarise(String message) {
    	Report report = new Report(null, null, null, message, null);
    	logger.info(report.toString());
    	summaries.add(report);
    }
    
    
    public Collection<Report> getErrors() {
		return errors;
	}
	public Collection<Report> getWarnings() {
		return warnings;
	}
	public Collection<Report> getSummaries() {
		return summaries;
	}

	public static class Report {
    	public Class<?> entity;
    	public String identifier;
    	public String field;
    	public String message;
    	public Exception exception;
		public Report(Class<?> entity, String identifier, String field, String message,
				Exception exception) {
			super();
			this.entity = entity;
			this.identifier = identifier;
			this.message = message;
			this.exception = exception;
		}
		public Class<?> getEntity() {
			return entity;
		}
		public String getIdentifier() {
			return identifier;
		}
		public String getField() {
			return field;
		}
		public String getMessage() {
			return message;
		}
		public Exception getException() {
			return exception;
		}
		public String toString() {
			StrBuilder txt = new StrBuilder();
			if (entity != null || identifier != null)
				txt.append("in ");
			if (entity != null)
				txt.append(entity.getSimpleName());
			if (identifier != null)
				txt.append("[").append(identifier).append("]");
			if (entity != null || identifier != null)
				txt.append(", ");
			txt.append(message);
			//if (exception != null)
			//	txt.append(" \n cause ").append(exception);
			return txt.toString();
		}
    }

}
