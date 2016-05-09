package ro.atrifan.persistence.entities;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import ro.atrifan.model.MessagesModel;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


public class JSONMessagesModel implements UserType {

    ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Reconstruct an object from the cacheable representation. At the very
     * least this method should perform a deep copy if the type is mutable.
     * (optional operation)
     *
     * @param cached
     *            the object to be cached
     * @param owner
     *            the owner of the cached object
     * @return a reconstructed object from the cachable representation
     * @throws HibernateException
     */
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return this.deepCopy(cached);
    }

    /**
     * Return a deep copy of the persistent state, stopping at entities and st
     * collections. It is not necessary to copy immutable objects, or null
     * values, in which case it is safe to simple return the argument.
     *
     * @param value
     *            the object to be cloned, which may be null
     *
     * @return object a copy
     * @throws HibernateException
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    /**
     * Transform the object into its cacheable representation. At the very least
     * this method should perform a deep copy if the type is mutable. That may
     * not be enough for some implementations, however; for example,
     * associations must be cached as identifier values. (optional operation)
     *
     * @param value
     *            the object to be cached
     * @return a cachable representation of the object
     * @throws HibernateException
     */
    public Serializable disassemble(Object value) throws HibernateException {
        return (String) this.deepCopy(value);
    }

    /**
     * Compare two instances of the class mapped by this type for persistence
     * "equality". Equality of the persistence state.
     *
     * @param x
     * @param y
     * @return boolean
     * @throws HibernateException
     */
    public boolean equals(Object x, Object y) throws HibernateException {

        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    /**
     * Get a hashcode for the instance, consistent with persistence "equality".
     */
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    /**
     * Are objects of this type mutable?
     *
     * @return boolean
     */
    public boolean isMutable() {
        return true;
    }

    /**
     * Retrieve an instance of the mapped class from a JDBC resultset.
     * Implementors should handle possibility of null values.
     *
     * @param rs
     *            a JDBC result set
     * @param names
     *            the column names
     * @param session
     * @param owner
     *            the containing entity
     * @return
     * @throws HibernateException
     * @throws SQLException
     */
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        MessagesModel jsonObject = null;
        try {
            jsonObject = objectMapper.readValue(rs.getString(names[0]), MessagesModel.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Write an instance of the mapped class to a prepared statement.
     * Implementors should handle possibility of null values. A multi-column
     * type should be written to parameters starting from <tt>index</tt>
     *
     * @param st
     *            a JDBC prepared statement
     * @param value
     *            the object to write
     * @param index
     *            statement parameter index
     * @param session
     * @throws HibernateException
     * @throws SQLException
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {

        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }

        try {
            st.setObject(index, objectMapper.writeValueAsString(value), Types.OTHER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * During merge, replace the existing (target) values in the entity we are
     * merging to with a new (original) value from the detched entity we are
     * merging. For immutable objects, or null values, it is safe to return a
     * copy of the first parameter. For the objects with component values, it
     * might make sense to recursively replace component values
     *
     * @param original
     *            the value from the detched entity being merged
     * @param target
     *            the value in the managed entity
     * @param owner
     * @return the value to be merged
     * @throws HibernateException
     */
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    /**
     * The class returned by <tt>nullSafeGet()</tt>
     *
     * @return Class
     */
    public Class returnedClass() {
        return String.class;
    }

    /**
     * Returns the SQL type codes for the columns mapped by this type. The codes
     * are defined on <tt>java.sql.Types</tt>
     *
     * @return int[] the typecodes
     * @see java.sql.Types
     */
    public int[] sqlTypes() {
        return new int[] { Types.JAVA_OBJECT };
    }

}