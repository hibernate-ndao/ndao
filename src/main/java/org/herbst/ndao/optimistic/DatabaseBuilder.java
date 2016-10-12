package org.herbst.ndao.optimistic;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;

import java.net.URL;

/**
 * Created by kris on 23.08.16.
 */
public class DatabaseBuilder {

    private Configuration configuration;

    private String driverClass;

    private String resourceStr;
    private URL resourceURL;

    public DatabaseBuilder() {
        configuration = new Configuration();
    }

    public DatabaseBuilder withDriverClass(String driverClass){
        this.driverClass=driverClass;
        configuration.setProperty("hibernate.connection.driver_class", driverClass);
        return this;
    }

    public DatabaseBuilder withDialect(String dialect){
        configuration.setProperty("hibernate.dialect", dialect);
        return this;
    }

    public DatabaseBuilder withUrl(String url){
        configuration.setProperty("hibernate.connection.url", url);
        return this;
    }

    public DatabaseBuilder withAuth(String login, String password){
        configuration.setProperty("hibernate.connection.username", login);
        configuration.setProperty("hibernate.connection.password", password);
        return this;
    }

    public DatabaseBuilder withProperty(String propertyName, String value){
        configuration.setProperty(propertyName, value);
        return this;
    }

    public DatabaseBuilder withAnnotatedClass(Class annotatedClass){
        configuration.addAnnotatedClass(annotatedClass);
        return this;
    }

    public DatabaseBuilder withResource(String resource) {
        this.resourceStr=resource;
        return this;
    }

    public DatabaseBuilder withResource(URL resource) {
        this.resourceURL=resource;
        return this;
    }

    public Configuration getHibernateConfiguration() {
        return configuration;
    }

    public Database build() throws ClassNotFoundException, HibernateException {
        //Загружаем драйвер
        if (driverClass!=null) {
            Class.forName(driverClass);
        }

        //Конфигурируем
        if (resourceURL!=null) {
            configuration.configure(resourceURL);
        } else if (resourceStr!=null) {
            configuration.configure(resourceStr);
        } else {
            configuration.configure();
        }

        return new Database(configuration);
    }
}
