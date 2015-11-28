package ch.speleo.scis.persistence.typemapping;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

// based on http://www.intersult.com/wiki/Wiki.jsp?page=Hibernate%20EnumSet

public class EnumSetTypeX<Type extends Enum<Type>> 
implements UserType, ParameterizedType {
	
	public static final String CLASSNAME = "ch.speleo.scis.persistence.typemapping.EnumSetTypeX";
	public static final String TYPE = "type";
	private static final int[] SQL_TYPES = {Types.VARCHAR};
	protected static final char ASSIGNED = 'X';
	protected static final char NOT_ASSIGNED= '/';
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
	public Object deepCopy(Object value) {
		return ((EnumSet<?>)value).clone();
	}
	public boolean isMutable() {
		return true;
	}
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException,
			SQLException {
		String name = resultSet.getString(names[0]);
		if (resultSet.wasNull())
			return null;
		EnumSet<Type> enumSet = EnumSet.noneOf(type);
		if (StringUtils.isNotEmpty(name)) {
			Type[] values = type.getEnumConstants();
			for (int i = 0 ; i < name.length() ; i++) {
				if (name.charAt(i) == ASSIGNED) {
					enumSet.add(values[i]);
				}
			}
		}
		return enumSet;
	}
	public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException,
			SQLException {
		if (value == null) {
			statement.setNull(index, Types.VARCHAR);
		} else {
			StringBuilder buffer = new StringBuilder();
			EnumSet<Type> fullSet = EnumSet.allOf(type);
			for (Enum<Type> anyValue : fullSet) {
				buffer.append((((EnumSet<?>)value).contains(anyValue))?ASSIGNED:NOT_ASSIGNED);
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
	public void setParameterValues(Properties parameters) {
		String typeName = parameters.getProperty(TYPE);
		try {
			type = (Class<Type>)Class.forName(typeName);
		} catch (ClassNotFoundException exception) {
			throw new IllegalArgumentException(exception);
		}
	}
}