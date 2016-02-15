package ch.speleo.scis.persistence.typemapping;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import ch.speleo.scis.model.common.Codeable;

/**
 * Transforms a {@link Codeable coded enumeration} to its code for saving in the database and reverse for reading. 
 * 
 * <p> Usage with Hibernate: 
 * <pre>@Type(type=CodedEnumType.CLASSNAME, parameters={ @Parameter(name=CodedEnumType.TYPE, value="package.AnEnumImplementingCodeable")})</pre>
 * </p>
 * 
 * @author Florian Hof
 */
public class CodedEnumType 
implements UserType, ParameterizedType {

	public static final String CLASSNAME = "ch.speleo.scis.persistence.typemapping.CodedEnumType";
	public static final String TYPE = "type";
	private static final int[] SQL_TYPES = {Types.VARCHAR};
	private Class<? extends Codeable> type;

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	/**
	 * Class of the mapped values. 
	 * <p>Can be overwritten to a UserType for a specific enumeration. So is the parameter {@code type} not needed. 
	 */
	public Class<? extends Codeable> returnedClass() {
		return type;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == null)
			return y == null;
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImplementor, Object owner)
			throws HibernateException, SQLException {
		String code = resultSet.getString(names[0]);
		if (resultSet.wasNull())
			return null;
		if (StringUtils.isNotEmpty(code)) {
			return Codeable.Utils.fromCode(code, returnedClass().getEnumConstants());
		}
		return null;
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor sessionImplementor)
			throws HibernateException, SQLException {
		if (value == null) {
			statement.setNull(index, Types.VARCHAR);
		} else {
			statement.setString(index, ((Codeable) value).getCode());
		}
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value; // enums are constants
	}

	public boolean isMutable() {
		return false;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return ((Serializable) value);
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public void setParameterValues(Properties parameters) {
		String typeName = parameters.getProperty(TYPE);
		try {
			type = Class.forName(typeName).asSubclass(Codeable.class);
		} catch (ClassNotFoundException exception) {
			throw new IllegalArgumentException(exception);
		}
	}

}
