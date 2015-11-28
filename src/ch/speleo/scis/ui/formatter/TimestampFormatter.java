package ch.speleo.scis.ui.formatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.formatters.IFormatter;
import org.openxava.util.Dates;
import org.openxava.util.Is;
import org.openxava.util.XavaResources;

public class TimestampFormatter implements IFormatter {
	
	// inspired from org.openxava.formatters.DateTimeCombinedFormatter
	
	Log loger;
	String dateFormatStr;
	
	public TimestampFormatter() {
		dateFormatStr = "dd.MM.yyyy HH:mm:ss.SSS";
		loger = LogFactory.getLog(TimestampFormatter.class);
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
