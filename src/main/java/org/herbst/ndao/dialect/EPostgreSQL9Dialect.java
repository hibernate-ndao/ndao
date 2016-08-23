package org.herbst.ndao.dialect;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

/**
 * Created by Kris on 22.04.2015.
 */
public class EPostgreSQL9Dialect extends PostgreSQL9Dialect {

    public EPostgreSQL9Dialect() {
        super();
        registerColumnType(Types.ARRAY, "bigint[$l]" );
    }
}
