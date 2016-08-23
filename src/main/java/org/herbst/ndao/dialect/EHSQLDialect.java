package org.herbst.ndao.dialect;

import org.hibernate.dialect.HSQLDialect;

import java.sql.Types;

/**
 * Created by Kris on 22.04.2015.
 */
public class EHSQLDialect extends HSQLDialect {

    public EHSQLDialect() {
        super();
        registerColumnType(Types.ARRAY, "bigint[$l]" );
    }
}
