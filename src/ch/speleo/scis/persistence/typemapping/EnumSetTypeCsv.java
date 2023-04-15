package ch.speleo.scis.persistence.typemapping;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.lang.*;
import org.hibernate.*;
import org.hibernate.engine.spi.*;
import org.hibernate.usertype.*;

// copied from http://www.intersult.com/wiki/Wiki.jsp?page=Hibernate%20EnumSet

public class EnumSetTypeCsv<Type extends Enum<Type>> 
implements UserType, ParameterizedType {
	
	public static final String TYPE = "type";
	private static final int[] SQL_TYPES = {Types.VARCHAR};
	private static final Pattern SEPARATOR = Pattern.compile(",");
	private Class<Type> type;

	public int[] sqlTypes() {
		return SQL_TYPES;
	}
	public Class<?> returnedClass() {
		return type;
	}
	public boolean equals(Object x, Object y) {
		if (x == null)
			return y == null;
		return x.equals(y);
	}
	@SuppressWarnings("unchecked")
	public Object deepCopy(Object value) {
		return ((EnumSet<Type>)value).clone();
	}
	public boolean isMutable() {
		return true;
	}
	public Object nullSafeGet(ResultSet resultSet, String[] names, SharedSessionContractImplementor sessionImplementor, Object owner) throws HibernateException,
			SQLException {
		String name = resultSet.getString(names[0]);
		if (resultSet.wasNull())
			return null;
		EnumSet<Type> enumSet = EnumSet.noneOf(type);
		if (StringUtils.isNotEmpty(name)) {
			String[] enumNames = SEPARATOR.split(name);
			for (String enumName : enumNames) {
				enumSet.add(Enum.valueOf(type, enumName));
			}
		}
		return enumSet;
	}
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SharedSessionContractImplementor sessionImplementor) throws HibernateException,
			SQLException {
		if (value == null) {
			statement.setNull(index, Types.VARCHAR);
		} else {
			StringBuilder buffer = new StringBuilder();
			for (Enum<?> enumValue : (EnumSet<?>)value) {
			        if (buffer.length() > 0)
			                buffer.append(",");
				buffer.append(enumValue.name());
			}
			statement.setString(index, buffer.toString());
		}
	}
    
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached == null ? null : EnumSet.copyOf((EnumSet<?>)cached);
	}
	public Serializable disassemble(Object value) throws HibernateException {
		return value == null ? null : EnumSet.copyOf((EnumSet<?>)value);
	}
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}
	@SuppressWarnings("unchecked")
	public void setParameterValues(Properties parameters) {
		String typeName = parameters.getProperty(TYPE);
		try {
			type = (Class<Type>)Class.forName(typeName);
		} catch (ClassNotFoundException exception) {
			throw new IllegalArgumentException(exception);
		}
	}
}