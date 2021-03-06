package org.herbst.ndao.usertype.enums.uid;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created with IntelliJ IDEA.
 * User: kris
 * Date: 16.08.12
 * Time: 14:14
 * Абстрактный класс позволяющий enum использовать getId
 */
public abstract class PersistentEnumUidUserType<T extends PersistentEnumUid> implements UserType {

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable)value;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x==null && y==null) return true;
        return (x!=null && x.equals(y));
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        String uid = rs.getString(names[0]);
        if(rs.wasNull()) {
            return null;
        }
        for(PersistentEnumUid value : returnedClass().getEnumConstants()) {
            if( uid.equals(value.getUid()) ) {
                return value;
            }
        }
        throw new IllegalStateException("Nothing enum item: " + uid + ", type enums: " + returnedClass().getSimpleName());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, ((PersistentEnumUid)value).getUid());
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public abstract Class<T> returnedClass();

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.VARCHAR};
    }

}
