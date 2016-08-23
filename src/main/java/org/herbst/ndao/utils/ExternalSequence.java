package org.herbst.ndao.utils;

import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.ReturningWork;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by kris on 07.04.15.
 */
public class ExternalSequence {

    private final String sequenceName;

    private Dialect dialect;

    public ExternalSequence(final Session session, final String sequenceName) {
        this.sequenceName = sequenceName;
        dialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();

        //Проверяем что такой sequence есть, иначе создаем
        ReturningWork<Boolean> checkAndCreateSequence = new ReturningWork<Boolean>() {
            @Override
            public Boolean execute(Connection connection) throws SQLException {
                Dialect dialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;
                try {

                    //Сначала проверяем наличие
                    preparedStatement = connection.prepareStatement( dialect.getQuerySequencesString());
                    resultSet = preparedStatement.executeQuery();
                    while ( resultSet.next() ) {
                        if(sequenceName.equals(resultSet.getString(1))) return false;
                    }

                    //Если дошли до сюда значит его нет-создаем
                    for (String sql: dialect.getCreateSequenceStrings(sequenceName, 1, 1)){
                        connection.prepareStatement(sql).execute();
                    }
                    return true;
                }catch (SQLException e) {
                    throw e;
                } finally {
                    if(preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if(resultSet != null) {
                        resultSet.close();
                    }
                    return false;
                }
            }
        };
        session.doReturningWork(checkAndCreateSequence);
    }

    public long nextVal(final Session session){
        ReturningWork<Long> nextValSequenceWork = new ReturningWork<Long>() {
            @Override
            public Long execute(Connection connection) throws SQLException {
                Dialect dialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;
                try {
                    preparedStatement = connection.prepareStatement( dialect.getSequenceNextValString(sequenceName));
                    resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    return resultSet.getLong(1);
                }catch (SQLException e) {
                    throw e;
                } finally {
                    if(preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if(resultSet != null) {
                        resultSet.close();
                    }
                }

            }
        };
        return session.doReturningWork(nextValSequenceWork);
    }
}
