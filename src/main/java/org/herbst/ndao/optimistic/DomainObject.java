package org.herbst.ndao.optimistic;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Created by Kris on 28.03.2015.
 */
@MappedSuperclass
public class DomainObject<T> extends BaseDomainObject<T> {

    /**
     * Уникальный идентификатор объекта.
     */
    private long id;

    /**
     * Возвращает уникальный идентификатор объекта.
     *
     * @return Уникальный идентификатор объекта.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор объекта.
     *
     * @param value Уникальный идентификатор объекта.
     */
    @Override
    public void setId(Serializable value) {
        this.id = (long) value;
    }
}
