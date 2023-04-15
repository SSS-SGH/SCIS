package ch.speleo.scis.ui.formatter;

import java.text.*;

import javax.servlet.http.*;

import org.apache.commons.logging.*;
import org.openxava.formatters.*;
import org.openxava.util.*;

public class TimestampFormatter implements IFormatter {

	private static final Log loger = LogFactory.getLog(TimestampFormatter.class);
	
	// inspired from org.openxava.formatters.DateTimeCombinedFormatter
	
	String dateFormatStr;
	
	public TimestampFormatter() {
		dateFormatStr = "dd.MM.yyyy HH:mm:ss.SSS";
	}

	public String format(HttpServletRequest request, Object date) {
		if (date == null) return "";
		if (Dates.getYear((java.util.Date)date) < 2) return "";
		return getDateTimeFormat().format(date);
	}

	public Object parse(HttpServletRequest request, String string) throws ParseException {
		if (Is.emptyString(string)) return null;
		try {
			java.util.Date result = (java.util.Date) getDateTimeFormat().parseObject(string);
			return new java.sql.Timestamp( result.getTime() );
		}
		catch (ParseException ex) {
			loger.warn(ex.toString());
			throw new ParseException(XavaResources.getString("bad_date_format",string),-1);
		}
	}

	protected DateFormat getDateTimeFormat() {
		return new SimpleDateFormat(dateFormatStr);
	}

}
