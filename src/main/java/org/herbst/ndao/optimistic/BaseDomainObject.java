package org.herbst.ndao.optimistic;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Базовый класс доменных объектов, предоставляющий базовые операции сохранения/восстановления
 * доменного объекта в базе данных {@link Database} по умолчанию. Параметр класса ID задает тип
 * ключевого поля объекта.
 *
 * @author jatvarthur
 */
@MappedSuperclass
public abstract class BaseDomainObject<T> {

	/**
	 * Версия объекта для оптимистичных блокировок.
	 */
	private long version;

	@Transient
	public abstract Serializable getId();
	@Transient
	public abstract void setId(Serializable value);

	/**
	 * Возвращает версию объекта.
	 *
	 * @return Версия объекта.
	 */
	@Version
	@Column(nullable = false)
	public long getVersion() {
		return version;
	}

	/**
	 * Устанавливает версию объекта.
	 *
	 * @param value Версия объекта.
	 */
	public void setVersion(long value) {
		this.version = value;
	}

	/**
	 * Возвращает значение указанного свойства используя рефлексию.
	 *
	 * @param name Имя свойства.
	 * @return Значение свойства.
	 */
	@Transient
	public Object getProperty(final String name) {
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(name, getClass());
			final Method method = descriptor.getReadMethod();
			return method.invoke(this);
		} catch (IntrospectionException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		} catch (InvocationTargetException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	/**
	 * Устанавливает значение указанного свойства используя рефлексию.
	 *
	 * @param name  Имя свойства.
	 * @param value Значение свойства.
	 */
	public void setProperty(final String name, final Object value) {
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(name, getClass());
			final Method method = descriptor.getWriteMethod();
			method.invoke(this, value);
		} catch (IntrospectionException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		} catch (InvocationTargetException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

    /**
     * Сохраняет объект в БД по умолчанию.
     *
     * @throws PersistenceException Если возникла ошибка.
     */
    public T save(final MonadicContext cx) throws PersistenceException {
        cx.getSession().saveOrUpdate(this);
        return (T)this;
    }

	/**
	 * Сохраняет объект в БД по умолчанию.
	 *
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public T merge(final MonadicContext cx) throws PersistenceException {
		cx.getSession().merge(this);
		return (T)this;
	}

	/**
	 * Обновляет объект из БД. <b>Важно!</b>: при этом все изменения сделанные в
	 * объекте до вызова этого метода и не сохраненные в БД потеряются. Поэтому нужно
	 * вызывать этот метод первым в сессии.
	 *
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public void refresh(final MonadicContext cx) throws PersistenceException {
        cx.getSession().refresh(this);
	}

	/**
	 * Удаляет объект из БД.
	 *
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public void delete(final MonadicContext cx) throws PersistenceException {
        cx.getSession().delete(this);
	}

	/**
	 * Выполняет сравнение данных двух доменных объектов. Используется для
	 * определения равенства не сохраненных копий объектов. Обычно реализация
	 * должна выполнять сравнение бизнес-ключей, которые уникальны (например,
	 * имена пользователей и т.д.)
	 *
	 * @param other Объект, с которым выполняется сравнение.
	 * @return <code></code>
	 */
	protected boolean equalsDeep(final BaseDomainObject other) {
		return false;
	}

	/**
	 * Вспомогательный метод возвращает массив полей объекта для вычисления хеша объекта.
	 *
	 * @return Массив полей для вычисления хеша.
	 */
	protected Object[] hashCodeFields() {
		return new Object[]{getId()};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object other) {
		if (this == other)
			return true;

		if (other == null || getClass() != other.getClass())
			return false;

		BaseDomainObject that = (BaseDomainObject) other;
		if (this.getId() != null && that.getId() != null) {
			return this.getId().equals(that.getId()) && this.version == that.version;
		}
		return equalsDeep(that);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hcode = 17, c = 37;
		for (final Object field : hashCodeFields()) {
			hcode = hcode * c;
			if (field != null) hcode += field.hashCode();
		}
		return hcode;
	}

    public static <T> T get(final MonadicContext cx, final Class clazz, Serializable id) {
        return (T)cx.getSession().get(clazz, id);
    }
}
