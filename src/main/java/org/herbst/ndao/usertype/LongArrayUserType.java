package org.herbst.ndao.usertype;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;

/**
 * Created by kris on 20.04.15.
 */
public class LongArrayUserType implements UserType {

    /**
     * Восстанавливает значение поля при чтении из базы.
     */
    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        Array array = rs.getArray(names[0]);
        Object[] javaArray = (Object[]) array.getArray();

        final long[] primitiveArray = new long[javaArray.length];
        for (int i = 0; i < javaArray.length; i++) {
            primitiveArray[i] = ((Long)javaArray[i]).longValue();
        }
        return primitiveArray;
    }

    /**
     * Кодирует значение поля для записи в базу.
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        Connection connection = st.getConnection();
        long[] castObject = (long[]) value;

        final Long[] longs = new Long[castObject.length];
        for (int i = 0; i < castObject.length; i++) {
            longs[i] = Long.valueOf(castObject[i]);
        }

        Array array = connection.createArrayOf("bigint", longs);
        st.setArray(index, array);
    }

    /**
     * Конвертирует объект в вид, пригодный для хранения в кэше второго уровня.
     */
    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return cached;
    }

    /**
     * Осуществляет полное копирование объекта.
     */
    @Override
    public Object deepCopy(final Object o) throws HibernateException {
        return o == null ? null : ((long[]) o).clone();
    }

    /**
     * Восстанавливает объект из вида, пригодного для хранения в кэше второго уровня.
     */
    @Override
    public Serializable disassemble(final Object o) throws HibernateException {
        return (Serializable) o;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == null) return (y == null);

        long[] lX = (long[])x;
        long[] lY = (long[])y;
        if (lX.length!=lY.length) return false;
        for (int i=0; i<lX.length; i++) {
            if (lX[i] != lY[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode(final Object o) throws HibernateException {
        if (o == null) return 0;

        int result=0;
        long[] longs = (long[])o;
        for (int i=0; i<longs.length; i++) {
            result = 31 * result + (int) (longs[i] ^ (longs[i] >>> 32));
        }
        return result;
    }

    /**
     * Возвращает true, если объект может меняться.
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    /**
     * Копирует изменения из нового значения в старое.
     */
    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

    @Override
    public Class<long[]> returnedClass() {
        return long[].class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.ARRAY };
    }
}
