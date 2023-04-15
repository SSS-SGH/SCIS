package ch.speleo.scis.persistence.utils;

import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang.*;
import org.apache.commons.lang.text.*;
import org.hibernate.envers.*;
import org.hibernate.envers.query.*;
import org.openxava.jpa.*;

/**
 * Utilities to easily execute database queries. 
 * @author florian
 *
 */
public class SimpleQueries {

	/**
	 * Searches an entity based on a single field. The search should return a unique result. 
	 * Use it preferably to search on a field with a unique constraint. 
	 * @param resultClass  the entity to search for
	 * @param fieldName    the field name to query
	 * @param value        the value to search for
	 * @return             the matching entity
	 * @throws NoResultException if there is no result
	 * 
	 * @see #getSingleResult
	 */
	public static <T> T getByUniqueField(Class<T> resultClass, String fieldName, Object value) {
    	if (value == null) 
    		throw new IllegalArgumentException(fieldName+" of "+resultClass.getSimpleName()+" to search for should not be null");
    	StrBuilder msg = new StrBuilder();
    	msg.append(" while searching ").append(resultClass.getSimpleName());
    	msg.append(" with ").append(fieldName).append(" = " ).append( value);
    	return getSingleResult(msg.toString(), resultClass, fieldName+" = ?1", value);
	}
	
	/**
	 * Searches an entity. The search should return a unique result. 
	 * @param infoForException  some context information that will be added to the exception message (if any)
	 * @param resultClass       the entity to search for
	 * @param criteria          search criteria, aka where-clause in SQL
	 * @param parameters        search parameters for the criteria
	 * @return                  the matching entity
	 * 
	 * @see javax.persistence.EntityManager#createQuery(String, Class)
	 * @see javax.persistence.TypedQuery#getSingleResult()
	 * @throws NoResultException if there is no result
	 * @throws NonUniqueResultException if more than one result
	 * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
	 * @throws QueryTimeoutException if the query execution exceeds the query timeout value set and only the statement is rolled back
	 * @throws TransactionRequiredException if a lock mode has been set and there is no transaction
	 * @throws PessimisticLockException if pessimistic locking fails and the transaction is rolled back
	 * @throws LockTimeoutException if pessimistic locking fails and only the statement is rolled back
	 * @throws PersistenceException if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public static <T> T getSingleResult(String infoForException, Class<T> resultClass, String criteria, Object... parameters) {
		StrBuilder queryStr = new StrBuilder();
		queryStr.append("from ").append(resultClass.getSimpleName());
		if (StringUtils.isNotBlank(criteria))
			queryStr.append(" where ").append(criteria);
    	TypedQuery<T> query = XPersistence.getManager().createQuery(queryStr.toString(), resultClass);
    	int i = 1;
    	for (Object parameter: parameters) {
    		query.setParameter(i, parameter);
    		i++;
    	}
    	try {
    		return query.getSingleResult();
    	} catch (NoResultException e) {
    		throw new NoResultException(e.getMessage() + infoForException);
    	} catch (NonUniqueResultException e) {
    		throw new NonUniqueResultException(e.getMessage() + infoForException);
    	} catch (IllegalStateException e) {
    		throw new IllegalStateException(e.getMessage() + infoForException, e);
    	} catch (QueryTimeoutException e) {
    		throw new QueryTimeoutException(e.getMessage() + infoForException, e);
    	} catch (TransactionRequiredException e) {
    		throw new TransactionRequiredException(e.getMessage() + infoForException);
    	} catch (PessimisticLockException e) {
    		throw new PessimisticLockException(e.getMessage() + infoForException, e);
    	} catch (LockTimeoutException e) {
    		throw new LockTimeoutException(e.getMessage() + infoForException, e);
    	} catch (PersistenceException e) {
    		throw new PersistenceException(e.getMessage() + infoForException, e);
		}
	}
	
	/**
	 * Searches an entity. The search should return a unique result. 
	 * @param infoForException  some context information that will be added to the exception message (if any)
	 * @param resultClass       the entity to search for
	 * @param criteria          search criteria, aka where-clause in SQL
	 * @param parameters        search parameters for the criteria
	 * @return                  the matching entities
	 * 
	 * @see javax.persistence.EntityManager#createQuery(String, Class)
	 * @see javax.persistence.TypedQuery#getSingleResult()
	 * @throws NoResultException if there is no result
	 * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
	 * @throws QueryTimeoutException if the query execution exceeds the query timeout value set and only the statement is rolled back
	 * @throws TransactionRequiredException if a lock mode has been set and there is no transaction
	 * @throws PessimisticLockException if pessimistic locking fails and the transaction is rolled back
	 * @throws LockTimeoutException if pessimistic locking fails and only the statement is rolled back
	 * @throws PersistenceException if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public static <T> List<T> getMultipleResults(String infoForException, Class<T> resultClass, String criteria, Object... parameters) {
		StrBuilder queryStr = new StrBuilder();
		queryStr.append("from ").append(resultClass.getSimpleName()).append(" e");
		if (StringUtils.isNotBlank(criteria))
			queryStr.append(" where ").append(criteria);
    	TypedQuery<T> query = XPersistence.getManager().createQuery(queryStr.toString(), resultClass);
    	int i = 1;
    	for (Object parameter: parameters) {
    		query.setParameter(i, parameter);
    		i++;
    	}
    	try {
    		return query.getResultList();
    	} catch (NoResultException e) {
    		throw new NoResultException(e.getMessage() + infoForException);
    	} catch (NonUniqueResultException e) {
    		throw new NonUniqueResultException(e.getMessage() + infoForException);
    	} catch (IllegalStateException e) {
    		throw new IllegalStateException(e.getMessage() + infoForException, e);
    	} catch (QueryTimeoutException e) {
    		throw new QueryTimeoutException(e.getMessage() + infoForException, e);
    	} catch (TransactionRequiredException e) {
    		throw new TransactionRequiredException(e.getMessage() + infoForException);
    	} catch (PessimisticLockException e) {
    		throw new PessimisticLockException(e.getMessage() + infoForException, e);
    	} catch (LockTimeoutException e) {
    		throw new LockTimeoutException(e.getMessage() + infoForException, e);
    	} catch (PersistenceException e) {
    		throw new PersistenceException(e.getMessage() + infoForException, e);
		}
	}
	
	private static AuditQueryCreator createAuditQuery() {
		AuditReader auditReader = AuditReaderFactory.get(XPersistence.getManager());
		return auditReader.createQuery();
	
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> getAuditedValuesOfEntity(Class<T> entityClass, Object entityId) {
		AuditQuery query = SimpleQueries.createAuditQuery()
			    .forRevisionsOfEntity(entityClass, true, true)
			    .add(AuditEntity.id().eq(entityId));
		List<T> results = query.getResultList();
		return results;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<Object[]> getAuditedInfosOfEntity(Class<T> entityClass, Object entityId) {
		AuditQuery query = SimpleQueries.createAuditQuery()
			    .forRevisionsOfEntity(entityClass, false, true)
			    .add(AuditEntity.id().eq(entityId));
		return query.getResultList();
	}

}
