package org.herbst.ndao;

import org.herbst.ndao.domain.FakeDomain;
import org.herbst.ndao.optimistic.Database;
import org.herbst.ndao.optimistic.DatabaseBuilder;
import org.herbst.ndao.optimistic.Monad;
import org.herbst.ndao.optimistic.MonadicContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kris on 23.08.16.
 */
public class EmptyTest extends Assert {

    @Test
    public void run() throws Exception {

        Database database = new DatabaseBuilder()
                .withDriverClass("org.hsqldb.jdbcDriver")
                .withDialect("org.herbst.ndao.dialect.EHSQLDialect")
                .withUrl("jdbc:hsqldb:mem:testdb")
                .withAuth("sa", "")
                .withAnnotatedClass(FakeDomain.class)
                .build();


        database.execute(new Monad() {
            @Override
            public void action(final MonadicContext cx) {
                FakeDomain.get(cx, FakeDomain.class, 0L);
            }
        });

        assertTrue(true);
    }

}
