package org.herbst.ndao.optimistic;

import org.hibernate.Session;

import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Класс, предоставляющий базовые операции для восстановления/сохранения устойчивого
 * класса <code>T</code> с идентификатором <code>ID</code>.
 *
 * @author Edward
 */
public abstract class Entity<T, ID extends Serializable> {

	/**
	 * Управляемый тип.
	 */
	protected Class<?> type;

	/**
	 * Строка запроса для получения всех элементов указанного типа.
	 */
	protected String findAllQuery;

	/**
	 * Строка запроса для получения количества всех элементов указанного типа.
	 */
	protected String countAllQuery;

	/**
	 * Конструктор.
	 */
	public Entity() {
		this.type = (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.findAllQuery = "from " + type.getSimpleName();
		this.countAllQuery = "select count (*) from " + type.getSimpleName();
	}

	/**
	 * Загружает объект из БД по указанному идентификатору.
	 *
	 * @param sx Исходная сессия.
	 * @param id Идентификатор объекта.
	 * @return Загруженный объект.
	 * @throws javax.persistence.PersistenceException
	 *          Если возникла ошибка.
	 */
	@SuppressWarnings("unchecked")
	public T get(final Session sx, final ID id) throws PersistenceException {
		return (T) sx.get(type, id);
	}

	/**
	 * Выполняет поиск объекта в БД по указанному идентификатору.
	 *
	 * @param sx Исходная сессия.
	 * @param id Идентификатор объекта.
	 * @return Найденный объект.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public T findByID(final Session sx, final ID id) throws PersistenceException {
		return get(sx, id);
	}

	/**
	 * Выполняет поиск и загрузку всех объектов управляемого типа.
	 *
	 * @param sx Исходная сессия.
	 * @return Список объектов.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	@SuppressWarnings("unchecked")
	public List<T> findAll(final Session sx) throws PersistenceException {
		return (List<T>) sx.createQuery(findAllQuery).list();
	}

	/**
	 * Выполняет подсчет всех объектов управляемого типа.
	 *
	 * @param sx Исходная сессия.
	 * @return Количество объектов.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	@SuppressWarnings("unchecked")
	public int countAll(final Session sx) throws PersistenceException {
		return ((Number) sx.createQuery(countAllQuery).uniqueResult()).intValue();
	}

	/**
	 * Сохраняет объект в БД.
	 *
	 * @param sx     Исходная сессия.
	 * @param object Сохраняемый объект.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public void store(final Session sx, final T object) throws PersistenceException {
		sx.saveOrUpdate(object);
	}

	/**
	 * Обновляет объект из БД. <b>Важно!</b>: при этом все изменения сделанные в
	 * объекте до вызова этого метода и несохраненные в БД потеряются. Поэтому нужно
	 * вызывать этот метод первым в сессии.
	 *
	 * @param sx     Исходная сессия.
	 * @param object Объект, который нужно сохранить.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public void refresh(final Session sx, final T object) throws PersistenceException {
		sx.refresh(object);
	}

	/**
	 * Удаляет указанный объект из БД.
	 *
	 * @param sx     Исходная сессия, если <code>null</code>, то открывается новая.
	 * @param object Удаляемый объект.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public void remove(final Session sx, final T object) throws PersistenceException {
		sx.delete(object);
	}

}
